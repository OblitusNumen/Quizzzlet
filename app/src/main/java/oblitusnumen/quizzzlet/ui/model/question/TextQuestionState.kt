package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.mutableStateOf
import oblitusnumen.quizzzlet.implementation.data.questions.TextQuestion

class TextQuestionState(question: TextQuestion) : QuestionState(question) {
    val answer = mutableStateOf("")

    override fun checkAnswer(): Boolean = (question as TextQuestion).checkAnswer(answer.value)
}
