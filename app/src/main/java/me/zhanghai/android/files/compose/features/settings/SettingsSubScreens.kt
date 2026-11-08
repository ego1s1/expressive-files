/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.components.ConfirmDialog
import me.zhanghai.android.files.compose.components.ExpressiveScaffold
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.StandardDirectory
import me.zhanghai.android.files.navigation.getExternalStorageDirectory
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.Storage
import me.zhanghai.android.files.util.valueCompat

/**
 * Native management screens that replace the legacy storage/bookmark/standard-directory
 * list activities. Lists are observed directly so edits apply reactively.
 */

@Composable
fun StorageSettingsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Storage) -> Unit,
    onDelete: (Storage) -> Unit
) {
    val storages by Settings.STORAGES.observeAsState(Settings.STORAGES.valueCompat)
    var pendingDelete by rememberSaveable { mutableStateOf<Int?>(null) }
    ExpressiveScaffold(
        title = stringResource(R.string.settings_storages_title),
        onNavigateUp = onBack,
        actions = {
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.compose_add_storage)
                )
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = storages,
                key = { it.id }
            ) { storage ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .clickable(
                                role = Role.Button,
                                onClick = { onEdit(storage) }
                            )
                            .padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(storage.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = storage.getName(LocalContext.current),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (storage.description.isNotBlank()) {
                                Text(
                                    text = storage.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                pendingDelete = storages.indexOfFirst { it.id == storage.id }
                                    .takeIf { it >= 0 }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
    val deleteIndex = pendingDelete
    if (deleteIndex != null) {
        val target = storages.getOrNull(deleteIndex)
        if (target == null) {
            pendingDelete = null
        } else {
            ConfirmDialog(
                title = stringResource(R.string.compose_delete_storage),
                message = target.getName(LocalContext.current),
                confirmLabel = stringResource(R.string.delete),
                destructive = true,
                onConfirm = {
                    onDelete(target)
                    pendingDelete = null
                },
                onDismiss = { pendingDelete = null }
            )
        }
    }
}

@Composable
fun BookmarkSettingsScreen(
    onBack: () -> Unit,
    onOpen: (BookmarkDirectory) -> Unit,
    onAddCurrent: () -> Unit,
    onAddCustom: (name: String, path: String) -> Unit,
    onRename: (BookmarkDirectory, String) -> Unit,
    onDelete: (BookmarkDirectory) -> Unit
) {
    val bookmarks by Settings.BOOKMARK_DIRECTORIES.observeAsState(
        Settings.BOOKMARK_DIRECTORIES.valueCompat
    )
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var renameTarget by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    ExpressiveScaffold(
        title = stringResource(R.string.settings_bookmark_directories_title),
        onNavigateUp = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.compose_add_bookmark)
                )
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = bookmarks,
                key = { it.id }
            ) { bookmark ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .clickable(
                                role = Role.Button,
                                onClick = { onOpen(bookmark) }
                            )
                            .padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = bookmark.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = bookmark.path.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { renameTarget = bookmark.id }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.rename)
                            )
                        }
                        IconButton(onClick = { pendingDelete = bookmark.id }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            item(key = "add-current") {
                FilledTonalButton(
                    onClick = onAddCurrent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.compose_add_current_folder))
                }
            }
        }
    }
    if (showAdd) {
        BookmarkEditDialog(
            title = stringResource(R.string.compose_add_bookmark),
            initialName = "",
            initialPath = "",
            onConfirm = { name, path ->
                onAddCustom(name, path)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
    val renameId = renameTarget
    val renameBookmark = bookmarks.find { it.id == renameId }
    if (renameId != null) {
        if (renameBookmark == null) {
            renameTarget = null
        } else {
            BookmarkEditDialog(
                title = stringResource(R.string.rename),
                initialName = renameBookmark.customName.orEmpty(),
                initialPath = renameBookmark.path.toString(),
                pathEditable = false,
                onConfirm = { name, _ ->
                    onRename(renameBookmark, name)
                    renameTarget = null
                },
                onDismiss = { renameTarget = null }
            )
        }
    }
    val deleteId = pendingDelete
    val deleteBookmark = bookmarks.find { it.id == deleteId }
    if (deleteId != null) {
        if (deleteBookmark == null) {
            pendingDelete = null
        } else {
            ConfirmDialog(
                title = stringResource(R.string.delete),
                message = deleteBookmark.name,
                confirmLabel = stringResource(R.string.delete),
                destructive = true,
                onConfirm = {
                    onDelete(deleteBookmark)
                    pendingDelete = null
                },
                onDismiss = { pendingDelete = null }
            )
        }
    }
}

@Composable
fun StandardDirectorySettingsScreen(
    directories: List<StandardDirectory>,
    isEnabled: (StandardDirectory) -> Boolean,
    onToggle: (StandardDirectory, Boolean) -> Unit,
    onBack: () -> Unit
) {
    ExpressiveScaffold(
        title = stringResource(R.string.settings_standard_directories_title),
        onNavigateUp = onBack
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = directories,
                key = { it.key }
            ) { directory ->
                val checked = isEnabled(directory)
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .toggleable(
                                value = checked,
                                role = Role.Switch,
                                onValueChange = { onToggle(directory, it) }
                            )
                            .padding(start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(directory.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = directory.getTitle(LocalContext.current),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = getExternalStorageDirectory(directory.relativePath),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = checked,
                            onCheckedChange = null,
                            thumbContent = if (checked) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkEditDialog(
    title: String,
    initialName: String,
    initialPath: String,
    pathEditable: Boolean = true,
    onConfirm: (name: String, path: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var path by rememberSaveable { mutableStateOf(initialPath) }
    val canConfirm = if (pathEditable) {
        path.isNotBlank()
    } else {
        true
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.compose_bookmark_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (pathEditable) {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text(stringResource(R.string.compose_enter_folder_path)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), path.trim()) },
                enabled = canConfirm
            ) {
                Text(stringResource(R.string.compose_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.compose_cancel))
            }
        }
    )
}

@Composable
fun DefaultFolderDialog(
    initialPath: String,
    onConfirm: (String) -> Unit,
    onUseCurrentFolder: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var path by rememberSaveable { mutableStateOf(initialPath) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_default_directory_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text(stringResource(R.string.compose_enter_folder_path)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onUseCurrentFolder) {
                        Text(stringResource(R.string.compose_use_current_folder))
                    }
                    TextButton(onClick = {
                        onReset()
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.compose_reset_to_default))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(path.trim())
                    onDismiss()
                },
                enabled = path.isNotBlank()
            ) {
                Text(stringResource(R.string.compose_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.compose_cancel))
            }
        }
    )
}
