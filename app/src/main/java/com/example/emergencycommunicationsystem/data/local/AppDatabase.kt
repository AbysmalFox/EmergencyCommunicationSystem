package com.example.emergencycommunicationsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlertEntity::class, UserEntity::class, CallLogEntity::class, CallMessageEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun userDao(): UserDao
    abstract fun callLogDao(): CallLogDao
    abstract fun callMessageDao(): CallMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emergency_app_database"
                )
                .fallbackToDestructiveMigration() // Recreate database if schema changes (for development)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
