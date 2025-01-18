package oblitusnumen.quizzzlet.implementation.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import oblitusnumen.quizzzlet.Config
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

class DataManager(val context: Context) {
    private val poolsDir: File
    var config: Config = Config.ofString(getSharedPrefs(context).getString(CONFIG_PREF_NAME, "")!!)
        set(config) {
            getSharedPrefs(context).edit().putString(CONFIG_PREF_NAME, config.toString()).apply()
            field = config
        }

    init {
        val poolsDirectory = File(context.filesDir, QUESTION_POOL_DIRECTORY)
        if (!poolsDirectory.exists() && !poolsDirectory.mkdirs()) throw IOException("Could not create directory")
        poolsDir = poolsDirectory
    }

    fun getQuestionPools(): List<QuestionPool> {
        val result: MutableList<QuestionPool> = ArrayList()
        for (poolFile in poolsDir.listFiles()
            ?.sortedBy { Files.readAttributes(it.toPath(), BasicFileAttributes::class.java).creationTime() }
            ?: throw IOException("Could not list files from directory")) {
            result.add(QuestionPool(poolFile))
        }
        return result
    }

    fun getQuestionPool(fileName: String): QuestionPool {
        return QuestionPool(File(poolsDir, fileName))
    }

    fun copyPool(uri: Uri): String? {// FIXME: ask pool name
        val randomUUID = UUID.randomUUID().toString()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                QuestionPool(inputStream)
            }
        } catch (e: Exception) {
            return null
        }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(File(poolsDir, randomUUID)).use { outputStream ->
                inputStream.copyTo(outputStream)
                outputStream.flush()
                outputStream.fd.sync()
            }
        }
        return randomUUID
    }

    companion object {
        private const val QUESTION_POOL_DIRECTORY: String = "question-pools"
        private const val SHARED_PREFERENCES_NAME: String = "quizzzlet_preferences"
        private const val CONFIG_PREF_NAME: String = "config"

        fun getSharedPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }
}
