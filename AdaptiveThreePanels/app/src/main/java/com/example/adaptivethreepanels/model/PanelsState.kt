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

package com.example.adaptivethreepanels.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
