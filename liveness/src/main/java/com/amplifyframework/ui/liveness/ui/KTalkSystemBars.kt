/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.ui.liveness.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.amplifyframework.ui.liveness.util.findActivity

/**
 * KTalk fork: 시스템 바 글자색을 화면 배경에 맞춘다.
 *
 * 준비 화면은 흰 배경이라 어두운 글자가, 촬영 화면은 검은 배경이라 밝은 글자가
 * 읽힌다. 화면이 시스템 바 아래까지 그려지므로 이 값을 맞추지 않으면 시계가
 * 배경에 묻힌다.
 *
 * @param darkIcons true 면 어두운 글자 — 밝은 배경에서 쓴다.
 */
@Composable
internal fun SystemBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current

    DisposableEffect(darkIcons) {
        val window = context.findActivity()?.window
            ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previousStatusBar = controller.isAppearanceLightStatusBars
        val previousNavigationBar = controller.isAppearanceLightNavigationBars

        controller.isAppearanceLightStatusBars = darkIcons
        controller.isAppearanceLightNavigationBars = darkIcons

        onDispose {
            controller.isAppearanceLightStatusBars = previousStatusBar
            controller.isAppearanceLightNavigationBars = previousNavigationBar
        }
    }
}
