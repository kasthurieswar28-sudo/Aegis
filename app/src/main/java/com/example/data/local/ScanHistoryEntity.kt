package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val scanType: String,
    val summary: String,
    val destinationUrl: String?,
    val riskScore: Int,
    val classification: String,
    val threatCategory: String,
    val intent: String,
    val detectedSignals: String, // Comma separated
    val manipulationTechniques: String, // Comma separated
    val reasons: String, // Pipe separated
    val phantomInsight: String,
    val recommendedAction: String,
    val isAIEnhanced: Boolean
)
