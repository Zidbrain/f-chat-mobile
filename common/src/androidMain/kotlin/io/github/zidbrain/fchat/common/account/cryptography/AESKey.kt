package io.github.zidbrain.fchat.common.account.cryptography

import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey

actual class AESKey actual constructor(actual val encoded: ByteArray) : CryptographicKey {
    actual override fun encrypt(content: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = X509EncodedKeySpec(encoded)
        val keyFactory = KeyFactory.getInstance("AES")
        val key = keyFactory.generatePrivate(keySpec)

        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(content)
    }
    actual override fun decrypt(content: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = X509EncodedKeySpec(encoded)
        val keyFactory = KeyFactory.getInstance("AES")
        val key = keyFactory.generatePrivate(keySpec)

        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(content)
    }
}

fun SecretKey.toModel() = AESKey(encoded)

private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"