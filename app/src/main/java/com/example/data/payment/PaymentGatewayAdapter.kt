package com.example.data.payment

/**
 * Common contract for all licensed payment gateway integrations in Hibret.
 * Telebirr, CBE Birr, Awash Bank, and International Card gateways implement this.
 */
interface PaymentGatewayAdapter {
    val providerId: String
    val providerName: String
    val isSandboxMode: Boolean
    val supportedCurrencies: List<String>

    /**
     * Initializes an authorization session or USSD push request with the provider.
     */
    suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult

    /**
     * Verifies the cryptographic authenticity of an asynchronous server callback / webhook.
     */
    fun verifyCallbackSignature(payload: ProviderCallbackPayload): Boolean

    /**
     * Performs server-to-server query to reconcile transaction status directly with bank/telco host.
     */
    suspend fun queryTransactionStatus(transactionRef: String, providerTransactionId: String): ServerVerificationResult
}
