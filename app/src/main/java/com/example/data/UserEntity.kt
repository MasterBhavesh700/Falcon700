package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val passwordHash: String,
    val name: String,
    val rank: String,
    val initialCapital: Double = 500000.0,
    val monthlySip: Double = 50000.0,
    val cloudEndpoint: String = "https://aistudio-backup.cloud/api/v1",
    val cloudEnabled: Boolean = false,
    val lastSyncTime: Long = 0L
)
