package com.amplifyframework.ui.liveness.ui

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.amplifyframework.ui.liveness.ml.FaceDetector
import com.amplifyframework.ui.liveness.model.LivenessCheckState
import com.amplifyframework.ui.testing.ComposeTest
import com.amplifyframework.ui.testing.ScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.annotation.Config

class CaptureInstructionsTest : ComposeTest() {
    @Test
    @ScreenshotTest
    @Config(qualifiers = "w375dp-h667dp-mdpi")
    fun instructionsStayReadableAndBackCancels() {
        var cancellations = 0
        composeTestRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                WithAppLanguage("en") {
                    val density = LocalDensity.current
                    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.5f)) {
                        Box(Modifier.fillMaxSize().background(Color.White)) {
                            KTalkBackButton { cancellations++ }
                            Column(
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                InstructionMessage(LivenessCheckState.Running.withMoveFaceMessage())
                                InstructionMessage(
                                    LivenessCheckState.Running.withFaceOvalPosition(
                                        FaceDetector.FaceOvalPosition.TOO_CLOSE
                                    )
                                )
                                InstructionMessage(LivenessCheckState.Success(RectF()))
                            }
                        }
                    }
                }
            }
        }
        for (message in listOf("Move closer", "Move back", "Verifying")) {
            composeTestRule.onNodeWithText(message).assertIsDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, cancellations)
    }
}
