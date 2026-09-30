package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    
    // Bottom Nav Tabs
    object Dashboard : Screen("dashboard")
    object Scan : Screen("scan?type={type}") {
        fun createRoute(type: com.example.data.model.ScanType = com.example.data.model.ScanType.URL) = "scan?type=${type.name}"
    }
    object History : Screen("history")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")

    // Sub-screens
    object ScanProcessing : Screen("scan_processing")
    object ScanResult : Screen("scan_result/{scanId}") {
        fun createRoute(scanId: Long) = "scan_result/$scanId"
    }
    object ThreatDetails : Screen("threat_details/{scanId}") {
        fun createRoute(scanId: Long) = "threat_details/$scanId"
    }
    object Faq : Screen("faq")
    object Support : Screen("support")
    object About : Screen("about")
}
