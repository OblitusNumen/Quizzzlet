package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.TextQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.equalsStripIgnoreCase
import oblitusnumen.quizzzlet.implementation.measureTextLine
import oblitusnumen.quizzzlet.ui.model.question.QuestionState
import oblitusnumen.quizzzlet.ui.model.question.TextQuestionState

class TextQuestion(id: Int?, question: String, attachments: List<String>?, private val answer: String) : Question(
    id, question,
    attachments
) {
    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean,
        coroutineScope: CoroutineScope,
        scrollState: LazyListState
    ) {
        val answer = (questionState as TextQuestionState).answer// FIXME: add multiple field support
        scope.item {
            val focusRequester = remember { FocusRequester() }
            val modifier1 = Modifier//.weight(1f).align(Alignment.CenterVertically)
            answerField(
                "Answer",
                hasAnswered,
                this@TextQuestion.answer,
                answer,
                modifier1,
                focusRequester,
                submit
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }

    @Composable
    fun answerField(
        label: String,
        lock: Boolean,
        correctOne: String,
        typedValue: MutableState<String>,
        modifier: Modifier = Modifier,
        focusRequester: FocusRequester,
        onDone: () -> Unit,
    ) {
        val correct = remember(lock) { typedValue.value.equalsStripIgnoreCase(correctOne) }
        Column(modifier = modifier) {
            OutlinedTextField(
                value = typedValue.value,
                onValueChange = {
                    typedValue.value = it
                },
                label = { Text(label) },
                readOnly = lock,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.padding(8.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
                    .focusRequester(focusRequester)
            )
            val spaceModifier = Modifier.padding(8.dp)
                .defaultMinSize(minHeight = measureTextLine(MaterialTheme.typography.bodySmall) * 1.2f)
            if (lock) {
                if (correct)
                    Text(
                        "Correct", color = Color.Green,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = spaceModifier.align(Alignment.CenterHorizontally)
                    )
                else
                    Text(
                        "Correct: $correctOne",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = spaceModifier.align(Alignment.CenterHorizontally)
                    )
            } else
                Spacer(modifier = spaceModifier)
        }
    }

    fun checkAnswer(answer: String): Boolean = answer.equalsStripIgnoreCase(this.answer)

    override fun newQuestionState(): QuestionState = TextQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<TextQuestion> = TextQuestionJsonizer()
    }
}