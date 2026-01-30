package com.indoora.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.indoora.app.navigation.IndooraNavGraph

@Composable
fun IndooraApp() {
    MaterialTheme {
        Surface {
            IndooraNavGraph()
        }
    }
}
