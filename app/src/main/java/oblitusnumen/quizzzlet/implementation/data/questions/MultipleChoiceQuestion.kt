package oblitusnumen.quizzzlet.implementation.data.questions

import oblitusnumen.quizzzlet.implementation.data.jsonizer.MultipleChoiceQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer

class MultipleChoiceQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: List<Int>
) : Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<MultipleChoiceQuestion> = MultipleChoiceQuestionJsonizer()
    }
}
