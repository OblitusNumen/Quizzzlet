package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import oblitusnumen.quizzzlet.implementation.data.questions.OrderQuestion

class OrderQuestionState(question: OrderQuestion) : QuestionState(question) {
    val draggedItemIndex: MutableState<Int?> = mutableStateOf(null)
    private val itemHeights: MutableMap<String, Int> = mutableMapOf()
    var order = mutableStateListOf(*question.answer.shuffled().toTypedArray())
    private var slideStates: MutableMap<String, SlideState> =
        mutableStateMapOf(*order.map { it to SlideState.NONE }.toTypedArray())

    override fun checkAnswer(): Boolean = (question as OrderQuestion).checkAnswer(order)

    fun getSlideState(element: String): SlideState = slideStates[element] ?: SlideState.NONE

    fun setSlideState(element: String, slideState: SlideState) {
        slideStates[element] = slideState
    }

    fun onMeasured(element: String, height: Int) = itemHeights.putIfAbsent(element, height)

    fun getDraggedItemHeight(): Int? =
        if (draggedItemIndex.value == null) null else getItemHeight(order[draggedItemIndex.value!!])

    fun getItemHeight(element: String): Int = itemHeights[element] ?: 0

    fun resetSlide() {
        slideStates.clear()
        slideStates.putAll(order.map { it to SlideState.NONE }.toTypedArray())
    }

    enum class SlideState {
        NONE,
        UP,
        DOWN
    }
}
