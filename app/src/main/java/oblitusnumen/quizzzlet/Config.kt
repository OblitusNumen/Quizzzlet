package oblitusnumen.quizzzlet

class Config private constructor(string: String) {
    var repeatNotCorrect: Boolean = true
    var fastMode: Boolean = false
    var enableSelectQs: Boolean = true
    var enableMultipleChoiceQs: Boolean = true
    var enableTextQs: Boolean = true
    var enableCategoryQs: Boolean = true
    var enableOrderQs: Boolean = true

    init {
        if (string.isNotEmpty()) {
            repeatNotCorrect = string[0] == '1'
            fastMode = string[1] == '1'
            enableSelectQs = string[2] == '1'
            enableMultipleChoiceQs = string[3] == '1'
            enableTextQs = string[4] == '1'
            enableCategoryQs = string[5] == '1'
            enableOrderQs = string[6] == '1'
        }
    }

    override fun toString(): String {
        return (if (repeatNotCorrect) "1" else "0") +
                (if (fastMode) "1" else "0") +
                (if (enableSelectQs) "1" else "0") +
                (if (enableMultipleChoiceQs) "1" else "0") +
                (if (enableTextQs) "1" else "0") +
                (if (enableCategoryQs) "1" else "0") +
                (if (enableOrderQs) "1" else "0")
    }

    companion object {
        fun ofString(string: String): Config {
            return Config(string)
        }
    }
}