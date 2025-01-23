package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import oblitusnumen.quizzzlet.implementation.data.questions.OrderQuestion

class OrderQuestionState(question: OrderQuestion) : QuestionState(question) {
    var order: List<String> by mutableStateOf(listOf())
    val candidates = question.answer.shuffled()

    override fun checkAnswer(): Boolean = (question as OrderQuestion).checkAnswer(order)
}
