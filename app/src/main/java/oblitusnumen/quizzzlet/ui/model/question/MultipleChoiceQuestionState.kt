package oblitusnumen.quizzzlet.ui.model.question

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import oblitusnumen.quizzzlet.implementation.data.questions.MultipleChoiceQuestion

class MultipleChoiceQuestionState(question: MultipleChoiceQuestion) : QuestionState(question) {
    val candidates = question.candidates.shuffled()
    val answers: List<MutableState<Boolean>> = run {
        val l: MutableList<MutableState<Boolean>> = mutableListOf()
        repeat(candidates.size) {
            l.add(mutableStateOf(false))
        }
        l
    }

    override fun checkAnswer(): Boolean = (question as MultipleChoiceQuestion).checkAnswer(candidates, answers)
}
