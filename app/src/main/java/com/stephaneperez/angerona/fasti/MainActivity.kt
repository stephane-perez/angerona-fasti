package com.stephaneperez.angerona.fasti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.stephaneperez.angerona.fasti.ui.FastiScreen
import com.stephaneperez.angerona.fasti.ui.theme.AngeronaFastiTheme
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngeronaFastiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FastiColors.ground,
                ) {
                    FastiScreen()
                }
            }
        }
    }
}
