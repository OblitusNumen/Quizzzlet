package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.OrderQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.ui.model.question.OrderQuestionState
import oblitusnumen.quizzzlet.ui.model.question.QuestionState

class OrderQuestion(id: Int?, question: String, attachments: List<String>?, val answer: List<String>) :
    Question(id, question, attachments) {
    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean
    ) {
        val candidates = (questionState as OrderQuestionState).candidates
        scope.item {
            HorizontalDivider(Modifier.height(8.dp))
        }
        scope.items(questionState.order.size) { i ->
            val bg: Color =
                if (hasAnswered)
                    if (questionState.order[i] == answer[i])
                        Color.Green.copy(alpha = 0.7f)
                    else
                        Color.Red.copy(alpha = 0.7f)
                else
                    Color.Transparent
            if (!hasAnswered)
                addButton(
                    candidates,
                    questionState.order,
                    Modifier/*.align(Alignment.CenterHorizontally)*/
                ) { e: String ->
                    questionState.order =
                        questionState.order.subList(0, i) + e + questionState.order.subList(i, questionState.order.size)
                }
            Row(
                Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clickable { if (!hasAnswered) questionState.order -= questionState.order[i] }
                    .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                    .background(bg, shape = RoundedCornerShape(4.dp))
            ) {
                Text(
                    modifier = Modifier.weight(1.0f).padding(8.dp).align(Alignment.CenterVertically),
                    text = questionState.order[i],
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        scope.item {
            if (!hasAnswered && questionState.order.size != candidates.size)
                addButton(
                    candidates,
                    questionState.order,
                    Modifier.fillMaxWidth()/*.align(Alignment.CenterHorizontally)*/
                ) { e: String ->
                    questionState.order += e
                }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            if (hasAnswered && !checkAnswer(questionState.order)) {
                var dialogShown by remember { mutableStateOf(false) }
                Box(
                    Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp)).clickable { dialogShown = true }) {
                    Text(
                        "Show correct",
                        style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (dialogShown)
                    showDialog(answer) { dialogShown = false }
            }
        }
    }

    @Composable
    fun addButton(candidates: List<String>, order: List<String>, modifier: Modifier, onChoose: (String) -> Unit) {
        var dialogShown by remember { mutableStateOf(false) }
        Box(modifier.fillMaxWidth().clickable { dialogShown = true }) {
            IconButton(onClick = { dialogShown = true }, Modifier.align(Alignment.Center)) {
                Icon(Icons.Filled.Add, "Add element", Modifier.align(Alignment.Center).padding(8.dp))
            }
        }

        if (dialogShown)
            showDialog(candidates.filter { !order.contains(it) }) {
                if (it != null) onChoose(it)
                dialogShown = false
            }
    }

    @Composable
    fun showDialog(remainingOptions: List<String>, onChoose: (String?) -> Unit) {
        Dialog(onDismissRequest = { onChoose(null) }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(remainingOptions.size) { i ->
                        item {
                            if (i != 0) HorizontalDivider(Modifier.padding(8.dp))
                            Text(
                                remainingOptions[i],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().defaultMinSize(48.dp).padding(4.dp).clickable {
                                    onChoose(remainingOptions[i])
                                })
                        }
                    }
                }
            }
        }
    }

    fun checkAnswer(order: List<String>): Boolean {
        if (order.size != answer.size) return false
        repeat(order.size) { i ->
            if (order[i] != answer[i]) return false
        }
        return true
    }

    override fun newQuestionState(): QuestionState = OrderQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<OrderQuestion> = OrderQuestionJsonizer()
    }
}