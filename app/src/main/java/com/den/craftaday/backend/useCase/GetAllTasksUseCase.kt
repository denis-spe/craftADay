// Glory be to the name of the LORD of hosts
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTasksUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    operator fun invoke(): Flow<List<Task>> {
        val userId = accountService.currentUserId
        return dataStorage.getAllDatasets(userId = userId)
    }
}