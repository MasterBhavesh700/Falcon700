package com.example.data

import kotlinx.coroutines.flow.Flow

class FalconRepository(private val dao: FalconDao) {
    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }

    fun getAllDailyLogs(userEmail: String): Flow<List<DailyLogEntity>> = dao.getAllDailyLogs(userEmail)
    
    fun getAllFlashcards(userEmail: String): Flow<List<FlashcardEntity>> = dao.getAllFlashcards(userEmail)

    fun getFlashcardsBySubject(userEmail: String, subject: String): Flow<List<FlashcardEntity>> {
        return dao.getFlashcardsBySubject(userEmail, subject)
    }

    suspend fun getDailyLogByDate(userEmail: String, date: String): DailyLogEntity? {
        return dao.getDailyLogByDate(userEmail, date)
    }

    suspend fun insertDailyLog(log: DailyLogEntity) {
        dao.insertDailyLog(log)
    }

    suspend fun insertFlashcard(card: FlashcardEntity) {
        dao.insertFlashcard(card)
    }

    suspend fun insertFlashcards(cards: List<FlashcardEntity>) {
        dao.insertFlashcards(cards)
    }

    suspend fun updateFlashcardMastery(id: Int, state: String, time: Long) {
        dao.updateFlashcardMastery(id, state, time)
    }

    suspend fun deleteFlashcardById(id: Int) {
        dao.deleteFlashcardById(id)
    }

    // --- Habits ---
    fun getAllHabits(userEmail: String): Flow<List<HabitEntity>> = dao.getAllHabits(userEmail)

    suspend fun getHabitById(id: String): HabitEntity? = dao.getHabitById(id)

    suspend fun insertHabit(habit: HabitEntity) = dao.insertHabit(habit)

    suspend fun insertHabits(habits: List<HabitEntity>) = dao.insertHabits(habits)

    suspend fun deleteHabitById(id: String) = dao.deleteHabitById(id)
}
