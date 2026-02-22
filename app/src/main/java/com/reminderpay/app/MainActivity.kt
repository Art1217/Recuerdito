package com.reminderpay.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.reminderpay.app.navigation.ReminderPayNavGraph
import com.reminderpay.app.ui.theme.ReminderPayTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity — hosts the entire Compose navigation graph.
 * Requests POST_NOTIFICATIONS permission on Android 13+ at startup.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Runtime permission launcher for POST_NOTIFICATIONS (required on API 33+)
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Optional: show a snackbar if denied. For now we proceed regardless.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask for notification permission on Android 13+ (API 33)
        requestNotificationPermissionIfNeeded()

        setContent {
            ReminderPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    ReminderPayNavGraph()
                }
            }
        }
    }

    /**
     * On Android 13+, POST_NOTIFICATIONS is a runtime permission.
     * Without it, NotificationManagerCompat.notify() throws SecurityException silently.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
