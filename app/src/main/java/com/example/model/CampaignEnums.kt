package com.example.model

import com.example.R

enum class CampaignCategory(val id: String, val localizedName: LocalizedText, val iconRes: Int? = null) {
    ALL("all", LocalizedText("All", "ሁሉም", "Hunda")),
    MEDICAL("medical", LocalizedText("Medical", "ህክምና", "Yaalaa")),
    EDUCATION("education", LocalizedText("Education", "ትምህርት", "Barnoota")),
    EMERGENCY("emergency", LocalizedText("Emergency", "አስቸኳይ", "Ariifachiisaa")),
    FAMILY("family", LocalizedText("Family", "ቤተሰብ", "Maatii")),
    COMMUNITY("community", LocalizedText("Community", "ማህበረሰብ", "Hawaasa")),
    ENVIRONMENT("environment", LocalizedText("Environment", "አካባቢ", "Naannoo")),
    OTHER("other", LocalizedText("Other", "ሌሎች", "Kan biroo"));

    companion object {
        fun fromId(id: String): CampaignCategory = values().find { it.id == id } ?: ALL
    }
}

enum class CampaignStatus(val label: LocalizedText) {
    DRAFT(LocalizedText("Draft", "ረቂቅ", "Wixinee")),
    SUBMITTED(LocalizedText("Submitted", "የቀረበ", "Dhiyaateera")),
    UNDER_REVIEW(LocalizedText("Under Review", "በግምገማ ላይ", "Gamaaggama Irra")),
    MORE_INFO_REQUIRED(LocalizedText("More Info Required", "ተጨማሪ መረጃ ይፈለጋል", "Odeeffannoo Dabalataa")),
    APPROVED(LocalizedText("Approved", "የፀደቀ", "Mirkanaa'eera")),
    REJECTED(LocalizedText("Rejected", "ውድቅ የተደረገ", "Kufaa Ta'e")),
    SUSPENDED(LocalizedText("Suspended", "የታገደ", "Dhabameera")),
    COMPLETED(LocalizedText("Completed", "የተጠናቀቀ", "Xumurameera"))
}

enum class VerificationBadge(val title: LocalizedText, val desc: LocalizedText) {
    UNVERIFIED(
        LocalizedText("Unverified", "ያልተረጋገጠ", "Mirkaneeffamuu kan hin dandeenye"),
        LocalizedText("Identity review pending", "የማንነት ማረጋገጫ በመጠባበቅ ላይ", "Eenyummeessaa eeggachaa jira")
    ),
    IDENTITY_VERIFIED(
        LocalizedText("ID Verified", "መታወቂያ ተረጋግጧል", "Eenyummaan Mirkanaa'e"),
        LocalizedText("National/Kebele ID verified by Hibret", "በሂብረት ብሔራዊ/የቀበሌ መታወቂያ የተረጋገጠ", "Eenyummaa biyyaalessaa Hibret mirkaneesse")
    ),
    MEDICAL_VERIFIED(
        LocalizedText("Hospital & ID Verified", "የሆስፒታልና መታወቂያ የተረጋገጠ", "Yaalaafi Eenyummaan Mirkanaa'e"),
        LocalizedText("Hospital letters & clinical quotes verified with provider", "የሆስፒታል ደብዳቤና የህክምና ዋጋ ዝርዝር ከህክምና ተቋሙ የተረጋገጠ", "Waraqaan yaalaafi baasiin hospitaalaa mirkanaa'eera")
    ),
    COMMUNITY_VERIFIED(
        LocalizedText("Community Committee Verified", "በማህበረሰብ ኮሚቴ የተረጋገጠ", "Koree Hawaasaatiin Mirkanaa'e"),
        LocalizedText("Local leadership and development committee confirmed", "በአካባቢው አመራርና የልማት ኮሚቴ ማረጋገጫ የተሰጠበት", "Gaggeessitoota naannootiin kan mirkanaa'e")
    )
}

data class BudgetItem(
    val title: String,
    val amount: Double
)

data class CampaignUpdate(
    val date: String,
    val title: String,
    val body: String,
    val author: String
)
