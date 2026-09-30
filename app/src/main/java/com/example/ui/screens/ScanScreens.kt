package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanType
import com.example.ui.components.CameraQrScannerView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.ThreatRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ScanProcessState
import com.example.ui.viewmodel.ScanViewModel
import com.example.util.QrCodeDecoder
import java.io.InputStream

@Composable
fun ScanScreen(
    initialScanType: ScanType = ScanType.URL,
    onNavigateBack: () -> Unit,
    onScanCompleted: (Long) -> Unit,
    viewModel: ScanViewModel
) {
    var selectedTab by remember {
        mutableIntStateOf(
            when (initialScanType) {
                ScanType.URL -> 0
                ScanType.QR_CODE -> 1
                ScanType.MESSAGE -> 2
                ScanType.SCREENSHOT -> 3
            }
        )
    }

    val scanState by viewModel.scanState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // State for URL Tab
    var urlInput by remember { mutableStateOf("https://secure-chase-update.info/auth/login") }

    // State for QR Tab
    var qrPayload by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrDecodeStatus by remember { mutableStateOf<String?>(null) }
    var qrDecodeSuccess by remember { mutableStateOf<Boolean?>(null) }

    // State for Message Tab
    var messageInput by remember {
        mutableStateOf("URGENT: Your Chase checking account has been suspended due to suspicious activity. Verify immediately within 24 hours to prevent permanent closure: https://secure-login-chase-update.info/auth/login")
    }

    // State for Screenshot Tab
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var screenshotDesc by remember { mutableStateOf("Suspected fake login screen received via WhatsApp") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    if (selectedTab == 1) {
                        // QR Code Tab: Extract QR pattern with ZXing decoder
                        qrBitmap = bitmap
                        val decoded = QrCodeDecoder.decodeQrCode(bitmap)
                        if (decoded != null && decoded.isNotBlank()) {
                            qrPayload = decoded
                            qrDecodeStatus = "QR Code Detected: $decoded"
                            qrDecodeSuccess = true
                        } else {
                            qrDecodeStatus = "Image loaded. If no automatic link was found, verify the payload below."
                            qrDecodeSuccess = null
                        }
                    } else {
                        // Screenshot tab
                        selectedBitmap = bitmap
                    }
                }
            } catch (e: Exception) {
                if (selectedTab == 1) {
                    qrDecodeStatus = "Failed to parse image file: ${e.message}"
                    qrDecodeSuccess = false
                }
            }
        }
    }

    LaunchedEffect(scanState) {
        if (scanState is ScanProcessState.Completed) {
            val id = (scanState as ScanProcessState.Completed).scanId
            onScanCompleted(id)
        }
    }

    if (scanState is ScanProcessState.Processing) {
        val proc = scanState as ScanProcessState.Processing
        ScanProcessingView(stepIndex = proc.stepIndex, message = proc.message)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Cyber Threat Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Multi-Modal Cybersecurity Analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan
                )
            }
        }

        // 4 Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("URL", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Link, contentDescription = "URL") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("QR Code", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.QrCode, contentDescription = "QR") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Message", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Message, contentDescription = "Message") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Screenshot", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Image, contentDescription = "Screenshot") }
            )
        }

        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedTab) {
                0 -> {
                    // ================= URL TAB =================
                    Text(
                        text = "Paste or enter a URL to scan for phishing, spoofing, and malicious redirections.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Destination URL / Link") },
                        placeholder = { Text("e.g. https://login-bank-verify.com") },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.getText()?.let {
                                    urlInput = it.text
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = NeonCyan)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_url_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.startScan(ScanType.URL, urlInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("scan_url_submit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color(0xFF00363D)
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze URL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Preset Test Targets
                    PresetSection(
                        title = "Quick Cyber Testing Presets",
                        presets = listOf(
                            PresetItem("🚨 Chase Bank Phishing", "https://secure-login-chase-update.info/auth/login", ThreatRed),
                            PresetItem("🚨 Crypto Wallet Drainer", "https://connect-metamask-sync-wallet.xyz/claim", ThreatRed),
                            PresetItem("⚠️ Suspicious PayPal", "http://paypal-verification-alert.net/security", WarningAmber),
                            PresetItem("✅ Verified Google Official", "https://google.com/security", SafeGreen),
                            PresetItem("✅ Verified Microsoft Official", "https://login.microsoftonline.com", SafeGreen)
                        ),
                        onSelect = { urlInput = it }
                    )
                }

                1 -> {
                    // ================= QR CODE TAB =================
                    Text(
                        text = "Scan with your camera or upload a QR code image to inspect hidden destinations and quishing attacks before opening them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Camera & Photo Viewfinder
                    CameraQrScannerView(
                        uploadedBitmap = qrBitmap,
                        onQrCodeDetected = { decodedUrl, bitmap ->
                            if (decodedUrl.isNotBlank()) {
                                qrPayload = decodedUrl
                                qrDecodeStatus = "✓ QR Code Extracted: $decodedUrl"
                                qrDecodeSuccess = true
                            } else {
                                qrDecodeStatus = "Photo captured. Please verify or paste the URL below."
                                qrDecodeSuccess = null
                            }
                            if (bitmap != null) {
                                qrBitmap = bitmap
                            }
                        },
                        onClearUploadedImage = {
                            qrBitmap = null
                            qrDecodeStatus = null
                            qrDecodeSuccess = null
                        },
                        onPickImageFromGallery = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Status card if available
                    if (qrDecodeStatus != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (qrDecodeSuccess == true) SafeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (qrDecodeSuccess == true) SafeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (qrDecodeSuccess == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (qrDecodeSuccess == true) SafeGreen else WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = qrDecodeStatus ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    OutlinedTextField(
                        value = qrPayload,
                        onValueChange = { 
                            qrPayload = it
                            if (it.isBlank()) {
                                qrDecodeStatus = null
                            }
                        },
                        label = { Text("Detected QR Payload / Embedded URL") },
                        placeholder = { Text("Point camera at QR or enter link to scan") },
                        trailingIcon = {
                            if (qrPayload.isNotEmpty()) {
                                IconButton(onClick = { 
                                    qrPayload = "" 
                                    qrDecodeStatus = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_qr_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("upload_qr_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload QR", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (qrPayload.isBlank()) {
                                    qrDecodeStatus = "Please point your camera at a QR code, upload an image, or choose a test preset below."
                                    qrDecodeSuccess = false
                                } else {
                                    viewModel.startScan(ScanType.QR_CODE, qrPayload.trim(), qrBitmap)
                                }
                            },
                            modifier = Modifier
                                .weight(1.35f)
                                .height(52.dp)
                                .testTag("scan_qr_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan QR Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PresetSection(
                        title = "Sample QR Code Attack Vectors",
                        presets = listOf(
                            PresetItem("🚨 Quishing: Fake Parking Ticket Fine", "https://city-parking-pay-fine.xyz/ticket?id=928", ThreatRed),
                            PresetItem("🚨 Quishing: Spoofed Bank Login QR", "https://chase-mobile-quickpay.top/login", ThreatRed),
                            PresetItem("🚨 Quishing: Fake Crypto Airdrop", "https://claim-airdrop-crypto-rewards.click/drain", ThreatRed),
                            PresetItem("✅ Safe Restaurant Digital Menu", "https://menu.toasttab.com/order/cafe", SafeGreen)
                        ),
                        onSelect = { 
                            qrPayload = it
                            qrDecodeStatus = "Selected test payload: $it"
                            qrDecodeSuccess = true
                        }
                    )
                }

                2 -> {
                    // ================= MESSAGE TAB =================
                    Text(
                        text = "Paste suspicious SMS, WhatsApp, or email texts to evaluate urgency tricks, fake sender origins, and concealed links.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text("Message Body / SMS Content") },
                        minLines = 4,
                        maxLines = 8,
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.getText()?.let {
                                    messageInput = it.text
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = NeonCyan)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("scan_message_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.startScan(ScanType.MESSAGE, messageInput)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("scan_message_submit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color(0xFF00363D)
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PresetSection(
                        title = "Sample Scam SMS / Email Templates",
                        presets = listOf(
                            PresetItem("🚨 Banking Suspension Scam", "URGENT: Your Chase checking account has been suspended due to suspicious activity. Verify immediately: https://secure-login-chase-update.info/auth", ThreatRed),
                            PresetItem("🚨 USPS Package Delivery Issue", "USPS Alert: Your package #US83912 has missing house number. Update address in 12 hours or item will be returned: http://usps-redelivery-hub.top/track", ThreatRed),
                            PresetItem("🚨 IRS Tax Refund Scam", "IRS Notification: You have an unclaimed tax refund of $1,420.50. Claim funds directly to bank: https://irs-tax-refund-portal.click", ThreatRed),
                            PresetItem("✅ Normal Security OTP Code", "Your Google verification code is 592810. Do not share this code with anyone.", SafeGreen)
                        ),
                        onSelect = { messageInput = it }
                    )
                }

                3 -> {
                    // ================= SCREENSHOT TAB =================
                    Text(
                        text = "Upload a screenshot of any suspicious chat, login screen, invoice, or email for AI visual threat analysis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Selected Screenshot",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to choose image from Gallery",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Supports PNG, JPEG, Screenshots",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    OutlinedTextField(
                        value = screenshotDesc,
                        onValueChange = { screenshotDesc = it },
                        label = { Text("Image context / Notes") },
                        modifier = Modifier.fillMaxWidth().testTag("scan_screenshot_desc_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Image", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.startScan(
                                    inputType = ScanType.SCREENSHOT,
                                    input = screenshotDesc,
                                    bitmap = selectedBitmap
                                )
                            },
                            modifier = Modifier.weight(1.3f).height(50.dp).testTag("scan_screenshot_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF00363D)
                            )
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Image", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PresetSection(
                        title = "Sample Screenshot Threat Contexts",
                        presets = listOf(
                            PresetItem("🚨 WhatsApp Urgent Transfer Request", "Screenshot of WhatsApp message demanding emergency wire transfer from CEO", ThreatRed),
                            PresetItem("🚨 Fake Apple ID Lockout Dialog", "Popup claiming Apple ID locked with prompt to call +1-800 support", ThreatRed),
                            PresetItem("✅ Verified Bank Transaction Receipt", "Legitimate receipt screenshot with authentic confirmation number", SafeGreen)
                        ),
                        onSelect = { screenshotDesc = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ScanProcessingView(
    stepIndex: Int,
    message: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "proc")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NeonCyan.copy(alpha = 0.3f), Color(0xFF0F172A))
                            )
                        )
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "AI Threat Analysis in Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.height(44.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Step Counter Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val isActive = index <= stepIndex
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == stepIndex) 30.dp else 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isActive) NeonCyan else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

private data class PresetItem(
    val title: String,
    val payload: String,
    val color: Color
)

@Composable
private fun PresetSection(
    title: String,
    presets: List<PresetItem>,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            presets.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(item.payload) }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.payload,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Use",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}
