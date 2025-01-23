package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.SelectQuestionJsonizer
import oblitusnumen.quizzzlet.ui.model.question.QuestionState
import oblitusnumen.quizzzlet.ui.model.question.SelectQuestionState

class SelectQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: Int
) : Question(id, question, attachments) {
    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean
    ) {
        val candidates = (questionState as SelectQuestionState).candidates
        scope.items(candidates.size) {
            val option = candidates[it]
            val bg: Color =
                if (hasAnswered)
                    when (option) {
                        this@SelectQuestion.candidates[this@SelectQuestion.answer] -> Color.Green.copy(alpha = 0.7f)
                        questionState.answer -> Color.Red.copy(alpha = 0.7f)
                        else -> Color.Transparent
                    }
                else
                    if (option == questionState.answer)
                        Color.Gray.copy(alpha = 0.7f)
                    else
                        Color.Transparent
            Row(
                Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clickable {
                        if (!hasAnswered)
                            questionState.answer = option
                        if (dataManager.config.fastMode)
                            submit()
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
    }

    fun checkAnswer(answer: String?): Boolean =
        answer == null && this.answer == -1 || candidates.indexOf(answer) == this.answer

    override fun newQuestionState(): QuestionState = SelectQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<SelectQuestion> = SelectQuestionJsonizer()
    }
}
