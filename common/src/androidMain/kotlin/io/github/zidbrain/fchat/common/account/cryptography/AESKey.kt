package io.github.zidbrain.fchat.common.account.cryptography

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class AESKey actual constructor(actual val encoded: ByteArray) : CryptographicKey {
    companion object {
        private val random = SecureRandom()
    }

    actual override fun encrypt(content: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = SecretKeySpec(encoded, "AES")

        val ivBytes = ByteArray(16)
        random.nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        return cipher.doFinal(content) + ivBytes
    }

    actual override fun decrypt(content: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = SecretKeySpec(encoded, "AES")

        val ivBytes = content.takeLast(16).toByteArray()
        val ivSpec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        return cipher.doFinal(content.dropLast(16).toByteArray())
    }
}

fun SecretKey.toModel() = AESKey(encoded)

private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"