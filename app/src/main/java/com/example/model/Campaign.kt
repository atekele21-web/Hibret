package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey
    val id: String,
    val title: String,
    val titleAm: String = "",
    val titleOm: String = "",
    val shortDescription: String,
    val shortDescriptionAm: String = "",
    val shortDescriptionOm: String = "",
    val story: String,
    val categoryId: String,
    val region: String,
    val city: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val donorsCount: Int,
    val organizerName: String,
    val organizerPhone: String,
    val organizerRole: String = "Campaign Organizer",
    val beneficiaryName: String,
    val beneficiaryRelation: String,
    val coverDrawableName: String = "img_medical_aid",
    val isVerified: Boolean = false,
    val verificationType: String = "UNVERIFIED",
    val verificationNotes: String = "",
    val daysRemaining: Int = 30,
    val status: String = "APPROVED",
    val createdAt: Long = System.currentTimeMillis(),
    val budgetBreakdownRaw: String = "", // JSON or formatted string: "Medication:150000|Surgery:320000"
    val updatesRaw: String = "", // Formatted updates
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false
) {
    val category: CampaignCategory
        get() = CampaignCategory.fromId(categoryId)

    val progressPercent: Float
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100f).toFloat().coerceIn(0f, 100f) else 0f

    val progressFraction: Float
        get() = (progressPercent / 100f).coerceIn(0f, 1f)

    fun getDisplayTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AMHARIC -> if (titleAm.isNotBlank()) titleAm else title
        AppLanguage.AFAAN_OROMOO -> if (titleOm.isNotBlank()) titleOm else title
        else -> title
    }

    fun getDisplayShortDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.AMHARIC -> if (shortDescriptionAm.isNotBlank()) shortDescriptionAm else shortDescription
        AppLanguage.AFAAN_OROMOO -> if (shortDescriptionOm.isNotBlank()) shortDescriptionOm else shortDescription
        else -> shortDescription
    }

    fun getVerificationBadge(): VerificationBadge = try {
        VerificationBadge.valueOf(verificationType)
    } catch (e: Exception) {
        if (isVerified) VerificationBadge.IDENTITY_VERIFIED else VerificationBadge.UNVERIFIED
    }

    fun getBudgetItems(): List<BudgetItem> {
        if (budgetBreakdownRaw.isBlank()) return emptyList()
        return budgetBreakdownRaw.split("|").mapNotNull { part ->
            val split = part.split(":")
            if (split.size == 2) {
                val amt = split[1].toDoubleOrNull() ?: 0.0
                BudgetItem(split[0].trim(), amt)
            } else null
        }
    }

    fun getUpdates(): List<CampaignUpdate> {
        if (updatesRaw.isBlank()) return emptyList()
        return updatesRaw.split("###").mapNotNull { part ->
            val lines = part.trim().split("\n")
            if (lines.size >= 3) {
                CampaignUpdate(
                    date = lines[0].trim(),
                    title = lines[1].trim(),
                    body = lines.drop(2).joinToString("\n").trim(),
                    author = organizerName
                )
            } else null
        }
    }
}
