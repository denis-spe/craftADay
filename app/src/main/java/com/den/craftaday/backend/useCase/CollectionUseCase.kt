package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import javax.inject.Inject

class CollectionUseCase @Inject constructor(
    private val dataStorageService: DataStorageService,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId
    fun addCollection(collection: ListCollectionEntity) = dataStorageService.addCollection(userId, collection)
}