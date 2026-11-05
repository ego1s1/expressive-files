/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compose.features.filelist.FileRow
import me.zhanghai.android.files.compose.model.FileEntryUi
import me.zhanghai.android.files.compose.model.FileListUiState

private const val SEARCH_FIELD_HEIGHT_DP = 56
private const val SEARCH_ICON_SIZE_DP = 20
private const val SEARCH_ACTION_SIZE_DP = 48
private const val RESULT_LIST_BOTTOM_PADDING_DP = 112

/**
 * Full-screen search surface that replaces the inline top-bar search field.
 *
 * [state] carries the already-filtered results; this screen owns no ViewModel and reports all
 * interactions through callbacks.
 */
@Composable
fun SearchScreen(
    query: String,
    state: FileListUiState,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        SearchHeader(
            query = query,
            onQueryChange = onQueryChange,
            onBack = onBack
        )
        SearchBody(
            query = query,
            state = state,
            onEntryClick = onEntryClick,
            onEntryLongClick = onEntryLongClick,
            onEntryMenu = onEntryMenu
        )
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                keyboardController?.hide()
                onBack()
            },
            modifier = Modifier.size(SEARCH_ACTION_SIZE_DP.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close search" // TODO: localize
            )
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = SEARCH_FIELD_HEIGHT_DP.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SEARCH_FIELD_HEIGHT_DP.dp)
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_search),
                    contentDescription = null,
                    modifier = Modifier.size(SEARCH_ICON_SIZE_DP.dp),
                    tint = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search files", // TODO: localize
                                    color = colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        modifier = Modifier.size(SEARCH_ACTION_SIZE_DP.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search", // TODO: localize
                            modifier = Modifier.size(SEARCH_ICON_SIZE_DP.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBody(
    query: String,
    state: FileListUiState,
    onEntryClick: (FileEntryUi) -> Unit,
    onEntryLongClick: (FileEntryUi) -> Unit,
    onEntryMenu: (FileEntryUi) -> Unit
) {
    val error = state.error
    when {
        query.isBlank() -> SearchPrompt()
        state.isLoading -> SearchLoading()
        error != null -> SearchErrorMessage(message = error)
        state.entries.isEmpty() -> SearchNoResults()
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = RESULT_LIST_BOTTOM_PADDING_DP.dp
            )
        ) {
            items(
                items = state.entries,
                key = { entry -> entry.path }
            ) { entry ->
                FileRow(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onLongClick = { onEntryLongClick(entry) },
                    onMenuClick = { onEntryMenu(entry) }
                )
            }
        }
    }
}

@Composable
private fun SearchPrompt() {
    SearchMessage(
        title = "Search files", // TODO: localize
        body = "Find files and folders by name across your storage.", // TODO: localize
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer
    )
}

@Composable
private fun SearchNoResults() {
    SearchMessage(
        title = "No matching files", // TODO: localize
        body = "Try a different name or check the spelling.", // TODO: localize
        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer
    )
}

@Composable
private fun SearchLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun SearchErrorMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            modifier = Modifier.widthIn(max = 360.dp),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchMessage(
    title: String,
    body: String,
    iconTint: Color,
    iconContainerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(color = iconContainerColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_expressive_search),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            modifier = Modifier.widthIn(max = 360.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            modifier = Modifier.widthIn(max = 360.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}
