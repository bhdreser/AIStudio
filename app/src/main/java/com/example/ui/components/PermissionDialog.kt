package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.LanguageCode
import com.example.data.model.Strings

@Composable
fun PermissionDialog(
    permissionType: String, // "camera" or "microphone"
    language: LanguageCode,
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    val titleKey = if (permissionType == "camera") "camera_denied" else "mic_denied"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.get(titleKey, language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = if (permissionType == "camera") {
                    "What's This? needs camera access to take photos of objects, insects, plants, text, and signs for visual AI discovery."
                } else {
                    "What's This? needs microphone access so you can speak your questions naturally."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onGrant) {
                Text(text = Strings.get("grant_permission", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = Strings.get("cancel", language))
            }
        }
    )
}
