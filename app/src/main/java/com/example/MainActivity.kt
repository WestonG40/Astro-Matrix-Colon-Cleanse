package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CosmicTopBar
import com.example.ui.screens.GalaxyMapScreen
import com.example.ui.screens.MatrixChessScreen
import com.example.ui.screens.OrbitSimulatorScreen
import com.example.ui.screens.ShipCommandScreen
import com.example.ui.theme.AstroMatrixTheme
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.HyperMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.StarWhite
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.AstroMatrixViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AstroMatrixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AstroMatrixTheme {
                AstroMatrixApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AstroMatrixApp(viewModel: AstroMatrixViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val currentSectorId by viewModel.currentSectorId.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val currentStarSystem = sectors[currentSectorId]

    // Back handler: return to Orbit Simulator if on another screen
    BackHandler(enabled = currentScreen != ActiveScreen.ORBIT_SIMULATOR) {
        viewModel.setScreen(ActiveScreen.ORBIT_SIMULATOR)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicVoid),
        topBar = {
            CosmicTopBar(
                system = currentStarSystem,
                inventory = inventory,
                statusMessage = statusMessage,
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            AstroMatrixBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.setScreen(it) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicVoid)
        ) {
            when (currentScreen) {
                ActiveScreen.ORBIT_SIMULATOR -> OrbitSimulatorScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                ActiveScreen.GALAXY_MAP -> GalaxyMapScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                ActiveScreen.MATRIX_CHESS -> MatrixChessScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                ActiveScreen.SHIP_COMMAND -> ShipCommandScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun AstroMatrixBottomNav(
    currentScreen: ActiveScreen,
    onSelectScreen: (ActiveScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceSurface)
            .border(1.dp, SpaceBorder)
            .testTag("bottom_nav_bar"),
        containerColor = SpaceSurface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == ActiveScreen.ORBIT_SIMULATOR,
            onClick = { onSelectScreen(ActiveScreen.ORBIT_SIMULATOR) },
            icon = {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Orbit Flight",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Orbit",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = SpaceCard
            ),
            modifier = Modifier.testTag("nav_orbit")
        )

        NavigationBarItem(
            selected = currentScreen == ActiveScreen.GALAXY_MAP,
            onClick = { onSelectScreen(ActiveScreen.GALAXY_MAP) },
            icon = {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Galaxy Map",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Galaxy",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = SpaceCard
            ),
            modifier = Modifier.testTag("nav_galaxy")
        )

        NavigationBarItem(
            selected = currentScreen == ActiveScreen.MATRIX_CHESS,
            onClick = { onSelectScreen(ActiveScreen.MATRIX_CHESS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Matrix Chess",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Chess",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HyperMagenta,
                selectedTextColor = HyperMagenta,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = SpaceCard
            ),
            modifier = Modifier.testTag("nav_chess")
        )

        NavigationBarItem(
            selected = currentScreen == ActiveScreen.SHIP_COMMAND,
            onClick = { onSelectScreen(ActiveScreen.SHIP_COMMAND) },
            icon = {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = "Ship Command",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Command",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = SpaceCard
            ),
            modifier = Modifier.testTag("nav_command")
        )
    }
}
