package oblitusnumen.quizzzlet.implementation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import oblitusnumen.quizzzlet.ui.model.AnimatableOffset
import oblitusnumen.quizzzlet.ui.model.question.OrderQuestionState
import java.io.File
import java.io.InputStream
import java.lang.Float.min
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.pow
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
fun imagePreview(bitmap: ImageBitmap, imgName: String) {
    var isFullscreen by remember { mutableStateOf(false) }
    if (isFullscreen) {
        fullscreenImageDialog(bitmap) { isFullscreen = false }
    }
    Image(
        bitmap,
        imgName,
        modifier = Modifier.padding(12.dp).defaultMinSize(minWidth = (screenWidthInDp() / 2).dp)
            .clickable { isFullscreen = true },
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun fullscreenImageDialog(bitmap: ImageBitmap, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        zoomableImage(bitmap, onDismiss)
    }
}

@Composable
fun zoomableImage(bitmap: ImageBitmap, onDismiss: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val size = IntSize(
        with(density) { configuration.screenWidthDp.dp.toPx() }.toInt(),
        with(density) { configuration.screenHeightDp.dp.toPx() }.toInt()
    )
    val imgWidth = remember { bitmap.width }
    val imgHeight = remember { bitmap.height }
    val width = min(imgWidth.toFloat(), size.width.toFloat())
    val height = min(width / imgWidth * imgHeight, size.height.toFloat())
    val maxScale = max(min(size.width.toFloat() / imgWidth, size.height.toFloat() / imgHeight), 5f)
    val doubleTapScaleFactor = maxScale.pow(.5f)
    val inertiaAmount = remember { 50f }
    val scale = remember { Animatable(1f) }
    val offset = remember { AnimatableOffset() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val maxOffsetX =
                        if (width * scale.targetValue <= size.width.toFloat())
                            0f
                        else
                            (width * scale.targetValue - size.width) / 2f
                    val maxOffsetY =
                        if (height * scale.targetValue <= size.height.toFloat())
                            0f
                        else
                            (height * scale.targetValue - size.height) / 2f
                    coroutineScope.launch {
                        scale.snapTo((scale.value * zoom/*.pow(2f)*/).coerceIn(1f, maxScale))
                    }
                    coroutineScope.launch {
                        offset.offsetX.stop()
                        offset.offsetX.snapTo(
                            (offset.offsetX.value + pan.x/* * scale.targetValue*/).coerceIn(
                                -maxOffsetX,
                                maxOffsetX
                            )
                        )
                        if (zoom == 1f)
                            offset.offsetX.animateTo(
                                (offset.offsetX.value + pan.x * inertiaAmount/* * scale.targetValue*/).coerceIn(
                                    -maxOffsetX,
                                    maxOffsetX
                                ), tween(1000, easing = EaseOut)
                            )
                    }
                    coroutineScope.launch {
                        offset.offsetY.stop()
                        offset.offsetY.snapTo(
                            (offset.offsetY.value + pan.y/* * scale.targetValue*/).coerceIn(
                                -maxOffsetY,
                                maxOffsetY
                            )
                        )
                        if (zoom == 1f)
                            offset.offsetY.animateTo(
                                (offset.offsetY.value + pan.y * inertiaAmount/* * scale.targetValue*/).coerceIn(
                                    -maxOffsetY,
                                    maxOffsetY
                                ), tween(1000, easing = EaseOut)
                            )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset zoom and pan on double tap
                        if (scale.value < maxScale) {
                            coroutineScope.launch {
                                scale.animateTo(min(maxScale, scale.value * doubleTapScaleFactor), tween(500))
                            }
                        } else {
                            coroutineScope.launch {
                                scale.animateTo(1f, tween(500))
                            }
                            coroutineScope.launch {
                                offset.offsetX.animateTo(0f, tween(500))
                            }
                            coroutineScope.launch {
                                offset.offsetY.animateTo(0f, tween(500))
                            }
                        }
                    },
                    onTap = { onDismiss() }
                )
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "Fullscreen Image",
            contentScale = ContentScale.Fit, // Adjust image scale behavior
            modifier = Modifier
                .align(Alignment.Center)
//                .fillMaxSize() // Forces image to take up the full width & height
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offset.offsetX.value,
                    translationY = offset.offsetY.value
                )
        )
    }
}

@Composable
fun screenWidthInDp(): Int = LocalConfiguration.current.screenWidthDp

fun Modifier.modifyConditionally(condition: Boolean, modifier: (Modifier) -> Modifier): Modifier {
    return if (condition)
        modifier(this)
    else
        this
}

fun Modifier.dragToReorder(
    animatableOffset: AnimatableOffset,
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
    val offsetX = remember { animatableOffset.offsetX }
    val offsetY = remember { animatableOffset.offsetY }
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
            detectDragGestures(
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
    }
}

fun Modifier.reorderable(
    animatableOffset: AnimatableOffset
): Modifier = composed {
    val offsetX = remember { animatableOffset.offsetX }
    val offsetY = remember { animatableOffset.offsetY }
    offset {
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