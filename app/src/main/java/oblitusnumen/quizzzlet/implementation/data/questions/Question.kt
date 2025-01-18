package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.runtime.Composable
import oblitusnumen.quizzzlet.implementation.data.DataManager

abstract class Question internal constructor(val id: Int?, val question: String, val attachments: List<String>?) {
    @Composable
    open fun compose(
        dataManager: DataManager,
        screenEnd: @Composable (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        submit: (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        hasAnswered: Boolean
    ) {
        screenEnd({ true }, {})// FIXME:
    }
}
