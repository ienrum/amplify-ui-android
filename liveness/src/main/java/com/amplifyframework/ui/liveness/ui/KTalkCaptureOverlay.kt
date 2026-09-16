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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amplifyframework.ui.liveness.R

/*
 * KTalk fork: 촬영 중 화면의 겉모습(DSN-APP-IDT-0007-06).
 *
 * 여기 있는 것은 표시뿐이다. 타원 좌표와 얼굴 판정, 색광의 순서·시간은 모두 SDK 가
 * 정한 값을 그대로 읽어 그린다.
 */

/** 도면의 「녹화 표시」. 촬영이 시작된 뒤에만 보인다. */
@Composable
internal fun KTalkRecordingBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(KTalkCaptureStyle.capturePill)
            .padding(
                start = KTalkCaptureStyle.recordingPadStart,
                end = KTalkCaptureStyle.recordingPadEnd,
                top = KTalkCaptureStyle.recordingPadVertical,
                bottom = KTalkCaptureStyle.recordingPadVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(KTalkCaptureStyle.recordingDotSize)
                .clip(CircleShape)
                .background(KTalkCaptureStyle.recordingDotColor)
        )
        Spacer(modifier = Modifier.size(KTalkCaptureStyle.recordingGap))
        Text(
            text = stringResource(R.string.amplify_ui_liveness_challenge_recording_indicator_label),
            style = KTalkCaptureStyle.recordingLabel
        )
    }
}

/** 도면의 「취소」. 동작은 촬영 화면이 쓰던 사용자 취소 그대로다. */
@Composable
internal fun KTalkCloseButton(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val label = stringResource(
        R.string.amplify_ui_liveness_challenge_a11y_cancel_content_description
    )
    // 도면의 원은 38dp 지만 손가락이 닿는 곳은 48dp 로 둔다 — 화면에서 유일한
    // 취소 수단이다.
    Box(
        modifier = modifier
            .size(KTalkCaptureStyle.closeTouchTarget)
            .clickable(role = Role.Button, onClick = onClose)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(KTalkCaptureStyle.closeButtonSize)
                .clip(CircleShape)
                .background(KTalkCaptureStyle.capturePill),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(KTalkCaptureStyle.closeIconSize)) {
                val stroke = KTalkCaptureStyle.closeIconStroke.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** 도면의 「상태 안내」 말풍선. 문구는 SDK 가 고른 안내를 그대로 쓴다. */
@Composable
internal fun KTalkInstructionBubble(
    message: String,
    containerColor: Color,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(
                horizontal = KTalkCaptureStyle.bubblePadHorizontal,
                vertical = KTalkCaptureStyle.bubblePadVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.size(KTalkCaptureStyle.recordingGap))
        }
        Text(
            text = message,
            style = KTalkCaptureStyle.bubbleLabel,
            textAlign = TextAlign.Center
        )
    }
}

/** 도면의 「보조 문구」. */
@Composable
internal fun KTalkCaptureHint(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = KTalkCaptureStyle.captureHint,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}
