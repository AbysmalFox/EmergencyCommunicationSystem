package com.example.emergencycommunicationsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AlertEntity::class, WeatherEntity::class, UserEntity::class, CallLogEntity::class, CallMessageEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun weatherDao(): WeatherDao
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
                .fallbackToDestructiveMigration() // Recreate database if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
