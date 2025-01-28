package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.MultipleChoiceQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.ui.model.question.MultipleChoiceQuestionState
import oblitusnumen.quizzzlet.ui.model.question.QuestionState

class MultipleChoiceQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: List<Int>
) : Question(id, question, attachments) {
    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean,
        coroutineScope: CoroutineScope,
        scrollState: LazyListState
    ) {
        val candidates = (questionState as MultipleChoiceQuestionState).candidates
        val answers: List<MutableState<Boolean>> = questionState.answers
        scope.items(candidates.size) { i ->
            val option = candidates[i]
            val selected = answers[i].value
            val bg: Color =
                if (hasAnswered)
                    if (checkAnswer(option, selected))
                        if (selected)
                            Color.Green.copy(alpha = 0.7f)
                        else
                            Color.Transparent
                    else
                        if (selected)
                            Color.Red.copy(alpha = 0.7f)
                        else
                            Color.Yellow.copy(alpha = 0.7f)
                else
                    if (selected)
                        Color.Gray.copy(alpha = 0.7f)
                    else
                        Color.Transparent
            Row(
                Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clickable { if (!hasAnswered) answers[i].value = !answers[i].value }
                    .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                    .background(bg, shape = RoundedCornerShape(4.dp))
            ) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { answers[i].value = !answers[i].value },
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterVertically)
                )
                Text(option, modifier = Modifier.padding(8.dp).align(Alignment.CenterVertically))
//                    Text(
//                        modifier = Modifier.weight(1.0f).padding(start = 8.dp, end = 8.dp)
//                            .align(Alignment.CenterVertically),
//                        text = option,
//                        style = MaterialTheme.typography.bodyLarge
//                    )
            }
        }
    }

    private fun checkAnswer(candidate: String, answer: Boolean): Boolean =
        answer == this.answer.contains(candidates.indexOf(candidate))

    fun checkAnswer(candidates: List<String>, answers: List<MutableState<Boolean>>): Boolean {
        repeat(answers.size) { i ->
            if (!checkAnswer(candidates[i], answers[i].value)) return false
        }
        return true
    }

    override fun newQuestionState(): QuestionState = MultipleChoiceQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<MultipleChoiceQuestion> = MultipleChoiceQuestionJsonizer()
    }
}
