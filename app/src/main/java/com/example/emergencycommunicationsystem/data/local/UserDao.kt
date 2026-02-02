package com.example.emergencycommunicationsystem.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY username ASC")
    fun getOnlineUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :userId")
    suspend fun updateUserOnlineStatus(userId: Int, isOnline: Boolean)
    
    @Query("UPDATE users SET lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateUserLastSeen(userId: Int, lastSeen: Long)
    
    @Query("UPDATE users SET isOnline = 0 WHERE isOnline = 1")
    suspend fun setAllUsersOffline()
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)
    
    @Query("SELECT COUNT(*) FROM users WHERE isOnline = 1")
    suspend fun getOnlineUsersCount(): Int
}
