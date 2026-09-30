package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.NotificationType
import com.example.data.model.ScanType
import com.example.data.model.ThemePreference
import com.example.data.model.ThreatClassification

class Converters {
    @TypeConverter
    fun fromScanType(value: ScanType): String = value.name

    @TypeConverter
    fun toScanType(value: String): ScanType = try {
        ScanType.valueOf(value)
    } catch (e: Exception) {
        ScanType.URL
    }

    @TypeConverter
    fun fromThreatClassification(value: ThreatClassification): String = value.name

    @TypeConverter
    fun toThreatClassification(value: String): ThreatClassification = try {
        ThreatClassification.valueOf(value)
    } catch (e: Exception) {
        ThreatClassification.SAFE
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (e: Exception) {
        NotificationType.SCAN_COMPLETED
    }

    @TypeConverter
    fun fromThemePreference(value: ThemePreference): String = value.name

    @TypeConverter
    fun toThemePreference(value: String): ThemePreference = try {
        ThemePreference.valueOf(value)
    } catch (e: Exception) {
        ThemePreference.SYSTEM
    }
}
