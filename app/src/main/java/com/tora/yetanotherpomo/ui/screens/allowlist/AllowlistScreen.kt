package com.tora.yetanotherpomo.ui.screens.allowlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.domain.model.InstalledApp
import com.tora.yetanotherpomo.ui.components.AppRow
import com.tora.yetanotherpomo.ui.components.appAvatarTone
import com.tora.yetanotherpomo.ui.theme.Organic

@Composable
fun AllowlistScreen(
    installedApps: List<InstalledApp>,
    allowedPackages: Set<String>,
    onToggleApp: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
        }
        Column(modifier = Modifier.padding(horizontal = Organic.size.screenGutter, vertical = 6.dp)) {
            Text(text = "Allowed apps", style = type.heading2, color = colors.text)
            Text(
                text = "Everything else goes quiet and unopenable until the timer ends.",
                style = type.bodySmall,
                color = colors.neutral.c700,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = Organic.size.listGutter)) {
            itemsIndexed(installedApps, key = { _, app -> app.packageName }) { index, app ->
                val (bg, fg) = appAvatarTone(index)
                AppRow(
                    label = app.label,
                    note = app.packageName,
                    packageName = app.packageName,
                    toneBg = bg,
                    toneFg = fg,
                    checked = app.packageName in allowedPackages,
                    onToggle = { onToggleApp(app.packageName) },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Organic.size.screenGutter, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${allowedPackages.size} of ${installedApps.size} apps stay awake",
                style = type.bodySmall,
                color = colors.neutral.c700,
            )
        }
    }
}
