package com.indoora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.indoora.app.navigation.NavGraph
import com.indoora.app.ui.theme.IndooraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndooraTheme(darkTheme = true ) {
                NavGraph()
            }
        }
    }
}
