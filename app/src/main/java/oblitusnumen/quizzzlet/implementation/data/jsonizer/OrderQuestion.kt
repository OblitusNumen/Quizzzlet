package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.Question
import java.lang.reflect.Type

class OrderQuestion(id: Int?, question: String, attachments: List<String>?, val answer: List<String>) :
    Question(id, question, attachments) {
    companion object {
        fun getJsonizer(): QuestionJsonizer<OrderQuestion> = OrderQuestionJsonizer()
    }

    class OrderQuestionJsonizer : QuestionJsonizer<OrderQuestion>() {
        override fun deserialize(json: JsonElement, type: Type?, context: JsonDeserializationContext): OrderQuestion {
            val jsonObject = json.asJsonObject
            return OrderQuestion(
                if (jsonObject.has("id")) jsonObject.get("id").asInt else null,
                jsonObject.get("question").asString,
                if (jsonObject.has("attachments"))
                    jsonObject.get("attachments").asJsonArray.map { it.asString }
                else
                    null,
                jsonObject.get("answer").asJsonArray.map { it.asString }
            )
        }
    }
}