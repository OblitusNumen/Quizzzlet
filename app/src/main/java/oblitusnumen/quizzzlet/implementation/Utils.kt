package oblitusnumen.quizzzlet.implementation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp

fun String.equalsStripIgnoreCase(other: String) = this.lowercase().trim() == other.lowercase().trim()

@Composable
fun measureTextLine(style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val linePx = remember(textMeasurer, style) {
        textMeasurer.measure("0", style).size.height
    }
    return with(LocalDensity.current) { linePx.toDp() }
}