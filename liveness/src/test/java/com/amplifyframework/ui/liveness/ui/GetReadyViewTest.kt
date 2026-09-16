package com.amplifyframework.ui.liveness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.IntSize
import com.amplifyframework.ui.liveness.ui.helper.VideoViewportSize
import com.amplifyframework.ui.testing.ComposeTest
import com.amplifyframework.ui.testing.ScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.annotation.Config

class GetReadyViewTest : ComposeTest() {
    @Test
    @ScreenshotTest
    fun readyScreenInDarkMode() = checkReadyScreen(Color(0xFFFF7A59))

    @Test
    @ScreenshotTest
    @Config(qualifiers = "w375dp-h667dp-mdpi")
    fun readyScreenOnSmallDisplay() = checkReadyScreen(Color(0xFF6759FF))

    private fun checkReadyScreen(accent: Color) {
        var starts = 0
        var backs = 0
        composeTestRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                WithAppLanguage("en") {
                    CompositionLocalProvider(LocalKTalkAccent provides accent) {
                        val config = LocalConfiguration.current
                        val density = LocalDensity.current
                        val size = IntSize(
                            (config.screenWidthDp * density.density).toInt(),
                            (config.screenHeightDp * density.density).toInt()
                        )
                        // The blank preview stands in for the camera, not the ready UI.
                        Box(Modifier.fillMaxSize().background(Color.Black)) {
                            GetReadyView(
                                videoViewportSize = VideoViewportSize.create(size, density),
                                loadingCameraPreview = false,
                                onBegin = { starts++ },
                                onBack = { backs++ }
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.onAllNodesWithText("Center your face on the screen").assertCountEquals(1)
        composeTestRule.onNodeWithText("Photosensitivity warning", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Center your face", substring = false).assertDoesNotExist()
        composeTestRule.onNodeWithText("Start video check").performClick()
        assertEquals(1, starts)
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backs)
    }
}
