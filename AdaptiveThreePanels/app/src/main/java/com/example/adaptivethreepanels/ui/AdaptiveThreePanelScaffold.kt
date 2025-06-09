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

package com.example.adaptivethreepanels.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.adaptivethreepanels.model.ActivePanel
import com.example.adaptivethreepanels.model.PanelsState
import com.example.adaptivethreepanels.ui.theme.Grey20
import com.example.adaptivethreepanels.ui.theme.Grey40
import com.example.adaptivethreepanels.ui.theme.Grey60
import com.example.adaptivethreepanels.ui.theme.HandleColor

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
