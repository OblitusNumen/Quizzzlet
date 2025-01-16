package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import oblitusnumen.quizzzlet.implementation.data.Question
import oblitusnumen.quizzzlet.implementation.data.QuestionList
import java.lang.reflect.Type

class QuestionListTypeAdapter : JsonDeserializer<QuestionList> {
    private val jsonizerMap: MutableMap<String, QuestionJsonizer<*>> = HashMap()

    init {
        jsonizerMap["select"] = SelectQuestion.getJsonizer()
        jsonizerMap["multiple-select"] = MultipleChoiceQuestion.getJsonizer()
        jsonizerMap["text"] = TextQuestion.getJsonizer()
        jsonizerMap["order"] = OrderQuestion.getJsonizer()
        jsonizerMap["category"] = CategoryQuestion.getJsonizer()
    }

    override fun deserialize(
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext
    ): QuestionList {
        return QuestionList(json.asJsonArray.map { deserializeQuestion(it, type, context) })
    }

    private fun deserializeQuestion(json: JsonElement, type: Type?, context: JsonDeserializationContext): Question {
        val jsonObject = json.asJsonObject
        val qType: String = jsonObject.get("type").asString
        val jsonizer = jsonizerMap[qType]
        if (jsonizer == null) {
            throw IllegalArgumentException("could not find jsonizer: $qType")
        } else {
            return jsonizer.deserialize(json, type, context)
        }
    }
}