package oblitusnumen.quizzzlet.implementation.data

class PoolSetting private constructor(private val enabledPools: MutableList<Boolean>) {
    constructor(string: String) : this(mutableListOf(*string.toCharArray().map { it == '1' }.toTypedArray()))

    fun enabledPools(): List<Boolean> = enabledPools

    fun update(poolsEnabledStates: List<Boolean>) {
        if (poolsEnabledStates.size != enabledPools.size)
            throw IllegalArgumentException("poolsEnabledStates must have the same number of pools")
        enabledPools.clear()
        enabledPools.addAll(poolsEnabledStates)
    }

    override fun toString(): String {
        return enabledPools.joinToString("") { if (it) "1" else "0" }
    }

    companion object {
        const val QUESTIONS_IN_POOL_BIT = 15

        fun ofPool(questionPool: QuestionPool): PoolSetting {
            if (questionPool.countQuestions() == 0)
                return PoolSetting(mutableListOf())
            if (questionPool.countQuestions() <= QUESTIONS_IN_POOL_BIT)
                return PoolSetting(mutableListOf(true))
            var numberOfPools = questionPool.countQuestions() / QUESTIONS_IN_POOL_BIT
            if (questionPool.countQuestions() % QUESTIONS_IN_POOL_BIT > QUESTIONS_IN_POOL_BIT / 2)
                numberOfPools++
            return PoolSetting(mutableListOf(*(1..numberOfPools).map { true }.toTypedArray()))
        }
    }
}
