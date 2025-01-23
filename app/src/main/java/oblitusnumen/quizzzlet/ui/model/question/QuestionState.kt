package oblitusnumen.quizzzlet.ui.model.question

import oblitusnumen.quizzzlet.implementation.data.questions.Question

abstract class QuestionState(val question: Question) {
    abstract fun checkAnswer(): Boolean
}
