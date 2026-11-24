package com.aibuilt.pikashow1082adssubscriptionrem.ui

import android.net.Uri
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

private const val PLAYER_ROUTE = "player/{key}/{contentId}/{episodeId}/{server}/{live}"

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Filled.Home),
    Tab("search", "Search", Icons.Filled.Search),
    Tab("live", "Live TV", Icons.Filled.LiveTv),
    Tab("library", "Library", Icons.Filled.VideoLibrary),
    Tab("downloads", "Downloads", Icons.Filled.Download)
)

private fun playerRoute(key: String, contentId: Int, episodeId: Int, server: Int, live: Boolean): String =
    "player/${Uri.encode(key)}/$contentId/$episodeId/$server/${if (live) 1 else 0}"

@Composable
fun PikaNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route != null && tabs.any { it.route == route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                if (route != tab.route) {
                                    nav.navigate(tab.route) {
                                        popUpTo("home")
                                        launchSingleTop = true
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
            navController = nav,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(onDone = {
                    nav.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }

            composable("home") {
                HomeScreen(
                    onOpenShow = { id -> nav.navigate("detail/$id") },
                    onOpenSearch = { nav.navigate("search") { launchSingleTop = true } },
                    onOpenSettings = { nav.navigate("settings") },
                    onOpenLive = { nav.navigate("live") { launchSingleTop = true } },
                    onOpenDownloads = { nav.navigate("downloads") { launchSingleTop = true } },
                    onResume = { contentId, episodeId, name ->
                        val key = "resume-$contentId-$episodeId"
                        MediaCache.put(key, name)
                        nav.navigate(playerRoute(key, contentId, episodeId, 0, false))
                    }
                )
            }

            composable("search") {
                SearchScreen(onOpenShow = { id -> nav.navigate("detail/$id") })
            }

            composable("live") {
                LiveScreen(onPlay = { key -> nav.navigate(playerRoute(key, 0, 0, 0, true)) })
            }

            composable("library") {
                LibraryScreen(
                    onOpenShow = { id -> nav.navigate("detail/$id") },
                    onResume = { contentId, episodeId, name ->
                        val key = "resume-$contentId-$episodeId"
                        MediaCache.put(key, name)
                        nav.navigate(playerRoute(key, contentId, episodeId, 0, false))
                    }
                )
            }

            composable("downloads") {
                DownloadsScreen(onPlay = { key, contentId, episodeId ->
                    nav.navigate(playerRoute(key, contentId, episodeId, -1, false))
                })
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenPro = { nav.navigate("pro") },
                    onOpenDownloads = { nav.navigate("downloads") }
                )
            }

            composable("pro") { ProInfoScreen(onBack = { nav.popBackStack() }) }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backEntry ->
                val id = backEntry.arguments?.getInt("id") ?: 0
                DetailScreen(
                    id = id,
                    onBack = { nav.popBackStack() },
                    onPlay = { contentId, episodeId, server, key ->
                        val live = MediaCache.url(key)?.startsWith("http") == true
                        nav.navigate(playerRoute(key, contentId, episodeId, server, live))
                    },
                    onOpenDownloads = { nav.navigate("downloads") }
                )
            }

            composable(
                route = PLAYER_ROUTE,
                arguments = listOf(
                    navArgument("key") { type = NavType.StringType },
                    navArgument("contentId") { type = NavType.IntType },
                    navArgument("episodeId") { type = NavType.IntType },
                    navArgument("server") { type = NavType.IntType },
                    navArgument("live") { type = NavType.IntType }
                )
            ) { backEntry ->
                val args = backEntry.arguments
                PlayerScreen(
                    key = args?.getString("key").orEmpty(),
                    contentId = args?.getInt("contentId") ?: 0,
                    episodeId = args?.getInt("episodeId") ?: 0,
                    server = args?.getInt("server") ?: 0,
                    isLive = (args?.getInt("live") ?: 0) == 1,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
