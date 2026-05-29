package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String, // unique id, e.g., "h1", "h2", ... or random uuid
    val name: String,
    val cat: String, // category: DISCIPLINE, PHYSICAL, COGNITIVE, HEALTH, MENTAL, NUTRITION
    val time: String, // scheduled time, e.g., "05:00"
    val icon: String, // Emoji representation
    val xp: Int, // XP award on completion
    val userEmail: String, // partition per user account
    val doneDays: String = "" // Comma-separated date strings "YYYY-MM-DD" describing completed days
)
