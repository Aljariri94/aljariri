package com.example.data.repository

import com.example.data.local.CalculationDao
import com.example.data.local.CalculationEntity
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {

    val allHistory: Flow<List<CalculationEntity>> = dao.getAllHistory()

    suspend fun insertCalculation(expression: String, result: String, note: String = "", tafqeet: String = ""): Long {
        val entity = CalculationEntity(
            expression = expression,
            result = result,
            note = note,
            tafqeet = tafqeet,
            timestamp = System.currentTimeMillis()
        )
        return dao.insert(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
