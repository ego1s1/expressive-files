/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.features.filelist.FileListScreen
import me.zhanghai.android.files.compose.features.filelist.FileSortBy
import me.zhanghai.android.files.compose.features.filelist.SortSheet
import me.zhanghai.android.files.compose.features.home.HomeScreen
import me.zhanghai.android.files.compose.features.navigation.AppDrawer
import me.zhanghai.android.files.compose.features.navigation.BottomPillNav
import me.zhanghai.android.files.compose.features.navigation.LocationsScreen
import me.zhanghai.android.files.compose.features.onboarding.PermissionRationaleSheet
import me.zhanghai.android.files.compose.features.operations.CreateEntrySheet
import me.zhanghai.android.files.compose.features.operations.DeleteConfirmDialog
import me.zhanghai.android.files.compose.features.operations.RenameDialog
import me.zhanghai.android.files.compose.features.properties.PropertiesScreen
import me.zhanghai.android.files.compose.features.settings.AboutScreen
import me.zhanghai.android.files.compose.features.search.SearchScreen
import me.zhanghai.android.files.compose.features.settings.SettingsScreen
import me.zhanghai.android.files.compose.features.viewer.ImageViewerScreen
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.HomeUiState
import me.zhanghai.android.files.compose.model.PropertiesUiState
import me.zhanghai.android.files.compose.model.PropertyRowUi
import me.zhanghai.android.files.compose.model.PropertySectionUi
import me.zhanghai.android.files.compose.state.FavoritesStore
import me.zhanghai.android.files.compose.state.NavigationStateBuilder
import me.zhanghai.android.files.compose.state.RecentsStore
import me.zhanghai.android.files.compose.state.SettingsStateBuilder
import me.zhanghai.android.files.compose.state.mapFileListState
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.fileSize
import me.zhanghai.android.files.file.formatShort
import me.zhanghai.android.files.file.icon
import me.zhanghai.android.files.file.fileProviderUri
import me.zhanghai.android.files.filelist.FileItemSet
import me.zhanghai.android.files.filelist.FileListViewModel
import me.zhanghai.android.files.filelist.FileSortOptions
import me.zhanghai.android.files.filelist.FileViewType
import me.zhanghai.android.files.filelist.OpenFileActivity
import me.zhanghai.android.files.filelist.fileItemSetOf
import me.zhanghai.android.files.filelist.isListable
import me.zhanghai.android.files.filelist.listablePath
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.settings.SettingsActivity
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.createSendStreamIntent
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.withChooser

private const val ROUTE_FILES = "files"
private const val ROUTE_RECENTS = "recents"
private const val ROUTE_FAVORITES = "favorites"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_ABOUT = "about"
private const val ROUTE_LOCATIONS = "locations"
private const val ROUTE_SEARCH = "search"

/** Phase 1 of the bottom-navigation migration; flip to false to restore the drawer. */
private const val USE_BOTTOM_PILL_NAV = true

/**
 * Compose-first application shell.
 *
 * The file list, drawer, home, settings and about routes are native Compose. Legacy surfaces
 * (properties, viewers, server setup, FTP) remain reachable and are tracked in docs/MIGRATION.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveFilesApp() {
    val context = LocalContext.current
    val viewModel: FileListViewModel = viewModel()
    // The legacy LiveData graph requires a non-null trail before it is observed.
    // This is an idempotent initialization, not a per-frame side effect.
    remember(viewModel) {
        if (!viewModel.hasTrail) {
            viewModel.resetTo(defaultPath())
        }
    }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)

    var hasStorageAccess by remember { mutableStateOf(isStorageAccessGranted(context)) }
    var permissionDismissed by rememberSaveable { mutableStateOf(false) }
    val allFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasStorageAccess = isStorageAccessGranted(context)
    }
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasStorageAccess = isStorageAccessGranted(context)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasStorageAccess = isStorageAccessGranted(context)
    }

    LaunchedEffect(hasStorageAccess) {
        FavoritesStore.initialize(context)
        RecentsStore.initialize(context)
        if (hasStorageAccess) {
            viewModel.reload()
        }
    }

    val favorites by FavoritesStore.favorites.collectAsStateWithLifecycle()
    val recents by RecentsStore.recents.collectAsStateWithLifecycle()

    val currentPath by viewModel.currentPathLiveData.observeAsState()
    val breadcrumb by viewModel.breadcrumbLiveData.observeAsState()
    val fileListStateful: Stateful<List<FileItem>> by viewModel.fileListLiveData.observeAsState(
        Loading<List<FileItem>>(null)
    )
    val viewType by viewModel.viewTypeLiveData.observeAsState(FileViewType.LIST)
    val sortOptions: FileSortOptions by viewModel.sortOptionsLiveData.observeAsState(
        FileSortOptions(FileSortOptions.By.NAME, FileSortOptions.Order.ASCENDING, true)
    )

    // The legacy FileItemSet is mutated in place; this epoch forces Compose to re-read it.
    var selectionEpoch by remember { mutableIntStateOf(0) }
    val selectedFiles = remember(selectionEpoch) {
        runCatching { viewModel.selectedFilesLiveData.valueCompat }.getOrNull() ?: fileItemSetOf()
    }
    DisposableEffect(viewModel) {
        val observer = Observer<FileItemSet> { selectionEpoch++ }
        viewModel.selectedFilesLiveData.observeForever(observer)
        onDispose { viewModel.selectedFilesLiveData.removeObserver(observer) }
    }

    var route by rememberSaveable { mutableStateOf(ROUTE_FILES) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSortSheet by remember { mutableStateOf(false) }
    var createTarget by remember { mutableStateOf<Boolean?>(null) }
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<FileItem?>(null) }
    var propertiesFile by remember { mutableStateOf<FileItem?>(null) }
    var imageViewerFile by remember { mutableStateOf<FileItem?>(null) }
    var settingsTick by remember { mutableIntStateOf(0) }
    var settingsQuery by remember { mutableStateOf("") }

    val files = fileListStateful.value.orEmpty()
    val filesByPath = remember(files) { files.associateBy { it.path.toString() } }
    val selectedCount = selectedFiles.size

    LaunchedEffect(route, searchQuery) {
        if (route != ROUTE_SEARCH) {
            viewModel.stopSearching()
            return@LaunchedEffect
        }
        delay(350)
        if (searchQuery.isBlank()) {
            viewModel.stopSearching()
        } else {
            viewModel.search(searchQuery)
        }
    }

    val layoutMode = if (viewType == FileViewType.GRID) {
        FileLayoutMode.GRID
    } else {
        FileLayoutMode.LIST
    }
    val sortLabel = when (sortOptions.by) {
        FileSortOptions.By.NAME -> "Name" // TODO: localize
        FileSortOptions.By.TYPE -> "Type" // TODO: localize
        FileSortOptions.By.SIZE -> "Size" // TODO: localize
        FileSortOptions.By.LAST_MODIFIED -> "Modified" // TODO: localize
    }
    val uiState: me.zhanghai.android.files.compose.model.FileListUiState = remember(
        fileListStateful,
        currentPath,
        breadcrumb,
        selectionEpoch,
        layoutMode,
        sortLabel,
        searchQuery
    ) {
        mapFileListState(
            context = context,
            currentPath = currentPath,
            stateful = fileListStateful,
            breadcrumb = breadcrumb,
            selectedFiles = selectedFiles,
            layoutMode = layoutMode,
            sortLabel = sortLabel,
            query = searchQuery,
            isSearching = route == ROUTE_SEARCH,
            canPaste = false,
            canNavigateUp = (breadcrumb?.selectedIndex ?: 0) > 0
        )
    }

    val navigationState = remember(route, currentPath, favorites, recents) {
        NavigationStateBuilder.build(context, currentPath)
    }

    val canHandleBack = imageViewerFile != null || propertiesFile != null || selectedCount > 0 ||
        route != ROUTE_FILES || (breadcrumb?.selectedIndex ?: 0) > 0
    BackHandler(enabled = canHandleBack) {
        when {
            imageViewerFile != null -> imageViewerFile = null
            propertiesFile != null -> propertiesFile = null
            route == ROUTE_SEARCH -> {
                searchQuery = ""
                route = ROUTE_FILES
                viewModel.stopSearching()
            }

            selectedCount > 0 -> viewModel.clearSelectedFiles()
            route == ROUTE_SETTINGS || route == ROUTE_ABOUT -> route = ROUTE_LOCATIONS
            route != ROUTE_FILES -> route = ROUTE_FILES
            else -> viewModel.navigateUp()
        }
    }

    if (!hasStorageAccess && !permissionDismissed) {
        PermissionRationaleSheet(
            title = "Allow access to your files", // TODO: localize
            body = "Expressive Files needs file access to browse, copy, and organize files on this device.", // TODO: localize
            primaryLabel = "Continue", // TODO: localize
            onPrimary = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    runCatching { allFilesLauncher.launch(intent) }.onFailure {
                        runCatching {
                            allFilesLauncher.launch(
                                Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            )
                        }
                    }
                } else {
                    legacyPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            },
            onDismiss = { permissionDismissed = true }
        )
    }

    val showBottomPill = USE_BOTTOM_PILL_NAV && selectedCount == 0 &&
        imageViewerFile == null && propertiesFile == null

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !USE_BOTTOM_PILL_NAV && route == ROUTE_FILES,
        drawerContent = {
            if (!USE_BOTTOM_PILL_NAV) AppDrawer(
                state = navigationState,
                currentRoute = route,
                onNavigate = { newRoute ->
                    route = newRoute
                    scope.launch { drawerState.close() }
                },
                onEntryClick = { entry ->
                    scope.launch { drawerState.close() }
                    when (entry.id) {
                        NavigationStateBuilder.ACTION_SETTINGS -> route = ROUTE_SETTINGS
                        NavigationStateBuilder.ACTION_ABOUT -> route = ROUTE_ABOUT
                        else -> openPath(viewModel, entry.id)
                    }
                },
                onSettings = {
                    scope.launch { drawerState.close() }
                    route = ROUTE_SETTINGS
                },
                onAbout = {
                    scope.launch { drawerState.close() }
                    route = ROUTE_ABOUT
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomPill) 88.dp else 0.dp)
            ) {
        val imageViewer = imageViewerFile
        val properties = propertiesFile
        if (imageViewer != null) {
            ComposeImageViewer(
                file = imageViewer,
                onBack = { imageViewerFile = null },
                onShare = { shareFiles(context, listOf(imageViewer)) },
                onDelete = {
                    viewModel.replaceSelectedFiles(fileItemSetOf(imageViewer))
                    showDelete = true
                },
                onInfo = { propertiesFile = imageViewer }
            )
        } else if (properties != null) {
            PropertiesScreen(
                state = remember(properties) { buildPropertiesUiState(context, properties) },
                onTabSelected = {},
                onCopy = { value ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("value", value))
                },
                onBack = { propertiesFile = null }
            )
        } else when (route) {
            ROUTE_RECENTS -> HomeScreen(
                title = "Recents", // TODO: localize
                state = HomeUiState(recents = recents, favorites = favorites),
                onOpen = { shortcut ->
                    if (shortcut.isDirectory) {
                        openPath(viewModel, shortcut.path)
                    } else {
                        openFileIntent(context, shortcut.path)
                    }
                    route = ROUTE_FILES
                },
                onToggleFavorite = { shortcut ->
                    runCatching { Paths.get(shortcut.path) }
                        .getOrNull()
                        ?.let(FavoritesStore::remove)
                },
                onOpenDrawer = if (USE_BOTTOM_PILL_NAV) null else ({ scope.launch { drawerState.open() } })
            )

            ROUTE_FAVORITES -> HomeScreen(
                title = "Favorites", // TODO: localize
                state = HomeUiState(recents = recents, favorites = favorites),
                onOpen = { shortcut ->
                    if (shortcut.isDirectory) {
                        openPath(viewModel, shortcut.path)
                    } else {
                        openFileIntent(context, shortcut.path)
                    }
                    route = ROUTE_FILES
                },
                onToggleFavorite = { shortcut ->
                    runCatching { Paths.get(shortcut.path) }
                        .getOrNull()
                        ?.let(FavoritesStore::remove)
                },
                onOpenDrawer = if (USE_BOTTOM_PILL_NAV) null else ({ scope.launch { drawerState.open() } })
            )

            ROUTE_SEARCH -> SearchScreen(
                query = searchQuery,
                state = uiState,
                onQueryChange = { searchQuery = it },
                onBack = {
                    searchQuery = ""
                    route = ROUTE_FILES
                    viewModel.stopSearching()
                },
                onEntryClick = { entry ->
                    val file = filesByPath[entry.path] ?: return@SearchScreen
                    openEntry(context, viewModel, file)
                    if (file.isListable) {
                        searchQuery = ""
                        route = ROUTE_FILES
                    }
                },
                onEntryLongClick = {},
                onEntryMenu = { entry -> actionTarget = filesByPath[entry.path] }
            )

            ROUTE_LOCATIONS -> LocationsScreen(
                state = navigationState,
                onEntryClick = { entry ->
                    when (entry.id) {
                        NavigationStateBuilder.ACTION_SETTINGS -> route = ROUTE_SETTINGS
                        NavigationStateBuilder.ACTION_ABOUT -> route = ROUTE_ABOUT
                        else -> {
                            openPath(viewModel, entry.id)
                            route = ROUTE_FILES
                        }
                    }
                },
                onSettings = { route = ROUTE_SETTINGS },
                onAbout = { route = ROUTE_ABOUT }
            )

            ROUTE_SETTINGS -> {
                val settingsState = remember(settingsTick, settingsQuery) {
                    SettingsStateBuilder.build(context, settingsQuery)
                }
                SettingsScreen(
                    state = settingsState,
                    onQueryChange = { settingsQuery = it },
                    onItemClick = { item ->
                        applySetting(context, item.key, item.checked == true)
                        settingsTick++
                    },
                    onBack = { route = ROUTE_LOCATIONS },
                    onOpenLegacySettings = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }
                )
            }

            ROUTE_ABOUT -> AboutScreen(
                appName = context.getString(R.string.app_name),
                version = runCatching { context.getString(R.string.app_version) }.getOrDefault(""),
                onBack = { route = ROUTE_LOCATIONS },
                onOpenLink = { url ->
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                    }
                }
            )

            else -> FileListScreen(
                state = uiState,
                onOpenDrawer = if (USE_BOTTOM_PILL_NAV) null else ({ scope.launch { drawerState.open() } }),
                onNavigateUp = { viewModel.navigateUp() },
                onBreadcrumbClick = { crumb -> openPath(viewModel, crumb.path) },
                onEntryClick = { entry ->
                    val file = filesByPath[entry.path] ?: return@FileListScreen
                    if (selectedCount > 0) {
                        viewModel.selectFile(file, !(file in selectedFiles))
                    } else if (file.mimeType.type == "image") {
                        RecentsStore.record(file)
                        imageViewerFile = file
                    } else {
                        openEntry(context, viewModel, file)
                    }
                },
                onEntryLongClick = { entry ->
                    filesByPath[entry.path]?.let { viewModel.selectFile(it, true) }
                },
                onEntryMenu = { entry -> actionTarget = filesByPath[entry.path] },
                onSortClick = { showSortSheet = true },
                onLayoutToggle = {
                    viewModel.viewType = if (viewType == FileViewType.LIST) {
                        FileViewType.GRID
                    } else {
                        FileViewType.LIST
                    }
                },
                onRefresh = { viewModel.reload() },
                onSelectAll = {
                    if (files.isNotEmpty()) {
                        viewModel.replaceSelectedFiles(fileItemSetOf(*files.toTypedArray()))
                    }
                },
                onCreateFolder = { createTarget = true },
                onCreateFile = { createTarget = false },
                onPaste = {},
                onSelectionShare = {
                    shareFiles(context, selectedFiles.toList())
                    viewModel.clearSelectedFiles()
                },
                onSelectionDelete = { showDelete = true },
                onSelectionClear = { viewModel.clearSelectedFiles() },
                onSelectionMore = {
                    if (files.isNotEmpty()) {
                        viewModel.replaceSelectedFiles(fileItemSetOf(*files.toTypedArray()))
                    }
                }
            )
        }
            }
            if (showBottomPill) {
                BottomPillNav(
                    currentRoute = route,
                    onNavigate = { route = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }

    if (showSortSheet) {
        SortSheet(
            layoutMode = layoutMode,
            sortBy = sortOptions.by.toFileSortBy(),
            ascending = sortOptions.order == FileSortOptions.Order.ASCENDING,
            foldersFirst = sortOptions.isDirectoriesFirst,
            onLayoutModeChange = { mode ->
                viewModel.viewType = if (mode == FileLayoutMode.GRID) {
                    FileViewType.GRID
                } else {
                    FileViewType.LIST
                }
            },
            onSortSelected = { by -> viewModel.setSortBy(by.toFileSortOptionsBy()) },
            onAscendingChange = { ascending ->
                viewModel.setSortOrder(
                    if (ascending) {
                        FileSortOptions.Order.ASCENDING
                    } else {
                        FileSortOptions.Order.DESCENDING
                    }
                )
            },
            onFoldersFirstChange = { viewModel.setSortDirectoriesFirst(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    createTarget?.let { isDirectory ->
        CreateEntrySheet(
            isDirectory = isDirectory,
            existingNames = files.map { it.name },
            onConfirm = { name ->
                val directory = currentPath
                if (directory != null) {
                    FileJobService.create(directory.resolve(name), isDirectory, context)
                }
                createTarget = null
            },
            onDismiss = { createTarget = null }
        )
    }

    renameTarget?.let { file ->
        RenameDialog(
            initialName = file.name,
            existingNames = files.filter { it.path != file.path }.map { it.name },
            onConfirm = { newName ->
                FileJobService.rename(file.path, newName, context)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    if (showDelete) {
        val selected = selectedFiles.toList()
        DeleteConfirmDialog(
            count = selected.size,
            names = selected.map { it.name },
            onConfirm = {
                FileJobService.delete(selected.map { it.path }, context)
                viewModel.clearSelectedFiles()
                showDelete = false
            },
            onDismiss = { showDelete = false }
        )
    }

    actionTarget?.let { file ->
        FileActionSheet(
            file = file,
            isFavorite = FavoritesStore.isFavorite(file.path),
            onOpen = {
                openEntry(context, viewModel, file)
                actionTarget = null
            },
            onRename = {
                renameTarget = file
                actionTarget = null
            },
            onShare = {
                shareFiles(context, listOf(file))
                actionTarget = null
            },
            onDelete = {
                viewModel.replaceSelectedFiles(fileItemSetOf(file))
                showDelete = true
                actionTarget = null
            },
            onToggleFavorite = {
                FavoritesStore.toggle(file)
                actionTarget = null
            },
            onCopyPath = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("path", file.path.toString()))
                actionTarget = null
            },
            onProperties = {
                propertiesFile = file
                actionTarget = null
            },
            onDismiss = { actionTarget = null }
        )
    }
}

@Composable
private fun ComposeImageViewer(
    file: FileItem,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, file) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                file.path.newInputStream().use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
    ImageViewerScreen(
        bitmap = bitmap,
        title = file.name,
        subtitle = null,
        onBack = onBack,
        onShare = onShare,
        onDelete = onDelete,
        onInfo = onInfo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileActionSheet(
    file: FileItem,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyPath: () -> Unit,
    onProperties: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            ActionRow("Open", onOpen) // TODO: localize
            ActionRow("Rename", onRename) // TODO: localize
            ActionRow("Share", onShare) // TODO: localize
            ActionRow(
                if (isFavorite) "Remove from favorites" else "Add to favorites", // TODO: localize
                onToggleFavorite
            )
            ActionRow("Copy path", onCopyPath) // TODO: localize
            ActionRow("Properties", onProperties) // TODO: localize
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            ActionRow("Delete", onDelete, destructive = true) // TODO: localize
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = if (destructive) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

private fun buildPropertiesUiState(context: Context, file: FileItem): PropertiesUiState {
    val attributes = file.attributes
    val rows = buildList {
        add(PropertyRowUi("Name", file.name, copyable = true)) // TODO: localize
        add(PropertyRowUi("Path", file.path.toString(), copyable = true)) // TODO: localize
        if (!attributes.isDirectory) {
            add(PropertyRowUi("Size", attributes.fileSize.formatHumanReadable(context))) // TODO: localize
        }
        add(
            PropertyRowUi(
                "Modified", // TODO: localize
                attributes.lastModifiedTime().toInstant().formatShort(context)
            )
        )
        add(
            PropertyRowUi(
                "Type", // TODO: localize
                if (attributes.isDirectory) "Folder" else file.mimeType.value // TODO: localize
            )
        )
    }
    return PropertiesUiState(
        title = file.name,
        subtitle = file.path.toString(),
        iconRes = file.mimeType.icon.resourceId,
        tabTitles = listOf("Basic"), // TODO: localize
        selectedTab = 0,
        sections = listOf(PropertySectionUi("File", rows)) // TODO: localize
    )
}

private fun isStorageAccessGranted(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }

private fun defaultPath(): Path = runCatching {
    Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat
}.getOrElse {
    Paths.get(Environment.getExternalStorageDirectory().absolutePath)
}

private fun openEntry(context: Context, viewModel: FileListViewModel, file: FileItem) {
    RecentsStore.record(file)
    viewModel.clearSelectedFiles()
    if (file.isListable) {
        viewModel.navigateTo(Bundle(), file.listablePath)
    } else {
        runCatching {
            context.startActivity(OpenFileActivity.createIntent(file.path, file.mimeType))
        }
    }
}

private fun openPath(viewModel: FileListViewModel, pathString: String) {
    val path = runCatching { Paths.get(pathString) }.getOrNull() ?: return
    viewModel.clearSelectedFiles()
    viewModel.navigateTo(Bundle(), path)
}

private fun openFileIntent(context: Context, pathString: String) {
    val path = runCatching { Paths.get(pathString) }.getOrNull() ?: return
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(path.fileProviderUri, "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }
}

private fun shareFiles(context: Context, files: List<FileItem>) {
    if (files.isEmpty()) {
        return
    }
    runCatching {
        val uris = files.map { it.path.fileProviderUri }
        val mimeTypes = files.map { it.mimeType }
        context.startActivity(uris.createSendStreamIntent(mimeTypes).withChooser())
    }
}

private fun applySetting(context: Context, key: String, checked: Boolean) {
    when (key) {
        "appearance_black_theme" -> Settings.BLACK_NIGHT_MODE.putValue(!checked)
        "appearance_animations" -> Settings.FILE_LIST_ANIMATION.putValue(!checked)
        "behavior_show_hidden" -> Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(!checked)
        else -> context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}

private fun FileSortOptions.By.toFileSortBy(): FileSortBy = when (this) {
    FileSortOptions.By.NAME -> FileSortBy.NAME
    FileSortOptions.By.TYPE -> FileSortBy.TYPE
    FileSortOptions.By.SIZE -> FileSortBy.SIZE
    FileSortOptions.By.LAST_MODIFIED -> FileSortBy.LAST_MODIFIED
}

private fun FileSortBy.toFileSortOptionsBy(): FileSortOptions.By = when (this) {
    FileSortBy.NAME -> FileSortOptions.By.NAME
    FileSortBy.TYPE -> FileSortOptions.By.TYPE
    FileSortBy.SIZE -> FileSortOptions.By.SIZE
    FileSortBy.LAST_MODIFIED -> FileSortOptions.By.LAST_MODIFIED
}
