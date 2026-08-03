package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantName: String,
    val date: String,
    val receiptNumber: String = "",
    val category: String,
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxRate: Double = 20.0,
    val totalAmount: Double,
    val currency: String = "₺",
    val paymentMethod: String = "Kredi Kartı",
    val itemsSummary: String = "",
    val imagePath: String = "",
    val notes: String = "",
    val tags: String = "İş", // Comma-separated tags e.g. "İş", "Kişisel", "Seyahat"
    val timestamp: Long = System.currentTimeMillis()
)
