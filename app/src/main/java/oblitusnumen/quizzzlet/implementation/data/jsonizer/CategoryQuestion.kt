package oblitusnumen.quizzzlet.implementation.data.jsonizer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.Question
import java.lang.reflect.Type

class CategoryQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    private val candidates: List<String>,
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

    companion object {
        fun getJsonizer(): QuestionJsonizer<CategoryQuestion> = CategoryQuestionJsonizer()
    }

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
            val answers: List<MutableState<Int?>> = remember(hack) {
                val l: MutableList<MutableState<Int?>> = mutableListOf()
                repeat(candidates.size) {
                    l.add(mutableStateOf(null))
                }
                return@remember l
            }
            repeat(candidates.size) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp).padding(8.dp),
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
            screenEnd({ checkAnswer(candidates, answers) }, { hack = !hack })
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
                modifier = Modifier.fillMaxHeight().menuAnchor(),
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

    private fun checkAnswer(candidates: List<String>, answers: List<MutableState<Int?>>): Boolean {
        repeat(answers.size) { i ->
            if (!checkAnswer(candidates[i], answers[i].value)) return false
        }
        return true
    }

    class CategoryQuestionJsonizer : QuestionJsonizer<CategoryQuestion>() {
        override fun deserialize(
            json: JsonElement,
            type: Type?,
            context: JsonDeserializationContext
        ): CategoryQuestion {
            val jsonObject = json.asJsonObject
            return CategoryQuestion(
                if (jsonObject.has("id")) jsonObject.get("id").asInt else null,
                jsonObject.get("question").asString,
                if (jsonObject.has("attachments"))
                    jsonObject.get("attachments").asJsonArray.map { it.asString }
                else
                    null,
                jsonObject.get("candidates").asJsonArray.map { it.asString },
                jsonObject.get("categories").asJsonArray.map { it.asString },
                jsonObject.get("answer").asJsonArray.map { it.asInt })
        }
    }
}