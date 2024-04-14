package io.github.zidbrain.fchat.common.account.storage

interface EncryptedStorage {
    var refreshToken: String?
    var email: String?
}