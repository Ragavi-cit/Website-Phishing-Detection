package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ThreatClassification
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getActiveUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getScansForUser(userId: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScans(userId: String, limit: Int = 5): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    suspend fun getScanById(scanId: Long): ScanEntity?

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    fun getScanByIdFlow(scanId: Long): Flow<ScanEntity?>

    @Query("""
        SELECT * FROM scans 
        WHERE userId = :userId 
        AND (:classification IS NULL OR classification = :classification)
        AND (:query = '' OR targetDomain LIKE '%' || :query || '%' OR rawInput LIKE '%' || :query || '%' OR threatType LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    fun searchAndFilterScans(
        userId: String,
        classification: ThreatClassification?,
        query: String
    ): Flow<List<ScanEntity>>

    @Query("SELECT COUNT(*) FROM scans WHERE userId = :userId")
    fun getTotalScanCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM scans WHERE userId = :userId AND classification = 'SAFE'")
    fun getSafeCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM scans WHERE userId = :userId AND classification = 'SUSPICIOUS'")
    fun getSuspiciousCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM scans WHERE userId = :userId AND classification = 'PHISHING'")
    fun getPhishingCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity): Long

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteScanById(id: Long)

    @Query("DELETE FROM scans WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearNotifications(userId: String)
}
