package com.example.data

import com.example.model.Campaign
import com.example.model.Donation
import com.example.model.DraftCampaign
import com.example.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HibretRepository(private val database: AppDatabase) {
    private val campaignDao = database.campaignDao()
    private val donationDao = database.donationDao()
    private val draftDao = database.draftDao()
    private val notificationDao = database.notificationDao()

    val serverPaymentEngine = com.example.data.payment.ServerPaymentEngine(
        donationDao = donationDao,
        campaignDao = campaignDao,
        notificationDao = notificationDao
    )

    val allApprovedCampaigns: Flow<List<Campaign>> = campaignDao.getAllApprovedCampaigns()
    val allCampaignsForAdmin: Flow<List<Campaign>> = campaignDao.getAllCampaignsForAdmin()
    val allDonations: Flow<List<Donation>> = donationDao.getAllDonations()
    val totalDonatedAmount: Flow<Double?> = donationDao.getTotalDonatedAmount()
    val activeDraft: Flow<DraftCampaign?> = draftDao.getActiveDraft()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getCampaignById(id: String): Flow<Campaign?> = campaignDao.getCampaignById(id)

    fun getDonationsForCampaign(campaignId: String): Flow<List<Donation>> =
        donationDao.getDonationsForCampaign(campaignId)

    fun getUserCampaigns(phone: String, name: String): Flow<List<Campaign>> =
        campaignDao.getUserCampaigns(phone, name)

    suspend fun saveDraft(draft: DraftCampaign) {
        draftDao.saveDraft(draft)
    }

    suspend fun clearDraft() {
        draftDao.clearDraft()
    }

    suspend fun submitNewCampaign(
        title: String,
        shortDesc: String,
        story: String,
        categoryId: String,
        region: String,
        city: String,
        targetAmount: Double,
        organizerName: String,
        organizerPhone: String,
        beneficiaryName: String,
        beneficiaryRelation: String,
        budgetExplanation: String,
        coverDrawableName: String = "img_community_aid"
    ): Campaign {
        val id = "camp_" + UUID.randomUUID().toString().take(8)
        val newCampaign = Campaign(
            id = id,
            title = title,
            shortDescription = shortDesc,
            story = story,
            categoryId = categoryId,
            region = region,
            city = city,
            targetAmount = targetAmount,
            currentAmount = 0.0,
            donorsCount = 0,
            organizerName = organizerName,
            organizerPhone = organizerPhone,
            beneficiaryName = beneficiaryName,
            beneficiaryRelation = beneficiaryRelation,
            coverDrawableName = coverDrawableName,
            isVerified = false,
            verificationType = "IDENTITY_VERIFIED",
            verificationNotes = "Submitted for Hibret Trust & Safety verification review.",
            daysRemaining = 30,
            status = "UNDER_REVIEW",
            createdAt = System.currentTimeMillis(),
            budgetBreakdownRaw = budgetExplanation.ifBlank { "Project Implementation:$targetAmount" },
            isFeatured = false,
            isTrending = false
        )
        campaignDao.insertCampaign(newCampaign)
        clearDraft()

        // Notify user
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + System.currentTimeMillis(),
                title = "Campaign Submitted for Review",
                titleAm = "ዘመቻው ለግምገማ ቀርቧል",
                message = "'$title' was successfully submitted. Hibret moderators will inspect documents within 24 hours.",
                messageAm = "'$title' ለግምገማ ቀርቧል። የሂብረት ተቆጣጣሪዎች በ24 ሰዓት ውስጥ ይመረምሩታል።",
                timestamp = System.currentTimeMillis(),
                type = "campaign",
                targetCampaignId = id
            )
        )
        return newCampaign
    }

    suspend fun processDonation(
        campaignId: String,
        campaignTitle: String,
        donorName: String,
        donorPhone: String,
        donorEmail: String,
        isAnonymous: Boolean,
        amount: Double,
        platformTip: Double,
        paymentMethod: String
    ): Donation {
        val methodCode = when (paymentMethod) {
            "Telebirr" -> "TL"
            "CBE Birr" -> "CB"
            "Awash Bank" -> "AW"
            else -> "ET"
        }
        val randomSuffix = (10000..99999).random()
        val ref = "HIB-2026-$methodCode-$randomSuffix"

        val donation = Donation(
            id = "don_" + UUID.randomUUID().toString().take(8),
            campaignId = campaignId,
            campaignTitle = campaignTitle,
            donorName = donorName.ifBlank { "Anonymous Donor" },
            donorPhone = donorPhone,
            donorEmail = donorEmail,
            isAnonymous = isAnonymous,
            amount = amount,
            platformTip = platformTip,
            feeAmount = 0.0, // Hibret 0% fee platform
            totalPaid = amount + platformTip,
            paymentMethod = paymentMethod,
            transactionReference = ref,
            timestamp = System.currentTimeMillis(),
            status = "COMPLETED"
        )

        donationDao.insertDonation(donation)
        campaignDao.recordDonation(campaignId, amount)

        // Add notification
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + System.currentTimeMillis(),
                title = "Payment Successful: ${amount.toInt()} ETB",
                titleAm = "የ${amount.toInt()} ብር ልገሳ ተጠናቋል",
                message = "Your contribution to '$campaignTitle' via $paymentMethod is confirmed. Ref: $ref",
                messageAm = "ለ'$campaignTitle' በ$paymentMethod ያደረጉት ልገሳ ተረጋግጧል። መለያ፡ $ref",
                timestamp = System.currentTimeMillis(),
                type = "donation",
                targetCampaignId = campaignId
            )
        )

        return donation
    }

    suspend fun updateCampaignStatus(id: String, newStatus: String, notes: String = "") {
        campaignDao.updateCampaignStatus(id, newStatus)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + System.currentTimeMillis(),
                title = "Campaign Status Update: $newStatus",
                titleAm = "የዘመቻ ሁኔታ ለውጥ፡ $newStatus",
                message = "Campaign ID $id status changed to $newStatus. $notes",
                messageAm = "የዘመቻ መለያ $id ሁኔታ ወደ $newStatus ተቀይሯል። $notes",
                timestamp = System.currentTimeMillis(),
                type = "system",
                targetCampaignId = id
            )
        )
    }

    suspend fun updateCampaign(campaign: Campaign) {
        campaignDao.updateCampaign(campaign)
    }

    suspend fun publishCampaignUpdate(
        campaignId: String,
        title: String,
        body: String,
        author: String
    ) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val todayStr = dateFormat.format(Date())
        val newUpdateBlock = "$todayStr\n$title\n$body"

        // Fetch current campaign directly from DAO
        val existing = campaignDao.getCampaignById(campaignId)
        // Since getCampaignById returns Flow, we can update via SQL or read snapshot
        // We can do a direct query or update updatesRaw
        campaignDao.appendCampaignUpdate(campaignId, newUpdateBlock)

        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + System.currentTimeMillis(),
                title = "Campaign Update Published",
                titleAm = "አዲስ የዘመቻ መረጃ ተለጥፏል",
                message = "Your update '$title' is now live for supporters.",
                messageAm = "ስለ '$title' ያጋሩት አዲስ መረጃ ለደጋፊዎች ቀርቧል።",
                timestamp = System.currentTimeMillis(),
                type = "campaign",
                targetCampaignId = campaignId
            )
        )
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }
}
