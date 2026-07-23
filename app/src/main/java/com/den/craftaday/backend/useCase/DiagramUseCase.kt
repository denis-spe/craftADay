// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.DiagramNode
import javax.inject.Inject

class DiagramUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun getDiagramNodes() = dataStorage.getDiagramNodes(userId)

    fun addDiagramNode(node: DiagramNode) {
        dataStorage.addDiagramNode(userId, node)
    }

    fun updateDiagramNode(node: DiagramNode) {
        dataStorage.updateDiagramNode(userId, node)
    }

    fun deleteDiagramNode(nodeId: String) {
        dataStorage.deleteDiagramNode(userId, nodeId)
    }
}
