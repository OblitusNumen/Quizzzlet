package oblitusnumen.quizzzlet.implementation.data

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import oblitusnumen.quizzzlet.implementation.data.jsonizer.QuestionListTypeAdapter
import oblitusnumen.quizzzlet.implementation.data.questions.Question
import java.io.File
import java.io.InputStream

class QuestionPool {
    val poolDir: String
    val name: String
        get() = dataManager.getPoolName(poolDir)
    private val questions: List<Question>
    private val dataManager: DataManager

    constructor(dataManager: DataManager, file: File) : this(
        dataManager,
        dataManager.getPoolFile(file.name).readText(),
        file.name
    )

    constructor(dataManager: DataManager, inputStream: InputStream) : this(
        dataManager,
        inputStream.bufferedReader().use { it.readText() },
        ""
    )

    private constructor(dataManager: DataManager, source: String, filename: String) {
        this.dataManager = dataManager
        this.poolDir = filename
        this.questions = gson.fromJson(source, QuestionList::class.java).questions
    }

    fun getAttachment(path: String): ImageBitmap? {
        val attachmentFile = File(dataManager.getPoolDir(poolDir), path)
        if (!attachmentFile.exists())
            return null
        val asImageBitmap =
            BitmapFactory.decodeFile(attachmentFile.absolutePath).asImageBitmap()
        return asImageBitmap
    }

    fun countQs(): Int {
        return questions.size
    }

    fun questionsScrambled(): List<Question> = questions.shuffled()

    fun delete() {
        dataManager.getPoolDir(poolDir).deleteRecursively()
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