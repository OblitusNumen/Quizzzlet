package oblitusnumen.quizzzlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.ui.model.MainScreen
import oblitusnumen.quizzzlet.ui.model.QScreen
import oblitusnumen.quizzzlet.ui.theme.QuizzzletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzzletTheme {
                val dataManager = remember { DataManager(this) }
                val mainScreen = remember { MainScreen(dataManager) }
                var qScreen: QScreen? by remember { mutableStateOf(null) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (qScreen == null)
                            mainScreen.topBar()
                        else
                            qScreen!!.topBar { qScreen = null }
                    },
                    floatingActionButton = {
                        if (qScreen == null)
                            mainScreen.functionButton()
                    },
                    bottomBar = {
                        qScreen?.bottomBar()
                    }
                ) { innerPadding ->
                    if (qScreen == null)
                        mainScreen.compose(Modifier.padding(innerPadding)) { qScreen = QScreen(dataManager, it) }
                    else {
                        BackHandler {
                            qScreen = null
                        }
                        qScreen!!.compose(innerPadding)
                    }
                }
            }
        }
    }
}