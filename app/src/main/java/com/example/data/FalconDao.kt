package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FalconDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_logs WHERE userEmail = :userEmail ORDER BY date DESC")
    fun getAllDailyLogs(userEmail: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE userEmail = :userEmail AND date = :date LIMIT 1")
    suspend fun getDailyLogByDate(userEmail: String, date: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLogEntity)

    // --- Flashcards ---
    @Query("SELECT * FROM flashcards WHERE userEmail = :userEmail ORDER BY id ASC")
    fun getAllFlashcards(userEmail: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userEmail = :userEmail AND subject = :subject ORDER BY id ASC")
    fun getFlashcardsBySubject(userEmail: String, subject: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Query("UPDATE flashcards SET masteryState = :state, lastReviewed = :time WHERE id = :id")
    suspend fun updateFlashcardMastery(id: Int, state: String, time: Long)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: Int)

    // --- Habits ---
    @Query("SELECT * FROM habits WHERE userEmail = :userEmail")
    fun getAllHabits(userEmail: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)
}
