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
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adaptivethreepanels.ui.theme.Grey20
import com.example.adaptivethreepanels.ui.theme.Grey40
import com.example.adaptivethreepanels.ui.theme.Grey60
import com.example.adaptivethreepanels.ui.theme.HandleColor
import com.example.adaptivethreepanels.ui.theme.AdaptiveThreePanelsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.parcelize.Parcelize


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

                AdaptiveThreePanelScaffold(
                    widthSizeClass = windowSizeClass.widthSizeClass,
                    panelsState = panelsState,
                    onLeftPanelToggle = viewModel::toggleLeftPanel,
                    onRightPanelToggle = viewModel::toggleRightPanel,
                    onBottomPanelToggle = viewModel::toggleBottomPanel,
                    onPanelSelect = viewModel::selectCompactPanel,
                    mainContent = { BasicText("Main Content", style = MaterialTheme.typography.bodyLarge) },
                    leftPanelContent = { BasicText("Left Panel", style = MaterialTheme.typography.bodyLarge) },
                    rightPanelContent = { BasicText("Right Panel", style = MaterialTheme.typography.bodyLarge) },
                    bottomPanelContent = { BasicText("Bottom Panel", style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }
    }
}

/**
 * The primary entry point for the adaptive three-panel layout.
 *
 * This composable is now fully stateless and preview-friendly. It observes the window size
 * and intelligently delegates to either a standard or compact layout, passing down the
 * state and event handlers it receives from a ViewModel-driven caller.
 */
@Composable
fun AdaptiveThreePanelScaffold(
    widthSizeClass: WindowWidthSizeClass,
    panelsState: PanelsState,
    onLeftPanelToggle: () -> Unit,
    onRightPanelToggle: () -> Unit,
    onBottomPanelToggle: () -> Unit,
    onPanelSelect: (ActivePanel) -> Unit,
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
    leftPanelContent: @Composable () -> Unit,
    rightPanelContent: @Composable () -> Unit,
    bottomPanelContent: @Composable () -> Unit
) {
    val mainContentColor: Color = Grey20
    val panelColor: Color = Grey40
    val dividerColor: Color = Grey60
    val handleColor: Color = HandleColor

    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactThreePanelScaffold(
                modifier = modifier,
                mainContentColor = mainContentColor,
                panelColor = panelColor,
                panelsState = panelsState,
                onPanelSelect = onPanelSelect,
                mainContent = mainContent,
                leftPanelContent = leftPanelContent,
                rightPanelContent = rightPanelContent,
                bottomPanelContent = bottomPanelContent
            )
        }
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            ThreePanelScaffold(
                modifier = modifier,
                mainContentColor = mainContentColor,
                panelColor = panelColor,
                dividerColor = dividerColor,
                handleColor = handleColor,
                panelsState = panelsState,
                onLeftPanelToggle = onLeftPanelToggle,
                onRightPanelToggle = onRightPanelToggle,
                onBottomPanelToggle = onBottomPanelToggle,
                mainContent = mainContent,
                leftPanelContent = leftPanelContent,
                rightPanelContent = rightPanelContent,
                bottomPanelContent = bottomPanelContent
            )
        }
    }
}


// region Data Models
/**
 * Represents the collective visibility state of all panels.
 * Being Parcelable allows it to be saved in a Bundle by the ViewModel's SavedStateHandle.
 */
@Parcelize
data class PanelsState(
    val isLeftPanelOpen: Boolean = false,
    val isRightPanelOpen: Boolean = false,
    val isBottomPanelOpen: Boolean = false
) : Parcelable

/** Represents the currently active panel in the compact layout. */
enum class ActivePanel { LEFT, RIGHT, BOTTOM }
// endregion


// region Scaffold Implementations
/**
 * The standard three-panel layout with collapsible side and bottom panels.
 * This composable is "controlled" (stateless); it receives the current state and calls
 * event lambdas (`on...Toggle`) to report user interactions.
 */
@Composable
fun ThreePanelScaffold(
    modifier: Modifier = Modifier,
    mainContentColor: Color,
    panelColor: Color,
    dividerColor: Color,
    handleColor: Color,
    panelsState: PanelsState,
    onLeftPanelToggle: () -> Unit,
    onRightPanelToggle: () -> Unit,
    onBottomPanelToggle: () -> Unit,
    mainContent: @Composable () -> Unit,
    leftPanelContent: @Composable () -> Unit,
    rightPanelContent: @Composable () -> Unit,
    bottomPanelContent: @Composable () -> Unit,
) {
    val animationSpec = tween<Float>(400)
    // The root Column consumes the navigation bar insets to prevent the UI from being
    // obscured by the system taskbar on tablets or gesture navigation bar on phones.
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Row(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(visible = panelsState.isLeftPanelOpen, modifier = Modifier.fillMaxHeight().background(panelColor), enter = fadeIn(animationSpec) + expandHorizontally(), exit = fadeOut(animationSpec) + shrinkHorizontally()) {
                Box(Modifier.padding(16.dp).fillMaxHeight(), Alignment.Center) { leftPanelContent() }
            }
            Box(Modifier.fillMaxHeight().width(16.dp).background(dividerColor).clickable(onClick = onLeftPanelToggle), Alignment.Center) {
                Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(handleColor))
            }
            Box(Modifier.weight(1f).fillMaxHeight().background(mainContentColor), Alignment.Center) { mainContent() }
            Box(Modifier.fillMaxHeight().width(16.dp).background(dividerColor).clickable(onClick = onRightPanelToggle), Alignment.Center) {
                Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(handleColor))
            }
            AnimatedVisibility(visible = panelsState.isRightPanelOpen, modifier = Modifier.fillMaxHeight().background(panelColor), enter = fadeIn(animationSpec) + expandHorizontally(), exit = fadeOut(animationSpec) + shrinkHorizontally()) {
                Box(Modifier.padding(16.dp).fillMaxHeight(), Alignment.Center) { rightPanelContent() }
            }
        }
        Box(Modifier.fillMaxWidth().height(16.dp).background(dividerColor).clickable(onClick = onBottomPanelToggle), Alignment.Center) {
            Box(Modifier.height(4.dp).width(40.dp).clip(RoundedCornerShape(2.dp)).background(handleColor))
        }
        AnimatedVisibility(visible = panelsState.isBottomPanelOpen, modifier = Modifier.fillMaxWidth().background(panelColor), enter = fadeIn(animationSpec) + expandVertically(), exit = fadeOut(animationSpec) + shrinkVertically()) {
            Box(Modifier.padding(16.dp).fillMaxWidth(), Alignment.Center) { bottomPanelContent() }
        }
    }
}

/**
 * A scaffold for compact screens where panels are shown one at a time at the bottom.
 * This composable is also "controlled," receiving state and reporting selection events.
 */
@Composable
fun CompactThreePanelScaffold(
    modifier: Modifier = Modifier,
    mainContentColor: Color,
    panelColor: Color,
    panelsState: PanelsState,
    onPanelSelect: (ActivePanel) -> Unit,
    mainContent: @Composable () -> Unit,
    leftPanelContent: @Composable () -> Unit,
    rightPanelContent: @Composable () -> Unit,
    bottomPanelContent: @Composable () -> Unit,
) {
    // Derives which single panel should be shown from the shared state.
    val activePanel: ActivePanel? = when {
        panelsState.isLeftPanelOpen -> ActivePanel.LEFT
        panelsState.isRightPanelOpen -> ActivePanel.RIGHT
        panelsState.isBottomPanelOpen -> ActivePanel.BOTTOM
        else -> null
    }
    val animationSpec = tween<Float>(300)
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = activePanel == ActivePanel.LEFT, onClick = { onPanelSelect(ActivePanel.LEFT) }, icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Left Panel") }, label = { Text("Left") })
                NavigationBarItem(selected = activePanel == ActivePanel.BOTTOM, onClick = { onPanelSelect(ActivePanel.BOTTOM) }, icon = { Icon(Icons.Default.KeyboardArrowUp, "Bottom Panel") }, label = { Text("Bottom") })
                NavigationBarItem(selected = activePanel == ActivePanel.RIGHT, onClick = { onPanelSelect(ActivePanel.RIGHT) }, icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Right Panel") }, label = { Text("Right") })
            }
        }
    ) { innerPadding ->
        // The innerPadding provided by Scaffold ensures our content is not obscured by the NavigationBar.
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Box(Modifier.weight(1f).fillMaxWidth().background(mainContentColor), Alignment.Center) { mainContent() }
            AnimatedVisibility(visible = activePanel != null, enter = fadeIn(animationSpec) + expandVertically(), exit = fadeOut(animationSpec) + shrinkVertically()) {
                Box(Modifier.fillMaxWidth().background(panelColor).padding(16.dp), Alignment.Center) {
                    when (activePanel) {
                        ActivePanel.LEFT -> leftPanelContent()
                        ActivePanel.RIGHT -> rightPanelContent()
                        ActivePanel.BOTTOM -> bottomPanelContent()
                        null -> {}
                    }
                }
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