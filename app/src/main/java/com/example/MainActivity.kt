package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.LoginScreen
import com.example.ui.NationSelectionScreen
import com.example.ui.HexCanvasScreen
import com.example.viewmodel.MainViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      HexaNationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          AppNavigation()
        }
      }
    }
  }
}

@Composable
fun HexaNationTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = darkColorScheme(
      background = Color(0xFF0F0F0F),
      surface = Color(0xFF1E1E1E),
      primary = Color(0xFF7E57C2),
      onPrimary = Color.White,
      surfaceVariant = Color(0xFF282828)
    ),
    content = content
  )
}

@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
  val navController = rememberNavController()
  
  NavHost(navController = navController, startDestination = "login") {
    composable("login") {
      LoginScreen(
        onLoginSuccess = { navController.navigate("nation_select") {
          popUpTo("login") { inclusive = true }
        } }
      )
    }
    composable("nation_select") {
      NationSelectionScreen(
        onNationSelected = { nation ->
          viewModel.selectNation(nation)
          navController.navigate("canvas")
        }
      )
    }
    composable("canvas") {
      val currentNation by viewModel.currentNation.collectAsStateWithLifecycle()
      val hexagons by viewModel.hexagons.collectAsStateWithLifecycle()
      
      HexCanvasScreen(
        nationName = currentNation ?: "Unknown",
        hexagons = hexagons,
        onPurchaseAction = { q, r, color ->
          viewModel.purchaseHexagon(q, r, color)
        },
        onBack = {
          navController.popBackStack()
        }
      )
    }
  }
}
