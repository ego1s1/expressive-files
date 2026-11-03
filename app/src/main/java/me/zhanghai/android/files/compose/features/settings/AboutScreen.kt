/*
 * Copyright (c) 2026 Expressive Files contributors
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compose.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.zhanghai.android.files.R

/**
 * About screen with links, legal notices and author credits. Links are opened
 * by the host through [onOpenLink] (an external browser intent); this screen
 * deliberately does not embed a WebView.
 */
@Composable
fun AboutScreen(
    appName: String,
    version: String,
    onBack: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AboutTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 112.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expressive_folder),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (version.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Version $version", // TODO: localize
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(32.dp))
            AboutCard(title = "Links") { // TODO: localize
                AboutLinkRow(
                    title = "View on GitHub", // TODO: localize
                    subtitle = "Source code and releases", // TODO: localize
                    url = GITHUB_URL,
                    onOpenLink = onOpenLink
                )
                AboutDivider()
                AboutLinkRow(
                    title = "Report an issue", // TODO: localize
                    url = ISSUES_URL,
                    onOpenLink = onOpenLink
                )
            }
            Spacer(Modifier.height(16.dp))
            AboutCard(title = "Legal") { // TODO: localize
                AboutLinkRow(
                    title = "Licenses", // TODO: localize
                    subtitle = "Open-source licenses", // TODO: localize
                    url = LICENSE_URL,
                    onOpenLink = onOpenLink
                )
                AboutDivider()
                AboutLinkRow(
                    title = "Privacy policy", // TODO: localize
                    url = PRIVACY_POLICY_URL,
                    onOpenLink = onOpenLink
                )
            }
            Spacer(Modifier.height(16.dp))
            AboutCard(title = "Author") { // TODO: localize
                AboutLinkRow(
                    title = "Hai Zhang", // TODO: localize
                    subtitle = "Maintainer", // TODO: localize
                    url = AUTHOR_RESUME_URL,
                    onOpenLink = onOpenLink
                )
                AboutDivider()
                AboutLinkRow(
                    title = "Follow on GitHub", // TODO: localize
                    url = AUTHOR_GITHUB_URL,
                    onOpenLink = onOpenLink
                )
                AboutDivider()
                AboutLinkRow(
                    title = "Follow on Twitter", // TODO: localize
                    url = AUTHOR_TWITTER_URL,
                    onOpenLink = onOpenLink
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .statusBarsPadding()
            .height(64.dp)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back", // TODO: localize
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "About", // TODO: localize
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AboutCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun AboutLinkRow(
    title: String,
    url: String,
    onOpenLink: (String) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button) { onOpenLink(url) }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun AboutDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

private const val GITHUB_URL = "https://github.com/zhanghai/MaterialFiles"
private const val ISSUES_URL = "https://github.com/zhanghai/MaterialFiles/issues"
private const val LICENSE_URL = "https://github.com/zhanghai/MaterialFiles/blob/master/LICENSE"
private const val PRIVACY_POLICY_URL =
    "https://github.com/zhanghai/MaterialFiles/blob/master/PRIVACY.md"
private const val AUTHOR_RESUME_URL = "https://resume.zhanghai.me/"
private const val AUTHOR_GITHUB_URL = "https://github.com/zhanghai"
private const val AUTHOR_TWITTER_URL = "https://twitter.com/zhanghai95"

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen(
            appName = "Expressive Files",
            version = "1.7.5 (40)",
            onBack = {},
            onOpenLink = {}
        )
    }
}
