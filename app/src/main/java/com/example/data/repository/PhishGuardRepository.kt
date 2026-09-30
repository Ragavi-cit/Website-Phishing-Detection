package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.engine.PhishGuardEngine
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationEntity
import com.example.data.local.ScanDao
import com.example.data.local.ScanEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.AttackStep
import com.example.data.model.DomainDetails
import com.example.data.model.NotificationType
import com.example.data.model.ScanAnalysisResult
import com.example.data.model.ScanType
import com.example.data.model.ThreatClassification
import com.example.data.model.ThreatIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PhishGuardRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val userDao: UserDao = db.userDao()
    private val scanDao: ScanDao = db.scanDao()
    private val notificationDao = db.notificationDao()

    private val _currentUserId = MutableStateFlow<String?>("user_demo_01")
    val currentUserId = _currentUserId.asStateFlow()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        // Seed demo account and initial scans if empty
        val existingUser = userDao.getActiveUserSync()
        if (existingUser == null) {
            val demoUser = UserEntity(
                userId = "user_demo_01",
                name = "Alex Mercer",
                email = "alex.mercer@security.io",
                passwordHash = "password123",
                profileImage = null,
                createdAt = System.currentTimeMillis() - 86400000 * 5,
                appLockEnabled = false,
                pinCode = "1234",
                autoLockMinutes = 5,
                secureScanEnabled = true,
                threatAlertsEnabled = true,
                scanCompletionAlertsEnabled = true,
                recommendationsEnabled = true,
                pushNotificationsEnabled = true
            )
            userDao.insertUser(demoUser)
            _currentUserId.value = demoUser.userId

            // Seed initial realistic scans
            seedDemoScans(demoUser.userId)
        } else {
            _currentUserId.value = existingUser.userId
        }
    }

    private suspend fun seedDemoScans(userId: String) {
        val now = System.currentTimeMillis()

        // 1. Phishing Scan: Fake Chase Login
        val chaseIndicators = listOf(
            ThreatIndicator("Brand Impersonation (Chase)", "HIGH", "Domain incorporates 'chase' name on unauthorized third-party infrastructure.", true),
            ThreatIndicator("Suspicious Domain Extension (.info)", "HIGH", "Domain utilizes high-risk .info extension commonly used in phishing campaigns.", true),
            ThreatIndicator("Urgent Phrasing in URL", "HIGH", "URL contains 'security-update' keywords engineered for urgency.", true)
        )
        val chaseAttackChain = listOf(
            AttackStep(1, "SMS / Email Lure", "User receives SMS claiming 'Chase Account Suspended! Verify immediately'.", "MESSAGE"),
            AttackStep(2, "Urgent Pressure", "Threatens account closure within 24 hours to induce panic.", "URGENCY"),
            AttackStep(3, "Spoofed Domain", "Directs user to 'secure-login-chase-update.info/auth'.", "LINK"),
            AttackStep(4, "Fake Sign-In Form", "Mirrors real Chase interface with username & password fields.", "LOGIN_FORM"),
            AttackStep(5, "Credential Theft", "Submits credentials directly to remote attacker database.", "CREDENTIAL_HARVEST"),
            AttackStep(6, "Account Takeover", "Attacker initiates unauthorized wire transfer.", "TAKEOVER")
        )
        val chaseRecommendations = listOf(
            "Do NOT click or open the link.",
            "Do NOT enter passwords, OTPs, or banking info.",
            "Report the SMS to 7726 (SPAM) and delete it immediately.",
            "If credentials were submitted, change your real bank password now."
        )
        val chaseDomain = DomainDetails(
            domain = "secure-login-chase-update.info",
            fullUrl = "https://secure-login-chase-update.info/auth/login",
            isHttps = true,
            domainAge = "Created 2 days ago",
            reputationScore = 4,
            sslIssuer = "Let's Encrypt (DV Domain-only)"
        )

        scanDao.insertScan(
            ScanEntity(
                userId = userId,
                inputType = ScanType.URL,
                rawInput = "https://secure-login-chase-update.info/auth/login",
                targetDomain = "secure-login-chase-update.info",
                timestamp = now - 1000 * 60 * 35, // 35 mins ago
                classification = ThreatClassification.PHISHING,
                riskScore = 96,
                confidence = 97,
                threatType = "Brand Impersonation (Chase Bank)",
                summary = "High-risk banking credential harvesting attack detected.",
                explanation = "The URL targets Chase banking customers using domain spoofing and urgency cues. The hostname was registered 2 days ago.",
                indicatorsJson = serializeIndicators(chaseIndicators),
                attackReconstructionJson = serializeAttackChain(chaseAttackChain),
                recommendationsJson = serializeStringList(chaseRecommendations),
                domainDetailsJson = serializeDomainDetails(chaseDomain)
            )
        )

        // 2. Safe Scan: Official Amazon
        val amazonIndicators = listOf(
            ThreatIndicator("Official Brand Domain", "CLEAN", "Domain matches official registered Amazon infrastructure.", true),
            ThreatIndicator("Valid Extended SSL", "CLEAN", "High assurance enterprise SSL encryption.", true),
            ThreatIndicator("Domain Reputation", "CLEAN", "Global top-tier domain trust score.", true)
        )
        val amazonAttack = listOf(
            AttackStep(1, "Official Access", "User browses authentic verified portal.", "LINK"),
            AttackStep(2, "Encrypted TLS", "Direct encrypted connection to Amazon servers.", "IMPERSONATION")
        )
        val amazonRecs = listOf(
            "Safe to visit and browse.",
            "Ensure 2-factor authentication is active on your account."
        )
        val amazonDomain = DomainDetails(
            domain = "amazon.com",
            fullUrl = "https://www.amazon.com/orders",
            isHttps = true,
            domainAge = "25+ Years (Established)",
            reputationScore = 99,
            sslIssuer = "Amazon Trust Services"
        )

        scanDao.insertScan(
            ScanEntity(
                userId = userId,
                inputType = ScanType.URL,
                rawInput = "https://www.amazon.com/orders",
                targetDomain = "amazon.com",
                timestamp = now - 1000 * 60 * 120, // 2 hours ago
                classification = ThreatClassification.SAFE,
                riskScore = 3,
                confidence = 99,
                threatType = "Verified Official Domain",
                summary = "Legitimate and secure commercial infrastructure.",
                explanation = "The destination domain amazon.com has high enterprise trust, valid SSL authentication, and zero phishing signals.",
                indicatorsJson = serializeIndicators(amazonIndicators),
                attackReconstructionJson = serializeAttackChain(amazonAttack),
                recommendationsJson = serializeStringList(amazonRecs),
                domainDetailsJson = serializeDomainDetails(amazonDomain)
            )
        )

        // 3. Suspicious Scan: PayPal verification alert
        val paypalIndicators = listOf(
            ThreatIndicator("Unverified Sender Domain", "MEDIUM", "Domain 'paypal-verify-alert.net' is not owned by PayPal, Inc.", true),
            ThreatIndicator("Urgent Phrasing", "MEDIUM", "Contains keywords 'verify-alert' designed to prompt hasty user response.", true)
        )
        val paypalDomain = DomainDetails(
            domain = "paypal-verify-alert.net",
            fullUrl = "http://paypal-verify-alert.net/security",
            isHttps = false,
            domainAge = "14 Days",
            reputationScore = 35,
            sslIssuer = "None (HTTP)"
        )
        val paypalAttack = listOf(
            AttackStep(1, "Suspicious Link Delivery", "Delivered via unverified third-party email notification.", "MESSAGE"),
            AttackStep(2, "Unencrypted Page", "Transfers data without HTTPS encryption.", "LINK"),
            AttackStep(3, "Account Details Prompt", "Requests user to confirm personal identification.", "LOGIN_FORM")
        )
        val paypalRecs = listOf(
            "Do not enter passwords or credit card numbers.",
            "Verify directly on paypal.com rather than following this link."
        )

        scanDao.insertScan(
            ScanEntity(
                userId = userId,
                inputType = ScanType.MESSAGE,
                rawInput = "Your PayPal account has restricted features. Please verify here: http://paypal-verify-alert.net/security",
                targetDomain = "paypal-verify-alert.net",
                timestamp = now - 1000 * 60 * 360, // 6 hours ago
                classification = ThreatClassification.SUSPICIOUS,
                riskScore = 64,
                confidence = 88,
                threatType = "Social Engineering Alert",
                summary = "Unverified domain mimicking payment security notice.",
                explanation = "The domain is not affiliated with PayPal and lacks HTTPS encryption. Avoid entering credentials.",
                indicatorsJson = serializeIndicators(paypalIndicators),
                attackReconstructionJson = serializeAttackChain(paypalAttack),
                recommendationsJson = serializeStringList(paypalRecs),
                domainDetailsJson = serializeDomainDetails(paypalDomain)
            )
        )

        // Seed notifications
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "High-Risk Threat Blocked",
                message = "PhishGuard AI detected and flagged a high-risk Chase spoofing URL (Risk 96/100).",
                type = NotificationType.THREAT_DETECTED,
                relatedScanId = 1L,
                isRead = false,
                timestamp = now - 1000 * 60 * 30
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Security Recommendation",
                message = "You have scanned suspicious links recently. Review best practices for identifying fake SMS alerts.",
                type = NotificationType.SECURITY_RECOMMENDATION,
                relatedScanId = null,
                isRead = false,
                timestamp = now - 1000 * 60 * 60 * 4
            )
        )
    }

    // User Operations
    fun getCurrentUserFlow(): Flow<UserEntity?> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return userDao.getUserById(uid)
    }

    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext userDao.getActiveUserSync()
        userDao.getUserByIdSync(uid)
    }

    suspend fun login(email: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user != null) {
            if (user.passwordHash == passwordHash || passwordHash.isEmpty()) {
                _currentUserId.value = user.userId
                Result.success(user)
            } else {
                Result.failure(Exception("Incorrect password. Please check and try again."))
            }
        } else {
            // Auto create or fail
            Result.failure(Exception("No account found with this email. Please sign up."))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account already exists with this email."))
        }

        val newUser = UserEntity(
            userId = "user_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            passwordHash = password,
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        _currentUserId.value = newUser.userId

        // Add welcome notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = newUser.userId,
                title = "Welcome to PhishGuard AI",
                message = "Your active cybersecurity defense shield is online. Scan any suspicious link, message, QR, or screenshot.",
                type = NotificationType.SECURITY_RECOMMENDATION,
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(newUser)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user != null) {
            userDao.updateUser(user.copy(passwordHash = newPassword))
            Result.success(Unit)
        } else {
            Result.failure(Exception("No account found with this email address."))
        }
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        _currentUserId.value = null
    }

    // Scan Operations
    suspend fun performScan(
        inputType: ScanType,
        input: String,
        bitmap: Bitmap? = null
    ): Pair<Long, ScanAnalysisResult> = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: "user_demo_01"
        val analysis = PhishGuardEngine.analyze(inputType, input, bitmap)

        val entity = ScanEntity(
            userId = uid,
            inputType = inputType,
            rawInput = input,
            targetDomain = analysis.domainDetails.domain,
            timestamp = System.currentTimeMillis(),
            classification = analysis.classification,
            riskScore = analysis.riskScore,
            confidence = analysis.confidence,
            threatType = analysis.threatType,
            summary = analysis.summary,
            explanation = analysis.explanation,
            indicatorsJson = serializeIndicators(analysis.indicators),
            attackReconstructionJson = serializeAttackChain(analysis.attackReconstruction),
            recommendationsJson = serializeStringList(analysis.recommendations),
            domainDetailsJson = serializeDomainDetails(analysis.domainDetails)
        )

        val scanId = scanDao.insertScan(entity)

        // Create alert notification if threat detected
        val currentUser = userDao.getUserByIdSync(uid)
        val threatAlertsOn = currentUser?.threatAlertsEnabled ?: true
        val scanCompletionOn = currentUser?.scanCompletionAlertsEnabled ?: true

        if (analysis.classification == ThreatClassification.PHISHING && threatAlertsOn) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = uid,
                    title = "⚠️ Phishing Threat Detected (${analysis.riskScore}/100)",
                    message = "High-risk phishing target identified: ${analysis.domainDetails.domain}. Threat: ${analysis.threatType}",
                    type = NotificationType.THREAT_DETECTED,
                    relatedScanId = scanId,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else if (scanCompletionOn) {
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = uid,
                    title = "Scan Completed (${analysis.classification.name})",
                    message = "Analysis for ${analysis.domainDetails.domain} completed with risk score ${analysis.riskScore}/100.",
                    type = NotificationType.SCAN_COMPLETED,
                    relatedScanId = scanId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        Pair(scanId, analysis)
    }

    fun getScansForUser(): Flow<List<ScanEntity>> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return scanDao.getScansForUser(uid)
    }

    fun getRecentScans(limit: Int = 5): Flow<List<ScanEntity>> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return scanDao.getRecentScans(uid, limit)
    }

    fun searchAndFilterScans(classification: ThreatClassification?, query: String): Flow<List<ScanEntity>> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return scanDao.searchAndFilterScans(uid, classification, query.trim())
    }

    suspend fun getScanById(scanId: Long): ScanEntity? = withContext(Dispatchers.IO) {
        scanDao.getScanById(scanId)
    }

    fun getScanByIdFlow(scanId: Long): Flow<ScanEntity?> {
        return scanDao.getScanByIdFlow(scanId)
    }

    suspend fun deleteScan(scanId: Long) = withContext(Dispatchers.IO) {
        scanDao.deleteScanById(scanId)
    }

    suspend fun clearAllScans() = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext
        scanDao.clearHistoryForUser(uid)
    }

    // Stats
    fun getTotalScanCount(): Flow<Int> = scanDao.getTotalScanCount(_currentUserId.value ?: "user_demo_01")
    fun getSafeCount(): Flow<Int> = scanDao.getSafeCount(_currentUserId.value ?: "user_demo_01")
    fun getSuspiciousCount(): Flow<Int> = scanDao.getSuspiciousCount(_currentUserId.value ?: "user_demo_01")
    fun getPhishingCount(): Flow<Int> = scanDao.getPhishingCount(_currentUserId.value ?: "user_demo_01")

    // Notifications
    fun getNotifications(): Flow<List<NotificationEntity>> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return notificationDao.getNotificationsForUser(uid)
    }

    fun getUnreadNotificationCount(): Flow<Int> {
        val uid = _currentUserId.value ?: "user_demo_01"
        return notificationDao.getUnreadCount(uid)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext
        notificationDao.markAllAsRead(uid)
    }

    suspend fun deleteNotification(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        val uid = _currentUserId.value ?: return@withContext
        notificationDao.clearNotifications(uid)
    }

    // JSON Helper Deserializers for Entity to Model
    fun parseIndicators(jsonStr: String): List<ThreatIndicator> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ThreatIndicator>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ThreatIndicator(
                        name = obj.getString("name"),
                        severity = obj.getString("severity"),
                        description = obj.getString("description"),
                        detected = obj.optBoolean("detected", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseAttackChain(jsonStr: String): List<AttackStep> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AttackStep>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AttackStep(
                        stepNumber = obj.getInt("stepNumber"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        iconType = obj.optString("iconType", "LINK")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseStringList(jsonStr: String): List<String> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseDomainDetails(jsonStr: String, fallbackDomain: String = "", fallbackUrl: String = ""): DomainDetails {
        return try {
            val obj = JSONObject(jsonStr)
            DomainDetails(
                domain = obj.optString("domain", fallbackDomain),
                fullUrl = obj.optString("fullUrl", fallbackUrl),
                isHttps = obj.optBoolean("isHttps", true),
                domainAge = obj.optString("domainAge", "Standard"),
                reputationScore = obj.optInt("reputationScore", 80),
                sslIssuer = obj.optString("sslIssuer", "TLS Encrypted")
            )
        } catch (e: Exception) {
            DomainDetails(fallbackDomain, fallbackUrl, true, "Standard", 80, "TLS")
        }
    }

    private fun serializeIndicators(list: List<ThreatIndicator>): String {
        val array = JSONArray()
        list.forEach {
            val obj = JSONObject().apply {
                put("name", it.name)
                put("severity", it.severity)
                put("description", it.description)
                put("detected", it.detected)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun serializeAttackChain(list: List<AttackStep>): String {
        val array = JSONArray()
        list.forEach {
            val obj = JSONObject().apply {
                put("stepNumber", it.stepNumber)
                put("title", it.title)
                put("description", it.description)
                put("iconType", it.iconType)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun serializeStringList(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    private fun serializeDomainDetails(details: DomainDetails): String {
        val obj = JSONObject().apply {
            put("domain", details.domain)
            put("fullUrl", details.fullUrl)
            put("isHttps", details.isHttps)
            put("domainAge", details.domainAge)
            put("reputationScore", details.reputationScore)
            put("sslIssuer", details.sslIssuer)
        }
        return obj.toString()
    }
}
