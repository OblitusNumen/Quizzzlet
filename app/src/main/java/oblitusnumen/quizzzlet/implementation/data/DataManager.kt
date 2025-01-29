package oblitusnumen.quizzzlet.implementation.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import oblitusnumen.quizzzlet.Config
import oblitusnumen.quizzzlet.implementation.extractZip
import oblitusnumen.quizzzlet.implementation.inputStreamFromZip
import java.io.File
import java.io.IOException
import java.io.PrintWriter
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
            result.add(QuestionPool(this, poolFile))
        }
        return result
    }

    fun copyPool(uri: Uri, name: String): String? {
        val randomUUID = UUID.randomUUID().toString()
        try {
            val attachments: MutableSet<String> = mutableSetOf(POOL_FILENAME)
            context.contentResolver.openInputStream(uri).use { zipInputStream ->
                if (zipInputStream != null) {
                    inputStreamFromZip(zipInputStream, POOL_FILENAME).use { inputStream ->
                        if (inputStream != null) {
                            for (question in QuestionPool(this, inputStream).questionsScrambled()) {
                                for (attachment in question.attachments ?: continue) {
                                    attachments.add(attachment)
                                }
                            }
                        } else
                            throw IOException("Could not copy question pool")
                    }
                } else
                    throw IOException("Could not copy question pool")
            }
            extractZip(context.contentResolver.openInputStream(uri)!!, attachments, getPoolDir(randomUUID))
            getPoolNameFile(randomUUID).outputStream().use { outputStream ->
                outputStream.writer().use { writer ->
                    writer.append(name)
                    outputStream.flush()
                    outputStream.fd.sync()
                }
            }
        } catch (e: Exception) {
            getPoolDir(randomUUID).deleteRecursively()
            return null
        }
        return randomUUID
    }

    fun getQuestionPool(poolDir: String): QuestionPool = QuestionPool(this, getPoolDir(poolDir))

    fun getPoolName(poolDir: String): String = getPoolNameFile(poolDir).readText()

    private fun getPoolNameFile(poolDir: String) = File(getPoolDir(poolDir), POOL_NAME_FILENAME)

    fun getPoolFile(poolDir: String): File = File(getPoolDir(poolDir), POOL_FILENAME)

    fun getPoolDir(poolDir: String) = File(poolsDir, poolDir)

    fun getPoolSetting(poolDir: String): PoolSetting? {
        val poolSettingFile = getPoolSettingFile(poolDir)
        if (!poolSettingFile.exists()) return null
        val contents = poolSettingFile.readText()
        if (contents.isEmpty()) return null
        return PoolSetting(contents)
    }

    fun setPoolSetting(poolDir: String, poolSetting: String) {
        getPoolSettingFile(poolDir).outputStream().use { os ->
            PrintWriter(os).use { writer ->
                writer.write(poolSetting)
                os.flush()
                os.fd.sync()
            }
        }
    }

    private fun getPoolSettingFile(poolDir: String) = File(getPoolDir(poolDir), POOL_SETTING_FILENAME)

    companion object {
        private const val QUESTION_POOL_DIRECTORY: String = "question-pools"
        private const val POOL_FILENAME: String = "questions.json"
        private const val POOL_SETTING_FILENAME: String = "pool-setting"
        private const val POOL_NAME_FILENAME: String = "pool-name"
        private const val SHARED_PREFERENCES_NAME: String = "quizzzlet_preferences"
        private const val CONFIG_PREF_NAME: String = "config"

        fun getSharedPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }
}
