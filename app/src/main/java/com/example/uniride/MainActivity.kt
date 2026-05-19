package com.example.uniride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uniride.ui.theme.AppNavigation
import com.example.uniride.ui.theme.UnirideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnirideTheme {
                AppNavigation()
            }
        }
    }
}