package oblitusnumen.quizzzlet.implementation.data.jsonizer

import com.google.gson.JsonDeserializer
import oblitusnumen.quizzzlet.implementation.data.questions.Question

abstract class QuestionJsonizer<T : Question> : JsonDeserializer<T>
