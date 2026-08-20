package com.tora.yetanotherpomo.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.tora.yetanotherpomo.domain.model.InstalledApp
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepositoryImpl(
    private val context: Context,
) : InstalledAppsRepository {

    override suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }
        val ownPackage = context.packageName
        resolveInfos
            .asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != ownPackage }
            .distinctBy { it.packageName }
            .map { activityInfo ->
                InstalledApp(
                    packageName = activityInfo.packageName,
                    label = activityInfo.loadLabel(pm).toString(),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
