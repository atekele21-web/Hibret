package com.example

import com.example.data.CampaignDao
import com.example.data.DonationDao
import com.example.data.NotificationDao
import com.example.data.payment.CbeBirrAdapter
import com.example.data.payment.PaymentErrorReason
import com.example.data.payment.PaymentInitRequest
import com.example.data.payment.PaymentStatus
import com.example.data.payment.ProviderCallbackPayload
import com.example.data.payment.ServerPaymentEngine
import com.example.data.payment.TelebirrAdapter
import com.example.model.Campaign
import com.example.model.Donation
import com.example.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ServerPaymentEngineTest {

    private lateinit var mockDonationDao: MockDonationDao
    private lateinit var mockCampaignDao: MockCampaignDao
    private lateinit var mockNotificationDao: MockNotificationDao
    private lateinit var engine: ServerPaymentEngine

    @Before
    fun setup() {
        mockDonationDao = MockDonationDao()
        mockCampaignDao = MockCampaignDao()
        mockNotificationDao = MockNotificationDao()
        engine = ServerPaymentEngine(mockDonationDao, mockCampaignDao, mockNotificationDao)
    }

    @Test
    fun testTelebirrAdapter_initializesWithPendingStatusAndTtl() = runBlocking {
        val adapter = TelebirrAdapter(isSandboxMode = true)
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Tikur Anbessa Surgery",
            donorName = "Abebe Bikila",
            donorPhone = "+251911234567",
            donorEmail = "abebe@hibret.et",
            isAnonymous = false,
            amount = 1000.0,
            tipAmount = 50.0,
            feeAmount = 0.0,
            totalAmount = 1050.0,
            providerId = "telebirr",
            idempotencyKey = "key_test_1"
        )
        val init = adapter.initializePayment(req)
        assertEquals(PaymentStatus.PENDING_CUSTOMER_ACTION, init.status)
        assertTrue(init.transactionRef.startsWith("HIB-2026-TL-"))
        assertTrue(init.expiresAt > System.currentTimeMillis())
    }

    @Test
    fun testServerEngine_idempotencyPreservesSameSession() = runBlocking {
        val key = "idem_unique_" + UUID.randomUUID().toString()
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Education in Hawassa",
            donorName = "Almaz",
            donorPhone = "+251922334455",
            donorEmail = "almaz@hibret.et",
            isAnonymous = true,
            amount = 500.0,
            tipAmount = 0.0,
            feeAmount = 0.0,
            totalAmount = 500.0,
            providerId = "telebirr",
            idempotencyKey = key
        )

        val firstInit = engine.initializePayment(req)
        val secondInit = engine.initializePayment(req)

        assertEquals("Same idempotency key MUST return exact same transaction reference",
            firstInit.transactionRef, secondInit.transactionRef)
        assertEquals(firstInit.providerTransactionId, secondInit.providerTransactionId)
    }

    @Test
    fun testServerEngine_successfulVerificationAndReconciliation() = runBlocking {
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Tikur Anbessa Surgery",
            donorName = "Dawit",
            donorPhone = "+251911998877",
            donorEmail = "dawit@hibret.et",
            isAnonymous = false,
            amount = 250.0,
            tipAmount = 0.0,
            feeAmount = 0.0,
            totalAmount = 250.0,
            providerId = "telebirr",
            idempotencyKey = "key_success_" + UUID.randomUUID().toString()
        )
        val init = engine.initializePayment(req)

        val (verif, donation) = engine.simulateSandboxExecution(
            init.transactionRef,
            ServerPaymentEngine.SandboxTestScenario.SUCCESS
        )

        assertTrue(verif.isSuccess)
        assertEquals(PaymentStatus.VERIFIED_SUCCESS, verif.status)
        assertNotNull(donation)
        assertEquals(250.0, donation?.amount ?: 0.0, 0.01)
        assertEquals(1, mockDonationDao.insertedDonations.size)
    }

    @Test
    fun testServerEngine_failureScenarioInsufficientFunds() = runBlocking {
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Tikur Anbessa Surgery",
            donorName = "Tadesse",
            donorPhone = "+251911001122",
            donorEmail = "tadesse@hibret.et",
            isAnonymous = false,
            amount = 5000.0,
            tipAmount = 0.0,
            feeAmount = 0.0,
            totalAmount = 5000.0,
            providerId = "telebirr",
            idempotencyKey = "key_funds_" + UUID.randomUUID().toString()
        )
        val init = engine.initializePayment(req)

        val (verif, donation) = engine.simulateSandboxExecution(
            init.transactionRef,
            ServerPaymentEngine.SandboxTestScenario.INSUFFICIENT_FUNDS
        )

        assertFalse(verif.isSuccess)
        assertEquals(PaymentStatus.FAILED, verif.status)
        assertEquals(PaymentErrorReason.INSUFFICIENT_FUNDS, verif.errorReason)
        assertEquals("Failed payment must not record a completed donation", 0, mockDonationDao.insertedDonations.size)
    }

    @Test
    fun testServerEngine_timeoutScenario() = runBlocking {
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Tikur Anbessa Surgery",
            donorName = "Marta",
            donorPhone = "+251911443322",
            donorEmail = "marta@hibret.et",
            isAnonymous = false,
            amount = 100.0,
            tipAmount = 0.0,
            feeAmount = 0.0,
            totalAmount = 100.0,
            providerId = "cbe_birr",
            idempotencyKey = "key_timeout_" + UUID.randomUUID().toString()
        )
        val init = engine.initializePayment(req)

        val (verif, _) = engine.simulateSandboxExecution(
            init.transactionRef,
            ServerPaymentEngine.SandboxTestScenario.TIMEOUT
        )

        assertFalse(verif.isSuccess)
        assertEquals(PaymentStatus.TIMEOUT, verif.status)
        assertEquals(PaymentErrorReason.USSD_TIMEOUT, verif.errorReason)
    }

    @Test
    fun testServerEngine_duplicateCallbackIgnored() = runBlocking {
        val req = PaymentInitRequest(
            campaignId = "camp_1",
            campaignTitle = "Tikur Anbessa Surgery",
            donorName = "Yared",
            donorPhone = "+251911554433",
            donorEmail = "yared@hibret.et",
            isAnonymous = false,
            amount = 300.0,
            tipAmount = 0.0,
            feeAmount = 0.0,
            totalAmount = 300.0,
            providerId = "cbe_birr",
            idempotencyKey = "key_dup_" + UUID.randomUUID().toString()
        )
        val init = engine.initializePayment(req)

        val (verif, _) = engine.simulateSandboxExecution(
            init.transactionRef,
            ServerPaymentEngine.SandboxTestScenario.DUPLICATE_CALLBACK
        )

        assertEquals("Duplicate callback should report duplicate transaction",
            PaymentErrorReason.DUPLICATE_TRANSACTION, verif.errorReason)
        assertEquals("Account must not be credited twice for duplicate webhooks", 1, mockDonationDao.insertedDonations.size)
    }

    // Mock DAOs for deterministic unit testing
    class MockDonationDao : DonationDao {
        val insertedDonations = mutableListOf<Donation>()
        override fun getAllDonations(): Flow<List<Donation>> = flowOf(insertedDonations)
        override fun getDonationsForCampaign(campaignId: String): Flow<List<Donation>> = flowOf(insertedDonations)
        override fun getTotalDonatedAmount(): Flow<Double?> = flowOf(insertedDonations.sumOf { it.amount })
        override fun getTotalDonationsCount(): Flow<Int> = flowOf(insertedDonations.size)
        override suspend fun insertDonation(donation: Donation) { insertedDonations.add(donation) }
        override suspend fun insertAll(donations: List<Donation>) { insertedDonations.addAll(donations) }
        override suspend fun getDonationsRecordCount(): Int = insertedDonations.size
    }

    class MockCampaignDao : CampaignDao {
        override fun getAllApprovedCampaigns(): Flow<List<Campaign>> = flowOf(emptyList())
        override fun getAllCampaignsForAdmin(): Flow<List<Campaign>> = flowOf(emptyList())
        override fun getCampaignById(id: String): Flow<Campaign?> = flowOf(null)
        override fun getUserCampaigns(phone: String, name: String): Flow<List<Campaign>> = flowOf(emptyList())
        override suspend fun insertCampaign(campaign: Campaign) {}
        override suspend fun insertAll(campaigns: List<Campaign>) {}
        override suspend fun updateCampaignStatus(id: String, status: String) {}
        override suspend fun recordDonation(id: String, amount: Double) {}
        override suspend fun getCampaignsCount(): Int = 0
        override suspend fun appendCampaignUpdate(id: String, serializedUpdate: String) {}
        override suspend fun updateCampaignDetails(id: String, title: String, shortDescription: String, story: String, targetAmount: Double, city: String, region: String, coverDrawableName: String, budgetBreakdownRaw: String) {}
    }

    class MockNotificationDao : NotificationDao {
        val notifications = mutableListOf<NotificationItem>()
        override fun getAllNotifications(): Flow<List<NotificationItem>> = flowOf(notifications)
        override fun getUnreadCount(): Flow<Int> = flowOf(notifications.count { !it.isRead })
        override suspend fun insertNotification(notification: NotificationItem) { notifications.add(notification) }
        override suspend fun insertAll(notificationsList: List<NotificationItem>) { notifications.addAll(notificationsList) }
        override suspend fun markAllAsRead() { notifications.forEach { /* mark read */ } }
        override suspend fun getCount(): Int = notifications.size
    }
}
