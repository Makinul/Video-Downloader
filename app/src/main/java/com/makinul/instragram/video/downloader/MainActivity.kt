package com.makinul.instragram.video.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.makinul.instragram.video.downloader.ui.DownloadScreen
import com.makinul.instragram.video.downloader.ui.GalleryScreen
import com.makinul.instragram.video.downloader.ui.SettingsScreen
import com.makinul.instragram.video.downloader.ui.theme.InstragramVideoDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InstragramVideoDownloaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScreenStructure()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenStructure() {
    val navController = rememberNavController() // Get NavController
    // NEW: Observe the current back stack entry to get the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("Download", Icons.Filled.Download, "download"),
        BottomNavItem("Gallery", Icons.Filled.PhotoLibrary, "gallery"),
        BottomNavItem("Settings", Icons.Filled.Settings, "settings")
    )

    // Determine the current title based on the route
    val currentTitle = items.find {
        it.route == currentRoute
    }?.title ?: stringResource(R.string.app_name)

    Scaffold(
        topBar = {
            TopBarContent(navController, currentRoute, currentTitle)
        },
        bottomBar = {
            BottomNavigationBar(navController, items)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, startDestination = "download",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("download") { DownloadScreen() }
            composable("gallery") { GalleryScreen() }
            composable("settings") { SettingsScreen() }

            composable(
                "profile",
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                }) { /* Content for Maps screen */ Text("Maps Screen Content") }

            composable("search") { /* Content for Maps screen */ Text("Maps Screen Content") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(navController: NavHostController, currentRoute: String?, currentTitle: String) {
    TopAppBar(
        title = {
            Text(currentTitle)
        },
        colors = TopAppBarDefaults.topAppBarColors( // Apply colors here
            containerColor = MaterialTheme.colorScheme.primary, // Use theme's primary color
            titleContentColor = Color.White, // Set title color to white
            actionIconContentColor = Color.White, // Set action icon color to white,
            navigationIconContentColor = Color.White // Set color for navigation icon
        ),
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home"
                )
            }
        }
    )
}


// Data class to represent each item in the bottom navigation
data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String // Not strictly needed for just design, but good practice
)

@Composable
fun BottomNavigationBar(navController: NavHostController, items: List<BottomNavItem>) {

    // Remember the currently selected item
    var selectedItem by remember { mutableStateOf(items[0]) } // Home is initially selected

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface // Use surface for bottom bar as in the screenshot
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = navController.currentBackStackEntryAsState().value?.destination?.route == item.route, // Update selection logic
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors( // Apply colors here
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Color when selected
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant // Color when unselected
                    // You can also set selectedTextColor and unselectedTextColor here
                )
            )
        }
    }
}