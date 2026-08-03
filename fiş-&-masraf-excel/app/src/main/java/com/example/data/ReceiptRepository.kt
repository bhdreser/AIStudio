package com.example.data

import com.example.data.local.ReceiptDao
import com.example.data.local.ReceiptEntity
import com.example.data.local.ReportDao
import com.example.data.local.ReportEntity
import kotlinx.coroutines.flow.Flow

class ReceiptRepository(
    private val receiptDao: ReceiptDao,
    private val reportDao: ReportDao
) {

    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()

    fun searchReceipts(query: String): Flow<List<ReceiptEntity>> {
        return if (query.isBlank()) {
            receiptDao.getAllReceipts()
        } else {
            receiptDao.searchReceipts(query.trim())
        }
    }

    suspend fun getReceiptById(id: Long): ReceiptEntity? = receiptDao.getReceiptById(id)

    suspend fun insertReceipt(receipt: ReceiptEntity): Long = receiptDao.insertReceipt(receipt)

    suspend fun updateReceipt(receipt: ReceiptEntity) = receiptDao.updateReceipt(receipt)

    suspend fun deleteReceipt(id: Long) = receiptDao.deleteReceiptById(id)

    suspend fun deleteAll() = receiptDao.deleteAllReceipts()

    // Report Operations
    suspend fun getReportById(id: Long): ReportEntity? = reportDao.getReportById(id)

    suspend fun insertReport(report: ReportEntity): Long = reportDao.insertReport(report)

    suspend fun updateReport(report: ReportEntity) = reportDao.updateReport(report)

    suspend fun deleteReport(id: Long) = reportDao.deleteReportById(id)
}
