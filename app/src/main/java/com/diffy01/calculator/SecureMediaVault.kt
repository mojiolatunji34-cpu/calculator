package com.diffy01.calculator

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import javax.crypto.CipherInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONArray
import org.json.JSONObject

data class SecureMediaItem(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long
)

/**
 * Stores imported media as AES-GCM encrypted files in app-private internal storage.
 * The encrypted catalogue is also protected with AES-GCM. No media permission is required:
 * the Android document picker grants access to a selected file and it is copied immediately.
 */
class SecureMediaVault(private val context: Context) {
    private val preferences = context.getSharedPreferences("secure_media_vault", Context.MODE_PRIVATE)
    private val directory = File(context.filesDir, "private_media").apply { mkdirs() }
    private val random = SecureRandom()
    private val keyAlias = "diffy01_secure_media_key"

    init {
        context.cacheDir.listFiles()
            ?.filter { it.name.startsWith("private_preview_") }
            ?.forEach { it.delete() }
    }

    @Synchronized
    fun listItems(): List<SecureMediaItem> = runCatching {
        val encoded = preferences.getString("catalogue_ciphertext", null) ?: return emptyList()
        val iv = preferences.getString("catalogue_iv", null)?.let(::decode) ?: return emptyList()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        val json = cipher.doFinal(decode(encoded)).toString(Charsets.UTF_8)
        val array = JSONArray(json)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(SecureMediaItem(
                    item.getString("id"),
                    item.getString("name"),
                    item.getString("mime"),
                    item.getLong("size")
                ))
            }
        }
    }.getOrDefault(emptyList())

    @Synchronized
    fun importFile(uri: Uri, displayName: String, mimeType: String): SecureMediaItem {
        val id = UUID.randomUUID().toString()
        val destination = File(directory, "$id.enc")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        var plainBytes = 0L
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: error("Android could not open the selected file.")
            input.use { source ->
                FileOutputStream(destination).use { raw ->
                    raw.write(cipher.iv)
                    CipherOutputStream(raw, cipher).use { encrypted ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            encrypted.write(buffer, 0, count)
                            plainBytes += count
                        }
                        buffer.fill(0)
                    }
                }
            }
            val safeName = displayName.substringAfterLast('/').take(180).ifBlank { "Private file" }
            val item = SecureMediaItem(id, safeName, mimeType.ifBlank { "application/octet-stream" }, plainBytes)
            saveCatalogue(listItems() + item)
            return item
        } catch (error: Exception) {
            destination.delete()
            throw error
        }
    }

    /** Decrypts one selected item to a short-lived cache file for in-app preview. */
    @Synchronized
    fun decryptToCache(id: String): File {
        require(id.matches(Regex("[a-fA-F0-9-]{36}"))) { "Invalid private file identifier." }
        val encryptedFile = File(directory, "$id.enc")
        require(encryptedFile.isFile) { "Private file not found." }
        val item = listItems().firstOrNull { it.id == id } ?: error("Private file not found.")
        val extension = when (item.mimeType.lowercase()) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "video/mp4" -> ".mp4"
            "video/webm" -> ".webm"
            "video/3gpp" -> ".3gp"
            "video/quicktime" -> ".mov"
            else -> ".tmp"
        }
        val temporary = File.createTempFile("private_preview_", extension, context.cacheDir)
        try {
            FileInputStream(encryptedFile).use { raw ->
                val iv = ByteArray(12)
                var offset = 0
                while (offset < iv.size) {
                    val count = raw.read(iv, offset, iv.size - offset)
                    if (count < 0) error("Private file is damaged.")
                    offset += count
                }
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
                CipherInputStream(raw, cipher).use { decrypted ->
                    FileOutputStream(temporary).use { output ->
                        decrypted.copyTo(output, 64 * 1024)
                    }
                }
            }
            return temporary
        } catch (error: Exception) {
            temporary.delete()
            throw error
        }
    }

    @Synchronized
    fun deleteItem(id: String) {
        require(id.matches(Regex("[a-fA-F0-9-]{36}"))) { "Invalid private file identifier." }
        File(directory, "$id.enc").delete()
        saveCatalogue(listItems().filterNot { it.id == id })
    }

    private fun saveCatalogue(items: List<SecureMediaItem>) {
        val array = JSONArray()
        items.take(500).forEach { item ->
            array.put(JSONObject()
                .put("id", item.id)
                .put("name", item.name)
                .put("mime", item.mimeType)
                .put("size", item.sizeBytes))
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString("catalogue_iv", encode(cipher.iv))
            .putString("catalogue_ciphertext", encode(encrypted))
            .apply()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)

    companion object {
        fun displaySize(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }
}
