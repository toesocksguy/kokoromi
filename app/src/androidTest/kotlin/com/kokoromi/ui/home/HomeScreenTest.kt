package com.kokoromi.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kokoromi.domain.usecase.CheckExperimentLifecycleUseCase
import com.kokoromi.domain.usecase.GetActiveExperimentsWithLogsUseCase
import com.kokoromi.ui.FakeDailyLogRepository
import com.kokoromi.ui.FakeExperimentRepository
import com.kokoromi.ui.makeExperiment
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeRepo: FakeExperimentRepository
    private lateinit var fakeLogs: FakeDailyLogRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeExperimentRepository()
        fakeLogs = FakeDailyLogRepository()
        viewModel = HomeViewModel(
            checkExperimentLifecycle = CheckExperimentLifecycleUseCase(fakeRepo),
            getActiveExperimentsWithLogs = GetActiveExperimentsWithLogsUseCase(fakeRepo, fakeLogs),
            experimentRepository = fakeRepo,
        )
    }

    @Test
    fun showsEmptyStateWhenNoExperiments() {
        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("No active experiments").assertIsDisplayed()
    }

    @Test
    fun showsCreateButtonInEmptyState() {
        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("Create experiment").assertIsDisplayed()
    }

    @Test
    fun clickingCreateButtonInEmptyStateInvokesCallback() {
        var called = false
        composeRule.setContent {
            HomeScreen(onCreateExperiment = { called = true }, onCheckIn = { _, _ -> }, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("Create experiment").performClick()

        assertTrue(called)
    }

    @Test
    fun showsExperimentCardWhenExperimentExists() {
        fakeRepo.setActiveExperiments(listOf(makeExperiment(action = "Read for 20 minutes")))

        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Read for 20 minutes").assertIsDisplayed()
    }

    @Test
    fun showsNewExperimentButtonWhenBelowSlotCap() {
        fakeRepo.setActiveExperiments(listOf(makeExperiment()))

        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("New experiment").assertIsDisplayed()
    }

    @Test
    fun hidesNewExperimentButtonAtSlotCap() {
        fakeRepo.setActiveExperiments(listOf(makeExperiment(), makeExperiment()))

        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("New experiment").assertDoesNotExist()
    }

    @Test
    fun clickingNewExperimentButtonInvokesCallback() {
        var called = false
        fakeRepo.setActiveExperiments(listOf(makeExperiment()))

        composeRule.setContent {
            HomeScreen(onCreateExperiment = { called = true }, onCheckIn = { _, _ -> }, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("New experiment").performClick()

        assertTrue(called)
    }

    @Test
    fun showsBothCardsWhenTwoExperimentsExist() {
        fakeRepo.setActiveExperiments(listOf(
            makeExperiment(action = "Meditate daily"),
            makeExperiment(action = "Write 500 words"),
        ))

        composeRule.setContent {
            HomeScreen(onCreateExperiment = {}, onCheckIn = { _, _ -> }, onNavigateToCompletion = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Meditate daily").assertIsDisplayed()
        composeRule.onNodeWithText("Write 500 words").assertIsDisplayed()
    }
}
