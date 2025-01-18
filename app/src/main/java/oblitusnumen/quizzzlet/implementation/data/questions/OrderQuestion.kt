package oblitusnumen.quizzzlet.implementation.data.questions

import oblitusnumen.quizzzlet.implementation.data.jsonizer.OrderQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer

class OrderQuestion(id: Int?, question: String, attachments: List<String>?, val answer: List<String>) :
    Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<OrderQuestion> = OrderQuestionJsonizer()
    }
}