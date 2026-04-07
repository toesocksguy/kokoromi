package com.kokoromi.ui.checkin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.usecase.LogDailyCheckInUseCase
import com.kokoromi.ui.FakeDailyLogRepository
import com.kokoromi.ui.FakeExperimentRepository
import com.kokoromi.ui.makeExperiment
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CheckInScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeExperimentRepo: FakeExperimentRepository
    private lateinit var fakeDailyLogRepo: FakeDailyLogRepository

    private val experimentId = "exp-1"

    @Before
    fun setUp() {
        fakeExperimentRepo = FakeExperimentRepository()
        fakeDailyLogRepo = FakeDailyLogRepository()
        fakeExperimentRepo.setActiveExperiments(listOf(makeExperiment(id = experimentId, action = "Meditate for 10 minutes")))
    }

    private fun makeViewModel(
        initialCompleted: Boolean = true,
        date: String? = null,
    ): CheckInViewModel {
        val args = buildMap<String, Any?> {
            put("experimentId", experimentId)
            put("initialCompleted", initialCompleted)
            if (date != null) put("date", date)
        }
        return CheckInViewModel(
            savedStateHandle = SavedStateHandle(args),
            experimentRepository = fakeExperimentRepo,
            dailyLogRepository = fakeDailyLogRepo,
            logDailyCheckIn = LogDailyCheckInUseCase(fakeExperimentRepo, fakeDailyLogRepo),
        )
    }

    @Test
    fun showsBackButton() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun clickingBackInvokesCallback() {
        var called = false
        composeRule.setContent {
            CheckInScreen(onBack = { called = true }, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(called)
    }

    @Test
    fun showsExperimentActionInTitle() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Meditate for 10 minutes")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Meditate for 10 minutes").assertIsDisplayed()
    }

    @Test
    fun showsDidYouDoItPromptWhenNoExistingLog() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Did you do it today?")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Did you do it today?").assertIsDisplayed()
    }

    @Test
    fun showsEditPromptWhenExistingLogPresent() {
        val existingLog = DailyLog(
            id = UUID.randomUUID().toString(),
            experimentId = experimentId,
            date = LocalDate.now(),
            completed = true,
            moodBefore = null,
            moodAfter = null,
            notes = null,
            loggedAt = Instant.now(),
        )
        fakeDailyLogRepo.seedLog(existingLog)

        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Edit log for today")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Edit log for today").assertIsDisplayed()
    }

    @Test
    fun showsYesAndSkipButtons() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("✓ YES")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("✓ YES").assertIsDisplayed()
        composeRule.onNodeWithText("✗ SKIP").assertIsDisplayed()
    }

    @Test
    fun showsMoodRatingLabel() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("How did it feel?")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("How did it feel?").assertIsDisplayed()
    }

    @Test
    fun showsNotesField() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Notes (optional)")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Notes (optional)").assertIsDisplayed()
    }

    @Test
    fun showsLogActivityButton() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Log Activity")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Log Activity").assertIsEnabled()
    }

    @Test
    fun moodStarsHaveEmptyContentDescriptionsByDefault() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Star 1, empty")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Star 1, empty").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Star 3, empty").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Star 5, empty").assertIsDisplayed()
    }

    @Test
    fun tappingMoodStarFillsUpToThatStar() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Star 3, empty")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Star 3, empty").performClick()

        composeRule.onNodeWithContentDescription("Star 1, filled").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Star 2, filled").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Star 3, filled, tap to clear").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Star 4, empty").assertIsDisplayed()
    }

    @Test
    fun tappingSelectedStarClearsMoodRating() {
        composeRule.setContent {
            CheckInScreen(onBack = {}, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Star 3, empty")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Star 3, empty").performClick()
        composeRule.onNodeWithContentDescription("Star 3, filled, tap to clear").performClick()

        composeRule.onNodeWithContentDescription("Star 3, empty").assertIsDisplayed()
    }

    @Test
    fun successfulSubmitInvokesOnBack() {
        var backCalled = false
        composeRule.setContent {
            CheckInScreen(onBack = { backCalled = true }, onReflect = {}, viewModel = makeViewModel())
        }

        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Log Activity")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Log Activity").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) { backCalled }
        assertTrue(backCalled)
    }
}
