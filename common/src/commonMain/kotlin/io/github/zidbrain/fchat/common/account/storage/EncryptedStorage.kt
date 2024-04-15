package io.github.zidbrain.fchat.common.account.storage

import io.github.zidbrain.fchat.common.host.repository.UserSessionInfo

interface EncryptedStorage {
    var userSession: UserSessionInfo?
}