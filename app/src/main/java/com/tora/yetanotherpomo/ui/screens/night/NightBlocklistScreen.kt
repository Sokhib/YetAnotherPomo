package com.tora.yetanotherpomo.ui.screens.night

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.domain.model.InstalledApp
import com.tora.yetanotherpomo.domain.model.SocialApps
import com.tora.yetanotherpomo.ui.components.AppRow
import com.tora.yetanotherpomo.ui.components.appAvatarTone
import com.tora.yetanotherpomo.ui.theme.Organic

/** The Night lock blocklist picker; same shape as the focus allowlist, opposite meaning. */
@Composable
fun NightBlocklistScreen(
    installedApps: List<InstalledApp>,
    blockedPackages: Set<String>,
    alwaysReachablePackages: Set<String>,
    hasPendingChange: Boolean,
    onToggleApp: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type

    // Blocked apps first, then the social apps worth considering, then everything else; each
    // group keeps the repository's alphabetical order. Home and Settings are left out: no list
    // can block them, so a switch for them would do nothing.
    val orderedApps = remember(installedApps, blockedPackages, alwaysReachablePackages) {
        val pickable = installedApps.filter { it.packageName !in alwaysReachablePackages }
        val (blocked, rest) = pickable.partition { it.packageName in blockedPackages }
        val (social, other) = rest.partition { SocialApps.isSocial(it.packageName) }
        blocked + social + other
    }
    val blockedCount = orderedApps.count { it.packageName in blockedPackages }

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
        }
        Column(modifier = Modifier.padding(horizontal = Organic.size.screenGutter, vertical = 6.dp)) {
            Text(text = "Blocked at night", style = type.heading2, color = colors.text)
            Text(
                text = "These stay closed for the whole night lock. Unblocking one starts after tonight's lock.",
                style = type.bodySmall,
                color = colors.neutral.c700,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = Organic.size.listGutter)) {
            itemsIndexed(orderedApps, key = { _, app -> app.packageName }) { index, app ->
                val (bg, fg) = appAvatarTone(index)
                AppRow(
                    label = app.label,
                    note = app.packageName,
                    packageName = app.packageName,
                    toneBg = bg,
                    toneFg = fg,
                    checked = app.packageName in blockedPackages,
                    onToggle = { onToggleApp(app.packageName) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Organic.size.screenGutter, vertical = 16.dp),
        ) {
            Text(
                text = if (blockedCount == 1) "1 app blocked at night" else "$blockedCount apps blocked at night",
                style = type.bodySmall,
                color = colors.neutral.c700,
            )
            if (hasPendingChange) {
                Text(
                    text = "Apps you just unblocked stay blocked until tonight's lock is over.",
                    style = type.bodySmall,
                    color = colors.accentRamp.c700,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
