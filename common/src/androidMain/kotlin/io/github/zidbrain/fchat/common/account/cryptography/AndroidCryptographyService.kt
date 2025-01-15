package io.github.zidbrain.fchat.common.account.cryptography

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.koin.core.annotation.Single
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal

@Single
class AndroidCryptographyService : CryptographyService {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val keys = mutableMapOf<String, RSAKeyPair>()

    override fun deviceKeyPair(email: String): RSAKeyPair {
        keys[email]?.let { return it }

        val entryAlias = DEVICE_KEYS + "_$email"
        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore").also {
                val paramSpec = KeyGenParameterSpec.Builder(
                    entryAlias,
                    KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
                ).run {
                    setKeySize(4096)
                    setCertificateSubject(X500Principal("CN=F-Chat"))
                    setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    setDigests(KeyProperties.DIGEST_SHA256)
                    build()
                }
                it.initialize(paramSpec)
            }

        val keys = if (!keyStore.containsAlias(entryAlias))
            generator.genKeyPair().toModel()
        else {
            val privateKeyEntry = keyStore.getEntry(entryAlias, null) as KeyStore.PrivateKeyEntry
            KeyPair(privateKeyEntry.certificate.publicKey, privateKeyEntry.privateKey).toModel()
        }
        this.keys[email] = keys

        return keys
    }

    override fun generateSymmetricKey(): AESKey {
        val generator = KeyGenerator.getInstance("AES")
        return generator.generateKey().toModel()
    }

    private companion object {
        const val DEVICE_KEYS = "device_keys"
    }
}