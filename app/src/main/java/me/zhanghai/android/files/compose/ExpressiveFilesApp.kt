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
import androidx.activity.BackEventCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import java.util.Locale
import androidx.lifecycle.Observer
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.features.filelist.CreateFabButton
import me.zhanghai.android.files.compose.features.filelist.CreateFabMenuItems
import me.zhanghai.android.files.compose.features.filelist.FileListScreen
import me.zhanghai.android.files.compose.features.filelist.FileSortBy
import me.zhanghai.android.files.compose.features.filelist.SelectionOptionsSheet
import me.zhanghai.android.files.compose.features.filelist.SortSheet
import me.zhanghai.android.files.compose.features.navigation.BottomPillNav
import me.zhanghai.android.files.compose.features.navigation.PillNavRail
import me.zhanghai.android.files.compose.features.navigation.LocationsScreen
import me.zhanghai.android.files.compose.features.onboarding.PermissionRationaleSheet
import me.zhanghai.android.files.compose.features.operations.CreateEntrySheet
import me.zhanghai.android.files.compose.features.operations.DeleteConfirmDialog
import me.zhanghai.android.files.compose.features.operations.RenameDialog
import me.zhanghai.android.files.compose.features.properties.PropertiesScreen
import me.zhanghai.android.files.compose.features.settings.AboutScreen
import me.zhanghai.android.files.compose.features.settings.BookmarkSettingsScreen
import me.zhanghai.android.files.compose.features.settings.DefaultFolderDialog
import me.zhanghai.android.files.compose.features.settings.SettingsChoiceDialog
import me.zhanghai.android.files.compose.features.settings.StandardDirectorySettingsScreen
import me.zhanghai.android.files.compose.features.settings.StorageSettingsScreen
import me.zhanghai.android.files.compose.features.search.SearchScreen
import me.zhanghai.android.files.compose.features.settings.SettingsScreen
import me.zhanghai.android.files.compose.features.viewer.ImageViewerScreen
import me.zhanghai.android.files.compose.features.viewer.TextEditorScreen
import me.zhanghai.android.files.compose.components.ConfirmDialog
import me.zhanghai.android.files.compose.components.EmptyState
import me.zhanghai.android.files.compose.components.LoadingState
import me.zhanghai.android.files.compose.model.FileLayoutMode
import me.zhanghai.android.files.compose.model.FileListUiState
import me.zhanghai.android.files.compose.model.PropertiesUiState
import me.zhanghai.android.files.compose.model.PropertyRowUi
import me.zhanghai.android.files.compose.model.PropertySectionUi
import me.zhanghai.android.files.compose.model.SettingsItemKind
import me.zhanghai.android.files.compose.model.SettingsItemUi
import me.zhanghai.android.files.compose.state.NavigationStateBuilder
import me.zhanghai.android.files.compose.state.SettingsStateBuilder
import me.zhanghai.android.files.compose.state.applySettingChoice
import me.zhanghai.android.files.compose.state.applySettingSwitch
import me.zhanghai.android.files.compose.state.mapFileListState
import me.zhanghai.android.files.compose.state.putDefaultDirectory
import me.zhanghai.android.files.compose.state.resetDefaultDirectory
import me.zhanghai.android.files.compose.state.resetSettingsToDefaults
import me.zhanghai.android.files.compose.theme.ExpressiveAccelerateEasing
import me.zhanghai.android.files.compose.theme.ExpressiveDecelerateEasing
import me.zhanghai.android.files.compose.theme.ExpressiveMotion
import me.zhanghai.android.files.compose.theme.WindowSizes
import me.zhanghai.android.files.compose.theme.rememberMotionEnabled
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
import me.zhanghai.android.files.navigation.BookmarkDirectories
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.StandardDirectoriesLiveData
import me.zhanghai.android.files.navigation.StandardDirectory
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.AddStorageDialogActivity
import me.zhanghai.android.files.storage.Storage
import me.zhanghai.android.files.storage.Storages
import me.zhanghai.android.files.filelist.OpenApkDefaultAction
import me.zhanghai.android.files.theme.night.NightMode
import me.zhanghai.android.files.theme.night.NightModeHelper
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.createSendStreamIntent
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.withChooser

private const val ROUTE_FILES = "files"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_SETTINGS_STORAGES = "settings/storages"
private const val ROUTE_SETTINGS_BOOKMARKS = "settings/bookmarks"
private const val ROUTE_SETTINGS_STANDARD_DIRS = "settings/standard-dirs"
private const val ROUTE_ABOUT = "about"
private const val ROUTE_LOCATIONS = "locations"
private const val ROUTE_SEARCH = "search"

/** Sibling order used to direct route transitions between peer destinations. */
private val RouteTransitionOrder = listOf(
    ROUTE_FILES,
    ROUTE_LOCATIONS,
    ROUTE_SETTINGS,
    ROUTE_SETTINGS_STORAGES,
    ROUTE_SETTINGS_BOOKMARKS,
    ROUTE_SETTINGS_STANDARD_DIRS,
    ROUTE_SEARCH,
    ROUTE_ABOUT
)

/**
 * Whether navigating from [from] to [to] moves forward. Leaving Files is forward,
 * returning to Files is back, Search behaves as an overlay (in is forward, out is back),
 * and About and the settings sub-screens are children of Settings.
 */
internal fun isForwardRouteTransition(from: String, to: String): Boolean = when {
    to == ROUTE_SEARCH -> true
    from == ROUTE_SEARCH -> false
    to == ROUTE_FILES -> false
    from == ROUTE_FILES -> true
    to == ROUTE_ABOUT || to in SettingsSubRoutes -> true
    from == ROUTE_ABOUT || from in SettingsSubRoutes -> false
    else -> RouteTransitionOrder.indexOf(to) >= RouteTransitionOrder.indexOf(from)
}

private val SettingsSubRoutes = setOf(
    ROUTE_SETTINGS_STORAGES,
    ROUTE_SETTINGS_BOOKMARKS,
    ROUTE_SETTINGS_STANDARD_DIRS
)

private val AllRoutes = RouteTransitionOrder.toSet()

/**
 * Compose-first application shell.
 *
 * The file list, drawer, home, settings and about routes are native Compose. Legacy surfaces
 * (properties, viewers, server setup, FTP) remain reachable and are tracked in docs/MIGRATION.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveFilesApp(
    initialRoute: String? = null
) {    val context = LocalContext.current
    val viewModel: FileListViewModel = viewModel()
    // The legacy LiveData graph requires a non-null trail before it is observed.
    // This must stay a synchronous remember: moving it to LaunchedEffect lets the
    // BreadcrumbLiveData observer attach while the trail is still null and crash.
    remember(viewModel) {
        if (!viewModel.hasTrail) {
            viewModel.resetTo(defaultPath())
        }
        viewModel.hasTrail
    }
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

    // The file list live data loads itself whenever the trail changes, so an
    // unconditional reload here duplicated the very first listing (double IO + flash).
    // Only reload on a genuine permission transition, which is the one case where the
    // existing listing could not have been produced.
    var previousStorageAccess by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(hasStorageAccess) {
        val previous = previousStorageAccess
        previousStorageAccess = hasStorageAccess
        if (previous == false && hasStorageAccess) {
            viewModel.reload()
        }
    }

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

    var route by rememberSaveable {
        mutableStateOf(initialRoute?.takeIf { it in AllRoutes } ?: ROUTE_FILES)
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSortSheet by remember { mutableStateOf(false) }
    var createTarget by remember { mutableStateOf<Boolean?>(null) }
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var showSelectionOptions by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<FileItem?>(null) }
    var propertiesFile by remember { mutableStateOf<FileItem?>(null) }
    var imageViewerFile by remember { mutableStateOf<FileItem?>(null) }
    var textEditorFile by remember { mutableStateOf<FileItem?>(null) }
    var textEditorContent by remember { mutableStateOf<String?>(null) }
    var textEditorDirty by remember { mutableStateOf(false) }
    var textEditorReload by remember { mutableIntStateOf(0) }
    var showDiscardText by remember { mutableStateOf(false) }
    var settingsTick by remember { mutableIntStateOf(0) }
    var settingsQuery by remember { mutableStateOf("") }
    var choiceTarget by remember { mutableStateOf<SettingsItemUi?>(null) }
    var showDefaultFolder by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var createMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val localeSystemLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        settingsTick++
    }

    val files = fileListStateful.value.orEmpty()
    val filesByPath = remember(files) { files.associateBy { it.path.toString() } }
    val selectedCount = selectedFiles.size

    // The create menu belongs to its folder: navigating away collapses it so it
    // never lingers over a new directory.
    LaunchedEffect(route, currentPath) {
        createMenuExpanded = false
    }

    LaunchedEffect(textEditorFile, textEditorReload) {
        val file = textEditorFile ?: return@LaunchedEffect
        textEditorContent = null
        textEditorContent = withContext(Dispatchers.IO) {
            runCatching {
                if (file.attributes.size() > 1024L * 1024L) {
                    null
                } else {
                    file.path.newInputStream().use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }.getOrNull()
        }
        textEditorDirty = false
    }

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
        FileSortOptions.By.NAME -> stringResource(R.string.file_list_action_sort_by_name)
        FileSortOptions.By.TYPE -> stringResource(R.string.file_list_action_sort_by_type)
        FileSortOptions.By.SIZE -> stringResource(R.string.file_list_action_sort_by_size)
        FileSortOptions.By.LAST_MODIFIED -> stringResource(R.string.compose_modified)
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

    // Snapshot stack of the lists we descended from. A folder-up gesture has no route
    // change and the folder we leave is still the mounted one, so this is the only
    // source of a real "reveal the parent" preview during the drag.
    val listHistory = remember { mutableStateListOf<FileListUiState>() }

    fun pushListSnapshot() {
        listHistory.add(uiState)
    }

    fun popListSnapshot() {
        if (listHistory.isNotEmpty()) {
            listHistory.removeAt(listHistory.lastIndex)
        }
    }

    val navigationState = remember(route, currentPath) {
        NavigationStateBuilder.build(context, currentPath)
    }

    // Hoisted so settings sub-screens and the route transition can share one build.
    val settingsState = remember(settingsTick, settingsQuery) {
        SettingsStateBuilder.build(context, settingsQuery)
    }

    // The settings search is scoped to a visit: leaving the screen clears it so
    // returning never lands on a stale "no settings found" filter.
    LaunchedEffect(route) {
        if (route == ROUTE_SETTINGS) {
            settingsQuery = ""
        }
    }

    fun onSettingsItemClick(item: SettingsItemUi) {
        when (item.kind) {
            SettingsItemKind.SWITCH -> {
                applySettingSwitch(item.key, item.checked == true)
                settingsTick++
            }
            SettingsItemKind.CHOICE -> choiceTarget = item
            SettingsItemKind.ACTION -> when (item.key) {
                "appearance_locale" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        runCatching {
                            localeSystemLauncher.launch(
                                Intent(
                                    android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    }
                }
                "behavior_default_folder" -> showDefaultFolder = true
                "behavior_storages" -> route = ROUTE_SETTINGS_STORAGES
                "behavior_standard_dirs" -> route = ROUTE_SETTINGS_STANDARD_DIRS
                "behavior_bookmarks" -> route = ROUTE_SETTINGS_BOOKMARKS
                "advanced_reset" -> showResetConfirm = true
                else -> {}
            }
            else -> {}
        }
    }

    val overlayOpen = imageViewerFile != null || propertiesFile != null ||
        textEditorFile != null
    val canNavigateUp = (breadcrumb?.selectedIndex ?: 0) > 0
    // Folder-up joins the gesture too: the list persists through navigateUp's reload,
    // so the parallax settles seamlessly onto the parent once it arrives.
    val predictiveBackEnabled = selectedCount == 0 &&
        (route != ROUTE_FILES || overlayOpen || canNavigateUp)
    val motionEnabled = rememberMotionEnabled()
    val layoutDirection = LocalLayoutDirection.current
    val backProgress = remember { Animatable(0f) }
    // Drag direction of the in-flight gesture: +1 follows the finger, derived from the
    // system swipe edge and mirrored in RTL so a right-edge swipe never looks opposed.
    var backDragSign by remember { mutableFloatStateOf(1f) }
    PredictiveBackHandler(enabled = predictiveBackEnabled) { progress ->
        try {
            progress.collect { event ->
                backDragSign = (if (event.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f) *
                    (if (layoutDirection == LayoutDirection.Rtl) -1f else 1f)
                backProgress.snapTo(event.progress)
            }
            // Route-backed backs let the directional page transition be the reveal, so
            // the parallax is released first. Folder-up has no route change (the list
            // stays mounted), so it settles instead.
            var releasedToTransition = true
            when {
                textEditorFile != null -> {
                    releasedToTransition = false
                    if (textEditorDirty) {
                        showDiscardText = true
                    } else {
                        textEditorFile = null
                    }
                }

                imageViewerFile != null -> {
                    releasedToTransition = false
                    imageViewerFile = null
                }

                propertiesFile != null -> {
                    releasedToTransition = false
                    propertiesFile = null
                }

                route == ROUTE_SEARCH -> {
                    searchQuery = ""
                    viewModel.stopSearching()
                    route = ROUTE_FILES
                }

                route == ROUTE_ABOUT || route in SettingsSubRoutes -> route = ROUTE_SETTINGS
                route != ROUTE_FILES -> route = ROUTE_FILES
                canNavigateUp -> {
                    releasedToTransition = false
                    popListSnapshot()
                    viewModel.navigateUp()
                }
            }
            if (releasedToTransition || !motionEnabled) {
                backProgress.snapTo(0f)
            } else {
                // Critically damped: a full-window surface must land, not bounce.
                backProgress.animateTo(0f, ExpressiveMotion.settle())
            }
        } catch (e: CancellationException) {
            backProgress.animateTo(
                targetValue = 0f,
                animationSpec = if (motionEnabled) ExpressiveMotion.settle() else snap()
            )
            throw e
        }
    }

    val canHandleBack = textEditorFile != null || imageViewerFile != null ||
        propertiesFile != null || selectedCount > 0 || route != ROUTE_FILES ||
        (breadcrumb?.selectedIndex ?: 0) > 0
    BackHandler(enabled = canHandleBack && !predictiveBackEnabled) {
        when {
            textEditorFile != null -> {
                if (textEditorDirty) {
                    showDiscardText = true
                } else {
                    textEditorFile = null
                }
            }

            imageViewerFile != null -> imageViewerFile = null
            propertiesFile != null -> propertiesFile = null
            route == ROUTE_SEARCH -> {
                searchQuery = ""
                route = ROUTE_FILES
                viewModel.stopSearching()
            }

            selectedCount > 0 -> viewModel.clearSelectedFiles()
            route == ROUTE_ABOUT || route in SettingsSubRoutes -> route = ROUTE_SETTINGS
            route != ROUTE_FILES -> route = ROUTE_FILES
            else -> {
                popListSnapshot()
                viewModel.navigateUp()
            }
        }
    }

    if (!hasStorageAccess && !permissionDismissed) {
        PermissionRationaleSheet(
            title = stringResource(R.string.compose_allow_access_to_your_files),
            body = stringResource(R.string.compose_expressive_files_needs_file_access_to_browse_copy_an),
            primaryLabel = stringResource(R.string.compose_continue),
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

    val showNavigation = selectedCount == 0 && imageViewerFile == null &&
        propertiesFile == null && textEditorFile == null && route != ROUTE_SEARCH &&
        route !in SettingsSubRoutes

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= WindowSizes.MediumMinWidth
        val showNavLabels = maxWidth >= WindowSizes.PillLabelMinWidth
        val useTwoPane = maxWidth >= WindowSizes.ExpandedMinWidth
        Box(modifier = Modifier.fillMaxSize()) {
            // Real reveal preview during a back gesture: the parent folder's last known
            // listing for folder-up, or the target screen for route backs. Without this
            // the drag only shifted the current screen, which read as "no preview".
            val revealPreview: FileListUiState? =
                if (motionEnabled && predictiveBackEnabled && backProgress.value > 0.001f) {
                    when {
                        route == ROUTE_FILES && canNavigateUp -> listHistory.lastOrNull()
                        else -> null
                    }
                } else {
                    null
                }
            if (revealPreview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = if (showNavigation && useNavigationRail) 88.dp else 0.dp
                        )
                        .clearAndSetSemantics {}
                ) {
                    FileListScreen(
                        state = revealPreview,
                        onNavigateUp = {},
                        onBreadcrumbClick = {},
                        onEntryClick = {},
                        onEntryLongClick = {},
                        onEntryMenu = {},
                        onSearchClick = {},
                        onSortClick = {},
                        onLayoutToggle = {},
                        onRefresh = {},
                        onSelectAll = {},
                        onCreateFolder = {},
                        onCreateFile = {},
                        onPaste = {},
                        onSelectionShare = {},
                        onSelectionDelete = {},
                        onSelectionClear = {},
                        onSelectionMore = {}
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Gated by the motion switch: with animations off the content must
                        // land in its final state instead of tracking the finger.
                        val progress = if (motionEnabled) backProgress.value else 0f
                        translationX = backDragSign * progress * size.width * 0.08f
                        val scale = 1f - 0.02f * progress
                        scaleX = scale
                        scaleY = scale
                        // Outgoing content fades as it leaves, revealing the parent list
                        // underneath instead of just shifting the same screen.
                        alpha = 1f - 0.35f * progress
                    }
                    .padding(
                        start = if (showNavigation && useNavigationRail) 88.dp else 0.dp
                    )
            ) {
        val textEditor = textEditorFile
        val imageViewer = imageViewerFile
        val properties = propertiesFile
        if (textEditor != null) {
            val content = textEditorContent
            if (content == null) {
                LoadingState()
            } else {
                TextEditorScreen(
                    title = textEditor.name,
                    text = content,
                    isDirty = textEditorDirty,
                    onTextChange = {
                        textEditorContent = it
                        textEditorDirty = true
                    },
                    onSave = {
                        val bytes = (textEditorContent ?: "").toByteArray(Charsets.UTF_8)
                        FileJobService.write(textEditor.path, bytes, context, null)
                        textEditorDirty = false
                    },
                    onReload = { textEditorReload++ },
                    onBack = {
                        if (textEditorDirty) {
                            showDiscardText = true
                        } else {
                            textEditorFile = null
                        }
                    }
                )
            }
        } else if (imageViewer != null) {
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
        } else AnimatedContent(
            targetState = route,
            transitionSpec = {
                if (motionEnabled) {
                    val forward = isForwardRouteTransition(initialState, targetState)
                    // In LTR, forward content enters from the right; mirrored in RTL.
                    // A /4 slide with a fade reads as a screen turn; the old /12 shift
                    // was too subtle to register as motion.
                    val direction = (if (forward) 1 else -1) *
                        (if (layoutDirection == LayoutDirection.Rtl) -1 else 1)
                    (fadeIn(tween(300, easing = ExpressiveDecelerateEasing)) +
                        slideInHorizontally(tween(300, easing = ExpressiveDecelerateEasing)) {
                            direction * it / 4
                        }
                        ) togetherWith
                        (fadeOut(tween(200, easing = ExpressiveAccelerateEasing)) +
                            slideOutHorizontally(tween(200, easing = ExpressiveAccelerateEasing)) {
                                -direction * it / 4
                            })
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "route"
        ) { animatedRoute ->
            when (animatedRoute) {
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
                    when {
                        file.isListable -> {
                            openEntry(context, viewModel, file, onDescend = { pushListSnapshot() })
                            searchQuery = ""
                            route = ROUTE_FILES
                        }

                        file.mimeType.type == "image" -> imageViewerFile = file

                        file.isEditableText() -> textEditorFile = file

                        else -> openEntry(context, viewModel, file, onDescend = { pushListSnapshot() })
                    }
                },
                onEntryLongClick = {},
                onEntryMenu = { entry -> actionTarget = filesByPath[entry.path] }
            )

            ROUTE_LOCATIONS -> LocationsScreen(
                state = navigationState,
                onEntryClick = { entry ->
                    openPath(viewModel, entry.id, onDescend = { pushListSnapshot() })
                    route = ROUTE_FILES
                }
            )

            ROUTE_SETTINGS -> {
                SettingsScreen(
                    state = settingsState,
                    onQueryChange = { settingsQuery = it },
                    onItemClick = { item -> onSettingsItemClick(item) },
                    onBack = { route = ROUTE_FILES },
                    onAbout = { route = ROUTE_ABOUT }
                )
            }

            ROUTE_SETTINGS_STORAGES -> StorageSettingsScreen(
                onBack = { route = ROUTE_SETTINGS },
                onAdd = {
                    runCatching {
                        context.startActivity(AddStorageDialogActivity::class.createIntent())
                    }
                },
                onEdit = { storage ->
                    runCatching { context.startActivity(storage.createEditIntent()) }
                },
                onDelete = { storage ->
                    runCatching { Storages.remove(storage) }
                }
            )

            ROUTE_SETTINGS_BOOKMARKS -> BookmarkSettingsScreen(
                onBack = { route = ROUTE_SETTINGS },
                onOpen = { bookmark ->
                    openPath(viewModel, bookmark.path.toString(), onDescend = { pushListSnapshot() })
                    route = ROUTE_FILES
                },
                onAddCurrent = {
                    val path = currentPath ?: return@BookmarkSettingsScreen
                    val displayName = breadcrumb?.let { data ->
                        data.nameProducers.getOrNull(data.selectedIndex)?.invoke(context)
                    } ?: path.name
                    runCatching {
                        BookmarkDirectories.add(BookmarkDirectory(displayName, path))
                    }
                    settingsTick++
                },
                onAddCustom = { name, pathString ->
                    val path = runCatching { Paths.get(pathString) }.getOrNull()
                        ?: return@BookmarkSettingsScreen
                    runCatching {
                        BookmarkDirectories.add(
                            BookmarkDirectory(name.ifBlank { null }, path)
                        )
                    }
                    settingsTick++
                },
                onRename = { bookmark, name ->
                    runCatching {
                        BookmarkDirectories.replace(
                            bookmark.copy(customName = name.ifBlank { null })
                        )
                    }
                    settingsTick++
                },
                onDelete = { bookmark ->
                    runCatching { BookmarkDirectories.remove(bookmark) }
                    settingsTick++
                }
            )

            ROUTE_SETTINGS_STANDARD_DIRS -> {
                val standardDirs by StandardDirectoriesLiveData.observeAsState(emptyList())
                val standardSettings by Settings.STANDARD_DIRECTORY_SETTINGS.observeAsState(
                    Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat
                )
                StandardDirectorySettingsScreen(
                    directories = standardDirs,
                    isEnabled = { directory ->
                        standardSettings.find { it.id == directory.key }?.isEnabled
                            ?: directory.isEnabled
                    },
                    onToggle = { directory, enabled ->
                        val settingsList = standardSettings.toMutableList()
                        val index = settingsList.indexOfFirst { it.id == directory.key }
                        if (index != -1) {
                            settingsList[index] = settingsList[index].copy(isEnabled = enabled)
                        } else {
                            settingsList += directory.toSettings().copy(isEnabled = enabled)
                        }
                        runCatching {
                            Settings.STANDARD_DIRECTORY_SETTINGS.putValue(settingsList)
                        }
                        settingsTick++
                    },
                    onBack = { route = ROUTE_SETTINGS }
                )
            }

            ROUTE_ABOUT -> AboutScreen(
                appName = stringResource(R.string.app_name),
                version = stringResource(R.string.app_version),
                onBack = { route = ROUTE_SETTINGS },
                onOpenLink = { url ->
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                    }
                }
            )

            else -> {
                val fileList: @Composable () -> Unit = {
                    FileListScreen(
                state = uiState,
                onNavigateUp = {
                    popListSnapshot()
                    viewModel.navigateUp()
                },
                onBreadcrumbClick = { crumb -> openPath(viewModel, crumb.path, onDescend = { pushListSnapshot() }) },
                onEntryClick = { entry ->
                    val file = filesByPath[entry.path] ?: return@FileListScreen
                    if (selectedCount > 0) {
                        viewModel.selectFile(file, !(file in selectedFiles))
                    } else if (file.mimeType.type == "image") {
                        imageViewerFile = file
                    } else if (file.isEditableText()) {
                        textEditorFile = file
                    } else {
                        openEntry(context, viewModel, file, onDescend = { pushListSnapshot() })
                    }
                },
                onEntryLongClick = { entry ->
                    filesByPath[entry.path]?.let { viewModel.selectFile(it, true) }
                },
                onEntryMenu = { entry -> actionTarget = filesByPath[entry.path] },
                onSearchClick = { route = ROUTE_SEARCH },
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
                onSelectionMore = { showSelectionOptions = true }
                    )
                }
                if (useTwoPane) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            fileList()
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        TwoPaneDetails(
                            file = selectedFiles.firstOrNull(),
                            context = context,
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    fileList()
                }
            }
            }
        }
            }
            if (showNavigation) {
                if (useNavigationRail) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(88.dp)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        PillNavRail(
                            currentRoute = route,
                            onNavigate = { route = it },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                } else {
                    // Pill and create button are centered together as one unit so the
                    // group's centroid stays on screen center on every route.
                    val showFab = route == ROUTE_FILES && uiState.selectedCount == 0 &&
                        !uiState.isSearching && !uiState.isLoading && uiState.error == null
                    if (showFab && createMenuExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clearAndSetSemantics {}
                                .pointerInput(Unit) {
                                    detectTapGestures { createMenuExpanded = false }
                                }
                        )
                        // The items live in their own bottom-end overlay so a wide menu
                        // never stretches the centered row past the screen edge.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 88.dp, end = 16.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            CreateFabMenuItems(
                                canPaste = uiState.canPaste,
                                onCreateFolder = { createTarget = true },
                                onCreateFile = { createTarget = false },
                                onPaste = {},
                                expanded = true,
                                onExpandedChange = { createMenuExpanded = it }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BottomPillNav(
                                currentRoute = route,
                                onNavigate = { route = it },
                                showSelectedLabel = showNavLabels
                            )
                            // Animated show/hide: navigate-up reloads flip isLoading, and
                            // the button must fade through that instead of popping.
                            AnimatedVisibility(
                                visible = showFab,
                                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                                exit = fadeOut() + scaleOut(targetScale = 0.8f)
                            ) {
                                CreateFabButton(
                                    expanded = createMenuExpanded,
                                    onClick = { createMenuExpanded = !createMenuExpanded }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDiscardText) {
        ConfirmDialog(
            title = stringResource(R.string.compose_discard_changes),
            message = stringResource(R.string.compose_your_edits_will_be_lost),
            confirmLabel = stringResource(R.string.discard),
            destructive = true,
            onConfirm = {
                showDiscardText = false
                textEditorFile = null
                textEditorDirty = false
            },
            onDismiss = { showDiscardText = false }
        )
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

    if (showSelectionOptions) {
        SelectionOptionsSheet(
            selectedCount = selectedCount,
            onDismiss = { showSelectionOptions = false },
            onSelectAll = {
                if (files.isNotEmpty()) {
                    viewModel.replaceSelectedFiles(fileItemSetOf(*files.toTypedArray()))
                }
            },
            onDeselectAll = { viewModel.clearSelectedFiles() }
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
            onOpen = {
                openEntry(context, viewModel, file, onDescend = { pushListSnapshot() })
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

    choiceTarget?.let { item ->
        SettingsChoiceDialog(
            title = item.title,
            options = item.options,
            onSelect = { option ->
                applySettingChoice(item.key, option.value)
                choiceTarget = null
                settingsTick++
            },
            onDismiss = { choiceTarget = null }
        )
    }

    if (showDefaultFolder) {
        DefaultFolderDialog(
            initialPath = runCatching {
                Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat.toString()
            }.getOrDefault(""),
            onConfirm = { pathString ->
                runCatching { Paths.get(pathString) }.getOrNull()?.let(::putDefaultDirectory)
                settingsTick++
            },
            onUseCurrentFolder = {
                val directory = currentPath ?: runCatching {
                    Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat
                }.getOrNull()
                if (directory != null) {
                    putDefaultDirectory(directory)
                }
                showDefaultFolder = false
                settingsTick++
            },
            onReset = {
                resetDefaultDirectory()
                settingsTick++
            },
            onDismiss = { showDefaultFolder = false }
        )
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.compose_reset_preferences),
            message = stringResource(R.string.compose_restore_all_settings_to_their_defaults),
            confirmLabel = stringResource(R.string.compose_reset_to_default),
            destructive = true,
            onConfirm = {
                resetSettingsToDefaults(context)
                showResetConfirm = false
                settingsTick++
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

/** Maximum preview dimension so large photos are downsampled instead of decoded at full size. */
private const val IMAGE_PREVIEW_MAX_DIMENSION = 2048

/**
 * Decodes [file] with `inSampleSize` downsampling so the preview never holds a full-resolution
 * bitmap in memory. Returns null when the image cannot be decoded.
 */
private fun decodeBoundedBitmap(file: FileItem): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    file.path.newInputStream().use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return null
    }
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > IMAGE_PREVIEW_MAX_DIMENSION ||
        bounds.outHeight / sampleSize > IMAGE_PREVIEW_MAX_DIMENSION
    ) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return file.path.newInputStream().use { BitmapFactory.decodeStream(it, null, options) }
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
            runCatching { decodeBoundedBitmap(file) }.getOrNull()
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
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
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
            ActionRow(stringResource(R.string.compose_open), onOpen)
            ActionRow(stringResource(R.string.rename), onRename)
            ActionRow(stringResource(R.string.share), onShare)
            ActionRow(stringResource(R.string.file_list_action_copy_path), onCopyPath)
            ActionRow(stringResource(R.string.file_item_action_properties), onProperties)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            ActionRow(stringResource(R.string.delete), onDelete, destructive = true)
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

@Composable
private fun TwoPaneDetails(
    file: FileItem?,
    context: Context,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        if (file == null) {
            EmptyState(
                title = stringResource(R.string.compose_no_properties_to_show),
                body = stringResource(R.string.compose_select_item)
            )
        } else {
            PropertiesScreen(
                state = remember(file) { buildPropertiesUiState(context, file) },
                onTabSelected = {},
                onCopy = { value ->
                    val clipboard = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("value", value))
                },
                onBack = {},
                showBackButton = false
            )
        }
    }
}

private fun buildPropertiesUiState(context: Context, file: FileItem): PropertiesUiState {
    val attributes = file.attributes
    val rows = buildList {
        add(
            PropertyRowUi(
                context.getString(R.string.file_list_action_sort_by_name),
                file.name,
                copyable = true
            )
        )
        add(
            PropertyRowUi(
                context.getString(R.string.storage_edit_device_storage_path),
                file.path.toString(),
                copyable = true
            )
        )
        if (!attributes.isDirectory) {
            add(
                PropertyRowUi(
                    context.getString(R.string.file_list_action_sort_by_size),
                    attributes.fileSize.formatHumanReadable(context)
                )
            )
        }
        add(
            PropertyRowUi(
                context.getString(R.string.compose_modified),
                attributes.lastModifiedTime().toInstant().formatShort(context)
            )
        )
        add(
            PropertyRowUi(
                context.getString(R.string.file_list_action_sort_by_type),
                if (attributes.isDirectory) {
                    context.getString(R.string.file_open_as_type_directory)
                } else {
                    file.mimeType.value
                }
            )
        )
    }
    return PropertiesUiState(
        title = file.name,
        subtitle = file.path.toString(),
        iconRes = file.mimeType.icon.resourceId,
        tabTitles = listOf(context.getString(R.string.file_properties_basic)),
        selectedTab = 0,
        sections = listOf(
            PropertySectionUi(context.getString(R.string.file_list_action_create_file), rows)
        )
    )
}

private fun FileItem.isEditableText(): Boolean =
    mimeType.type == "text" ||
        mimeType.value in setOf(
            "application/json",
            "application/xml",
            "application/javascript",
            "application/x-sh"
        )

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

private fun openEntry(
    context: Context,
    viewModel: FileListViewModel,
    file: FileItem,
    onDescend: () -> Unit = {}
) {
    viewModel.clearSelectedFiles()
    if (file.isListable) {
        onDescend()
        viewModel.navigateTo(Bundle(), file.listablePath)
    } else {
        runCatching {
            context.startActivity(OpenFileActivity.createIntent(file.path, file.mimeType))
        }
    }
}

private fun openPath(
    viewModel: FileListViewModel,
    pathString: String,
    onDescend: () -> Unit = {}
) {
    val path = runCatching { Paths.get(pathString) }.getOrNull() ?: return
    onDescend()
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
