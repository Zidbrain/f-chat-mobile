package io.github.zidbrain.fchat.common.account.encryption

interface EncryptionService {
    fun devicePublicKey(email: String): String
}