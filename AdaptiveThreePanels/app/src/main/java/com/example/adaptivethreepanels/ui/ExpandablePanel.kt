/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.adaptivethreepanels.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single collapsible panel with a title, an animated icon, and themed content areas.
 *
 * @param title The text to display in the panel's header.
 * @param initiallyExpanded Sets the initial state of the panel.
 * @param headerBackgroundColor The background color of the header row.
 * @param headerTitleColor The color of the header's title text and icon.
 * @param contentAreaColor The background color of the collapsible content area.
 */
@Composable
fun ExpandablePanel(
    title: String,
    initiallyExpanded: Boolean = false,
    headerBackgroundColor: Color = MaterialTheme.colorScheme.secondary,
    headerTitleColor: Color = MaterialTheme.colorScheme.onSecondary,
    contentAreaColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    // Internal state for the sliders.
    var sliderValue1 by remember { mutableFloatStateOf(0f) }
    var sliderValue2 by remember { mutableFloatStateOf(0.5f) }
    var sliderValue3 by remember { mutableFloatStateOf(1f) }

    // Root container for a single panel, providing consistent horizontal padding.
    Column(modifier = Modifier.fillMaxWidth()) {
        // Clickable header that toggles the expansion state.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = headerTitleColor)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand/Collapse Icon",
                modifier = Modifier.rotate(rotationState),
                tint = headerTitleColor
            )
        }

        // Content area with a shaped background that appears on expansion.
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = contentAreaColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Slider(value = sliderValue1, onValueChange = { sliderValue1 = it })
                Slider(value = sliderValue2, onValueChange = { sliderValue2 = it })
                Slider(value = sliderValue3, onValueChange = { sliderValue3 = it })
            }
        }
    }
}

/**
 * Arranges multiple [ExpandablePanel] composables in a vertical, scrollable list.
 * This component requires a defined height from its parent to enable scrolling.
 *
 * @param panelTitles A list of strings to use for each panel's title.
 * @param modifier The modifier to be applied to the component's container.
 * @param headerBackgroundColor The background color passed to each panel's header.
 * @param headerTitleColor The color for the text and icon passed to each panel's header.
 * @param contentAreaColor The background color passed to each panel's collapsible content area.
 */
@Composable
fun PanelColumn(
    panelTitles: List<String>,
    modifier: Modifier = Modifier,
    headerBackgroundColor: Color = MaterialTheme.colorScheme.secondary,
    headerTitleColor: Color = MaterialTheme.colorScheme.onSecondary,
    contentAreaColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    val systemBarInsets = WindowInsets.systemBars.asPaddingValues()
    val listState = rememberLazyListState()

    // A LazyColumn is inherently scrollable. On Android, the scrollbar will appear
    // automatically when the user interacts with the list.
    LazyColumn(
        state = listState,
        modifier = modifier // Apply the incoming modifier from the parent first.
            .padding(top = systemBarInsets.calculateTopPadding(), end = 12.dp)
    ) {
        itemsIndexed(panelTitles) { index, title ->
            ExpandablePanel(
                title = title,
                // Expand specific panels for demonstration purposes.
                initiallyExpanded = (index == 3 || index == 6),
                // Pass down configured colors to each child panel.
                headerBackgroundColor = headerBackgroundColor,
                headerTitleColor = headerTitleColor,
                contentAreaColor = contentAreaColor
            )
        }
    }
}

/**
 * Previews the [PanelColumn] component in Android Studio.
 */
@Preview(showBackground = true, widthDp = 320)
@Composable
fun PanelColumnPreview() {
    // Provides a themed, full-screen host for previewing the component's alignment.
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val titles = listOf(
                "DISPLAY SETTINGS", "AUDIO & SOUND", "NETWORK", "STORAGE",
                "PERMISSIONS", "NOTIFICATIONS", "ADVANCED", "ABOUT", "HELP"
            )

            PanelColumn(
                panelTitles = titles,
                modifier = Modifier
                    .fillMaxSize() // The panel column now fills its parent
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp), // Add some padding for the preview
                headerBackgroundColor = Color.DarkGray,
                headerTitleColor = Color.White,
                contentAreaColor = Color.Gray
            )
        }
    }
}
