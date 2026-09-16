/*
 * KTalk fork: capture screen design values.
 *
 * Geometry comes from the design frame DSN-APP-IDT-0007-06 (390x844). Typography
 * and colors come from the app's own tokens so the native screen reads like the
 * Flutter screens around it.
 *
 * The oval keeps upstream's size, so the gaps above and below it follow the SDK
 * rather than the frame.
 */
package com.amplifyframework.ui.liveness.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amplifyframework.ui.liveness.R

internal object KTalkCaptureStyle {
    val fontFamily = FontFamily(
        Font(R.font.pretendard_regular, FontWeight.W400),
        Font(R.font.pretendard_bold, FontWeight.W700)
    )

    // app: CommonColors.gray1E / gray73 / orangeFF
    val titleColor = Color(0xFF1E1E1E)
    val descriptionColor = Color(0xFF737373)
    val accent = Color(0xFFFF7A59)

    // app: DesignPageTitle
    val title = TextStyle(
        fontFamily = fontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.W700,
        color = titleColor
    )
    val description = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 21.sp,
        color = descriptionColor
    )
    val hint = description
    val buttonLabel = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.W700,
        color = Color.White
    )

    // app: the back chevron stroke in assets/icons/arrow/24-chevron-left.svg
    val backIconColor = Color(0xFF4E4E4E)

    // frame values
    val sideMargin = 20.dp
    val navBarHeight = 44.dp
    val backButtonWidth = 52.dp
    val backIconSize = 24.dp
    val titleTopFromNavBar = 40.dp
    val titleToDescription = 4.dp
    val hintToButton = 75.dp
    val buttonHeight = 64.dp
    val buttonBottomMargin = 34.dp
    val buttonCorner = 8.dp
}
