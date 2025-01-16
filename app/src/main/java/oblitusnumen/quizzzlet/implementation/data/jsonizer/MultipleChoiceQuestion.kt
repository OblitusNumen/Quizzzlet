package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.Question
import java.lang.reflect.Type

class MultipleChoiceQuestion(
    id: Int?,
    question: String,
    attachments: List<String>?,
    val candidates: List<String>,
    val answer: List<Int>
) : Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<MultipleChoiceQuestion> = MultipleChoiceQuestionJsonizer()
    }

    class MultipleChoiceQuestionJsonizer : QuestionJsonizer<MultipleChoiceQuestion>() {
        override fun deserialize(
            json: JsonElement,
            type: Type?,
            context: JsonDeserializationContext
        ): MultipleChoiceQuestion {
            val jsonObject = json.asJsonObject
            return MultipleChoiceQuestion(
                if (jsonObject.has("id")) jsonObject.get("id").asInt else null,
                jsonObject.get("question").asString,
                if (jsonObject.has("attachments"))
                    jsonObject.get("attachments").asJsonArray.map { it.asString }
                else
                    null,
                jsonObject.get("candidates").asJsonArray.map { it.asString },
                jsonObject.get("answer").asJsonArray.map { it.asInt })
        }
    }
}
