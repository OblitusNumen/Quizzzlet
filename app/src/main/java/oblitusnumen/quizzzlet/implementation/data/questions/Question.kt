package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.ui.model.question.QuestionState

abstract class Question internal constructor(val id: Int?, val question: String, val attachments: List<String>?) {
    abstract fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean,
        coroutineScope: CoroutineScope,
        scrollState: LazyListState
    )

    abstract fun newQuestionState(): QuestionState
}
