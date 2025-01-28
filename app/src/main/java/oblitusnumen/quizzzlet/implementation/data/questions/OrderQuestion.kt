package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.OrderQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.implementation.dragToReorder
import oblitusnumen.quizzzlet.implementation.modifyConditionally
import oblitusnumen.quizzzlet.ui.model.question.OrderQuestionState
import oblitusnumen.quizzzlet.ui.model.question.QuestionState

class OrderQuestion(id: Int?, question: String, attachments: List<String>?, val answer: List<String>) :
    Question(id, question, attachments) {
    @OptIn(ExperimentalAnimationApi::class)
    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean,
        coroutineScope: CoroutineScope,
        scrollState: LazyListState
    ) {
        scope.items((questionState as OrderQuestionState).order.size) { index ->
            key(questionState.order[index]) {
                orderElement(
                    element = questionState.order[index],
                    elementIndex = index,
                    questionState = questionState,
                    scrollState = scrollState,
                    hasAnswered = hasAnswered,
                )
            }
        }
        scope.item {
            if (hasAnswered && !checkAnswer(questionState.order)) {
                var dialogShown by remember { mutableStateOf(false) }
                Box(
                    Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                        .clickable { dialogShown = true }) {
                    Text(
                        "Show correct",
                        style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (dialogShown)
                    showDialog(answer, questionState) { dialogShown = false }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun orderElement(
        element: String,
        elementIndex: Int,
        questionState: OrderQuestionState,
        scrollState: LazyListState,
        hasAnswered: Boolean,
    ) {
        val verticalTranslation by animateIntAsState(
            targetValue = when (questionState.getSlideState(element)) {
                OrderQuestionState.SlideState.UP -> questionState.getDraggedItemHeight()!!
                OrderQuestionState.SlideState.DOWN -> -questionState.getDraggedItemHeight()!!
                else -> 0
            },
            label = "",
        )
        val isDragged = questionState.draggedItemIndex.value == elementIndex
        val zIndex = if (isDragged) 1.0f else 0.0f
        val scale = if (isDragged) 1.03f else 1.0f
        val elevation = if (isDragged) 8.dp else 0.dp
        val bg: Color =
            if (hasAnswered)
                if (element == answer[elementIndex])
                    Color.Green.copy(alpha = 0.7f)
                else
                    Color.Red.copy(alpha = 0.7f)
            else
                if (isDragged)
                    Color.Gray
                else
                    Color.Transparent
        val verticalPadding = 4.dp
        val paddingInPixels: Int
        val absoluteYLocation = mutableStateOf(0f)
        with(LocalDensity.current) {
            paddingInPixels = verticalPadding.times(2).toPx().toInt()
        }
        Row(Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
            .padding(vertical = verticalPadding, horizontal = 8.dp)
            .modifyConditionally(!hasAnswered) {
                it.dragToReorder(
                    element,
                    elementIndex,
                    questionState,
                    scrollState,
                    absoluteYLocation
                )
            }
            .scale(scale)
            .offset { IntOffset(0, verticalTranslation) }
            .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
            .shadow(elevation, RoundedCornerShape(4.dp))
            .background(bg, shape = RoundedCornerShape(4.dp))
            .zIndex(zIndex)
            .onGloballyPositioned {
                absoluteYLocation.value = it.positionInWindow().y
                questionState.onMeasured(element, it.size.height + paddingInPixels)
            }
        ) {
            Text(
                modifier = Modifier.weight(1.0f).padding(8.dp).align(Alignment.CenterVertically),
                text = element,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    @Composable
    fun showDialog(answer: List<String>, questionState: OrderQuestionState, onChoose: (String?) -> Unit) {
        Dialog(onDismissRequest = { onChoose(null) }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(answer.size) { i ->
                        item {
                            if (i != 0) HorizontalDivider(Modifier.padding(8.dp))
                            Text(
                                answer[i],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().defaultMinSize(48.dp).padding(4.dp)
                                    .clickable { onChoose(answer[i]) }.background(
                                        if (questionState.order[i] == answer[i])
                                            Color.Green.copy(alpha = 0.7f)
                                        else
                                            Color.Red.copy(alpha = 0.7f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    fun checkAnswer(order: List<String>): Boolean {
        if (order.size != answer.size) return false
        repeat(order.size) { i ->
            if (order[i] != answer[i]) return false
        }
        return true
    }

    override fun newQuestionState(): QuestionState = OrderQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<OrderQuestion> = OrderQuestionJsonizer()
    }
}