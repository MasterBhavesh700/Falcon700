package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    val id: String, // "$userEmail|$date"
    val date: String, // "YYYY-MM-DD"
    val userEmail: String, // Owner email
    val bodyChecked: Boolean = false,   // "BODY: trained + ate real food"
    val mindChecked: Boolean = false,   // "MIND: one real study block BEFORE any building"
    val honorChecked: Boolean = false,  // "HONOR: chose harder right + diary"
    val holdChecked: Boolean = false,   // "HOLD: no avoidance disguised as productivity"
    val diaryLine: String = "",
    val studyHoursFmt: Float = 0f,
    val studyHoursPsm: Float = 0f,
    val portfolioValue: Double = 0.0 // TrueYield index tracking portfolio size (e.g. up to 1 Crore)
)
