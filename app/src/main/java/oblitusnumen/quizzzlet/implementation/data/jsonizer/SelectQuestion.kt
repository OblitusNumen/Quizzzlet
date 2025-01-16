package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.Question
import java.lang.reflect.Type

class SelectQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: Int
) : Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<SelectQuestion> = SelectQuestionJsonizer()
    }

    class SelectQuestionJsonizer : QuestionJsonizer<SelectQuestion>() {
        override fun deserialize(json: JsonElement, type: Type?, context: JsonDeserializationContext): SelectQuestion {
            val jsonObject = json.asJsonObject
            return SelectQuestion(
                if (jsonObject.has("id")) jsonObject.get("id").asInt else null,
                jsonObject.get("question").asString,
                if (jsonObject.has("attachments"))
                    jsonObject.get("attachments").asJsonArray.map { it.asString }
                else
                    null,
                jsonObject.get("candidates").asJsonArray.map { it.asString },
                jsonObject.get("answer").asInt
            )
        }
    }
}
