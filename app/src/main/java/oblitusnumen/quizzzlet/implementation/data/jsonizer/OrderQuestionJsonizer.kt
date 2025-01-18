package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.questions.OrderQuestion
import java.lang.reflect.Type

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