package com.stephaneperez.angerona.fasti

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.stephaneperez.angerona.fasti.notification.NotificationHelper
import com.stephaneperez.angerona.fasti.ui.FastiScreen
import com.stephaneperez.angerona.fasti.ui.theme.AngeronaFastiTheme
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    /** The date to jump to, set when the Activity is opened from a reminder notification. */
    private var navigateToDate by mutableStateOf<LocalDate?>(null)

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No-op either way: reminders simply won't show if this is declined. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        navigateToDate = dateFromIntent(intent)

        setContent {
            AngeronaFastiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FastiColors.ground,
                ) {
                    FastiScreen(
                        navigateToDate = navigateToDate,
                        onDateConsumed = { navigateToDate = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navigateToDate = dateFromIntent(intent)
    }

    private fun dateFromIntent(intent: Intent?): LocalDate? {
        val raw = intent?.getStringExtra(NotificationHelper.EXTRA_EVENT_DATE)?.takeIf { it.isNotBlank() }
            ?: return null
        return runCatching { LocalDate.parse(raw) }.getOrNull()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
