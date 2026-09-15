/*
 * KTalk fork: the capture screen accent follows the mode the user is in.
 *
 * Signup has no mode yet and stays with the local orange. Changing a profile photo
 * happens inside a mode, so the capture screen wears that mode's color like the
 * screens around it.
 */
package com.amplifyframework.ui.liveness.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalKTalkAccent = compositionLocalOf { KTalkCaptureStyle.accent }

/** Reads an ARGB int the host passes in; falls back to the default accent. */
internal fun accentOf(argb: Int?): Color =
    if (argb == null || argb == 0) KTalkCaptureStyle.accent else Color(argb)
