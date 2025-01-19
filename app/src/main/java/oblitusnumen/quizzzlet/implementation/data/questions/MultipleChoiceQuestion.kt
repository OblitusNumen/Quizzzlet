package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.MultipleChoiceQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer

class MultipleChoiceQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    private val candidates: List<String>,
    val answer: List<Int>
) : Question(id, question, attachments) {
    @Composable
    override fun compose(
        dataManager: DataManager,
        screenEnd: @Composable (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        submit: (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        hasAnswered: Boolean
    ) {
        Column {
            var hack by remember { mutableStateOf(false) }// FIXME: yet another filthy hack
            val candidates = remember(hack) { candidates.shuffled() }
            val answers: List<MutableState<Boolean>> = remember(hack) {
                val l: MutableList<MutableState<Boolean>> = mutableListOf()
                repeat(candidates.size) {
                    l.add(mutableStateOf(false))
                }
                return@remember l
            }
            repeat(candidates.size) { i ->
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
            screenEnd({ checkAnswer(candidates, answers) }, { hack = !hack })
        }
    }

    private fun checkAnswer(candidate: String, answer: Boolean): Boolean =
        answer == this.answer.contains(candidates.indexOf(candidate))

    private fun checkAnswer(candidates: List<String>, answers: List<MutableState<Boolean>>): Boolean {
        repeat(answers.size) { i ->
            if (!checkAnswer(candidates[i], answers[i].value)) return false
        }
        return true
    }

    companion object {
        fun getJsonizer(): QuestionJsonizer<MultipleChoiceQuestion> = MultipleChoiceQuestionJsonizer()
    }
}
