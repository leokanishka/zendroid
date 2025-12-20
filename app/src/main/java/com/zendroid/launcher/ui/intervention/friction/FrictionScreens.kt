package com.zendroid.launcher.ui.intervention.friction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Friction UI components.
 * 
 * Performance (Gap F3):
 * - Uses rememberInfiniteTransition for GPU-accelerated animations
 * - Minimal recompositions during animation
 */

/**
 * Liquid Hold Button: User must hold for specified duration.
 */
@Composable
fun LiquidHoldButton(
    durationMs: Long,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "hold_progress"
    )

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                if (progress >= 1f) {
                    onComplete()
                }
                delay(16) // ~60fps
            }
        } else {
            progress = 0f
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Hold to Continue",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                // Background circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 8.dp.toPx())
                )
                // Progress arc
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx())
                )
            }
            Text(
                text = "${((1 - animatedProgress) * (durationMs / 1000)).toInt()}s",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Breathing Circle: 15s guided breathing animation.
 */
@Composable
fun BreathingCircle(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cycle by remember { mutableStateOf(0) }
    val totalCycles = 3 // 3 breath cycles = ~15s
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        repeat(totalCycles) {
            delay(5000) // One full breath cycle
            cycle++
        }
        onComplete()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = if (scale > 0.8f) "Breathe In..." else "Breathe Out...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(150.dp)
        ) {
            Canvas(modifier = Modifier.size((150 * scale).dp)) {
                drawCircle(
                    color = Color(0xFF64B5F6).copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cycle ${cycle + 1} of $totalCycles",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Math Challenge: User must solve a simple addition problem.
 */
@Composable
fun MathChallenge(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (problem, answer) = remember { FrictionEngine.generateMathProblem() }
    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = "Solve to Continue",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = problem,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = userInput,
            onValueChange = { 
                userInput = it
                isError = false
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (userInput.toIntOrNull() == answer) {
                        onComplete()
                    } else {
                        isError = true
                    }
                }
            ),
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        
        if (isError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try again",
                color = Color(0xFFEF5350),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (userInput.toIntOrNull() == answer) {
                    onComplete()
                } else {
                    isError = true
                }
            }
        ) {
            Text("Submit")
        }
    }
}
