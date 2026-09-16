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

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amplifyframework.ui.liveness.ml.FaceDetector
import com.amplifyframework.ui.liveness.model.LivenessCheckState

/**
 * 도면의 「상태 안내」. 어떤 문구를 언제 띄울지는 [livenessCheckState] 가 정한다 —
 * 여기서는 색만 고른다.
 *
 * @param highlighted 가까이 오라는 안내처럼 사용자가 움직여야 할 때 강조색을 쓴다.
 */
@Composable internal fun InstructionMessage(
    livenessCheckState: LivenessCheckState,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val instructionText = livenessCheckState.instructionId?.let { stringResource(it) } ?: return
    val isTooClose = livenessCheckState.instructionId ==
        FaceDetector.FaceOvalPosition.TOO_CLOSE.instructionStringRes

    KTalkInstructionBubble(
        message = instructionText,
        containerColor = when {
            isTooClose -> KTalkCaptureStyle.errorColor
            highlighted -> LocalKTalkAccent.current
            else -> KTalkCaptureStyle.captureBubble
        },
        // 연결 중·확인 중처럼 사용자가 할 일이 없는 안내에만 회전 표시를 붙인다.
        showProgress = !livenessCheckState.isActionable,
        modifier = modifier
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun InstructionMessagePreview() {
    LivenessPreviewContainer {
        KTalkInstructionBubble(
            message = "얼굴을 타원 안에 맞춰주세요",
            containerColor = KTalkCaptureStyle.captureBubble,
            showProgress = false
        )
    }
}

@Preview
@Composable
private fun InstructionMessageHighlightedPreview() {
    LivenessPreviewContainer {
        KTalkInstructionBubble(
            message = "조금 더 가까이 와주세요",
            containerColor = Color(0xFFFF7A59),
            showProgress = false
        )
    }
}

@Preview
@Composable
private fun InstructionMessageProgressPreview() {
    LivenessPreviewContainer {
        KTalkInstructionBubble(
            message = "확인 중",
            containerColor = KTalkCaptureStyle.captureBubble,
            showProgress = true
        )
    }
}
