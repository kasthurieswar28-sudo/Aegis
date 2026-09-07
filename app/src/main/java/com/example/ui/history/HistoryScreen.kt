package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RiskClassification
import com.example.domain.model.ScanResult
import com.example.ui.components.PhantomHeaderBar
import com.example.ui.components.RiskClassificationBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SafeEmeraldLight
import com.example.ui.theme.SlateCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThreatRed
import com.example.ui.viewmodel.PhantomViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: PhantomViewModel,
    onNavigateToResult: () -> Unit
) {
    val history by viewModel.allHistory.collectAsState()
    var selectedFilter by remember { mutableStateOf<RiskClassification?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredList = if (selectedFilter == null) {
        history
    } else {
        history.filter { it.classification == selectedFilter }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Scan History", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Permanently delete all stored scan history from your device?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("CLEAR ALL", color = ThreatRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = SlateCard
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("history_screen")
    ) {
        PhantomHeaderBar(subtitle = "Threat Analysis History")

        // Filter chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${history.size})", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SafeEmerald.copy(alpha = 0.15f),
                    selectedLabelColor = SafeEmeraldLight,
                    containerColor = SlateCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == null,
                    borderColor = BorderSubtle,
                    selectedBorderColor = SafeEmerald.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = selectedFilter == RiskClassification.CRITICAL || selectedFilter == RiskClassification.HIGH_RISK,
                onClick = {
                    selectedFilter = if (selectedFilter == RiskClassification.HIGH_RISK) null else RiskClassification.HIGH_RISK
                },
                label = { Text("Threats", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ThreatRed.copy(alpha = 0.15f),
                    selectedLabelColor = ThreatRed,
                    containerColor = SlateCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == RiskClassification.CRITICAL || selectedFilter == RiskClassification.HIGH_RISK,
                    borderColor = BorderSubtle,
                    selectedBorderColor = ThreatRed.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = selectedFilter == RiskClassification.SAFE,
                onClick = {
                    selectedFilter = if (selectedFilter == RiskClassification.SAFE) null else RiskClassification.SAFE
                },
                label = { Text("Safe", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SafeEmerald.copy(alpha = 0.15f),
                    selectedLabelColor = SafeEmeraldLight,
                    containerColor = SlateCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == RiskClassification.SAFE,
                    borderColor = BorderSubtle,
                    selectedBorderColor = SafeEmerald.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = TextMuted
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO SCANS IN HISTORY",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scanned QR codes and verified messages will be preserved here.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { scan ->
                    HistoryItemCard(
                        scan = scan,
                        onClick = {
                            viewModel.selectHistoryItem(scan)
                            onNavigateToResult()
                        },
                        onDelete = {
                            viewModel.deleteScan(scan.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    scan: ScanResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val scoreColor = when (scan.classification) {
        RiskClassification.SAFE -> SafeEmerald
        RiskClassification.SUSPICIOUS -> ElectricAmber
        RiskClassification.HIGH_RISK -> ThreatRed
        RiskClassification.CRITICAL -> ThreatRed
    }

    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .testTag("history_item_${scan.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(scoreColor.copy(alpha = 0.15f))
                        .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${scan.riskScore}",
                        color = scoreColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scan.threatCategory.displayName,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        RiskClassificationBadge(classification = scan.classification)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${scan.scanType.displayName} • ${dateFormat.format(Date(scan.timestamp))}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete item",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.rawContent.take(120) + if (scan.rawContent.length > 120) "..." else "",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2
            )

            // Detected signals pills
            val signals = if (scan.signals.signalDetails.isNotEmpty()) scan.signals.signalDetails
            else scan.evidenceItems.map { it.signalName }

            if (signals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    signals.take(3).forEach { sig ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianBg)
                                .border(0.5.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sig.take(24),
                                color = CyberSky,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (signals.size > 3) {
                        Text(
                            text = "+${signals.size - 3} more",
                            color = TextMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
