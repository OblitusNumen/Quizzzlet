package oblitusnumen.quizzzlet.implementation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import oblitusnumen.quizzzlet.ui.model.question.OrderQuestionState
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

@Composable
fun screenHeightInPixels(): Int {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val density = LocalDensity.current.density

    return (screenHeightDp * density).toInt()
}

fun Modifier.modifyConditionally(condition: Boolean, modifier: (Modifier) -> Modifier): Modifier {
    return if (condition)
        modifier(this)
    else
        this
}

fun Modifier.dragToReorder(
    element: String,
    elementIndex: Int,
    questionState: OrderQuestionState,
    scrollState: LazyListState,
    absoluteYLocation: MutableState<Float>,
): Modifier = composed {
//    val context = LocalContext.current
//    var fingerPosition by remember { mutableStateOf(0f) }
//    val screenHeight = screenHeightInPixels()
    // Remember the total scroll offset
//    val totalScrollOffset by remember {
//        derivedStateOf {
//            val firstVisibleItemIndex = scrollState.firstVisibleItemIndex
//            val firstVisibleItemOffset = scrollState.firstVisibleItemScrollOffset
//            // Assuming each item has a fixed height (e.g., 50.dp)
//            val itemHeightPx = 50.dp.toPx() // Convert DP to pixels
//            (firstVisibleItemIndex * itemHeightPx + firstVisibleItemOffset).toInt()
//        }
//    }

//    val startOffset = remember { scrollState.layoutInfo.viewportStartOffset }
//    var cumulativeOffset = remember { Animatable(0F) }
//    LaunchedEffect(scrollState.isScrollInProgress) {
////        Log.e("qqqqqqqqqqqqqqq", "viewportStartOffset: ${scrollState.layoutInfo.viewportStartOffset}, initialOffset: $startOffset")
////        cumulativeOffset = scrollState.layoutInfo.viewportStartOffset - startOffset
//    }
//    LaunchedEffect(questionState.draggedItemIndex.value) {
//        launch {
//            while (questionState.draggedItemIndex.value == elementIndex) {
////                Log.e("qqqqqqqqqqqqqqqq", "fingerPosition: $fingerPosition")
//                val initScrollOffset = screenHeight / 5
//                if (fingerPosition < initScrollOffset) {
//                    scrollState.animateScrollBy(-50f, tween(50))
//                    cumulativeOffset.snapTo(cumulativeOffset.value - 50)
//                } else if (fingerPosition > screenHeight - initScrollOffset) {
//                    scrollState.animateScrollBy(50f, tween(50))
//                    cumulativeOffset.snapTo(cumulativeOffset.value + 50)
//                }
//                delay(8) // not Smooth scrolling every 8ms (125fps)
//            }
//        }
//    }
    val onStopDrag: (destinationIndex: Int) -> Unit = { dIndex ->
        questionState.draggedItemIndex.value = null
        questionState.order.removeAt(elementIndex)
        questionState.order.add(dIndex, element)
        questionState.resetSlide()
    }
    val onStartDrag: () -> Unit = { questionState.draggedItemIndex.value = elementIndex }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            var numberOfItems = 0
            var previousNumberOfItems: Int
            var listOffset = 0

            val onDragStart = {
                // Interrupt any ongoing animation of other items.
                launch {
                    offsetX.stop()
                    offsetY.stop()
                }
                onStartDrag()
            }
            val onDrag = { change: PointerInputChange ->
                val horizontalDragOffset = offsetX.value + change.positionChange().x
                launch {
                    offsetX.snapTo(horizontalDragOffset)
                }
                val verticalDragOffset = offsetY.value + change.positionChange().y
                launch {
                    offsetY.snapTo(verticalDragOffset)
                    previousNumberOfItems = numberOfItems
                    numberOfItems = calculateNumberOfSlidItems(offsetY.value, questionState)
                    if (previousNumberOfItems.absoluteValue > numberOfItems.absoluteValue) {
                        questionState.setSlideState(
                            questionState.order[elementIndex + previousNumberOfItems],
                            OrderQuestionState.SlideState.NONE
                        )
                    } else if (numberOfItems - previousNumberOfItems != 0) {
                        questionState.setSlideState(
                            questionState.order[elementIndex + numberOfItems],
                            if (numberOfItems < 0)
                                OrderQuestionState.SlideState.UP
                            else
                                OrderQuestionState.SlideState.DOWN
                        )
                    }
                    listOffset = numberOfItems
                }
                // Consume the gesture event, not passed to external
                if (change.positionChange() != Offset.Zero) change.consume()
            }
            val onDragEnd = {
                launch {
                    offsetX.animateTo(0f)
                }
                launch {
                    offsetY.animateTo(
                        offsetBySlidItemNumber(
                            numberOfItems,
                            questionState
                        ).toFloat()
                    )
                    onStopDrag(elementIndex + listOffset)
                }
            }
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    if (questionState.draggedItemIndex.value == null)
                        onDragStart()
                },
                onDrag = { change, _ ->
                    if (questionState.draggedItemIndex.value == elementIndex) {
//                        // Get the Android View to calculate the screen position
//                        val rootView = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
//                        val view = (rootView as? FrameLayout)?.getChildAt(0) // Get the main root view
//                        // Calculate the global screen position (absolute) using getLocationOnScreen
//                        val location = IntArray(2)
//                        view?.getLocationOnScreen(location)
//                        // The absolute position on the screen
//                        fingerPosition = change.position.y + absoluteYLocation.value
//                        Log.e("qqqqqqqqqqqqq", "fingerPosition: $fingerPosition, posChange: ${change.position.y}, " +
//                                "absoluteY: ${absoluteYLocation.value}")
                        onDrag(change)
                    }
                },
                onDragEnd = {
                    if (questionState.draggedItemIndex.value == elementIndex)
                        onDragEnd()
                }
            )
        }
    }.offset {
        // Use the animating offset value here.
        IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
    }
}

private fun offsetBySlidItemNumber(
    numberOfSlidItems: Int,
    questionState: OrderQuestionState,
): Int {
    var leftoverNumber = numberOfSlidItems
    var offsetY = 0
    if (leftoverNumber < 0) {
        while (leftoverNumber != 0) {
            val idx = questionState.draggedItemIndex.value!! + numberOfSlidItems
            val itemHeight = questionState.getItemHeight(questionState.order[idx])
            leftoverNumber++
            offsetY -= itemHeight
        }
    } else {
        while (leftoverNumber != 0) {
            val idx = questionState.draggedItemIndex.value!! + numberOfSlidItems
            val itemHeight = questionState.getItemHeight(questionState.order[idx])
            leftoverNumber--
            offsetY += itemHeight
        }
    }
    return offsetY
}

private fun calculateNumberOfSlidItems(
    offsetY: Float,
    questionState: OrderQuestionState,
): Int {
    var leftoverOffset = offsetY.absoluteValue
    var numberOfSlidItems = 0
    if (offsetY < 0) {
        while (true) {
            val idx = questionState.draggedItemIndex.value!! + numberOfSlidItems
            if (idx <= 0) break
            val element = questionState.order[idx]
            val itemHeight = questionState.getItemHeight(element)
            if (leftoverOffset < itemHeight / 2) break
            numberOfSlidItems--
            leftoverOffset -= itemHeight
        }
    } else {
        while (true) {
            val idx = questionState.draggedItemIndex.value!! + numberOfSlidItems
            if (idx >= questionState.order.size - 1) break
            val element = questionState.order[idx]
            val itemHeight = questionState.getItemHeight(element)
            if (leftoverOffset < itemHeight / 2) break
            numberOfSlidItems++
            leftoverOffset -= itemHeight
        }
    }
    return numberOfSlidItems
}