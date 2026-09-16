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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.amplifyframework.ui.liveness.R

@Composable
internal fun KTalkBackButton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Box(
        modifier = modifier
            .size(
                width = KTalkCaptureStyle.backButtonWidth,
                height = KTalkCaptureStyle.navBarHeight
            )
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onBack),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            painter = painterResource(R.drawable.amplify_ui_liveness_ktalk_back),
            contentDescription = stringResource(
                R.string.amplify_ui_liveness_challenge_a11y_back_content_description
            ),
            tint = KTalkCaptureStyle.backIconColor,
            modifier = Modifier
                .padding(start = KTalkCaptureStyle.sideMargin)
                .size(KTalkCaptureStyle.backIconSize)
        )
    }
}
