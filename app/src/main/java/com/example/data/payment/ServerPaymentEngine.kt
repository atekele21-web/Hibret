package com.example.data.payment

import com.example.data.CampaignDao
import com.example.data.DonationDao
import com.example.data.NotificationDao
import com.example.model.Donation
import com.example.model.NotificationItem
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

/**
 * Hibret Server-Side Payment Orchestrator & Reconciliation Engine.
 *
 * Implements:
 * 1. Idempotency Key validation (prevents duplicate double charges).
 * 2. Independent adapter routing (Telebirr, CBE Birr, Awash, Cards).
 * 3. Never trusting frontend success messages: cryptographic signature verification of callbacks.
 * 4. Transaction reconciliation: verifying exact ETB amounts match before recording donations.
 * 5. Simulation testing engine for Success, Failure (wrong PIN / funds), Cancellation, Timeout,
 *    and Duplicate callback detection.
 */
class ServerPaymentEngine(
    private val donationDao: DonationDao,
    private val campaignDao: CampaignDao,
    private val notificationDao: NotificationDao
) {

    // Independent payment adapters registry
    val telebirrAdapter = TelebirrAdapter(isSandboxMode = true)
    val cbeBirrAdapter = CbeBirrAdapter(isSandboxMode = true)
    val awashAdapter = GenericBankAdapter(
        providerId = "awash_bank",
        providerName = "Awash Bank / Birr",
        supportedCurrencies = listOf("ETB"),
        isSandboxMode = true
    )
    val cardAdapter = GenericBankAdapter(
        providerId = "international_card",
        providerName = "International Card (Visa/Mastercard)",
        supportedCurrencies = listOf("ETB", "USD"),
        isSandboxMode = true
    )

    // In-memory server-side idempotency ledger & pending transaction session cache
    private val idempotencyLedger = ConcurrentHashMap<String, StoredPaymentSession>()
    private val transactionIndex = ConcurrentHashMap<String, StoredPaymentSession>()
    private val processedCallbackSignatures = ConcurrentHashMap<String, Long>()

    data class StoredPaymentSession(
        val initRequest: PaymentInitRequest,
        val initResult: PaymentInitResult,
        var currentStatus: PaymentStatus,
        var reconciledDonation: Donation? = null,
        val createdAt: Long = System.currentTimeMillis()
    )

    /**
     * Resolves the proper gateway adapter based on user selection.
     */
    fun getAdapterFor(methodName: String): PaymentGatewayAdapter {
        return when {
            methodName.contains("Telebirr", ignoreCase = true) -> telebirrAdapter
            methodName.contains("CBE", ignoreCase = true) -> cbeBirrAdapter
            methodName.contains("Awash", ignoreCase = true) -> awashAdapter
            else -> cardAdapter
        }
    }

    /**
     * Step 1: Initialize payment on server with idempotency check.
     */
    suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult {
        // Idempotency check: if already initialized with this key, return cached session result
        idempotencyLedger[request.idempotencyKey]?.let { existing ->
            return existing.initResult
        }

        val adapter = getAdapterFor(request.providerId)
        val initResult = adapter.initializePayment(request)

        val session = StoredPaymentSession(
            initRequest = request,
            initResult = initResult,
            currentStatus = PaymentStatus.PENDING_CUSTOMER_ACTION
        )

        idempotencyLedger[request.idempotencyKey] = session
        transactionIndex[initResult.transactionRef] = session

        return initResult
    }

    /**
     * Step 2: Server-side webhook / callback processor.
     * CRITICAL: Cryptographically verifies the signature and never trusts frontend client.
     */
    suspend fun processProviderCallback(payload: ProviderCallbackPayload): ServerVerificationResult {
        val session = transactionIndex[payload.transactionRef]
            ?: return ServerVerificationResult(
                isSuccess = false,
                transactionRef = payload.transactionRef,
                providerRef = payload.providerTransactionId,
                reconciledAmount = payload.amountPaid,
                status = PaymentStatus.FAILED,
                errorReason = PaymentErrorReason.NETWORK_FAILURE,
                verificationNotes = "Rejected: Unknown transaction reference in server ledger."
            )

        // Duplicate callback protection
        val signatureKey = "${payload.transactionRef}_${payload.signature}"
        if (processedCallbackSignatures.containsKey(signatureKey)) {
            return ServerVerificationResult(
                isSuccess = session.currentStatus == PaymentStatus.VERIFIED_SUCCESS,
                transactionRef = payload.transactionRef,
                providerRef = payload.providerTransactionId,
                reconciledAmount = payload.amountPaid,
                status = session.currentStatus,
                errorReason = PaymentErrorReason.DUPLICATE_TRANSACTION,
                verificationNotes = "Duplicate webhook callback ignored. Idempotency preserved."
            )
        }
        processedCallbackSignatures[signatureKey] = System.currentTimeMillis()

        // Cryptographic signature check
        val adapter = getAdapterFor(session.initRequest.providerId)
        val isSignatureValid = adapter.verifyCallbackSignature(payload)
        if (!isSignatureValid) {
            session.currentStatus = PaymentStatus.FAILED
            return ServerVerificationResult(
                isSuccess = false,
                transactionRef = payload.transactionRef,
                providerRef = payload.providerTransactionId,
                reconciledAmount = payload.amountPaid,
                status = PaymentStatus.FAILED,
                errorReason = PaymentErrorReason.SIGNATURE_VERIFICATION_FAILED,
                verificationNotes = "CRITICAL: Callback HMAC signature mismatch. Rejecting transaction as untrusted."
            )
        }

        // Handle specific failure / cancellation / timeout statuses
        when (payload.status.uppercase()) {
            "FAILED" -> {
                session.currentStatus = PaymentStatus.FAILED
                val reason = when (payload.errorCode) {
                    "INSUFFICIENT_FUNDS" -> PaymentErrorReason.INSUFFICIENT_FUNDS
                    "INCORRECT_PIN" -> PaymentErrorReason.INCORRECT_PIN
                    "LIMIT_EXCEEDED" -> PaymentErrorReason.TRANSACTION_LIMIT_EXCEEDED
                    else -> PaymentErrorReason.NETWORK_FAILURE
                }
                return ServerVerificationResult(
                    isSuccess = false,
                    transactionRef = payload.transactionRef,
                    providerRef = payload.providerTransactionId,
                    reconciledAmount = payload.amountPaid,
                    status = PaymentStatus.FAILED,
                    errorReason = reason,
                    verificationNotes = payload.errorMessage ?: "Payment provider declined transaction."
                )
            }
            "TIMEOUT" -> {
                session.currentStatus = PaymentStatus.TIMEOUT
                return ServerVerificationResult(
                    isSuccess = false,
                    transactionRef = payload.transactionRef,
                    providerRef = payload.providerTransactionId,
                    reconciledAmount = payload.amountPaid,
                    status = PaymentStatus.TIMEOUT,
                    errorReason = PaymentErrorReason.USSD_TIMEOUT,
                    verificationNotes = "USSD / PIN authorization window expired."
                )
            }
            "CANCELLED" -> {
                session.currentStatus = PaymentStatus.CANCELLED
                return ServerVerificationResult(
                    isSuccess = false,
                    transactionRef = payload.transactionRef,
                    providerRef = payload.providerTransactionId,
                    reconciledAmount = payload.amountPaid,
                    status = PaymentStatus.CANCELLED,
                    errorReason = PaymentErrorReason.USER_CANCELLED,
                    verificationNotes = "Customer explicitly cancelled mobile money prompt."
                )
            }
        }

        // Amount Reconciliation: strictly verify amount paid matches requested total
        val expectedTotal = session.initRequest.totalAmount
        if (Math.abs(payload.amountPaid - expectedTotal) > 0.01) {
            session.currentStatus = PaymentStatus.FAILED
            return ServerVerificationResult(
                isSuccess = false,
                transactionRef = payload.transactionRef,
                providerRef = payload.providerTransactionId,
                reconciledAmount = payload.amountPaid,
                status = PaymentStatus.FAILED,
                errorReason = PaymentErrorReason.NETWORK_FAILURE,
                verificationNotes = "Amount mismatch: Requested $expectedTotal ETB, but received ${payload.amountPaid} ETB."
            )
        }

        // If verified, record donation into Room database
        session.currentStatus = PaymentStatus.VERIFIED_SUCCESS
        val donation = Donation(
            id = "don_" + System.currentTimeMillis().toString().takeLast(6) + (100..999).random(),
            campaignId = session.initRequest.campaignId,
            campaignTitle = session.initRequest.campaignTitle,
            donorName = if (session.initRequest.isAnonymous) "Anonymous Donor" else session.initRequest.donorName.ifBlank { "Supporter" },
            donorPhone = session.initRequest.donorPhone,
            donorEmail = session.initRequest.donorEmail,
            isAnonymous = session.initRequest.isAnonymous,
            amount = session.initRequest.amount,
            platformTip = session.initRequest.tipAmount,
            feeAmount = session.initRequest.feeAmount,
            totalPaid = payload.amountPaid,
            paymentMethod = adapter.providerName,
            transactionReference = payload.transactionRef,
            timestamp = payload.timestamp,
            status = "COMPLETED"
        )

        session.reconciledDonation = donation
        donationDao.insertDonation(donation)
        campaignDao.recordDonation(donation.campaignId, donation.amount)

        // Trigger secure audit notification
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + System.currentTimeMillis(),
                title = "Verified Donation Received: ${donation.amount.toInt()} ETB",
                titleAm = "የተረጋገጠ ልገሳ ተቀብለናል፡ ${donation.amount.toInt()} ብር",
                message = "Payment confirmed by ${adapter.providerName}. Verified Ref: ${donation.transactionReference}",
                messageAm = "ክፍያው በ${adapter.providerName} ተረጋግጧል። መለያ፡ ${donation.transactionReference}",
                timestamp = System.currentTimeMillis(),
                type = "donation",
                targetCampaignId = donation.campaignId
            )
        )

        return ServerVerificationResult(
            isSuccess = true,
            transactionRef = payload.transactionRef,
            providerRef = payload.providerTransactionId,
            reconciledAmount = payload.amountPaid,
            status = PaymentStatus.VERIFIED_SUCCESS,
            verificationNotes = "Cryptographic signature validated. Reconciled $expectedTotal ETB to Escrow."
        )
    }

    /**
     * Sandbox Simulation Helper: Allows users/testers to trigger authentic success,
     * timeout, wrong pin, insufficient funds, user cancel, or duplicate callback scenarios.
     */
    suspend fun simulateSandboxExecution(
        transactionRef: String,
        scenario: SandboxTestScenario
    ): Pair<ServerVerificationResult, Donation?> {
        val session = transactionIndex[transactionRef]
            ?: throw IllegalStateException("Transaction session $transactionRef not found")

        delay(1200) // Realistic latency simulation of USSD roundtrip

        val payload = when (scenario) {
            SandboxTestScenario.SUCCESS -> {
                ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = session.initRequest.totalAmount,
                    status = "SUCCESS",
                    signature = "valid_sandbox_sha256_sig",
                    timestamp = System.currentTimeMillis()
                )
            }
            SandboxTestScenario.INSUFFICIENT_FUNDS -> {
                ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = 0.0,
                    status = "FAILED",
                    errorCode = "INSUFFICIENT_FUNDS",
                    errorMessage = "Telebirr account balance is lower than transaction amount.",
                    signature = "valid_sandbox_sha256_sig",
                    timestamp = System.currentTimeMillis()
                )
            }
            SandboxTestScenario.INCORRECT_PIN -> {
                ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = 0.0,
                    status = "FAILED",
                    errorCode = "INCORRECT_PIN",
                    errorMessage = "3 invalid PIN attempts on mobile money prompt.",
                    signature = "valid_sandbox_sha256_sig",
                    timestamp = System.currentTimeMillis()
                )
            }
            SandboxTestScenario.TIMEOUT -> {
                ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = 0.0,
                    status = "TIMEOUT",
                    errorMessage = "User did not respond to USSD authorization prompt within 90s.",
                    signature = "valid_sandbox_sha256_sig",
                    timestamp = System.currentTimeMillis()
                )
            }
            SandboxTestScenario.CANCELLED -> {
                ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = 0.0,
                    status = "CANCELLED",
                    errorMessage = "Customer pressed Cancel on payment confirmation screen.",
                    signature = "valid_sandbox_sha256_sig",
                    timestamp = System.currentTimeMillis()
                )
            }
            SandboxTestScenario.DUPLICATE_CALLBACK -> {
                // First simulate success, then trigger second duplicate
                val first = ProviderCallbackPayload(
                    transactionRef = transactionRef,
                    providerTransactionId = session.initResult.providerTransactionId,
                    amountPaid = session.initRequest.totalAmount,
                    status = "SUCCESS",
                    signature = "dup_sig_${System.currentTimeMillis()}",
                    timestamp = System.currentTimeMillis()
                )
                processProviderCallback(first)
                // Second call with same signature to trigger duplicate protection
                first
            }
        }

        val result = processProviderCallback(payload)
        return Pair(result, session.reconciledDonation)
    }

    enum class SandboxTestScenario(val label: String, val description: String) {
        SUCCESS("Success (200 OK)", "Simulates customer authorizing PIN promptly"),
        INSUFFICIENT_FUNDS("Insufficient Funds", "Wallet balance too low for donation"),
        INCORRECT_PIN("Wrong PIN", "Customer entered wrong mobile money PIN"),
        TIMEOUT("USSD Timeout", "Customer phone prompt was not answered in time"),
        CANCELLED("Customer Cancelled", "Donor pressed cancel on USSD prompt"),
        DUPLICATE_CALLBACK("Duplicate Callback", "Tests idempotency and double-spend protection")
    }
}
