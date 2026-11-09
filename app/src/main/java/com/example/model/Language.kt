package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    AMHARIC("am", "Amharic", "አማርኛ"),
    AFAAN_OROMOO("om", "Afaan Oromoo", "Afaan Oromoo")
}

data class LocalizedText(
    val en: String,
    val am: String,
    val om: String
) {
    fun get(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> en
        AppLanguage.AMHARIC -> am
        AppLanguage.AFAAN_OROMOO -> om
    }
}
