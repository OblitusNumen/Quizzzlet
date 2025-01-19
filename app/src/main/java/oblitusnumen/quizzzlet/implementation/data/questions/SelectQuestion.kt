package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.SelectQuestionJsonizer

class SelectQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    private val candidates: List<String>,
    val answer: Int
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
            var answer: String? by remember(hack) { mutableStateOf(null) }
            val candidates = remember(hack) { candidates.shuffled() }
            repeat(candidates.size) {
                val option = candidates[it]
                val bg: Color =
                    if (hasAnswered)
                        when (option) {
                            this@SelectQuestion.candidates[this@SelectQuestion.answer] -> Color.Green.copy(alpha = 0.7f)
                            answer -> Color.Red.copy(alpha = 0.7f)
                            else -> Color.Transparent
                        }
                    else
                        if (option == answer)
                            Color.Gray.copy(alpha = 0.7f)
                        else
                            Color.Transparent
                Row(
                    Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .clickable {
                            if (!hasAnswered) answer = option
                            if (dataManager.config.fastMode) submit({ checkAnswer(answer) }, { hack = !hack })
                        }
                        .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                        .background(bg, shape = RoundedCornerShape(4.dp))
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f).padding(8.dp).align(Alignment.CenterVertically),
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            screenEnd({ checkAnswer(answer) }, { hack = !hack })
        }
    }

    private fun checkAnswer(answer: String?): Boolean =
        answer == null && this.answer == -1 || candidates.indexOf(answer) == this.answer

    companion object {
        fun getJsonizer(): QuestionJsonizer<SelectQuestion> = SelectQuestionJsonizer()
    }
}
