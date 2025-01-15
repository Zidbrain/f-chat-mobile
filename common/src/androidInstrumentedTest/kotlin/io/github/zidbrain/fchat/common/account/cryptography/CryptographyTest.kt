package io.github.zidbrain.fchat.common.account.cryptography

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class CryptographyTest {

    @Test
    fun can_encrypt_and_decrypt_with_rsa() {
        val testMessage = "testMessage"
        val keyPair = AndroidCryptographyService().deviceKeyPair(UUID.randomUUID().toString())
        val encrypted = keyPair.encrypt(testMessage.encodeToByteArray())
        val decrypted = keyPair.decrypt(encrypted).decodeToString()

        assertEquals(testMessage, decrypted)
    }
}