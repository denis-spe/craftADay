// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.MapNode
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.ListCollection
import javax.inject.Inject

class MapUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun getMap(collectionId: String, mapId: String) = dataStorage.getMap(userId, collectionId, mapId)
    
    fun addMap(collectionId: String, map: ProjectMap) {
        dataStorage.addMap(userId, collectionId = collectionId, map = map)
    }

    fun deleteMap(collectionId: String, map: ProjectMap) {
        dataStorage.deleteMap(userId, collectionId = collectionId, map = map)
    }

    fun updateMap(collectionId: String, map: ProjectMap) {
        dataStorage.updateMap(userId, collectionId = collectionId, map = map)
    }

    fun getAllMaps() = dataStorage.getAllMaps(userId)
    
    fun getMapsInCollection(collectionId: String) = dataStorage.getMapsInCollection(userId, collectionId)

    fun getMapNodes(collectionId: String, mapId: String) = dataStorage.getMapNodes(
        userId, collectionId, mapId
    )

    fun addMapNode(collectionId: String, mapId: String, node: MapNode) {
        dataStorage.addMapNode(userId, collectionId = collectionId, mapId = mapId, node = node)
    }

    fun updateMapNode(collectionId: String, mapId: String, node: MapNode) {
        dataStorage.updateMapNode(userId, collectionId = collectionId, mapId = mapId, node = node)
    }

    fun deleteMapNode(collectionId: String, mapId: String, nodeId: String) {
        dataStorage.deleteMapNode(userId, collectionId = collectionId, mapId = mapId, nodeId = nodeId)
    }

    fun updateMapNodeFields(collectionId: String, mapId: String, nodeId: String, fields: Map<String, Any>) {
        dataStorage.updateMapNodeFields(userId, collectionId, mapId, nodeId, fields)
    }

    fun incrementUserStats(isSuccess: Boolean) {
        dataStorage.incrementUserStats(userId, isSuccess)
    }
    
    fun getAllCollections() = dataStorage.getAllCollections(userId)
    fun addCollection(collection: ListCollection) = dataStorage.addCollection(userId, collection)
}
