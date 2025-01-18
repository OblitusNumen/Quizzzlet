package oblitusnumen.quizzzlet.implementation.data.questions

import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.SelectQuestionJsonizer

class SelectQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: Int
) : Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<SelectQuestion> = SelectQuestionJsonizer()
    }
}
