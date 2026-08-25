// Great is the LORD GOD
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.ui.screens.screenManager.CollectionDetailRouter


@Composable
fun CollectionsList(
    collections: List<ListCollection>,
    selectedTab: Int,
    onAddCollection: () -> Unit,
    onTabSelected: (selectedTab: Int, collectionId: String) -> Unit
) {
    LazyRow (
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            AddCollection(
                onClick = onAddCollection
            )
        }
        items(collections.size) { index ->
            val collection = collections[index]

            CollectionItem(
                selectedTab = selectedTab == index,
                collection = collection,
                onClick = {
                    onTabSelected(index, collection.id)
                }
            )
        }
    }
}

@Composable
fun CollectionItem(
    selectedTab: Boolean,
    collection: ListCollection,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
        border = if (selectedTab) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            CardDefaults.outlinedCardBorder(enabled = false)
        }
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )

            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddCollection(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )

            Text(
                text = "collection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun EmptyCollectionList() {
    Box {
        Text(
            text = "No collections found",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun LoadingCollectionList() {
    Box {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ErrorCollectionList(
    state: DataState.Error
) {
    Box {
        Text(
            text = "Error: ${state.exception.message}",
            color = Color.Red,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}