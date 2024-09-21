package io.github.zidbrain.fchat.common.account.cryptography

import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

actual class RSAKeyPair(private val publicKey: PublicKey, private val privateKey: PrivateKey) :
    CryptographicKey {

    actual val public: ByteArray
        get() = publicKey.encoded

    actual override fun encrypt(content: ByteArray): ByteArray = content.encryptRSA(public)

    actual override fun decrypt(content: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(content)
    }
}

actual fun ByteArray.encryptRSA(publicKey: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    val keySpec = X509EncodedKeySpec(publicKey)
    val keyFactory = KeyFactory.getInstance("RSA")
    val key = keyFactory.generatePublic(keySpec)

    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(this)
}

fun KeyPair.toModel() = RSAKeyPair(public, private)

private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"