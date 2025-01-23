package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import oblitusnumen.quizzzlet.implementation.data.questions.SelectQuestion

class SelectQuestionState(question: SelectQuestion) : QuestionState(question) {
    var answer: String? by mutableStateOf(null)
    val candidates = question.candidates.shuffled()

    override fun checkAnswer(): Boolean = (question as SelectQuestion).checkAnswer(answer)
}
