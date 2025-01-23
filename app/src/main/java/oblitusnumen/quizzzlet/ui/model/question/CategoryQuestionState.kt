package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import oblitusnumen.quizzzlet.implementation.data.questions.CategoryQuestion

class CategoryQuestionState(question: CategoryQuestion) : QuestionState(question) {
    val candidates = question.candidates.shuffled()
    val answers: List<MutableState<Int?>> = run {
        val l: MutableList<MutableState<Int?>> = mutableListOf()
        repeat(candidates.size) {
            l.add(mutableStateOf(null))
        }
        l
    }

    override fun checkAnswer(): Boolean = (question as CategoryQuestion).checkAnswer(candidates, answers)
}