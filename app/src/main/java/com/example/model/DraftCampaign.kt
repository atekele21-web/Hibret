package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_campaigns")
data class DraftCampaign(
    @PrimaryKey
    val draftId: String = "active_draft",
    val step: Int = 1,
    val categoryId: String = "medical",
    val title: String = "",
    val shortDescription: String = "",
    val region: String = "Addis Ababa",
    val city: String = "Addis Ababa",
    val beneficiaryName: String = "",
    val beneficiaryRelation: String = "Myself",
    val story: String = "",
    val targetAmount: String = "50000",
    val durationDays: String = "30",
    val budgetExplanation: String = "",
    val organizerName: String = "Abebe Bikila",
    val organizerPhone: String = "+251911223344",
    val payoutMethod: String = "Telebirr",
    val payoutAccount: String = "0911223344",
    val verificationDocName: String = "Kebele_ID_Scan.pdf",
    val termsAccepted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
