package oblitusnumen.quizzzlet.implementation.data.jsonizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.Question
import oblitusnumen.quizzzlet.implementation.equalsStripIgnoreCase
import oblitusnumen.quizzzlet.implementation.measureTextLine
import java.lang.reflect.Type

class TextQuestion(id: Int?, question: String, attachments: List<String>?, private val answer: String) : Question(
    id, question,
    attachments
) {
    @Composable
    override fun compose(
        dataManager: DataManager,
        screenEnd: @Composable (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        submit: (checkAnswer: () -> Boolean, nullifyFields: () -> Unit) -> Unit,
        hasAnswered: Boolean
    ) {
        var hack by remember { mutableStateOf(false) }// FIXME: yet another filthy hack
        val answer = remember(hack) { mutableStateOf("") }// FIXME: add multiple field support
        val focusRequester = remember(hack) { FocusRequester() }
        Column {
            val modifier1 = Modifier//.weight(1f).align(Alignment.CenterVertically)
            answerField(
                "Answer",
                hasAnswered,
                this@TextQuestion.answer,
                answer,
                modifier1,
                focusRequester
            ) {
                submit({ checkAnswer(answer.value) }, {
                    focusRequester.requestFocus()
                    hack = !hack
                })
            }
        }
        screenEnd({ checkAnswer(answer.value) }, {
            focusRequester.requestFocus()
            hack = !hack
        })
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

    private fun checkAnswer(answer: String): Boolean = answer.equalsStripIgnoreCase(this.answer)

    companion object {
        fun getJsonizer(): QuestionJsonizer<TextQuestion> = TextQuestionJsonizer()
    }

    class TextQuestionJsonizer : QuestionJsonizer<TextQuestion>() {
        override fun deserialize(json: JsonElement, type: Type?, context: JsonDeserializationContext): TextQuestion {
            val jsonObject = json.asJsonObject
            return TextQuestion(
                if (jsonObject.has("id")) jsonObject.get("id").asInt else null,
                jsonObject.get("question").asString,
                if (jsonObject.has("attachments"))
                    jsonObject.get("attachments").asJsonArray.map { it.asString }
                else
                    null,
                jsonObject.get("answer").asString/*jsonObject.get("answer").asJsonArray.map { it.asInt }*/// FIXME: this can also be an array
            )
        }
    }
}