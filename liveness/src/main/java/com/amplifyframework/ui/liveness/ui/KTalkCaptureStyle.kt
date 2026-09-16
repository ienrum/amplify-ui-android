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
    // 담긴 굵기는 W400 과 W700 둘뿐이다. 도면의 Medium(W500)·SemiBold(W600) 은
    // 가장 가까운 값으로 떨어진다 — W500 은 Regular, W600 은 Bold 로 그려진다.
    val fontFamily = FontFamily(
        Font(R.font.pretendard_regular, FontWeight.W400),
        Font(R.font.pretendard_bold, FontWeight.W700)
    )

    // app: CommonColors.gray1E / gray73 / orangeFF
    val titleColor = Color(0xFF1E1E1E)
    val descriptionColor = Color(0xFF737373)
    val errorColor = Color(0xFFC84646) // app: CommonColors.redC8
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

    // ── 촬영 중 화면(DSN-APP-IDT-0007-06). 도면 좌표는 390x844 프레임이고 그 안에
    // 상태 표시줄 54 가 들어 있다. 여기 값은 상태 표시줄을 뺀 뒤의 여백이다.
    // 도면은 카메라 위에 rgba(9,7,6,0.84) 를 덮지만, 촬영 화면은 타원 밖을
    // 아예 가린다. 같은 색을 불투명으로 쓴다.
    val captureBackground = Color(0xFF090706)
    val capturePill = Color(0x8C120E0C) // rgba(18,14,12,0.55)
    val captureBubble = Color(0xBD120E0C) // rgba(18,14,12,0.74)
    val captureOvalStroke = Color(0xE0FFFFFF)
    val recordingDotColor = Color(0xFFFF4B3E)
    val captureHintColor = Color(0xC7FFFFFF) // rgba(255,255,255,0.78)

    val captureTopMargin = 6.dp // 도면 y=60 - 상태 표시줄 54
    val captureBubbleTop = 64.dp // 도면 y=118 - 상태 표시줄 54
    val captureHintBottom = 55.dp // 도면 y=734 아래 여백에서 홈 인디케이터 34 를 뺀 값
    val recordingDotSize = 8.dp
    val recordingGap = 7.dp
    val recordingPadStart = 11.dp
    val recordingPadEnd = 13.dp
    val recordingPadVertical = 7.dp
    val closeButtonSize = 38.dp
    // 도면보다 넓힌 터치 영역. 원의 자리는 그대로 두려고 여백에서 넓힌 만큼 뺀다.
    val closeTouchTarget = 48.dp
    val closeTouchOverhang = (closeTouchTarget - closeButtonSize) / 2
    val closeIconSize = 13.dp
    val closeIconStroke = 2.dp
    val bubblePadHorizontal = 20.dp
    val bubblePadVertical = 12.dp
    val captureOvalStrokeWidth = 3.dp

    // 진행 표시는 타원 바깥으로 4dp 만큼 나간다(도면의 252x334 대 244x326).
    // 타원 좌표 자체는 SDK 값 그대로 쓴다.
    val progressStrokeWidth = 4.dp
    val progressInset = 2.dp

    val recordingLabel = TextStyle(
        fontFamily = fontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.W500,
        lineHeight = 20.sp,
        color = Color.White
    )
    val bubbleLabel = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.W600,
        lineHeight = 24.sp,
        color = Color.White
    )
    val captureHint = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 21.sp,
        color = captureHintColor
    )
}
