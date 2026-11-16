package com.diffy01.calculator

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

data class VaultEntry(val title: String, val secret: String)

class SecureVault(context: Context) {
    private val prefs = context.getSharedPreferences("private_vault", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private val alias = "diffy01_vault_key"

    fun isConfigured() = prefs.contains("pin_hash") && prefs.contains("pin_salt")

    fun setPin(pin: CharArray): String {
        require(pin.size in 6..12 && pin.all { it.isDigit() }) { "PIN must contain 6–12 digits" }
        val recoveryCode = generateRecoveryCode()
        savePinHash(pin)
        saveRecoveryHash(recoveryCode)
        encryptEntries(emptyList())
        pin.fill(0.toChar())
        return recoveryCode
    }

    @Synchronized
    fun verifyPin(pin: CharArray): Boolean {
        try {
            val lockedUntil = prefs.getLong("locked_until", 0L)
            if (System.currentTimeMillis() < lockedUntil) return false
            val salt = prefs.getString("pin_salt", null)?.let(::unb64) ?: return false
            val expected = prefs.getString("pin_hash", null)?.let(::unb64) ?: return false
            val actual = pinHash(pin, salt)
            val valid = java.security.MessageDigest.isEqual(expected, actual)
            recordAttempt(valid)
            return valid
        } finally {
            pin.fill('\u0000')
        }
    }

    fun changePin(currentPin: CharArray, newPin: CharArray): String {
        try {
            require(verifyPin(currentPin)) { "Current PIN is incorrect" }
            require(newPin.size in 6..12 && newPin.all { it.isDigit() }) { "New PIN must contain 6–12 digits" }
            savePinHash(newPin)
            val replacementCode = generateRecoveryCode()
            saveRecoveryHash(replacementCode)
            return replacementCode
        } finally {
            currentPin.fill(0.toChar())
            newPin.fill(0.toChar())
        }
    }

    fun readEntries(): List<VaultEntry> {
        val cipherText = prefs.getString("payload", null) ?: return emptyList()
        val iv = prefs.getString("iv", null)?.let(::unb64) ?: return emptyList()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(unb64(cipherText)).toString(Charsets.UTF_8)
        val array = JSONArray(plain)
        return buildList { for (i in 0 until array.length()) { val o=array.getJSONObject(i); add(VaultEntry(o.getString("title"),o.getString("secret"))) } }
    }

    fun writeEntries(entries: List<VaultEntry>) {
        val array = JSONArray()
        entries.take(100).forEach { array.put(JSONObject().put("title", it.title).put("secret", it.secret)) }
        encryptEntries(entries.take(100), array.toString())
    }

    private fun encryptEntries(entries: List<VaultEntry>, json: String? = null) {
        val data = (json ?: JSONArray().toString()).toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        prefs.edit().putString("iv", b64(cipher.iv)).putString("payload", b64(cipher.doFinal(data))).apply()
    }

    fun resetPinWithRecoveryCode(recoveryCode: CharArray, newPin: CharArray): String {
        try {
            require(newPin.size in 6..12 && newPin.all { it.isDigit() }) { "New PIN must contain 6–12 digits" }
            require(verifyRecoveryCode(recoveryCode)) { "Recovery code is incorrect or temporarily locked" }
            savePinHash(newPin)
            val replacementCode = generateRecoveryCode()
            saveRecoveryHash(replacementCode)
            return replacementCode
        } finally {
            recoveryCode.fill(0.toChar())
            newPin.fill(0.toChar())
        }
    }

    private fun verifyRecoveryCode(code: CharArray): Boolean {
        try {
            if (System.currentTimeMillis() < prefs.getLong("locked_until", 0L)) return false
            val salt = prefs.getString("recovery_salt", null)?.let(::unb64) ?: return false
            val expected = prefs.getString("recovery_hash", null)?.let(::unb64) ?: return false
            val valid = java.security.MessageDigest.isEqual(expected, pinHash(code, salt))
            recordAttempt(valid)
            return valid
        } finally {
            code.fill(0.toChar())
        }
    }

    private fun savePinHash(pin: CharArray) {
        val salt = ByteArray(16).also(random::nextBytes)
        val hash = pinHash(pin, salt)
        prefs.edit().putString("pin_salt", b64(salt)).putString("pin_hash", b64(hash)).apply()
        pin.fill(0.toChar())
    }

    private fun saveRecoveryHash(code: String) {
        val salt = ByteArray(16).also(random::nextBytes)
        val hash = pinHash(code.toCharArray(), salt)
        prefs.edit().putString("recovery_salt", b64(salt)).putString("recovery_hash", b64(hash)).apply()
    }

    private fun generateRecoveryCode(): String = buildString {
        repeat(12) { append(random.nextInt(10)) }
    }

    private fun recordAttempt(valid: Boolean) {
        if (valid) {
            prefs.edit().remove("failed_attempts").remove("locked_until").apply()
        } else {
            val attempts = prefs.getInt("failed_attempts", 0) + 1
            val editor = prefs.edit().putInt("failed_attempts", attempts)
            if (attempts >= 5) editor.putLong("locked_until", System.currentTimeMillis() + 30_000L).putInt("failed_attempts", 0)
            editor.apply()
        }
    }

    private fun getKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    private fun pinHash(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin, salt, 180000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
    }
    private fun b64(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun unb64(value: String) = Base64.decode(value, Base64.NO_WRAP)
}
