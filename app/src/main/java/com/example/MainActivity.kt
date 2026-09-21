package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.model.WorkspaceType
import com.example.ui.MainAppContainer
import com.example.ui.MainAppViewModel
import com.example.ui.create.CreateTransactionViewModel
import com.example.ui.navigation.Screen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val mainAppViewModel: MainAppViewModel by viewModels()
    private val createViewModel: CreateTransactionViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(
                    mainAppViewModel = mainAppViewModel,
                    createViewModel = createViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true, name = "FinityArc App Preview")
@Composable
fun MainActivityPreview() {
    MyApplicationTheme {
        MainAppContainer(
            initialScreen = Screen.HOME,
            initialWorkspace = WorkspaceType.PERSONAL
        )
    }
}
