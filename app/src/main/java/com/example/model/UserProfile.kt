package com.example.model

data class UserProfile(
    val id: String = "user_001",
    val fullName: String = "Abebe Bikila",
    val email: String = "abebe.b@hibret.et",
    val phone: String = "+251 91 123 4567",
    val location: String = "Addis Ababa, Bole Sub-City",
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isVerified: Boolean = true,
    val verificationLevel: String = "IDENTITY_VERIFIED",
    val nationalIdStatus: String = "Verified (Fayda / Kebele #AA-9921)"
)
