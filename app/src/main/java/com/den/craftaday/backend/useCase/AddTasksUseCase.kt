// Bless be the LORD GOD of hosts, The GOD od Israel.
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.impl.AccountService
import com.den.craftaday.backend.impl.DataStorage
import javax.inject.Inject

class AddTasksUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    operator fun invoke(task: Task) {
        val userId = accountService.currentUserId
        dataStorage.addTask(userId = userId, task = task)
    }
}