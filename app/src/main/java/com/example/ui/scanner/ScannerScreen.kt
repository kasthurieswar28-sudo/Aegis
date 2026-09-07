package com.example.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.DetectionResult
import com.example.ui.components.CyberCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PhantomViewModel
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: PhantomViewModel,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val liveDetection by viewModel.liveDetection.collectAsState()
    val liveStages by viewModel.liveStages.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Photo picker (0 permissions needed, compliant with Google Play policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        viewModel.analyzeBitmap(bitmap)
                        onNavigateToResult()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("scanner_screen")
    ) {
        // Mode Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateCard,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyberCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Camera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Direct Text / URL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> {
                    if (hasCameraPermission) {
                        LiveCameraView(
                            viewModel = viewModel,
                            onAnalyze = { detection ->
                                viewModel.analyzeDetection(detection)
                                onNavigateToResult()
                            }
                        )
                    } else {
                        CameraPermissionFallback(
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onPickImage = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }
                1 -> {
                    ManualTextInputView(
                        onAnalyzeText = { text ->
                            viewModel.analyzeDirectText(text)
                            onNavigateToResult()
                        }
                    )
                }
            }

            // Global analyzing overlay with live stages
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_analysis_overlay"),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = CyberCyan,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "LIVE DECEPTION PIPELINE",
                                        color = CyberCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Multi-vector heuristic verification in progress",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            liveStages.forEach { stage ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    when (stage.status) {
                                        com.example.ui.viewmodel.StageStatus.COMPLETED -> {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = SafeEmerald,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        com.example.ui.viewmodel.StageStatus.ACTIVE -> {
                                            CircularProgressIndicator(
                                                color = CyberCyan,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        com.example.ui.viewmodel.StageStatus.FAILED -> {
                                            Icon(
                                                imageVector = Icons.Filled.Shield,
                                                contentDescription = null,
                                                tint = com.example.ui.theme.ThreatRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        com.example.ui.viewmodel.StageStatus.PENDING -> {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(BorderSubtle)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stage.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (stage.status == com.example.ui.viewmodel.StageStatus.PENDING) TextMuted else TextPrimary
                                        )
                                        Text(
                                            text = stage.detail,
                                            fontSize = 10.sp,
                                            color = if (stage.status == com.example.ui.viewmodel.StageStatus.ACTIVE) CyberSky else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Bar with Gallery Import
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateCard)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("import_gallery_button")
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, tint = CyberSky, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Image", color = TextPrimary, fontSize = 12.sp)
            }

            Text(
                text = "Privacy: Client-Side OCR",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LiveCameraView(
    viewModel: PhantomViewModel,
    onAnalyze: (DetectionResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = viewModel.scannerInstance()
    val liveDetection by viewModel.liveDetection.collectAsState()

    // Scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor,
                                scanner.createAnalyzer { detection ->
                                    viewModel.onLiveFrameDetected(detection)
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Overlay: Target Reticle & Scanning Beam
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val boxSize = size.width * 0.72f
            val left = cx - boxSize / 2
            val top = cy - boxSize / 2
            val right = cx + boxSize / 2
            val bottom = cy + boxSize / 2

            // Corner brackets
            val cornerLen = 32.dp.toPx()
            val strokeW = 4.dp.toPx()
            val bracketColor = CyberCyan

            // Top-left
            drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
            drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)

            // Top-right
            drawLine(bracketColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
            drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)

            // Bottom-left
            drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
            drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)

            // Bottom-right
            drawLine(bracketColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
            drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

            // Animated laser
            val currentLaserY = top + (bottom - top) * laserY
            drawLine(
                color = CyberCyan.copy(alpha = 0.85f),
                start = Offset(left + 8.dp.toPx(), currentLaserY),
                end = Offset(right - 8.dp.toPx(), currentLaserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // Live Detection Card Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val detection = liveDetection
            if (detection != null && (detection.hasQr || detection.hasText)) {
                CardBanner(
                    detection = detection,
                    onAnalyze = { onAnalyze(detection) }
                )
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Point camera at QR code or message text",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBanner(
    detection: DetectionResult,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCyan, RoundedCornerShape(14.dp)),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFF0F1E2E).copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (detection.hasQr) "QR CODE DETECTED" else "TEXT DETECTED",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val previewText = detection.qrCode ?: detection.textContent
            Text(
                text = previewText.take(90),
                color = TextPrimary,
                fontSize = 12.sp,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAnalyze,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_live_button")
            ) {
                Text(
                    text = "ANALYZE NOW",
                    color = ObsidianBg,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ManualTextInputView(
    onAnalyzeText: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "DIRECT INSPECTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            letterSpacing = 1.sp
        )
        Text(
            text = "Paste any suspicious SMS, WhatsApp message, email, or URL directly to analyze without camera.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = {
                Text("Paste suspicious message, link, or payment request here...", color = TextMuted, fontSize = 13.sp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .testTag("manual_text_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SlateCard,
                unfocusedContainerColor = SlateCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    onAnalyzeText(textInput)
                }
            },
            enabled = textInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                disabledContainerColor = CyberCyan.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("manual_analyze_button")
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = ObsidianBg)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EVALUATE RISKS",
                color = ObsidianBg,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun CameraPermissionFallback(
    onRequestPermission: () -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CAMERA ACCESS REQUIRED",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aegis analyzes QR codes and physical screens live to protect you from social engineering.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("request_camera_permission_button")
        ) {
            Text("GRANT CAMERA ACCESS", color = ObsidianBg, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onPickImage,
            colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("OR PICK IMAGE FROM GALLERY", color = CyberSky, fontSize = 12.sp)
        }
    }
}
