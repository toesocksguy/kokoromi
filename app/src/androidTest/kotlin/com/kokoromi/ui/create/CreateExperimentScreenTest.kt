package com.kokoromi.ui.create

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kokoromi.domain.usecase.CreateExperimentUseCase
import com.kokoromi.ui.FakeExperimentRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateExperimentScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeRepo: FakeExperimentRepository
    private lateinit var viewModel: CreateExperimentViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeExperimentRepository()
        viewModel = CreateExperimentViewModel(CreateExperimentUseCase(fakeRepo))
    }

    @Test
    fun showsHypothesisField() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("What do you want to test?").assertIsDisplayed()
    }

    @Test
    fun showsActionField() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("What will you do?").assertIsDisplayed()
    }

    @Test
    fun showsDurationOptions() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("1 week").assertIsDisplayed()
        composeRule.onNodeWithText("2 weeks").assertIsDisplayed()
        composeRule.onNodeWithText("4 weeks").assertIsDisplayed()
        composeRule.onNodeWithText("Custom").assertIsDisplayed()
    }

    @Test
    fun showsStartExperimentButton() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Start Experiment").assertIsDisplayed()
    }

    @Test
    fun showsBackButton() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun clickingBackInvokesCallback() {
        var called = false
        composeRule.setContent {
            CreateExperimentScreen(onBack = { called = true }, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(called)
    }

    @Test
    fun nonBlankActionShowsPactPreview() {
        viewModel.onActionChange("do 10 pushups a day")

        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("YOUR PACT", substring = true).assertIsDisplayed()
    }

    @Test
    fun startExperimentButtonIsEnabledByDefault() {
        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Start Experiment").assertIsEnabled()
    }

    @Test
    fun successfulSubmitSetsSuccessState() {
        viewModel.onHypothesisChange("If I exercise, I'll feel better")
        viewModel.onActionChange("Do 10 pushups every morning")

        composeRule.setContent {
            CreateExperimentScreen(onBack = {}, onSuccess = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Start Experiment").performClick()

        composeRule.waitUntil(timeoutMillis = 3_000) {
            viewModel.uiState.value is CreateExperimentUiState.Success
        }
    }
}
