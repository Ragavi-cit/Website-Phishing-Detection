package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScanEntity
import com.example.data.model.AttackStep
import com.example.data.model.DomainDetails
import com.example.data.model.ThreatClassification
import com.example.data.model.ThreatIndicator
import com.example.data.repository.PhishGuardRepository
import com.example.ui.components.AttackReconstructionView
import com.example.ui.components.IndicatorItem
import com.example.ui.components.RiskScoreGauge
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.ThreatRed
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanResultScreen(
    scanId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onScanAnother: () -> Unit,
    repository: PhishGuardRepository
) {
    val context = LocalContext.current
    var scanEntity by remember { mutableStateOf<ScanEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(scanId) {
        val entity = repository.getScanById(scanId)
        scanEntity = entity
        isLoading = false
    }

    if (isLoading || scanEntity == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonCyan)
        }
        return
    }

    val scan = scanEntity!!
    val indicators = repository.parseIndicators(scan.indicatorsJson)
    val attackSteps = repository.parseAttackChain(scan.attackReconstructionJson)
    val recommendations = repository.parseStringList(scan.recommendationsJson)
    val domainDetails = repository.parseDomainDetails(scan.domainDetailsJson, scan.targetDomain, scan.rawInput)

    val (headerColor, classificationText) = when (scan.classification) {
        ThreatClassification.PHISHING -> Pair(ThreatRed, "PHISHING THREAT DETECTED")
        ThreatClassification.SUSPICIOUS -> Pair(WarningAmber, "SUSPICIOUS CONTENT")
        ThreatClassification.SAFE -> Pair(SafeGreen, "VERIFIED SAFE CONTENT")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Scan Analysis Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "PhishGuard AI Security Report:\nTarget: ${scan.targetDomain}\nStatus: ${scan.classification.name} (Risk ${scan.riskScore}/100)\nThreat: ${scan.threatType}\nSummary: ${scan.summary}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Threat Intelligence"))
                    },
                    modifier = Modifier.testTag("share_scan_result_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Center Risk Score Gauge
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RiskScoreGauge(
                    score = scan.riskScore,
                    classification = scan.classification,
                    size = 170.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(headerColor.copy(alpha = 0.15f))
                        .border(1.dp, headerColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = classificationText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = scan.threatType,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = scan.targetDomain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // AI Threat Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(headerColor.copy(alpha = 0.25f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "AI Analysis",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Threat Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = scan.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Attack Reconstruction Section (PRD Requirement)
        if (attackSteps.isNotEmpty()) {
            item {
                AttackReconstructionView(attackSteps = attackSteps)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Detected Indicators
        if (indicators.isNotEmpty()) {
            item {
                Text(
                    text = "Key Security Indicators (${indicators.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(indicators) { indicator ->
                IndicatorItem(
                    indicator = indicator,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Recommended Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (scan.classification == ThreatClassification.PHISHING) Color(0xFF2A1215) else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(headerColor.copy(alpha = 0.3f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = headerColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommended Next Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    recommendations.forEachIndexed { idx, rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(headerColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = headerColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Action Buttons
        item {
            Button(
                onClick = { onNavigateToDetails(scan.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("view_detailed_threat_report_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF00363D)
                )
            ) {
                Text("View Detailed Technical Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onScanAnother,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("scan_another_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Another Item", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ThreatDetailsScreen(
    scanId: Long,
    onNavigateBack: () -> Unit,
    repository: PhishGuardRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var scanEntity by remember { mutableStateOf<ScanEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(scanId) {
        scanEntity = repository.getScanById(scanId)
        isLoading = false
    }

    if (isLoading || scanEntity == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonCyan)
        }
        return
    }

    val scan = scanEntity!!
    val indicators = repository.parseIndicators(scan.indicatorsJson)
    val attackSteps = repository.parseAttackChain(scan.attackReconstructionJson)
    val domainDetails = repository.parseDomainDetails(scan.domainDetailsJson, scan.targetDomain, scan.rawInput)

    val (badgeColor, classificationText) = when (scan.classification) {
        ThreatClassification.PHISHING -> Pair(ThreatRed, "High-Risk Threat")
        ThreatClassification.SUSPICIOUS -> Pair(WarningAmber, "Suspicious")
        ThreatClassification.SAFE -> Pair(SafeGreen, "Safe & Verified")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Text(
                            text = "Threat Forensics Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "PhishGuard Forensics:\nDomain: ${domainDetails.domain}\nFull URL: ${domainDetails.fullUrl}\nRisk: ${scan.riskScore}/100\nSSL: ${domainDetails.sslIssuer}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Forensics"))
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NeonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Overview Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(badgeColor.copy(alpha = 0.3f))
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = classificationText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                }

                                Text(
                                    text = "Confidence: ${scan.confidence}%",
                                    fontSize = 12.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = scan.threatType,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = scan.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Domain & SSL Technical Details
                item {
                    Text(
                        text = "Technical Infrastructure & DNS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            TechnicalDetailRow("Target Domain", domainDetails.domain, Icons.Default.Public)
                            TechnicalDetailRow("Full URL", domainDetails.fullUrl, Icons.Default.Info)
                            TechnicalDetailRow("HTTPS / Encryption", if (domainDetails.isHttps) "Enabled (HTTPS)" else "Unencrypted (HTTP)", if (domainDetails.isHttps) Icons.Default.Lock else Icons.Default.LockOpen)
                            TechnicalDetailRow("Domain Age", domainDetails.domainAge, Icons.Default.Public)
                            TechnicalDetailRow("Reputation Score", "${domainDetails.reputationScore} / 100", Icons.Default.Security)
                            TechnicalDetailRow("Certificate Issuer", domainDetails.sslIssuer, Icons.Default.Shield)
                            TechnicalDetailRow("Scan Timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(scan.timestamp)), Icons.Default.Info)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Attack Reconstruction Flow
                if (attackSteps.isNotEmpty()) {
                    item {
                        AttackReconstructionView(attackSteps = attackSteps)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Indicators List
                if (indicators.isNotEmpty()) {
                    item {
                        Text(
                            text = "Detected Indicators of Compromise",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(indicators) { ind ->
                        IndicatorItem(indicator = ind, modifier = Modifier.padding(bottom = 10.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Action: Report Threat / Block
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Threat reported to global anti-phishing telemetry database.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("report_threat_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThreatRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Dangerous, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Phishing Domain", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun TechnicalDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
