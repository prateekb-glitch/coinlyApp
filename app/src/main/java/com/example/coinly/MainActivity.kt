package com.example.coinly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coinly.ui.reward.RewardScreen
import com.example.coinly.ui.theme.CoinlyTheme

/**
 * Main Activity for Coinly rewards application.
 * Hosts the Jetpack Compose navigation and UI hierarchy.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinlyTheme {
                RewardScreen()
            }
        }
    }
}
