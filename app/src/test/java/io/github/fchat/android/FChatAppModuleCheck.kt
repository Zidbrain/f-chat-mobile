package io.github.fchat.android

import android.content.Context
import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import io.github.zidbrain.fchat.di.allModules
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.check.checkKoinModules
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mockito

class FChatAppModuleCheck : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun koinConfigurationIsCorrect() {
        checkKoinModules(allModules) {
            declareMock<EncryptedStorage>()
            declareMock<CryptographyService>()
            declareMock<Context>()
        }
    }
}