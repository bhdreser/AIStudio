package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ReceiptEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ExpenseWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, ExpenseWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }

        private suspend fun updateWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val db = AppDatabase.getDatabase(context)
            val allReceipts = db.receiptDao().getAllReceiptsSync()

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed

            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))
            val monthTitle = monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() } + " Toplamı"

            var currentMonthTotal = 0.0
            var currentMonthCount = 0

            val receiptCal = Calendar.getInstance()
            val df1 = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val df2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            allReceipts.forEach { receipt: ReceiptEntity ->
                var isThisMonth = false
                try {
                    val dateObj = try {
                        df1.parse(receipt.date)
                    } catch (e: Exception) {
                        try { df2.parse(receipt.date) } catch (e2: Exception) { null }
                    }

                    if (dateObj != null) {
                        receiptCal.time = dateObj
                        if (receiptCal.get(Calendar.YEAR) == currentYear &&
                            receiptCal.get(Calendar.MONTH) == currentMonth
                        ) {
                            isThisMonth = true
                        }
                    } else {
                        // Fallback to timestamp
                        receiptCal.timeInMillis = receipt.timestamp
                        if (receiptCal.get(Calendar.YEAR) == currentYear &&
                            receiptCal.get(Calendar.MONTH) == currentMonth
                        ) {
                            isThisMonth = true
                        }
                    }
                } catch (e: Exception) {
                    isThisMonth = true
                }

                if (isThisMonth) {
                    currentMonthTotal += receipt.totalAmount
                    currentMonthCount++
                }
            }

            val formattedTotal = String.format(Locale("tr", "TR"), "%,.2f ₺", currentMonthTotal)
            val formattedCount = "$currentMonthCount Fiş Kaydedildi"

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.expense_widget)

                views.setTextViewText(R.id.widget_month_text, monthTitle)
                views.setTextViewText(R.id.widget_total_text, formattedTotal)
                views.setTextViewText(R.id.widget_count_text, formattedCount)

                // Quick Camera Intent
                val cameraIntent = Intent(context, MainActivity::class.java).apply {
                    action = "SCAN_CAMERA"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val cameraPendingIntent = PendingIntent.getActivity(
                    context,
                    1001,
                    cameraIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_camera_btn, cameraPendingIntent)

                // Main App Launch Intent
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    action = "OPEN_APP"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    1002,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
