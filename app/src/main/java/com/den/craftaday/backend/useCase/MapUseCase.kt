// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.entities.MapEntity
import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import com.den.craftaday.backend.entities.MapNodeEntity
import com.den.craftaday.backend.entities.ListCollectionEntity
import javax.inject.Inject

class MapUseCase @Inject constructor(
    private val dataStorageService: DataStorageService,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun getMap(collectionId: String, mapId: String) = dataStorageService.getMap(userId, collectionId, mapId)
    
    fun addMap(collectionId: String, map: MapEntity) {
        dataStorageService.addMap(userId, collectionId = collectionId, map = map)
    }

    fun deleteMap(collectionId: String, map: MapEntity) {
        dataStorageService.deleteMap(userId, collectionId = collectionId, map = map)
    }

    fun updateMap(collectionId: String, map: MapEntity) {
        dataStorageService.updateMap(userId, collectionId = collectionId, map = map)
    }

    fun getAllMaps() = dataStorageService.getAllMaps(userId)
    
    fun getMapsInCollection(collectionId: String) = dataStorageService.getMapsInCollection(userId, collectionId)

    fun getMapNodes(collectionId: String, mapId: String) = dataStorageService.getMapNodes(
        userId, collectionId, mapId
    )

    fun addMapNode(collectionId: String, mapId: String, node: MapNodeEntity) {
        dataStorageService.addMapNode(userId, collectionId = collectionId, mapId = mapId, node = node)
    }

    fun updateMapNode(collectionId: String, mapId: String, node: MapNodeEntity) {
        dataStorageService.updateMapNode(userId, collectionId = collectionId, mapId = mapId, node = node)
    }

    fun deleteMapNode(collectionId: String, mapId: String, nodeId: String) {
        dataStorageService.deleteMapNode(userId, collectionId = collectionId, mapId = mapId, nodeId = nodeId)
    }

    fun updateMapNodeFields(collectionId: String, mapId: String, nodeId: String, fields: Map<String, Any>) {
        dataStorageService.updateMapNodeFields(userId, collectionId, mapId, nodeId, fields)
    }

    fun incrementUserStats(isSuccess: Boolean) {
        dataStorageService.incrementUserStats(userId, isSuccess)
    }
    
    fun getAllCollections() = dataStorageService.getAllCollections(userId)
    fun addCollection(collection: ListCollectionEntity) = dataStorageService.addCollection(userId, collection)
}
