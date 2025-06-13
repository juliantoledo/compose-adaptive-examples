/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.adaptivethreepanels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptivethreepanels.model.ActivePanel
import com.example.adaptivethreepanels.model.PanelsState
import com.example.adaptivethreepanels.ui.AdaptiveThreePanelScaffold
import com.example.adaptivethreepanels.ui.theme.AdaptiveThreePanelsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.adaptivethreepanels.ui.PanelColumn
import com.example.adaptivethreepanels.ui.theme.Grey40
import com.example.adaptivethreepanels.ui.theme.Grey60

/**
 * The ViewModel responsible for holding and managing the UI state for the panels.
 *
 * This class acts as the single source of truth for the panel visibility. It separates the
 * state and business logic from the UI, which improves testability and follows modern
 * Android architecture guidelines. It uses a [SavedStateHandle] to ensure that the
 * UI state survives both configuration changes (e.g., rotation) and process death.
 */
class PanelsViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        // A key for saving and retrieving the PanelsState from the SavedStateHandle.
        private const val PANELS_STATE_KEY = "panelsState"
    }

    // A private, mutable state flow that holds the current state.
    // It's initialized from the SavedStateHandle or with a default value.
    private val _panelsState = MutableStateFlow(
        savedStateHandle.get<PanelsState>(PANELS_STATE_KEY) ?: PanelsState()
    )
    // A public, read-only StateFlow that the UI can collect to observe state changes.
    val panelsState: StateFlow<PanelsState> = _panelsState.asStateFlow()

    // A single, private function to update the state in both the StateFlow and the SavedStateHandle.
    private fun updateState(newState: PanelsState) {
        _panelsState.value = newState
        savedStateHandle[PANELS_STATE_KEY] = newState
    }

    /** Handles a toggle event from the standard layout, flipping the visibility of the left panel. */
    fun toggleLeftPanel() = updateState(panelsState.value.copy(isLeftPanelOpen = !panelsState.value.isLeftPanelOpen))

    /** Handles a toggle event from the standard layout, flipping the visibility of the right panel. */
    fun toggleRightPanel() = updateState(panelsState.value.copy(isRightPanelOpen = !panelsState.value.isRightPanelOpen))

    /** Handles a toggle event from the standard layout, flipping the visibility of the bottom panel. */
    fun toggleBottomPanel() = updateState(panelsState.value.copy(isBottomPanelOpen = !panelsState.value.isBottomPanelOpen))

    /**
     * Handles a selection event from the compact layout's navigation bar. This follows a
     * "single selection" logic: tapping an item makes it the only active panel, and tapping
     * the active item deselects it.
     */
    fun selectCompactPanel(panel: ActivePanel) {
        val currentState = panelsState.value
        val currentlyActive = when {
            currentState.isLeftPanelOpen -> ActivePanel.LEFT
            currentState.isRightPanelOpen -> ActivePanel.RIGHT
            currentState.isBottomPanelOpen -> ActivePanel.BOTTOM
            else -> null
        }

        if (panel == currentlyActive) {
            updateState(PanelsState()) // Deselect if already active
        } else {
            // Otherwise, open the selected panel and ensure all others are closed.
            val newState = when (panel) {
                ActivePanel.LEFT -> PanelsState(isLeftPanelOpen = true)
                ActivePanel.RIGHT -> PanelsState(isRightPanelOpen = true)
                ActivePanel.BOTTOM -> PanelsState(isBottomPanelOpen = true)
            }
            updateState(newState)
        }
    }
}

/**
 * The main entry point of the application.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // This enables the app to draw behind the system bars.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AdaptiveThreePanelsTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val viewModel: PanelsViewModel = viewModel()
                val panelsState by viewModel.panelsState.collectAsState()

                val titles = listOf(
                    "DISPLAY SETTINGS", "AUDIO & SOUND", "NETWORK", "STORAGE",
                    "PERMISSIONS", "NOTIFICATIONS", "ADVANCED", "ABOUT", "HELP"
                )

                AdaptiveThreePanelScaffold(
                    widthSizeClass = windowSizeClass.widthSizeClass,
                    panelsState = panelsState,
                    onLeftPanelToggle = viewModel::toggleLeftPanel,
                    onRightPanelToggle = viewModel::toggleRightPanel,
                    onBottomPanelToggle = viewModel::toggleBottomPanel,
                    onPanelSelect = viewModel::selectCompactPanel,
                    mainContent = {
                        BasicText(
                            "Main Content",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leftPanelContent = {
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)
                        ) {
                            BasicText("Left Panel", style = MaterialTheme.typography.bodyLarge)
                        }
                    },

                    rightPanelContent = { PanelColumn(
                        titles,
                        headerBackgroundColor = Grey60,
                        headerTitleColor = Color.White,
                        contentAreaColor = Grey40,
                        modifier = Modifier.width(300.dp).background(Grey60)
                    ) },

                    bottomPanelContent = {
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)
                        ) {
                            BasicText("Bottom Panel", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )
            }
        }
    }
}
// endregion

// region Previews
/**
 * Previews for visualizing the different adaptive layouts. These are decoupled from the
 * ViewModel and can be passed any state directly to test different scenarios.
 */
@Preview(name = "Compact Layout (Left Panel Open)", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun CompactLayoutPreview() {
    AdaptiveThreePanelsTheme {
        AdaptiveThreePanelScaffold(
            widthSizeClass = WindowWidthSizeClass.Compact,
            panelsState = PanelsState(isLeftPanelOpen = true), // Preview with left panel open
            onLeftPanelToggle = {},
            onRightPanelToggle = {},
            onBottomPanelToggle = {},
            onPanelSelect = {},
            mainContent = { BasicText("Main Content") },
            leftPanelContent = { BasicText("Left Panel") },
            rightPanelContent = { BasicText("Right Panel") },
            bottomPanelContent = { BasicText("Bottom Panel") }
        )
    }
}

@Preview(name = "Medium Layout (All Panels Open)", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun MediumLayoutPreview() {
    AdaptiveThreePanelsTheme {
        AdaptiveThreePanelScaffold(
            widthSizeClass = WindowWidthSizeClass.Medium,
            panelsState = PanelsState(isLeftPanelOpen = true, isRightPanelOpen = true, isBottomPanelOpen = true),
            onLeftPanelToggle = {},
            onRightPanelToggle = {},
            onBottomPanelToggle = {},
            onPanelSelect = {},
            mainContent = { BasicText("Main Content") },
            leftPanelContent = { BasicText("Left Panel") },
            rightPanelContent = { BasicText("Right Panel") },
            bottomPanelContent = { BasicText("Bottom Panel") }
        )
    }
}
// endregion
