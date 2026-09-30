package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NotificationType
import com.example.data.model.ScanType
import com.example.data.model.ThemePreference
import com.example.data.model.ThreatClassification

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val appLockEnabled: Boolean = false,
    val pinCode: String = "",
    val autoLockMinutes: Int = 5,
    val secureScanEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val threatAlertsEnabled: Boolean = true,
    val scanCompletionAlertsEnabled: Boolean = true,
    val recommendationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true
)

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val inputType: ScanType,
    val rawInput: String,
    val targetDomain: String,
    val timestamp: Long = System.currentTimeMillis(),
    val classification: ThreatClassification,
    val riskScore: Int,
    val confidence: Int,
    val threatType: String,
    val summary: String,
    val explanation: String,
    val indicatorsJson: String,
    val attackReconstructionJson: String,
    val recommendationsJson: String,
    val domainDetailsJson: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedScanId: Long? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
