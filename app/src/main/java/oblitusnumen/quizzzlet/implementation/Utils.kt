package oblitusnumen.quizzzlet.implementation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun String.equalsStripIgnoreCase(other: String) = this.lowercase().trim() == other.lowercase().trim()

@Composable
fun measureTextLine(style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val linePx = remember(textMeasurer, style) {
        textMeasurer.measure("0", style).size.height
    }
    return with(LocalDensity.current) { linePx.toDp() }
}

fun inputStreamFromZip(zipInputStream: InputStream, filePath: String): InputStream? {
    val zipStream = ZipInputStream(zipInputStream)
    var entry: ZipEntry?
    while (zipStream.nextEntry.also { entry = it } != null) {
        if (entry?.name == filePath) {
            return zipStream
        }
    }
    zipStream.close()
    return null
}

fun extractZip(zipInputStream: InputStream, filePaths: Collection<String>, outputDir: File) {
    val zipStream = ZipInputStream(zipInputStream)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    zipStream.use { zipInputStream1 ->
        var entry: ZipEntry?
        while (zipInputStream1.nextEntry.also { entry = it } != null) {
            if (filePaths.contains(entry?.name)) {
                entry?.let {
                    val outFile = File(outputDir, it.name)
                    if (it.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.outputStream().use { fileOutputStream ->
                            zipInputStream1.copyTo(fileOutputStream)
                            fileOutputStream.flush()
                            fileOutputStream.fd.sync()
                        }
                    }
                    zipInputStream1.closeEntry()
                }
            }
        }
    }
}
