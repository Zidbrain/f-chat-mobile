package io.github.zidbrain.fchat.common.account.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.security.auth.x500.X500Principal
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AndroidEncryptionService : EncryptionService {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun ByteArray.encodeBase64(): String =
        Base64.Default.encode(this)

    private val keys = mutableMapOf<String, String>()

    override fun devicePublicKey(email: String): String {
        val entryAlias = DEVICE_KEYS + "_$email"
        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore").also {
                val paramSpec = KeyGenParameterSpec.Builder(
                    entryAlias,
                    KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
                ).run {
                    setKeySize(4096)
                    setCertificateSubject(X500Principal("CN=F-Chat"))
                    setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    setDigests(KeyProperties.DIGEST_SHA256)
                    build()
                }
                it.initialize(paramSpec)
            }

        val publicKey = if (!keyStore.containsAlias(entryAlias))
            generator.genKeyPair().public.encoded.encodeBase64()
        else {
            val privateKeyEntry = keyStore.getEntry(entryAlias, null) as KeyStore.PrivateKeyEntry
            privateKeyEntry.certificate.publicKey.encoded.encodeBase64()
        }
        keys[email] = publicKey

        return publicKey
    }

    private companion object {
        const val DEVICE_KEYS = "device_keys"
    }
}