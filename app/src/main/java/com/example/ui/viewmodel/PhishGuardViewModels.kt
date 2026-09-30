package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NotificationEntity
import com.example.data.local.ScanEntity
import com.example.data.local.UserEntity
import com.example.data.model.AttackStep
import com.example.data.model.DomainDetails
import com.example.data.model.ScanAnalysisResult
import com.example.data.model.ScanType
import com.example.data.model.ThemePreference
import com.example.data.model.ThreatClassification
import com.example.data.model.ThreatIndicator
import com.example.data.repository.PhishGuardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success(it) },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields.")
            return
        }
        if (password != confirmPass) {
            _authState.value = AuthState.Error("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(name, email, password)
            result.fold(
                onSuccess = { _authState.value = AuthState.Success(it) },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Sign up failed") }
            )
        }
    }

    fun forgotPassword(email: String, newPassword: String) {
        if (email.isBlank() || newPassword.isBlank()) {
            _authState.value = AuthState.Error("Please provide both email and a new password.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.resetPassword(email, newPassword)
            result.fold(
                onSuccess = { _authState.value = AuthState.ResetSuccess },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Reset failed") }
            )
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    object ResetSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    val currentUser = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentScans = repository.getRecentScans(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalScans = repository.getTotalScanCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val safeCount = repository.getSafeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val suspiciousCount = repository.getSuspiciousCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val phishingCount = repository.getPhishingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadAlerts = repository.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = _searchQuery.flatMapLatest { query ->
        repository.searchAndFilterScans(null, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    private val _scanState = MutableStateFlow<ScanProcessState>(ScanProcessState.Idle)
    val scanState = _scanState.asStateFlow()

    private val _activeAnalysis = MutableStateFlow<ScanAnalysisResult?>(null)
    val activeAnalysis = _activeAnalysis.asStateFlow()

    private val _lastScanId = MutableStateFlow<Long?>(null)
    val lastScanId = _lastScanId.asStateFlow()

    val analysisPipelineSteps = listOf(
        "Extracting target structure...",
        "Checking domain DNS & SSL certificate...",
        "Analyzing reputation & security telemetry...",
        "Scanning for brand impersonation & credential harvesting...",
        "Running Gemini AI deep threat model...",
        "Reconstructing attack trajectory & recommendations..."
    )

    fun startScan(
        inputType: ScanType,
        input: String,
        bitmap: Bitmap? = null
    ) {
        if (input.isBlank() && bitmap == null) {
            _scanState.value = ScanProcessState.Error("Please provide a URL, QR code, message, or screenshot to scan.")
            return
        }

        viewModelScope.launch {
            _scanState.value = ScanProcessState.Processing(stepIndex = 0, message = analysisPipelineSteps[0])

            // Animate realistic pipeline steps
            for (i in 0 until analysisPipelineSteps.size) {
                _scanState.value = ScanProcessState.Processing(
                    stepIndex = i,
                    message = analysisPipelineSteps[i]
                )
                delay(300)
            }

            try {
                val (scanId, result) = repository.performScan(inputType, input, bitmap)
                _activeAnalysis.value = result
                _lastScanId.value = scanId
                _scanState.value = ScanProcessState.Completed(scanId, result)
            } catch (e: Exception) {
                _scanState.value = ScanProcessState.Error(e.message ?: "Scan failed. Please try again.")
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ScanProcessState.Idle
    }
}

sealed class ScanProcessState {
    object Idle : ScanProcessState()
    data class Processing(val stepIndex: Int, val message: String) : ScanProcessState()
    data class Completed(val scanId: Long, val result: ScanAnalysisResult) : ScanProcessState()
    data class Error(val message: String) : ScanProcessState()
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    private val _filter = MutableStateFlow<ThreatClassification?>(null)
    val filter = _filter.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val scans: StateFlow<List<ScanEntity>> = combine(_filter, _query) { classification, q ->
        Pair(classification, q)
    }.flatMapLatest { (classification, q) ->
        repository.searchAndFilterScans(classification, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(classification: ThreatClassification?) {
        _filter.value = classification
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllScans()
        }
    }
}

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PhishGuardRepository(application)

    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateToggle(
        threatAlerts: Boolean? = null,
        scanCompletion: Boolean? = null,
        recommendations: Boolean? = null,
        push: Boolean? = null,
        appLock: Boolean? = null,
        autoLockMins: Int? = null,
        secureScan: Boolean? = null,
        theme: ThemePreference? = null,
        name: String? = null,
        email: String? = null
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(
                threatAlertsEnabled = threatAlerts ?: user.threatAlertsEnabled,
                scanCompletionAlertsEnabled = scanCompletion ?: user.scanCompletionAlertsEnabled,
                recommendationsEnabled = recommendations ?: user.recommendationsEnabled,
                pushNotificationsEnabled = push ?: user.pushNotificationsEnabled,
                appLockEnabled = appLock ?: user.appLockEnabled,
                autoLockMinutes = autoLockMins ?: user.autoLockMinutes,
                secureScanEnabled = secureScan ?: user.secureScanEnabled,
                themePreference = theme ?: user.themePreference,
                name = name ?: user.name,
                email = email ?: user.email
            )
            repository.updateUser(updated)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllScans()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
