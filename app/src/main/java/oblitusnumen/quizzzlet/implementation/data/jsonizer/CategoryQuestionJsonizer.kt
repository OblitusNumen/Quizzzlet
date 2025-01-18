package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.questions.CategoryQuestion
import java.lang.reflect.Type

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