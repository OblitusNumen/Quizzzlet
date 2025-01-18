package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.questions.SelectQuestion
import java.lang.reflect.Type

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