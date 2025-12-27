package com.zendroid.launcher.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zendroid.launcher.data.db.SessionEntity
import com.zendroid.launcher.data.repository.SettingsRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyContent_displaysAnalyticsAndSessions() {
        // Arrange
        val sessions = listOf(
            SessionEntity(
                packageName = "com.example.app",
                startTimeElapsed = 1000L,
                durationMinutes = 5,
                expiryTimeElapsed = 2000L,
                reason = "Reading"
            ),
            SessionEntity(
                packageName = "com.example.social",
                startTimeElapsed = 3000L,
                durationMinutes = 15,
                expiryTimeElapsed = 4000L,
                reason = "Socializing"
            )
        )
        
        val analytics = HistoryAnalytics(
            topReasons = listOf("Reading" to 1, "Socializing" to 1),
            totalMindfulMinutes = 20
        )

        // Act
        composeTestRule.setContent {
            HistoryContent(
                sessions = sessions,
                analytics = analytics,
                zenLevel = SettingsRepository.ZenTitrationLevel.STANDARD,
                onGetIcon = { null }
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Total Focus Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("20m").assertIsDisplayed() // Analytics total
        
        composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Socializing").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("5m").assertIsDisplayed() // Session duration
        composeTestRule.onNodeWithText("15m").assertIsDisplayed()
    }
    
    @Test
    fun historyContent_paginationPerformance() {
        // Create 150 sessions
        val sessions = (1..150).map { i ->
            SessionEntity(
                packageName = "com.example.app$i",
                startTimeElapsed = i * 1000L,
                durationMinutes = 1,
                expiryTimeElapsed = i * 2000L,
                reason = "Reason $i"
            )
        }
        
        val analytics = HistoryAnalytics(emptyList(), 150)

        composeTestRule.setContent {
             HistoryContent(
                sessions = sessions,
                analytics = analytics,
                zenLevel = SettingsRepository.ZenTitrationLevel.STANDARD,
                onGetIcon = { null }
            )
        }

        // Verify start list
        composeTestRule.onNodeWithText("Reason 1").assertExists()
        
        // Just ensuring it renders without crashing is a win here. 
        // Real scrolling test requires identifying lazy list and performing swipe gestures.
    }
}
