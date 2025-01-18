package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.questions.TextQuestion
import java.lang.reflect.Type

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