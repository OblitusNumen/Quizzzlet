package oblitusnumen.quizzzlet.implementation.data.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.jsonizer.CategoryQuestionJsonizer
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionJsonizer
import oblitusnumen.quizzzlet.ui.model.question.CategoryQuestionState
import oblitusnumen.quizzzlet.ui.model.question.QuestionState

class CategoryQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    private val categories: List<String>,
    private val answer: List<Int>
) : Question(id, question, attachments) {
    private val correctAnswers: Map<String, Int>

    init {
        val a: MutableMap<String, Int> = mutableMapOf()
        repeat(candidates.size) {
            a[candidates[it]] = answer[it]
        }
        correctAnswers = a
    }

    override fun compose(
        dataManager: DataManager,
        scope: LazyListScope,
        questionState: QuestionState,
        submit: () -> Unit,
        hasAnswered: Boolean,
        coroutineScope: CoroutineScope,
        scrollState: LazyListState
    ) {
        val candidates = (questionState as CategoryQuestionState).candidates
        val answers: List<MutableState<Int?>> = questionState.answers
        scope.items(candidates.size) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp).padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        candidates[it],
                        modifier = Modifier.padding(8.dp).weight(.5f).align(Alignment.CenterVertically)
                    )
                    answerSpinner(
                        answers[it],
                        Modifier.padding(8.dp).weight(.5f).align(Alignment.CenterVertically),
                        hasAnswered
                    )
                }
                if (hasAnswered) {
                    if (checkAnswer(candidates[it], answers[it].value))
                        Text(
                            "Correct", color = Color.Green,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    else
                        Text(
                            "Correct: ${categories[correctAnswers[candidates[it]]!!]}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun answerSpinner(selectedOption: MutableState<Int?>, modifier: Modifier = Modifier, lock: Boolean) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded, onExpandedChange = { if (!lock) expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = if (selectedOption.value != null) categories[selectedOption.value!!] else "",
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { if (!lock) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            if (!lock)
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            selectedOption.value = null
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                    repeat(categories.size) { i ->
                        HorizontalDivider(Modifier.padding(8.dp))
                        DropdownMenuItem(
                            text = { Text(categories[i], style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                selectedOption.value = i
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
        }
    }

    private fun checkAnswer(candidate: String, answer: Int?): Boolean =
        answer == null && correctAnswers[candidate] == -1 || answer == correctAnswers[candidate]

    fun checkAnswer(candidates: List<String>, answers: List<MutableState<Int?>>): Boolean {
        repeat(answers.size) { i ->
            if (!checkAnswer(candidates[i], answers[i].value)) return false
        }
        return true
    }

    override fun newQuestionState(): QuestionState = CategoryQuestionState(this)

    companion object {
        fun getJsonizer(): QuestionJsonizer<CategoryQuestion> = CategoryQuestionJsonizer()
    }
}