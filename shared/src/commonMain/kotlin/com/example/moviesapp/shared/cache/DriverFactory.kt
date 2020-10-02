package com.example.moviesapp.shared.cache

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun create(): SqlDriver
}