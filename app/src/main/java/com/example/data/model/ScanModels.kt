package com.example.data.model

enum class ScanType {
    URL,
    QR_CODE,
    MESSAGE,
    SCREENSHOT
}

enum class ThreatClassification {
    SAFE,
    SUSPICIOUS,
    PHISHING
}

data class ThreatIndicator(
    val name: String,
    val severity: String, // HIGH, MEDIUM, LOW, CLEAN
    val description: String,
    val detected: Boolean = true
)

data class AttackStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val iconType: String // MESSAGE, URGENCY, LINK, IMPERSONATION, LOGIN_FORM, CREDENTIAL_HARVEST, TAKEOVER
)

data class DomainDetails(
    val domain: String,
    val fullUrl: String,
    val isHttps: Boolean,
    val domainAge: String,
    val reputationScore: Int,
    val sslIssuer: String,
    val redirectChain: List<String> = emptyList()
)

data class ScanAnalysisResult(
    val classification: ThreatClassification,
    val riskScore: Int, // 0 - 100
    val confidence: Int, // 0 - 100
    val threatType: String,
    val summary: String,
    val explanation: String,
    val indicators: List<ThreatIndicator>,
    val attackReconstruction: List<AttackStep>,
    val recommendations: List<String>,
    val domainDetails: DomainDetails
)

enum class NotificationType {
    THREAT_DETECTED,
    SCAN_COMPLETED,
    SECURITY_RECOMMENDATION
}

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AutoLockOption(val minutes: Int, val label: String) {
    IMMEDIATELY(0, "Immediately"),
    ONE_MIN(1, "1 Minute"),
    FIVE_MINS(5, "5 Minutes"),
    FIFTEEN_MINS(15, "15 Minutes")
}
