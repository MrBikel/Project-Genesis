package com.meh.projectgenesis.core.security

import android.content.Context
import android.content.SharedPreferences

object SecretRedactor {
    fun maskApiKey(key: String?): String {
        if (key.isNullOrBlank()) return "সংরক্ষিত নেই (None)"
        if (key.length <= 8) return "••••••••"
        val prefix = key.take(4)
        val suffix = key.takeLast(4)
        return "$prefix••••••••$suffix"
    }

    fun redactAllSecrets(text: String): String {
        // Redact common key patterns like AIza..., sk-..., etc.
        val geminiRegex = Regex("AIza[0-9A-Za-z-_]{35}")
        val openAiRegex = Regex("sk-[0-9A-Za-z]{32,}")
        var sanitized = text.replace(geminiRegex, "[REDACTED_API_KEY]")
        sanitized = sanitized.replace(openAiRegex, "[REDACTED_API_KEY]")
        return sanitized
    }
}

class KeyManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("genesis_secure_keys", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI = "key_gemini"
        private const val KEY_OPENAI = "key_openai"
    }

    fun saveGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI, key.trim()).apply()
    }

    fun getGeminiApiKey(): String? {
        val stored = prefs.getString(KEY_GEMINI, null)
        return if (stored.isNullOrBlank()) null else stored
    }

    fun clearGeminiApiKey() {
        prefs.edit().remove(KEY_GEMINI).apply()
    }

    fun getMaskedGeminiKey(): String {
        return SecretRedactor.maskApiKey(getGeminiApiKey())
    }

    fun saveOpenAiApiKey(key: String) {
        prefs.edit().putString(KEY_OPENAI, key.trim()).apply()
    }

    fun getOpenAiApiKey(): String? {
        val stored = prefs.getString(KEY_OPENAI, null)
        return if (stored.isNullOrBlank()) null else stored
    }

    fun clearOpenAiApiKey() {
        prefs.edit().remove(KEY_OPENAI).apply()
    }

    fun getMaskedOpenAiKey(): String {
        return SecretRedactor.maskApiKey(getOpenAiApiKey())
    }

    fun hasAnyActiveKey(): Boolean {
        return !getGeminiApiKey().isNullOrBlank() || !getOpenAiApiKey().isNullOrBlank()
    }
}
