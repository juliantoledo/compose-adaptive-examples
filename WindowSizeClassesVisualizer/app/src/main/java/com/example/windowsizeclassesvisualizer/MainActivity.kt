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

package com.example.windowsizeclassesvisualizer

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.WindowMetricsCalculator
import com.example.windowsizeclassesvisualizertest.ui.theme.WindowSizeClassesTestTheme

/**
 * Data class to hold the definition for a single Window Size Class breakpoint.
 * @param name The official name of the breakpoint (e.g., "Compact").
 * @param size The maximum dp size for this class. Use [androidx.compose.ui.unit.Dp.Infinity] for unbounded classes.
 * @param color The color used to represent this class in the visualization.
 */
data class WscBreakpoint(
    val name: String,
    val size: DpSize,
    val color: Color
)

/**
 * Defines the list of official WSC breakpoints we will visualize.
 * This list is ordered from smallest to largest, which is important for the filtering logic.
 */
val wscBreakpoints = listOf(
    WscBreakpoint("Compact", DpSize(600.dp, 480.dp), Color(0xFF007BFF)),
    WscBreakpoint("Medium", DpSize(840.dp, 900.dp), Color(0xFF5A00B3)),
    WscBreakpoint("Expanded", DpSize(1200.dp, Dp.Infinity), Color(0xFFB3005A)),
    WscBreakpoint("Large", DpSize(1600.dp, Dp.Infinity), Color(0xFFE64A19)),
    WscBreakpoint("Extra Large",
        DpSize(Dp.Infinity, Dp.Infinity),
        Color(0xFFC2185B)
    )
)

/**
 * Determines the Window Size Class name for a given dimension (width or height).
 * @param size The dp value of the dimension to classify.
 * @param forWidth A boolean to indicate if we are classifying width (true) or height (false).
 * @return The name of the matching WSC breakpoint.
 */
fun getWindowSizeClassName(size: Dp, forWidth: Boolean): String {
    val breakpoint = wscBreakpoints.firstOrNull {
        val dimensionSize = if (forWidth) it.size.width else it.size.height
        size < dimensionSize
    }
    // If a size is larger than all defined finite breakpoints, it falls into the last category.
    return breakpoint?.name ?: wscBreakpoints.last().name
}


/**
 * The virtual canvas size provides a stable, large area to scale our breakpoints against,
 * ensuring the entire map is always visible in the "Full View" mode.
 */
private val VIRTUAL_CANVAS_WIDTH = 1800.dp
private val VIRTUAL_CANVAS_HEIGHT = 1100.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display for the app.
        enableEdgeToEdge()
        setContent {
            WindowSizeClassesTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WscVisualizer()
                }
            }
        }
    }
}

/**
 * The main screen composable. It manages state and composes the primary UI layers.
 */
@Composable
fun WscVisualizer() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var currentDeviceSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var showFullVisualization by rememberSaveable { mutableStateOf(true) }
    val systemBarInsets = WindowInsets.systemBars.asPaddingValues()
    var containerSize by remember { mutableStateOf<IntSize?>(null) }
    val density = LocalDensity.current

    // Recalculates the device's size whenever the configuration changes (e.g., on resize or rotation).
    LaunchedEffect(configuration) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val metrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
        val density = context.resources.displayMetrics.density
        currentDeviceSize = DpSize(
            width = (metrics.bounds.width() / density).dp,
            height = (metrics.bounds.height() / density).dp
        )
    }

    val widthClassName = getWindowSizeClassName(currentDeviceSize.width, forWidth = true)
    val heightClassName = getWindowSizeClassName(currentDeviceSize.height, forWidth = false)

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // --- LAYER 1: The Visualization Canvas (Bottom Layer) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    containerSize = it
                }
        ) {
            containerSize?.let { size ->
                val containerWidth = with(density) { size.width.toDp() }
                val containerHeight = with(density) { size.height.toDp() }
                WscCanvas(
                    containerWidth = containerWidth,
                    containerHeight = containerHeight,
                    currentDeviceSize = currentDeviceSize,
                    systemBarInsets = systemBarInsets,
                    showFullVisualization = showFullVisualization
                )
            }
        }

        // --- LAYER 2: The Title & Control Block (Top Layer) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = systemBarInsets.calculateTopPadding()) // Use system insets for safe padding
                .padding(top = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "WSC Breakpoint Visualization",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Your Device: ${currentDeviceSize.width.value.toInt()}dp x ${currentDeviceSize.height.value.toInt()}dp",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "Class: W: $widthClassName, H: $heightClassName",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { showFullVisualization = !showFullVisualization }) {
                Text(if (showFullVisualization) "Fit to Device" else "Show Full View")
            }
        }
    }
}

/**
 * A stateless composable responsible for drawing the WSC breakpoint rectangles and markers.
 *
 * @param containerWidth The maximum width available for drawing.
 * @param containerHeight The maximum height available for drawing.
 * @param currentDeviceSize The actual size of the current device in Dp.
 * @param systemBarInsets The padding values needed to avoid the system bars.
 * @param showFullVisualization A boolean that controls whether to show all breakpoints or only those relevant to the device.
 */
@Composable
fun WscCanvas(
    containerWidth: Dp,
    containerHeight: Dp,
    currentDeviceSize: DpSize,
    systemBarInsets: PaddingValues,
    showFullVisualization: Boolean
) {
    // --- Filtering Logic ---
    val breakpointsToDraw = if (showFullVisualization) {
        wscBreakpoints
    } else {
        val categoryIndex = wscBreakpoints.indexOfFirst {
            // A device "fits" in the first category that is larger than it in both dimensions.
            (currentDeviceSize.width < it.size.width || it.size.width == Dp.Infinity) &&
                    (currentDeviceSize.height < it.size.height || it.size.height == Dp.Infinity)
        }
        if (categoryIndex != -1) {
            wscBreakpoints.take(categoryIndex + 1)
        } else {
            wscBreakpoints
        }
    }

    // --- Scaling Logic ---
    val virtualWidth = if (showFullVisualization) VIRTUAL_CANVAS_WIDTH else currentDeviceSize.width
    val virtualHeight = if (showFullVisualization) VIRTUAL_CANVAS_HEIGHT else currentDeviceSize.height

    Box(
        modifier = Modifier
            .size(containerWidth, containerHeight)
            .background(Color.Black.copy(alpha = 0.05f))
    ) {
        // Draw each breakpoint rectangle, from largest to smallest, so they stack correctly.
        val reversedBreakpoints = breakpointsToDraw.asReversed()
        reversedBreakpoints.forEachIndexed { index, breakpoint ->
            val rectWidth =
                if (breakpoint.size.width != Dp.Infinity) containerWidth * (breakpoint.size.width / virtualWidth) else containerWidth
            val rectHeight =
                if (breakpoint.size.height != Dp.Infinity) containerHeight * (breakpoint.size.height / virtualHeight) else containerHeight

            Box(
                modifier = Modifier
                    .size(rectWidth, rectHeight)
                    .alpha(0.8f)
                    .background(breakpoint.color.copy(alpha = 0.2f))
                    .border(BorderStroke(2.dp, breakpoint.color)),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Format the label text, handling infinity cases.
                val widthText =
                    if (breakpoint.size.width != Dp.Infinity) "W: ${breakpoint.size.width.value.toInt()}dp" else "W: \u221E"
                val heightText =
                    if (breakpoint.size.height != Dp.Infinity) "H: ${breakpoint.size.height.value.toInt()}dp" else "H: \u221E"

                Text(
                    text = "${breakpoint.name}\n$widthText\n$heightText",
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        // Apply padding conditionally based on the item's position in the reversed list.
                        .then(
                            when (index) {
                                // For the outermost box (index 0), apply standard system insets.
                                0 -> Modifier.padding(systemBarInsets)

                                // For the second box, apply extra bottom padding only if the boxes are horizontally very close.
                                1 -> Modifier
                                    .padding(systemBarInsets)
                                    .then(
                                        if (containerWidth - rectWidth < 80.dp) Modifier.padding(
                                            bottom = 56.dp
                                        ) else Modifier
                                    )

                                2 -> Modifier.padding(systemBarInsets)

                                // All other items get no special system padding.
                                else -> Modifier
                            }
                        )
                        .padding(12.dp), // Apply our own additional padding to all items.
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.7f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }

        // --- "Your Device" Marker ---
        if (currentDeviceSize.width > 0.dp) {
            val markerX = containerWidth * (currentDeviceSize.width / virtualWidth)
            val markerY = containerHeight * (currentDeviceSize.height / virtualHeight)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            // Vertical Dotted Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .offset(x = markerX)
                    .drawBehind {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            pathEffect = pathEffect
                        )
                    })

            // Horizontal Dotted Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = markerY)
                    .drawBehind {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = pathEffect
                        )
                    })

            Text(
                "Your Device",
                modifier = Modifier
                    .offset(x = markerX + 4.dp, y = markerY + 4.dp)
                    .background(Color.Red.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Standard preview composable for Android Studio.
 */
@Preview(showBackground = true, widthDp = 840, heightDp = 900)
@Composable
fun VisualizerPreview() {
    WindowSizeClassesTestTheme {
        Surface {
            WscVisualizer()
        }
    }
}
