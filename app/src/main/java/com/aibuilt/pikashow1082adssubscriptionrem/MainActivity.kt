package com.aibuilt.pikashow1082adssubscriptionrem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.DetailScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.DownloadsScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.HomeScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.LibraryScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.LiveScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.PlayerScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.ProScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SearchScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SettingsScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SplashScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.theme.PikaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val container = (application as PikaApp).container
            val settings by container.settingsState.value.collectAsState()
            PikaTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestNotifications()
                    PikaApp Navigation@ {
                        MainNavigation()
                    }
                }
            }
        }
    }
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun MainNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val tabs = listOf(
        TabItem("home", "Home", Icons.Filled.Home),
        TabItem("live", "Live TV", Icons.Filled.LiveTv),
        TabItem("downloads", "Downloads", Icons.Filled.Download),
        TabItem("library", "Library", Icons.Filled.Bookmark)
    )
    val showBar = tabs.any { it.route == route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                if (route != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
                    onOpenShow = { id -> navController.navigate("show/$id") },
                    onOpenSearch = { navController.navigate("search") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenLive = { navController.navigate("live") },
                    onOpenDownloads = { navController.navigate("downloads") },
                    onResume = { id, episodeId, name ->
                        val key = if (episodeId == 0) "show-$id" else "episode-$episodeId"
                        MediaCache.put(key, name)
                        navController.navigate("player/$key/$id/$episodeId/-1/false")
                    }
                )
            }

            composable("search") {
                SearchScreen(onOpenShow = { id -> navController.navigate("show/$id") })
            }

            composable("live") {
                LiveScreen(onPlay = { key ->
                    navController.navigate("player/$key/0/0/0/true")
                })
            }

            composable("downloads") {
                DownloadsScreen(
                    onBack = { navController.navigate("home") },
                    onPlay = { id, episodeId, key ->
                        navController.navigate("player/$key/$id/$episodeId/-1/false")
                    }
                )
            }

            composable("library") {
                LibraryScreen(
                    onOpenShow = { id -> navController.navigate("show/$id") },
                    onResume = { id, episodeId, name ->
                        val key = if (episodeId == 0) "show-$id" else "episode-$episodeId"
                        MediaCache.put(key, name)
                        navController.navigate("player/$key/$id/$episodeId/-1/false")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPro = { navController.navigate("pro") }
                )
            }

            composable("pro") {
                ProScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "show/{id}",
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
                route = "player/{key}/{contentId}/{episodeId}/{server}/{live}",
                arguments = listOf(
                    navArgument("key") { type = NavType.StringType },
                    navArgument("contentId") { type = NavType.IntType },
                    navArgument("episodeId") { type = NavType.IntType },
                    navArgument("server") { type = NavType.IntType },
                    navArgument("live") { type = NavType.BoolType }
                )
            ) { entry ->
                val args = entry.arguments
                PlayerScreen(
                    key = args?.getString("key").orEmpty(),
                    contentId = args?.getInt("contentId") ?: 0,
                    episodeId = args?.getInt("episodeId") ?: 0,
                    server = args?.getInt("server") ?: 0,
                    isLive = args?.getBoolean("live") ?: false,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun RequestNotifications() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
