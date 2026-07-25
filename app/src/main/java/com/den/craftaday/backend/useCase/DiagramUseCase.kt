// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.DiagramProject
import javax.inject.Inject

class DiagramUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun getProject(projectId: String) = dataStorage.getProject(userId, projectId)
    fun addProject(project: DiagramProject) {
        dataStorage.addProject(userId, project = project)
    }

    fun deleteProject(digramProject: DiagramProject) {
        dataStorage.deleteProject(userId, project = digramProject)
    }

    fun updateProject(digramProject: DiagramProject) {
        dataStorage.updateProject(userId, project = digramProject)
    }

    fun getAllProjects() = dataStorage.getAllProjects(userId)

    fun getDiagramNodes(projectId: String) = dataStorage.getDiagramNodes(
        userId, projectId
    )

    fun addDiagramNode(projectId: String, node: DiagramNode) {
        dataStorage.addDiagramNode(userId, diagramProjectId = projectId, node = node)
    }

    fun updateDiagramNode(projectId: String, node: DiagramNode) {
        dataStorage.updateDiagramNode(userId, diagramProjectId = projectId, node = node)
    }

    fun deleteDiagramNode(projectId: String, nodeId: String) {
        dataStorage.deleteDiagramNode(userId, diagramProjectId = projectId, nodeId = nodeId)
    }
}
