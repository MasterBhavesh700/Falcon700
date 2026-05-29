package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String = "", // Link to owner email
    val subject: String,      // "FMT", "PSM", "FINANCE"
    val question: String,
    val answer: String,
    val difficulty: String = "MEDIUM",
    val masteryState: String = "UNSEEN", // "UNSEEN", "FLINCHED" (failed), "HOLD" (retaining), "MASTERED" (passed)
    val lastReviewed: Long = 0L,         // Epoch millis
    val explanation: String = ""         // Extra high-yield context or source citation (e.g., K Park 26th Ed / Reddy 34th Ed)
)
