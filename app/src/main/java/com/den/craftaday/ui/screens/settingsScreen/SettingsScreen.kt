// The LORD is rock and my fortress
package com.den.craftaday.ui.screens.settingsScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.den.craftaday.R
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.SettingsViewModel
import com.den.craftaday.ui.screens.HORIZONTAL_PADDING
import com.den.craftaday.ui.screens.screenManager.WelcomeRouter
import com.den.craftaday.ui.theme.ExtendedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(backStack: NavBackStack<NavKey>) {
    // ViewModel injection
    val viewModel: SettingsViewModel = hiltViewModel()

    val user by viewModel.userState.collectAsStateWithLifecycle()

    when(user) {
        is AuthState.Authenticated -> {}

        else -> {
            backStack.clear()
            backStack.add(WelcomeRouter)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge
                ) },
                navigationIcon = {
                    IconButton(
                        onClick = { backStack.removeLastOrNull() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(padding)
                .padding(horizontal = HORIZONTAL_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                UserSection(user = user) {
                    viewModel.logout()
                }
            }
        }
    }
}

@Composable
fun UserSection(user: AuthState, onLogout: () -> Unit) {
    val iconSize = 20.dp

    if (user !is AuthState.Authenticated){
        return
    }

    user.let { userItem ->
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shadowElevation = 4.dp,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (userItem.isAnonymous) {
                        Text(
                            text = "Guest",
                            style = MaterialTheme.typography.titleLarge,
                            color = ExtendedTheme.colors.text,
                            fontWeight = FontWeight.Bold
                        )

                        Row (
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    /**
                                     * TODO
                                     */
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync account",
                                modifier = Modifier.size(iconSize)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                modifier = Modifier,
                                text = "Sync account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = userItem.displayName ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = ExtendedTheme.colors.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userItem.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row (
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLogout() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExtendedTheme.colors.text,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Column {
                    UserProfilePhoto(userItem = userItem)
                }

            }
        }
    }
}

@Composable
fun UserProfilePhoto(userItem: AuthState.Authenticated) {
    Surface(
        modifier = Modifier
            .size(100.dp),
        shadowElevation = 10.dp,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(50)
    ) {
        if (userItem.isAnonymous) {
            Image(
                painter = painterResource(R.drawable.outlined_user),
                contentDescription = "Profile picture",
                colorFilter = ColorFilter.tint(ExtendedTheme.colors.text),
                modifier = Modifier.size(100.dp)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userItem.photoUrl)
                    .crossfade(true) // Smooth fade animation on load
                    .build(),
                placeholder = painterResource(R.drawable.user_placeholder),
                error = painterResource(R.drawable.user_placeholder),
                contentDescription = "Profile picture",
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

