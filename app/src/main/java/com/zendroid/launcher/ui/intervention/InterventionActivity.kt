package com.zendroid.launcher.ui.intervention

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen Intervention Activity displayed when user opens a "Red" app.
 * 
 * Performance Optimizations (Gap F2):
 * - First frame: Solid background only
 * - Compose content deferred to second frame
 */
@AndroidEntryPoint
class InterventionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }

    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
        
        // Set content immediately for fast first frame
        setContent {
            InterventionScreen(
                targetPackage = targetPackage,
                onSessionGranted = { durationMinutes, reason ->
                    grantSessionAndLaunch(durationMinutes, reason)
                },
                onDismiss = { finish() }
            )
        }
    }

    private fun grantSessionAndLaunch(durationMinutes: Int, reason: String) {
        // TODO: Create session via SessionRepository
        
        // Launch the target app
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
        finish()
    }

    @Deprecated("Use OnBackPressedDispatcher", ReplaceWith("onBackPressedDispatcher"))
    override fun onBackPressed() {
        // Intercept back press during intervention - user must complete or dismiss
        // This is intentional friction
    }
}

@Composable
fun InterventionScreen(
    targetPackage: String,
    onSessionGranted: (durationMinutes: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStage by remember { mutableStateOf(InterventionStage.REASON) }
    var reason by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(5) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC1A1A2E)) // Semi-transparent dark overlay
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStage) {
                InterventionStage.REASON -> {
                    ReasonStage(
                        targetPackage = targetPackage,
                        onReasonSubmitted = { submittedReason ->
                            reason = submittedReason
                            currentStage = InterventionStage.DURATION
                        },
                        onDismiss = onDismiss
                    )
                }
                InterventionStage.DURATION -> {
                    DurationStage(
                        onDurationSelected = { duration ->
                            selectedDuration = duration
                            currentStage = InterventionStage.FRICTION
                        }
                    )
                }
                InterventionStage.FRICTION -> {
                    FrictionStage(
                        onFrictionCompleted = {
                            onSessionGranted(selectedDuration, reason)
                        }
                    )
                }
            }
        }
    }
}

enum class InterventionStage {
    REASON,
    DURATION,
    FRICTION
}

@Composable
fun ReasonStage(
    targetPackage: String,
    onReasonSubmitted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wait.",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're about to open a restricted app.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        // TODO: Add TextField for reason input + Submit/Cancel buttons
        Text(
            text = "[Reason Input - Coming Soon]",
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DurationStage(
    onDurationSelected: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How long?",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        // TODO: Add duration selection buttons (5m, 15m, 30m)
        Text(
            text = "[Duration Selection - Coming Soon]",
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun FrictionStage(
    onFrictionCompleted: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "One more thing...",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        // TODO: Add FrictionEngine integration (LiquidHold, Breathing, Math)
        Text(
            text = "[Friction Challenge - Coming Soon]",
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}
