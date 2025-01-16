package oblitusnumen.quizzzlet.implementation.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionListTypeAdapter
import java.io.File
import java.io.InputStream

class QuestionPool {
    private var file: File? = null
    val filename: String
    val name: String
    private val questions: List<Question>

    constructor(file: File) : this(file.readText(), file.name) {
        this.file = file
    }

    constructor(inputStream: InputStream) : this(inputStream.bufferedReader().use { it.readText() }, "")

    private constructor(source: String, filename: String) {
        this.filename = filename
        name = "placeholder"// TODO: choose a name for it
        this.questions = gson.fromJson(source, QuestionList::class.java).questions
    }

    fun countQs(): Int {
        return questions.size
    }

    fun questionsScrambled(): List<Question> = questions.shuffled()

    fun delete() {
        file?.delete()
    }

    companion object {
        private val gson: Gson

        init {
            val builder = GsonBuilder()
            builder.registerTypeAdapter(QuestionList::class.java, QuestionListTypeAdapter())
            builder.setPrettyPrinting()
            gson = builder.create()
        }
    }
}