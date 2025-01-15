package io.github.zidbrain.fchat.common.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("io.github.zidbrain.fchat.common")
class CommonModule

sealed interface CommonQualifiers {
    data object Authorized : CommonQualifiers
    data object Unauthorized : CommonQualifiers
    data object HostUrl : CommonQualifiers
}