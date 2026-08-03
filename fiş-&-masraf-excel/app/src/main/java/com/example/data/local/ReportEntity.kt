package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryFilter: String = "Tümü",
    val receiptIds: String = "", // Comma-separated receipt IDs
    val totalAmount: Double = 0.0,
    val totalTax: Double = 0.0,
    val receiptCount: Int = 0,
    val versionName: String = "v1.0",
    val status: String = "Taslak", // Taslak, Onaylandı, Gönderildi
    val aiInsightSummary: String = "",
    val createdAt: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
