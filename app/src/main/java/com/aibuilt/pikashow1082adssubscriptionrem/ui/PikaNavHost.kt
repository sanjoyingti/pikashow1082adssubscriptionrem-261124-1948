package com.aibuilt.pikashow1082adssubscriptionrem.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.ProInfoScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SearchScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SettingsScreen
import com.aibuilt.pikashow1082adssubscriptionrem.ui.screens.SplashScreen

private val tabRoutes = setOf("home", "search", "live", "library", "downloads")

private fun NavHostController.go(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun PikaNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val play: (Int, Int, String, Int) -> Unit = { contentId, episodeId, key, server ->
        navController.navigate("player/$key/$contentId/$episodeId/$server/false")
    }

    Scaffold(
        bottomBar = {
            if (route == null || route in tabRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == "home",
                        onClick = { navController.go("home") },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = route == "search",
                        onClick = { navController.go("search") },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = route == "live",
                        onClick = { navController.go("live") },
                        icon = { Icon(Icons.Filled.LiveTv, contentDescription = "Live TV") },
                        label = { Text("Live") }
                    )
                    NavigationBarItem(
                        selected = route == "library",
                        onClick = { navController.go("library") },
                        icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = "Library") },
                        label = { Text("Library") }
                    )
                    NavigationBarItem(
                        selected = route == "downloads",
                        onClick = { navController.go("downloads") },
                        icon = { Icon(Icons.Filled.Download, contentDescription = "Downloads") },
                        label = { Text("Downloads") }
                    )
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
                    onOpenShow = { id -> navController.navigate("detail/$id") },
                    onOpenSearch = { navController.go("search") },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenLive = { navController.go("live") },
                    onOpenDownloads = { navController.go("downloads") },
                    onResume = { id, episodeId, name ->
                        val key = if (episodeId != 0) "episode-$episodeId" else "show-$id"
                        MediaCache.put(key, name)
                        play(id, episodeId, key, 0)
                    }
                )
            }

            composable("search") {
                SearchScreen(onOpenShow = { id -> navController.navigate("detail/$id") })
            }

            composable("live") {
                LiveScreen(onPlay = { key ->
                    navController.navigate("player/$key/0/0/0/true")
                })
            }

            composable("library") {
                LibraryScreen(
                    onOpenShow = { id -> navController.navigate("detail/$id") },
                    onResume = { id, episodeId, name ->
                        val key = if (episodeId != 0) "episode-$episodeId" else "show-$id"
                        MediaCache.put(key, name)
                        play(id, episodeId, key, 0)
                    }
                )
            }

            composable("downloads") {
                DownloadsScreen(
                    onOpenShow = { id -> navController.navigate("detail/$id") },
                    onPlayLocal = { contentId, episodeId, name, path ->
                        val key = if (episodeId != 0) "episode-$episodeId" else "show-$contentId"
                        MediaCache.put(key, name, path)
                        play(contentId, episodeId, key, 0)
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPro = { navController.navigate("pro") },
                    onOpenDownloads = { navController.go("downloads") },
                    onOpenLibrary = { navController.go("library") }
                )
            }

            composable("pro") {
                ProInfoScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                DetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onPlay = { contentId, episodeId, server, key -> play(contentId, episodeId, key, server) },
                    onOpenDownloads = { navController.go("downloads") }
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
                PlayerScreen(
                    key = entry.arguments?.getString("key").orEmpty(),
                    contentId = entry.arguments?.getInt("contentId") ?: 0,
                    episodeId = entry.arguments?.getInt("episodeId") ?: 0,
                    server = entry.arguments?.getInt("server") ?: 0,
                    isLive = entry.arguments?.getBoolean("live") ?: false,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
