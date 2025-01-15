package io.github.zidbrain.fchat.common.account.cryptography

interface CryptographyService {
    fun deviceKeyPair(email: String): RSAKeyPair

    fun generateSymmetricKey(): AESKey
}

interface CryptographicKey {
    fun encrypt(content: ByteArray): ByteArray
    fun decrypt(content: ByteArray): ByteArray
}

expect class RSAKeyPair : CryptographicKey {
    val public: ByteArray

    override fun encrypt(content: ByteArray): ByteArray
    override fun decrypt(content: ByteArray): ByteArray
}

expect fun ByteArray.encryptRSA(publicKey: ByteArray): ByteArray

expect class AESKey(encoded: ByteArray) : CryptographicKey {
    val encoded: ByteArray

    override fun encrypt(content: ByteArray): ByteArray
    override fun decrypt(content: ByteArray): ByteArray
}