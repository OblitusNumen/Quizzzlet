package oblitusnumen.quizzzlet.ui.model

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.QuestionPool

class MainScreen(private val dataManager: DataManager) {
    private var questionPools: List<QuestionPool> by mutableStateOf(dataManager.getQuestionPools())

    @Composable
    fun compose(modifier: Modifier = Modifier, openPool: (String) -> Unit) {
        LazyColumn(modifier = modifier) {
            items(questionPools) {
                drawQuestionPool(it) { openPool(it.poolDir) }
            }
            item {
                Button(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                    onClick = {
                        val context = dataManager.context
                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OblitusNumen/Quizzzlet"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("BrowserIntent", "Error starting activity", e)
                            Toast.makeText(context, "Failed to open browser", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                    Text(
                        modifier = Modifier.weight(1.0f).padding(start = 8.dp, end = 8.dp)
                            .align(Alignment.CenterVertically),
                        text = "Visit site",
                        style = MaterialTheme.typography.headlineSmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun drawQuestionPool(questionPool: QuestionPool, openPool: () -> Unit) {
        var deleteDialogShown by remember { mutableStateOf(false) }
        Row(
            Modifier.defaultMinSize(minHeight = 100.dp).fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)
                .combinedClickable(onLongClick = { deleteDialogShown = true }, onClick = { openPool() })
                .border(2.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
        ) {
            Text(
                modifier = Modifier.weight(1.0f).padding(start = 8.dp, end = 8.dp).align(Alignment.CenterVertically),
                text = questionPool.name,
                style = MaterialTheme.typography.headlineSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp, end = 8.dp),
                text = "${questionPool.countQs()} questions",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (deleteDialogShown)
            deleteDialog(questionPool) { deleteDialogShown = false }
    }

    @Composable
    fun deleteDialog(questionPool: QuestionPool, onClose: () -> Unit) {
        AlertDialog(
            onDismissRequest = onClose,
            dismissButton = {
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onClose()
                    questionPool.delete()
                    questionPools = dataManager.getQuestionPools()
                }) {
                    Text("OK")
                }
            },
            text = {
                Column {
                    Text("Delete ${questionPool.name}?")
                }
            }
        )
    }

    @Composable
    fun functionButton() {
        var uri: Uri? by remember { mutableStateOf(null) }
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) {
            uri = it
        }
        FloatingActionButton(onClick = { filePickerLauncher.launch("*/*") }) {
            Icon(Icons.Filled.Add, "Add a question pool")
        }
        if (uri != null) {
            showGetNameDialog { name ->
                if (name != null) {
                    val fileName = dataManager.copyPool(uri!!, name)
                    if (fileName != null)
                        questionPools = questionPools + dataManager.getQuestionPool(fileName)
                    else
                        Toast.makeText(dataManager.context, "File is invalid", Toast.LENGTH_SHORT).show()
                }
                uri = null
            }
        }
    }

    @Composable
    fun showGetNameDialog(onChooseName: (String?) -> Unit) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { onChooseName(null) },
            dismissButton = {
                TextButton(onClick = { onChooseName(null) }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = { onChooseName(name) }) {
                    Text("OK")
                }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        label = { Text("Question pool name") },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onValueChange = { name = it }
                    )
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topBar() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .9f),
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = { Text("Quizzzlet", maxLines = 1) },
            scrollBehavior = scrollBehavior,
        )
    }
}