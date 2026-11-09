package com.example.data.payment

/**
 * Standardized status for payment intents and transaction lifecycles in Hibret.
 */
enum class PaymentStatus {
    INITIALIZED,
    PENDING_CUSTOMER_ACTION, // USSD push sent, waiting for customer PIN entry
    PROCESSING,              // Callback received, verifying server-side signature
    VERIFIED_SUCCESS,        // Server cryptographic signature verified & amount reconciled
    FAILED,                  // Insufficient funds, wrong PIN, expired session
    TIMEOUT,                 // Customer did not authorize within TTL window
    CANCELLED                // Explicitly cancelled by user or merchant
}

/**
 * Detailed error reason to provide clear, actionable feedback to users.
 */
enum class PaymentErrorReason(val userMessage: String, val amharicMessage: String) {
    INSUFFICIENT_FUNDS(
        "Insufficient wallet/account balance. Please top up your account and try again.",
        "በቂ ሂሳብ የለም። እባክዎ ሂሳብዎን ይሙሉ እና እንደገና ይሞክሩ።"
    ),
    INCORRECT_PIN(
        "Incorrect security PIN entered on your mobile money prompt.",
        "የተሳሳተ የይለፍ ቃል (PIN) አስገብተዋል።"
    ),
    USSD_TIMEOUT(
        "Mobile money authorization request timed out. Please keep your phone unlocked.",
        "የቴሌብር/ባንክ ማረጋገጫ ጥያቄ ጊዜው አልቋል።"
    ),
    NETWORK_FAILURE(
        "Network communication failure between payment gateway and Hibret server.",
        "ከክፍያ አቅራቢው ጋር የግንኙነት መቆራረጥ አጋጥሟል።"
    ),
    TRANSACTION_LIMIT_EXCEEDED(
        "Daily mobile money transaction limit exceeded for this account.",
        "የቀን የግብይት ገደብ አልፏል።"
    ),
    SIGNATURE_VERIFICATION_FAILED(
        "Security verification failed: Untrusted payment callback signature.",
        "የክፍያ ማረጋገጫ ፊርማ አልፀደቀም።"
    ),
    DUPLICATE_TRANSACTION(
        "Duplicate payment detected. Your account has not been charged twice.",
        "ተደጋጋሚ ክፍያ ተገኝቷል። ሂሳብዎ ሁለት ጊዜ አልተቆረጠም።"
    ),
    USER_CANCELLED(
        "You cancelled the payment request.",
        "የክፍያውን ጥያቄ ሰርዘውታል።"
    ),
    PROVIDER_MAINTENANCE(
        "Payment provider is currently undergoing scheduled maintenance.",
        "የክፍያ አቅራቢው በጊዜያዊ ጥገና ላይ ነው።"
    )
}

/**
 * Payment initialization request sent from Hibret client.
 */
data class PaymentInitRequest(
    val campaignId: String,
    val campaignTitle: String,
    val donorName: String,
    val donorPhone: String,
    val donorEmail: String,
    val isAnonymous: Boolean,
    val amount: Double,
    val tipAmount: Double,
    val feeAmount: Double,
    val totalAmount: Double,
    val providerId: String, // "telebirr", "cbe_birr", "awash_bank", "international_card"
    val idempotencyKey: String,
    val clientIp: String = "196.188.240.1" // Ethiopian IP space
)

/**
 * Result returned after initializing the transaction with the provider adapter.
 */
data class PaymentInitResult(
    val transactionRef: String,
    val idempotencyKey: String,
    val providerTransactionId: String,
    val status: PaymentStatus,
    val checkoutUrl: String? = null,
    val ussdPushPrompt: String? = null,
    val instructions: String,
    val expiresAt: Long
)

/**
 * Cryptographically signed webhook / callback payload simulated from payment provider server.
 */
data class ProviderCallbackPayload(
    val transactionRef: String,
    val providerTransactionId: String,
    val amountPaid: Double,
    val currency: String = "ETB",
    val status: String, // "SUCCESS", "FAILED", "TIMEOUT", "CANCELLED"
    val signature: String, // HMAC-SHA256 signature
    val timestamp: Long,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

/**
 * Server-side verified transaction outcome.
 */
data class ServerVerificationResult(
    val isSuccess: Boolean,
    val transactionRef: String,
    val providerRef: String,
    val reconciledAmount: Double,
    val status: PaymentStatus,
    val errorReason: PaymentErrorReason? = null,
    val verificationNotes: String
)
