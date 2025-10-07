package student.projects.jetpackpam.localization


import androidx.compose.runtime.compositionLocalOf
import student.projects.jetpackpam.models.LanguageViewModel
import androidx.compose.runtime.Composable

val LocalLanguageViewModel = compositionLocalOf<LanguageViewModel> {
    error("No LanguageViewModel provided")
}

@Composable
fun t(key: String): String {
    val uiTexts = LocalLanguageViewModel.current.uiTexts
    return uiTexts[key] ?: key
}

