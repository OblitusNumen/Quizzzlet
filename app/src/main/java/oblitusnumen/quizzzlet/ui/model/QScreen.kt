package oblitusnumen.quizzzlet.ui.model

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.QuestionPool
import oblitusnumen.quizzzlet.implementation.data.questions.*

class QScreen(private val dataManager: DataManager, fileName: String) {
    private val questionPool: QuestionPool = dataManager.getQuestionPool(fileName)
    private val questionQueue: MutableList<Question> = questionPool.questionsScrambled().toMutableList()
    private var empty by mutableStateOf(questionQueue.isEmpty())
    private var bottomButton: @Composable (() -> Unit)? by mutableStateOf(null)

    init {
        filterQueue()
    }

    private fun filterQueue() {
        questionQueue.removeIf {
            when (it) {
                is TextQuestion -> !dataManager.config.enableTextQs
                is SelectQuestion -> !dataManager.config.enableSelectQs
                is MultipleChoiceQuestion -> !dataManager.config.enableMultipleChoiceQs
                is CategoryQuestion -> !dataManager.config.enableCategoryQs
                is OrderQuestion -> !dataManager.config.enableOrderQs
                else -> throw NotImplementedError()
            }
        }
        empty = questionQueue.isEmpty()
    }

    @Composable
    fun compose(modifier: Modifier = Modifier) {
        val correctNumber = remember { mutableStateOf(0) }
        val overallNumber = remember { mutableStateOf(0) }
        if (empty) {
            remember { bottomButton = null }
            Box(modifier = modifier.fillMaxSize()) {
                Text("No questions found", Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
            }
            return
        }
        var hack by remember { mutableStateOf(false) }// FIXME: yet another filthy hack
        val isCorrect = remember(hack) { mutableStateOf(false) }
        val hasAnswered = remember(hack) { mutableStateOf(false) }
        val question by remember(hack) { mutableStateOf(questionQueue[0]) }
        val questionState = remember(hack) { question.newQuestionState() }
        val attachments = remember(hack) { question.attachments ?: emptyList() }
        val nextQuestion = remember(hack) {
            nextQuestion@{
                overallNumber.value++
                filterQueue()
                while (questionQueue.size <= 10) {
                    questionQueue.addAll(questionPool.questionsScrambled())
                    filterQueue()
                    if (empty)
                        return@nextQuestion
                }
                if (isCorrect.value) {
                    questionQueue.removeAt(0)
                    correctNumber.value++
                } else {
                    if (dataManager.config.repeatNotCorrect) {
                        if (questionQueue[4] != question) questionQueue.add(4, question)
                        if (questionQueue[10] != question) questionQueue.add(10, question)
                    } else
                        questionQueue.removeAt(0)
                }
                hack = !hack
            }
        }
        val focusManager = LocalFocusManager.current
        val submit = remember(hack) {
            {
                focusManager.clearFocus()
                isCorrect.value = questionState.checkAnswer()
                hasAnswered.value = true
                if (dataManager.config.fastMode && isCorrect.value)
                    nextQuestion()
            }
        }
        remember(hack) {
            Log.e("aaaaaaaaaaaaaaaaaa", "button set")
            bottomButton = {
                Box(Modifier.padding(12.dp).padding(bottom = 24.dp).fillMaxWidth().defaultMinSize(minHeight = 48.dp)) {
                    val buttonModifier = Modifier.fillMaxWidth().align(Alignment.Center)
                    if (!hasAnswered.value) {
                        Button(onClick = { submit() }, modifier = buttonModifier) {
                            Text("Submit")
                        }
                    } else {
                        Button(onClick = { nextQuestion() }, modifier = buttonModifier) {
                            Text("Next")
                        }
                    }
                }
            }
        }
        LazyColumn(modifier = modifier) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Answered: ${overallNumber.value}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Text(
                    "Correct: ${correctNumber.value}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Text(
                    "Correct percentage: ${if (overallNumber.value == 0) "N/A" else "${100f * correctNumber.value / overallNumber.value}%"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (question.id != null)
                    Text(
                        "id: ${question.id}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Text(
                    question.question,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp)
                )
            }
            items(attachments.size) { index ->
                val attachment = attachments[index]
                val bitmap = questionPool.getAttachment(attachment)
                if (bitmap == null)
                    Box(
                        Modifier.padding(12.dp).fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .5f))
                    ) {
                        Text("attachment $attachment", Modifier.align(Alignment.Center))
                    }
                else
                    Image(
                        bitmap,
                        "attachment $attachment",
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),// TODO: clickable open in fullscreen
                        contentScale = ContentScale.FillWidth
                    )
            }
            question.compose(dataManager, this, questionState, submit, hasAnswered.value)
        }
    }

    @Composable
    fun showSettingsDialog(onClose: () -> Unit) {
        val config = dataManager.config
        val repeatNotCorrect = remember { mutableStateOf(config.repeatNotCorrect) }
        val fastMode = remember { mutableStateOf(config.fastMode) }
        val enableSelectQs = remember { mutableStateOf(config.enableSelectQs) }
        val enableMultipleChoiceQs = remember { mutableStateOf(config.enableMultipleChoiceQs) }
        val enableTextQs = remember { mutableStateOf(config.enableTextQs) }
        val enableCategoryQs = remember { mutableStateOf(config.enableCategoryQs) }
        val enableOrderQs = remember { mutableStateOf(config.enableOrderQs) }
        // TODO: checkboxes for q's 1-10, 11-20: save settings by pool
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
                    config.repeatNotCorrect = repeatNotCorrect.value
                    config.fastMode = fastMode.value
                    config.enableTextQs = enableTextQs.value
                    config.enableSelectQs = enableSelectQs.value
                    config.enableMultipleChoiceQs = enableMultipleChoiceQs.value
                    config.enableCategoryQs = enableCategoryQs.value
                    config.enableOrderQs = enableOrderQs.value
                    dataManager.config = config
                    if (empty) {
                        questionQueue.addAll(questionPool.questionsScrambled())
                        filterQueue()
                    }
                }) {
                    Text("OK")
                }
            },
            text = {
                Column {
                    checkboxOption(repeatNotCorrect, "Repeat incorrect")
                    checkboxOption(fastMode, "Fast mode")
                    HorizontalDivider(Modifier.padding(vertical = 2.dp))
                    Text("Question types:", Modifier.padding(8.dp))
                    checkboxOption(enableTextQs, "Text question")
                    checkboxOption(enableSelectQs, "Select questions")
                    checkboxOption(enableMultipleChoiceQs, "Multiple choice questions")
                    checkboxOption(enableCategoryQs, "Category questions")
                    checkboxOption(enableOrderQs, "Order questions")
                }
            }
        )
    }

    @Composable
    fun checkboxOption(checked: MutableState<Boolean>, label: String) {
        Row(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp).padding(8.dp).clickable {
                checked.value = !checked.value
            },
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = checked.value,
                onCheckedChange = { checked.value = it },
                modifier = Modifier.padding(8.dp).align(Alignment.CenterVertically)
            )
            Text(label, modifier = Modifier.padding(8.dp).align(Alignment.CenterVertically))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topBar(backPress: () -> Unit) {
        var settingsDialogShown by remember { mutableStateOf(false) }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .9f),
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = { Text(questionPool.name, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = backPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            actions = {
                IconButton(onClick = { settingsDialogShown = true }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null
                    )
                }
            }
        )
        if (settingsDialogShown)
            showSettingsDialog { settingsDialogShown = false }
    }

    @Composable
    fun bottomBar() {
        bottomButton?.let { it() }
    }
}
