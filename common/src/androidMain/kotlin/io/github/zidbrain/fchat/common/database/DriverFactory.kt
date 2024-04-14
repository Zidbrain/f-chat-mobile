package io.github.zidbrain.fchat.common.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.zidbrain.Database

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(Database.Schema, context, "fchat.db")
}