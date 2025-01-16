package oblitusnumen.quizzzlet.implementation.data

import androidx.compose.runtime.Composable

abstract class Question internal constructor(val id: Int?, val question: String, val attachments: List<String>?) {
    @Composable
    open fun compose(
        dataManager: DataManager,
        screenEnd: @Composable (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit
    ) {
        screenEnd({ true }, {})// FIXME:
    }
}
