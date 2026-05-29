package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyLogEntity::class, FlashcardEntity::class, UserEntity::class, HabitEntity::class], version = 3, exportSchema = false)
abstract class FalconDatabase : RoomDatabase() {
    abstract val dao: FalconDao

    companion object {
        @Volatile
        private var INSTANCE: FalconDatabase? = null

        fun getInstance(context: Context): FalconDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FalconDatabase::class.java,
                    "falcon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
