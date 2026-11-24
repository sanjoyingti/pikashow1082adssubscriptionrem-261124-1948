package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.StreamSources
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenDownloads: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var name by remember(settings.accountName) { mutableStateOf(settings.accountName) }
    var email by remember(settings.accountEmail) { mutableStateOf(settings.accountEmail) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                Card(
                    onClick = onOpenPro,
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                "PRO is unlocked",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "No ads, no subscription, nothing to buy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item { SectionTitle("Appearance") }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("System" to 0, "Light" to 1, "Dark" to 2).forEach { (label, mode) ->
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreamSources.servers(0, 0).forEachIndexed { index, server ->
                        FilterChip(
                            selected = settings.preferredServer == index,
                            onClick = { viewModel.setServer(index) },
                            label = { Text(server.name) }
                        )
                    }
                }
            }
            item {
                ToggleRow(
                    title = "Subtitles on by default",
                    checked = settings.subtitlesOn,
                    onChange = { viewModel.setSubtitles(it) }
                )
            }
            item {
                ToggleRow(
                    title = "Start playing automatically",
                    checked = settings.autoplay,
                    onChange = { viewModel.setAutoplay(it) }
                )
            }

            item { SectionTitle("Downloads") }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenDownloads)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Text(
                        "Manage downloads",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            item { SectionTitle("Account") }
            if (settings.accountName.isBlank()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                        if (error != null) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        Button(
                            onClick = {
                                error = when {
                                    name.isBlank() -> "Enter a name"
                                    !email.contains("@") || !email.contains(".") -> "Enter a valid email"
                                    else -> {
                                        viewModel.signIn(name, email)
                                        null
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) { Text("Save profile") }
                    }
                }
            } else {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(settings.accountName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            settings.accountEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.padding(top = 10.dp)
                        ) { Text("Sign out") }
                    }
                }
            }

            item { SectionTitle("About") }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "PikaShow 10.8.2 · ads and subscription removed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
