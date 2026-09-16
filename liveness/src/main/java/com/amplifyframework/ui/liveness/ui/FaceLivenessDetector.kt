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

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.amplifyframework.auth.AWSCredentials
import com.amplifyframework.auth.AWSCredentialsProvider
import com.amplifyframework.core.Action
import com.amplifyframework.core.Consumer
import com.amplifyframework.predictions.models.FaceLivenessChallengeType
import com.amplifyframework.predictions.models.FaceLivenessSession
import com.amplifyframework.ui.liveness.R
import com.amplifyframework.ui.liveness.camera.LivenessCoordinator
import com.amplifyframework.ui.liveness.camera.OnChallengeComplete
import com.amplifyframework.ui.liveness.media.VideoCodec
import com.amplifyframework.ui.liveness.ml.FaceDetector
import com.amplifyframework.ui.liveness.model.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.model.LivenessCheckState
import com.amplifyframework.ui.liveness.ui.helper.VideoViewportSize
import com.amplifyframework.ui.liveness.util.hasCameraPermission
import kotlinx.coroutines.launch

/**
 * @param sessionId of challenge
 * @param region AWS region to stream the video to. Current supported regions are listed in [add link here]
 * @param credentialsProvider to provide custom CredentialsProvider for authentication. Default uses initialized Amplify.Auth CredentialsProvider
 * @param disableStartView to bypass warmup screen.
 * @param onComplete callback notifying a completed challenge
 * @param onError callback containing exception for cause
 */
@Composable
fun FaceLivenessDetector(
    sessionId: String,
    region: String,
    credentialsProvider: AWSCredentialsProvider<AWSCredentials>? = null,
    disableStartView: Boolean = false,
    onComplete: Action,
    onError: Consumer<FaceLivenessDetectionException>
) = FaceLivenessDetector(
    sessionId,
    region,
    credentialsProvider,
    disableStartView,
    onComplete,
    onError,
    ChallengeOptions()
)

/**
 * @param sessionId of challenge
 * @param region AWS region to stream the video to. Current supported regions are listed in [add link here]
 * @param credentialsProvider to provide custom CredentialsProvider for authentication. Default uses initialized Amplify.Auth CredentialsProvider
 * @param disableStartView to bypass warmup screen.
 * @param challengeOptions is the list of ChallengeOptions that are to be overridden from the default configuration
 * @param videoCodec
 * @param onComplete callback notifying a completed challenge
 * @param onError callback containing exception for cause
 */
@Composable
fun FaceLivenessDetector(
    sessionId: String,
    region: String,
    credentialsProvider: AWSCredentialsProvider<AWSCredentials>? = null,
    disableStartView: Boolean = false,
    onComplete: Action,
    onError: Consumer<FaceLivenessDetectionException>,
    challengeOptions: ChallengeOptions = ChallengeOptions(),
    videoOptions: VideoOptions = VideoOptions(),
    // KTalk fork: BCP-47 tag of the app's own language setting. Null keeps the
    // upstream behaviour of following the device locale.
    languageTag: String? = null,
    // KTalk fork: ARGB accent for the oval stroke and the cancel button. Null uses
    // the default so signup keeps the local orange.
    accentArgb: Int? = null
) {
    val scope = rememberCoroutineScope()
    val key = DetectorStateKey(sessionId, region, credentialsProvider, videoOptions)
    var isFinished by remember(key) { mutableStateOf(false) }
    val currentOnComplete by rememberUpdatedState(onComplete)
    val currentOnError by rememberUpdatedState(onError)

    if (isFinished) {
        return
    }

    // fails challenge if no camera permission set
    if (!LocalContext.current.hasCameraPermission()) {
        LaunchedEffect(key) {
            isFinished = true
            currentOnError.accept(FaceLivenessDetectionException.CameraPermissionDeniedException())
        }
        return
    }

    // fails challenge if session ID is empty
    if (sessionId.isBlank()) {
        LaunchedEffect(key) {
            isFinished = true
            currentOnError.accept(
                FaceLivenessDetectionException.SessionNotFoundException("Session ID cannot be empty.")
            )
        }
        return
    }

    // Locks portrait orientation for duration of challenge and resets on complete
    LockPortraitOrientation { resetOrientation ->
        WithAppLanguage(languageTag) {
            CompositionLocalProvider(LocalKTalkAccent provides accentOf(accentArgb)) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AlwaysOnMaxBrightnessScreen()
                    ChallengeView(
                        key = key,
                        sessionId = sessionId,
                        region,
                        credentialsProvider = credentialsProvider,
                        disableStartView,
                        challengeOptions = challengeOptions,
                        videoOptions = videoOptions,
                        onChallengeComplete = {
                            scope.launch {
                                // if we are already finished, we already provided a result in complete or failed
                                if (!isFinished) {
                                    isFinished = true
                                    resetOrientation()
                                    currentOnComplete.call()
                                }
                            }
                        },
                        onChallengeFailed = {
                            scope.launch {
                                // if we are already finished, we already provided a result in complete or failed
                                if (!isFinished) {
                                    isFinished = true
                                    resetOrientation()
                                    currentOnError.accept(it)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun ChallengeView(
    key: Any,
    sessionId: String,
    region: String,
    credentialsProvider: AWSCredentialsProvider<AWSCredentials>?,
    disableStartView: Boolean,
    challengeOptions: ChallengeOptions,
    videoOptions: VideoOptions,
    onChallengeComplete: OnChallengeComplete,
    onChallengeFailed: Consumer<FaceLivenessDetectionException>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var coordinator by remember { mutableStateOf<LivenessCoordinator?>(null) }
    val currentOnChallengeComplete by rememberUpdatedState(onChallengeComplete)
    val currentOnChallengeFailed by rememberUpdatedState(onChallengeFailed)

    DisposableEffect(key) {
        try {
            coordinator = LivenessCoordinator(
                context,
                lifecycleOwner,
                sessionId,
                region,
                credentialsProvider,
                disableStartView,
                challengeOptions,
                videoOptions = videoOptions,
                onChallengeComplete = { currentOnChallengeComplete() },
                onChallengeFailed = { currentOnChallengeFailed.accept(it) }
            )
        } catch (e: Exception) {
            currentOnChallengeFailed.accept(
                FaceLivenessDetectionException(
                    message = "Failed to initialize video components required for Liveness check.",
                    throwable = e
                )
            )
        }

        onDispose {
            coordinator?.destroy(context)
        }
    }

    val livenessCoordinator = coordinator ?: return
    val livenessState = livenessCoordinator.livenessState

    // 준비 화면은 흰 배경, 촬영 화면은 검은 배경이다. 시스템 바 글자색을 맞추지
    // 않으면 시계가 배경에 묻힌다.
    SystemBarIcons(darkIcons = livenessState.showingStartView)

    val localDensity = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (livenessState.showingStartView) {
                    Color.White
                } else {
                    KTalkCaptureStyle.captureBackground
                }
            )
            // 배경은 시스템 바 아래까지 깔고 내용만 안쪽으로 넣는다. 호스트가
            // 콘텐츠 뷰에 여백을 주면 시스템 바 자리에 창 배경색이 드러난다.
            .windowInsetsPadding(WindowInsets.systemBars)
            .onGloballyPositioned {
                livenessState.updateVideoViewportSize(
                    VideoViewportSize.create(it.size, localDensity)
                )
            }
    ) {
        val videoViewportSize = livenessState.videoViewportSize

        if (videoViewportSize != null) {
            Box(
                modifier = Modifier
                    .size(videoViewportSize.viewportDpSize)
                    .align(Alignment.Center)
            ) {
                AndroidView(
                    { livenessCoordinator.previewTextureView },
                    Modifier
                        .size(videoViewportSize.viewportDpSize)
                        .align(Alignment.Center)
                )
            }

            if (livenessState.showingStartView) {
                GetReadyView(
                    videoViewportSize = videoViewportSize,
                    loadingCameraPreview = livenessState.loadingCameraPreview,
                    onBegin = { livenessState.onStartViewComplete() },
                    // The same cancel path as the close button during the challenge,
                    // so the socket closes with the user-cancelled code either way.
                    onBack = {
                        livenessCoordinator.processSessionError(
                            FaceLivenessDetectionException.UserCancelledException(),
                            true
                        )
                    }
                )
            } else {
                val checkState = livenessState.livenessCheckState
                // TOO_FAR / TOO_FAR_LEFT / TOO_FAR_RIGHT 이 같은 안내 문자열을 쓰고,
                // 얼굴이 사라졌을 때의 Running.withMoveFaceMessage() 도 같은 문자열을
                // 쓴다. 얼굴이 없는데 「절반쯤 왔어요」를 띄우지 않도록 faceDetected 를
                // 함께 본다.
                val showingProgress = livenessState.faceDetected &&
                    checkState.instructionId ==
                    FaceDetector.FaceOvalPosition.TOO_FAR.instructionStringRes
                val lightChallenge =
                    livenessState.livenessSessionInfo.isFaceMovementAndLightChallenge()

                // 확인 중에는 livenessState.faceGuideRect 가 비워지고 타원이
                // LivenessCheckState.Success 안으로 옮겨 간다. 그 값을 이어받아
                // 타원을 계속 그린다 — 얼굴이 보이는 채로 결과를 기다리게 한다.
                val faceGuideRect = livenessState.faceGuideRect
                    ?: (checkState as? LivenessCheckState.Success)?.faceGuideRect
                FaceGuide(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    // 서버가 타원을 알려주기 전에는 준비 화면과 같은 자리를
                    // 쓴다. 화면이 통짜 카메라로 보이지 않게만 하는 값이고,
                    // 판정에는 쓰이지 않는다.
                    faceGuideRect = faceGuideRect ?: PLACEHOLDER_FACE_GUIDE_RECT,
                    videoViewportSize = videoViewportSize,
                    backgroundColor = KTalkCaptureStyle.captureBackground,
                    strokeColor = KTalkCaptureStyle.captureOvalStroke,
                    strokeWidth = KTalkCaptureStyle.captureOvalStrokeWidth,
                    // 얼굴이 타원에 얼마나 맞았는지다. SDK 가 계산한 값을 그대로
                    // 테두리에 옮긴다. takeIf 로 쓰면 수신자를 먼저 읽어, 쓰지 않는
                    // 구간에도 매 프레임 재구성이 걸린다.
                    progress = if (showingProgress) livenessState.faceMatchPercentage else null,
                    progressColor = LocalKTalkAccent.current
                )

                if (livenessState.faceMatched) {
                    if (livenessState.livenessSessionInfo.isFaceMovementAndLightChallenge()) {
                        FreshnessChallenge(
                            key,
                            modifier = Modifier.fillMaxSize(),
                            colors = livenessState.colorChallenge!!.challengeColors,
                            onColorDisplayed = { currentColor, previousColor, sequenceNumber, colorStart ->
                                livenessCoordinator.processColorDisplayed(
                                    currentColor,
                                    previousColor,
                                    sequenceNumber,
                                    colorStart
                                )
                            },
                            onComplete = {
                                livenessCoordinator.processLivenessCheckComplete()
                            }
                        )
                    } else {
                        LaunchedEffect(key) {
                            livenessCoordinator.processLivenessCheckComplete()
                        }
                    }
                }

                KTalkRecordingBadge(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = KTalkCaptureStyle.sideMargin,
                            top = KTalkCaptureStyle.captureTopMargin
                        )
                )

                KTalkCloseButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            end = KTalkCaptureStyle.sideMargin -
                                KTalkCaptureStyle.closeTouchOverhang,
                            top = KTalkCaptureStyle.captureTopMargin -
                                KTalkCaptureStyle.closeTouchOverhang
                        )
                ) {
                    livenessCoordinator.processSessionError(
                        FaceLivenessDetectionException.UserCancelledException(),
                        true
                    )
                }

                if (shouldDisplayInstruction(
                        checkState,
                        livenessState.livenessSessionInfo?.challengeType
                    )
                ) {
                    InstructionMessage(
                        livenessCheckState = checkState,
                        highlighted = showingProgress,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                start = KTalkCaptureStyle.sideMargin,
                                end = KTalkCaptureStyle.sideMargin,
                                top = KTalkCaptureStyle.captureBubbleTop
                            )
                    )
                }

                // 연결 중·확인 중에는 보조 문구를 거둔다 — 사용자가 할 일이 없다.
                val hintRes = when {
                    !checkState.isActionable -> null
                    livenessState.faceMatched && lightChallenge ->
                        R.string.amplify_ui_liveness_challenge_capture_hint_light
                    showingProgress ->
                        R.string.amplify_ui_liveness_challenge_capture_hint_progress
                    else -> R.string.amplify_ui_liveness_challenge_capture_hint
                }
                if (hintRes != null) {
                    KTalkCaptureHint(
                        message = stringResource(hintRes),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(
                                start = KTalkCaptureStyle.sideMargin,
                                end = KTalkCaptureStyle.sideMargin,
                                bottom = KTalkCaptureStyle.captureHintBottom
                            )
                    )
                }
            }
        }
    }
}

/**
 * 준비 화면과 촬영 시작 직후가 쓰는 타원. 480x640 미리보기 기준으로 upstream 이
 * "as specified by science" 라고 적어 둔 값이다. 서버가 타원을 알려주면 그 값으로
 * 바뀐다.
 */
private val PLACEHOLDER_FACE_GUIDE_RECT = RectF(120f, 126f, 360f, 514f)

internal data class DetectorStateKey(
    val sessionId: String,
    val region: String,
    val credentialsProvider: AWSCredentialsProvider<AWSCredentials>?,
    val videoOptions: VideoOptions
)

data class ChallengeOptions(
    val faceMovementAndLight: LivenessChallenge.FaceMovementAndLight = LivenessChallenge.FaceMovementAndLight,
    val faceMovement: LivenessChallenge.FaceMovement = LivenessChallenge.FaceMovement()
) {
    internal fun getLivenessChallenge(challengeType: FaceLivenessChallengeType): LivenessChallenge =
        when (challengeType) {
            FaceLivenessChallengeType.FaceMovementAndLightChallenge -> faceMovementAndLight
            FaceLivenessChallengeType.FaceMovementChallenge -> faceMovement
        }

    /**
     * @return true if all of the challenge options are configured to use the same camera configuration
     */
    internal fun hasOneCameraConfigured(): Boolean =
        listOf(
            faceMovementAndLight,
            faceMovement
        ).all { it.camera == faceMovementAndLight.camera }
}

data class VideoOptions(
    val codec: VideoCodec = VideoCodec.VP8
)

sealed class LivenessChallenge(
    open val camera: Camera = Camera.Front
) {
    data class FaceMovement(override val camera: Camera = Camera.Front) : LivenessChallenge(
        camera = camera
    )
    data object FaceMovementAndLight : LivenessChallenge()
}

sealed class Camera {
    data object Front : Camera()
    data object Back : Camera()
}

private fun FaceLivenessSession?.isFaceMovementAndLightChallenge(): Boolean =
    this?.challengeType == FaceLivenessChallengeType.FaceMovementAndLightChallenge

private fun shouldDisplayInstruction(
    livenessCheckState: LivenessCheckState,
    challengeType: FaceLivenessChallengeType?
): Boolean =
    if (challengeType == null) {
        true
    } else if (livenessCheckState ==
        LivenessCheckState.Running.withFaceOvalPosition(FaceDetector.FaceOvalPosition.MATCHED) &&
        challengeType == FaceLivenessChallengeType.FaceMovementChallenge
    ) {
        false
    } else {
        true
    }

/** KTalk preparation screen; camera and session lifecycle remain in ChallengeView. */
@Composable
internal fun GetReadyView(
    videoViewportSize: VideoViewportSize,
    loadingCameraPreview: Boolean,
    onBegin: () -> Unit,
    onBack: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Keep the hint below the SDK oval on compact displays.
        val hintToButton = if (maxHeight < 700.dp) 24.dp else KTalkCaptureStyle.hintToButton
        if (loadingCameraPreview) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
            )
        }

        FaceGuide(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            faceGuideRect = PLACEHOLDER_FACE_GUIDE_RECT,
            videoViewportSize = videoViewportSize,
            backgroundColor = Color.White,
            strokeColor = LocalKTalkAccent.current
        )

        Column(modifier = Modifier.align(Alignment.TopStart)) {
            KTalkBackButton(onBack = onBack)

            Column(
                modifier = Modifier.padding(
                    start = KTalkCaptureStyle.sideMargin,
                    end = KTalkCaptureStyle.sideMargin,
                    top = KTalkCaptureStyle.titleTopFromNavBar
                ),
                verticalArrangement = Arrangement.spacedBy(
                    KTalkCaptureStyle.titleToDescription
                )
            ) {
                Text(
                    text = stringResource(R.string.amplify_ui_liveness_challenge_title),
                    style = KTalkCaptureStyle.title
                )
                Text(
                    text = stringResource(
                        R.string.amplify_ui_liveness_challenge_description
                    ),
                    style = KTalkCaptureStyle.description
                )
            }
        }

        // KTalk fork: 보조 문구와 시작 버튼. 버튼의 동작은 그대로
        // onStartViewComplete 다.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = KTalkCaptureStyle.sideMargin,
                    end = KTalkCaptureStyle.sideMargin,
                    bottom = KTalkCaptureStyle.buttonBottomMargin
                ),
            verticalArrangement = Arrangement.spacedBy(
                hintToButton
            )
        ) {
            Text(
                text = stringResource(R.string.amplify_ui_liveness_challenge_hint),
                style = KTalkCaptureStyle.hint
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(KTalkCaptureStyle.buttonHeight),
                shape = RoundedCornerShape(KTalkCaptureStyle.buttonCorner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalKTalkAccent.current
                ),
                onClick = onBegin
            ) {
                Text(
                    text = stringResource(
                        R.string.amplify_ui_liveness_get_ready_begin_check
                    ),
                    style = KTalkCaptureStyle.buttonLabel
                )
            }
        }
    }
}
