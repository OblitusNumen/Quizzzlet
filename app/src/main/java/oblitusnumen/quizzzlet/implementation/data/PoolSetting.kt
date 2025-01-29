package oblitusnumen.quizzzlet.implementation.data

import kotlin.math.max

class PoolSetting private constructor(val numberInPool: Int, private val enabledPools: MutableList<Boolean>) {
    constructor(string: String) : this(
        string.substring(0, string.indexOf(',')).toInt(),
        mutableListOf(*string.substring(string.indexOf(',') + 1).toCharArray().map { it == '1' }.toTypedArray())
    )

    fun enabledPools(): List<Boolean> = enabledPools

    fun update(poolsEnabledStates: List<Boolean>) {
        if (poolsEnabledStates.size != enabledPools.size)
            throw IllegalArgumentException("poolsEnabledStates must have the same number of pools")
        enabledPools.clear()
        enabledPools.addAll(poolsEnabledStates)
    }

    override fun toString(): String {
        return "$numberInPool," + enabledPools.joinToString("") { if (it) "1" else "0" }
    }

    companion object {
        fun ofPool(questionPool: QuestionPool): PoolSetting {
            if (questionPool.countQuestions() == 0)
                return PoolSetting(0, mutableListOf())
            val numberOfTens = max(questionPool.countQuestions() / 10, 1)
            val numberInPool: Int = (questionPool.countQuestions() + numberOfTens - 1) / numberOfTens
            val numberOfPools = (questionPool.countQuestions() + numberInPool - 1) / numberInPool
            return PoolSetting(numberInPool, mutableListOf(*(1..numberOfPools).map { true }.toTypedArray()))
        }
    }
}
