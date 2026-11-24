package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.StreamSources
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.SettingsViewModel

private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var signOutDialog by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settings.accountName, settings.accountEmail) {
        if (name.isBlank()) name = settings.accountName
        if (email.isBlank()) email = settings.accountEmail
    }

    val signedIn = settings.accountName.isNotBlank()
    val emailClean = email.trim()
    val nameClean = name.trim()
    val canSignIn = nameClean.isNotEmpty() && emailPattern.matches(emailClean)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAbout() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text("PRO unlocked", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "No ads, no subscription — every server, HD/FHD stream, download and subtitle is always available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }

        item { SectionTitle("Account") }

        item {
            if (signedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null)
                            Column(modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)) {
                                Text(settings.accountName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    settings.accountEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            "Signed in. Playlists, watch-later and progress stay with this profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        OutlinedButton(
                            onClick = { signOutDialog = true },
                            modifier = Modifier.padding(top = 10.dp)
                        ) { Text("Sign out") }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            message = null
                        },
                        label = { Text("Display name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            message = null
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        isError = email.isNotBlank() && !emailPattern.matches(emailClean),
                        supportingText = {
                            if (email.isNotBlank() && !emailPattern.matches(emailClean)) {
                                Text("Enter a valid email address")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (!canSignIn) {
                                message = "Fill in a name and a valid email"
                            } else {
                                viewModel.signIn(nameClean, emailClean)
                                message = "Signed in — PRO stays active"
                            }
                        },
                        enabled = canSignIn,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Sign in") }
                    Text(
                        "Local profile only — no billing, no purchase check.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item { SectionTitle("Appearance") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "System", 1 to "Light", 2 to "Dark").forEach { (mode, label) ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setTheme(mode) },
                        label = { Text(label) }
                    )
                }
            }
        }

        item { SectionTitle("Playback") }

        item {
            Text("Default quality", style = MaterialTheme.typography.bodyMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                StreamSources.qualities.forEach { (label, _) ->
                    FilterChip(
                        selected = settings.defaultQuality == label,
                        onClick = { viewModel.setQuality(label) },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Text(
                "Preferred server",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 14.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                StreamSources.servers(1, 0).forEachIndexed { index, server ->
                    FilterChip(
                        selected = settings.preferredServer == index,
                        onClick = { viewModel.setServer(index) },
                        label = { Text(server.name) }
                    )
                }
            }
        }

        item {
            SwitchRow(
                title = "Subtitles on by default",
                subtitle = "Burn English subtitles into playback when available",
                checked = settings.subtitlesOn,
                onChange = { viewModel.setSubtitles(it) }
            )
        }

        item {
            SwitchRow(
                title = "Autoplay",
                subtitle = "Start playing as soon as a stream is ready",
                checked = settings.autoplay,
                onChange = { viewModel.setAutoplay(it) }
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(top = 16.dp)) }

        item { SectionTitle("Your stuff") }

        item {
            LinkRow(
                icon = Icons.Filled.Download,
                title = "Downloads",
                subtitle = "Saved videos for offline playback",
                onClick = onOpenDownloads
            )
            LinkRow(
                icon = Icons.Filled.BookmarkBorder,
                title = "Library",
                subtitle = "Favourites, watch later and history",
                onClick = onOpenLibrary
            )
        }

        item { SectionTitle("Storage & info") }

        item {
            LinkRow(
                icon = Icons.Filled.Delete,
                title = "Clear watch history",
                subtitle = "Removes continue-watching and recent searches",
                onClick = { clearDialog = true }
            )
            LinkRow(
                icon = Icons.Filled.Info,
                title = "About PikaShow",
                subtitle = "Version 10.8.2 · ads and subscription removed",
                onClick = onOpenAbout
            )
        }

        item {
            Text(
                "PikaShow 10.8.2 (noads build)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 18.dp, bottom = 28.dp)
            )
        }
    }

    if (signOutDialog) {
        AlertDialog(
            onDismissRequest = { signOutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("Your downloads stay on the device. History and searches are cleared.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    name = ""
                    email = ""
                    signOutDialog = false
                    message = null
                }) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(onClick = { signOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (clearDialog) {
        AlertDialog(
            onDismissRequest = { clearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("Continue watching entries and recent searches will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    libraryViewModel.clearProgress()
                    libraryViewModel.clearSearchHistory()
                    clearDialog = false
                    message = "History cleared"
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { clearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun LinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.PlayCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
