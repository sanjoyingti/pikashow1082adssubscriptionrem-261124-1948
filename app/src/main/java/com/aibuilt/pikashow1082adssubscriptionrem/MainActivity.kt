package com.aibuilt.pikashow1082adssubscriptionrem

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.Settings
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.AboutScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.DetailScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.DownloadsScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.HomeScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.LibraryScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.LiveScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.PlayerScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SearchScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SettingsScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SplashScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.theme.PikaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (application as PikaApp).container
            val settings by container.settingsState.value.collectAsState()
            PikaTheme(themeMode = settings.themeMode) {
                PikaRoot(settings = settings)
            }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Filled.Home),
    Tab("search", "Search", Icons.Filled.Search),
    Tab("live", "Live TV", Icons.Filled.LiveTv),
    Tab("library", "Library", Icons.Filled.Download),
    Tab("settings", "Settings", Icons.Filled.Settings)
)

@Composable
private fun PikaRoot(settings: Settings) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val permissionLauncher = rememberLauncherForNotification()

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        runCatching { (context.applicationContext as PikaApp).container.settings.forcePro() }
    }

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val route = backStackEntry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(onDone = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }

            composable("home") {
                HomeScreen(
                    onOpenShow = { navController.navigate("detail/$it") },
                    onOpenSearch = { navController.navigate("search") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenLive = { navController.navigate("live") },
                    onOpenDownloads = { navController.navigate("downloads") },
                    onResume = { contentId, episodeId, name ->
                        val key = "show-$contentId"
                        MediaCache.put(key, name)
                        navController.navigate("player/$key/$contentId/$episodeId/${settings.preferredServer}/false")
                    }
                )
            }

            composable("search") {
                SearchScreen(onOpenShow = { navController.navigate("detail/$it") })
            }

            composable("live") {
                LiveScreen(onPlay = { key ->
                    navController.navigate("player/$key/0/0/0/true")
                })
            }

            composable("library") {
                LibraryScreen(
                    onOpenShow = { navController.navigate("detail/$it") },
                    onResume = { contentId, episodeId, name ->
                        val key = "show-$contentId"
                        MediaCache.put(key, name)
                        navController.navigate("player/$key/$contentId/$episodeId/${settings.preferredServer}/false")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(onOpenAbout = { navController.navigate("about") })
            }

            composable("downloads") {
                DownloadsScreen(
                    onPlayOffline = { contentId, episodeId, name, path ->
                        val key = "show-$contentId"
                        MediaCache.put(key, name, path)
                        navController.navigate("player/$key/$contentId/$episodeId/-1/false")
                    }
                )
            }

            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                DetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onPlay = { contentId, episodeId, server, key ->
                        navController.navigate("player/$key/$contentId/$episodeId/$server/false")
                    },
                    onOpenDownloads = { navController.navigate("downloads") }
                )
            }

            composable(
                route = "player/{key}/{contentId}/{episodeId}/{server}/{isLive}",
                arguments = listOf(
                    navArgument("key") { type = NavType.StringType },
                    navArgument("contentId") { type = NavType.IntType },
                    navArgument("episodeId") { type = NavType.IntType },
                    navArgument("server") { type = NavType.IntType },
                    navArgument("isLive") { type = NavType.BoolType }
                )
            ) { entry ->
                val args = entry.arguments
                PlayerScreen(
                    key = args?.getString("key").orEmpty(),
                    contentId = args?.getInt("contentId") ?: 0,
                    episodeId = args?.getInt("episodeId") ?: 0,
                    server = args?.getInt("server") ?: 0,
                    isLive = args?.getBoolean("isLive") ?: false,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun rememberLauncherForNotification(): androidx.activity.result.ActivityResultLauncher<String> {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    return remember(launcher) { launcher }
}
