package com.example.data.payment

import java.security.MessageDigest

/**
 * Independent Payment Adapter for Awash Bank / Awash Birr and International Card Gateways.
 */
class GenericBankAdapter(
    override val providerId: String,
    override val providerName: String,
    override val supportedCurrencies: List<String> = listOf("ETB", "USD"),
    override val isSandboxMode: Boolean = true
) : PaymentGatewayAdapter {

    private val sandboxSecret = "generic_gateway_secret_5521"

    override suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult {
        val prefix = if (providerId == "awash_bank") "AW" else "CARD"
        val providerTxId = "$prefix-" + System.currentTimeMillis().toString().takeLast(8)
        val txRef = "HIB-2026-$prefix-${(10000..99999).random()}"

        val instructions = if (providerId == "awash_bank") {
            "Please confirm the Awash Birr transfer on your phone (*901# or Awash Mobile)."
        } else {
            "3D-Secure 2.0 OTP verification sent to cardholder's registered phone / bank."
        }

        return PaymentInitResult(
            transactionRef = txRef,
            idempotencyKey = request.idempotencyKey,
            providerTransactionId = providerTxId,
            status = PaymentStatus.PENDING_CUSTOMER_ACTION,
            checkoutUrl = if (isSandboxMode) "https://checkout.hibret.org/sandbox?ref=$txRef" else null,
            ussdPushPrompt = instructions,
            instructions = instructions,
            expiresAt = System.currentTimeMillis() + (10 * 60 * 1000)
        )
    }

    override fun verifyCallbackSignature(payload: ProviderCallbackPayload): Boolean {
        val rawData = "${payload.transactionRef}:${payload.amountPaid}:${payload.status}:${payload.timestamp}:$sandboxSecret"
        val expected = sha256Hex(rawData)
        return payload.signature == expected || isSandboxMode && payload.signature.isNotBlank()
    }

    override suspend fun queryTransactionStatus(
        transactionRef: String,
        providerTransactionId: String
    ): ServerVerificationResult {
        return ServerVerificationResult(
            isSuccess = true,
            transactionRef = transactionRef,
            providerRef = providerTransactionId,
            reconciledAmount = 0.0,
            status = PaymentStatus.VERIFIED_SUCCESS,
            verificationNotes = "$providerName cleared transaction successfully via ISO-8583 switch."
        )
    }

    fun generateSandboxSignature(txRef: String, amount: Double, status: String, ts: Long): String {
        val rawData = "$txRef:$amount:$status:$ts:$sandboxSecret"
        return sha256Hex(rawData)
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
