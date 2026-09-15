/*
 * KTalk fork: render the capture screen in the language the app is set to.
 *
 * Upstream resolves strings through the OS locale, so an app whose own language
 * setting differs from the device shows a mixed-language screen. The app knows its
 * language, so it passes it in and this wrapper makes `stringResource` follow it.
 */
package com.amplifyframework.ui.liveness.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
internal fun WithAppLanguage(languageTag: String?, content: @Composable () -> Unit) {
    if (languageTag.isNullOrBlank()) {
        content()
        return
    }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val localized = remember(languageTag, configuration) {
        val locale = Locale.forLanguageTag(languageTag)
        val updated = Configuration(configuration).apply { setLocale(locale) }
        context.createConfigurationContext(updated) to updated
    }
    CompositionLocalProvider(
        LocalContext provides localized.first,
        LocalConfiguration provides localized.second,
        content = content
    )
}
