package oblitusnumen.quizzzlet.ui.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import oblitusnumen.quizzzlet.implementation.data.DataManager
import oblitusnumen.quizzzlet.implementation.data.Question
import oblitusnumen.quizzzlet.implementation.data.QuestionPool
import oblitusnumen.quizzzlet.implementation.data.jsonizer.*
import oblitusnumen.quizzzlet.implementation.equalsStripIgnoreCase
import oblitusnumen.quizzzlet.implementation.measureTextLine

class QScreen(private val dataManager: DataManager, fileName: String) {
    private val questionPool: QuestionPool = dataManager.getQuestionPool(fileName)
    private val questionQueue: MutableList<Question> = questionPool.questionsScrambled().toMutableList()

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
    }

    @Composable
    fun compose(modifier: Modifier = Modifier) {
        val correctNumber = remember { mutableStateOf(0) }
        val overallNumber = remember { mutableStateOf(0) }
        val isCorrect = remember { mutableStateOf(false) }
        val hasAnswered = remember { mutableStateOf(false) }
        if (questionQueue.isEmpty()) {
            Box(modifier = modifier.fillMaxSize()) {
                Text("No questions found", Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
            }
            return
        }
        val question = questionQueue[0]
        LazyColumn(modifier = modifier) {
            item {
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
            }
            item {
//            val focusRequester = remember { FocusRequester() }
                val nextQuestion: (() -> Unit) -> Unit = nextQuestion@{ nullifyFields ->
                    nullifyFields()
                    overallNumber.value++
                    hasAnswered.value = false
                    while (questionQueue.size <= 10) {
                        questionQueue.addAll(questionPool.questionsScrambled())
                        filterQueue()
                        if (questionQueue.isEmpty())
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
                    // FIXME: move to next q
                    isCorrect.value = false
                    //                focusRequester.requestFocus()
                }
                val focusManager = LocalFocusManager.current
                val submit = { checkAnswer: () -> Boolean, nullifyFields: () -> Unit ->
                    focusManager.clearFocus()
                    isCorrect.value = checkAnswer()
                    hasAnswered.value = true
                    if (dataManager.config.fastMode && isCorrect.value)
                        nextQuestion(nullifyFields)
                }
                val screenEnd = @Composable { checkAnswer: () -> Boolean, nullifyFields: () -> Unit ->
                    Column {
                        Spacer(Modifier.weight(1f))
                        val buttonModifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth()
                        if (!hasAnswered.value) {
                            Button(onClick = { submit(checkAnswer, nullifyFields) }, modifier = buttonModifier) {
                                Text("Submit")
                            }
                        } else {
                            Button(onClick = { nextQuestion(nullifyFields) }, modifier = buttonModifier) {
                                Text("Next")
                            }
                        }
                    }
                }
                question.compose(dataManager, screenEnd)
            }
        }
    }

    @Composable
    fun answerField(
        label: String,
        lock: Boolean,
        correctOne: String,
        typedValue: MutableState<String>,
        modifier: Modifier = Modifier,
        focusRequester: FocusRequester,
        onDone: () -> Unit,
    ) {
        val correct = remember(lock) { typedValue.value.equalsStripIgnoreCase(correctOne) }
        Column(modifier = modifier) {
            OutlinedTextField(
                value = typedValue.value,
                onValueChange = {
                    typedValue.value = it
                },
                label = { Text(label) },
                readOnly = lock,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally).focusRequester(focusRequester)
            )
            val spaceModifier = Modifier.padding(8.dp)
                .defaultMinSize(minHeight = measureTextLine(MaterialTheme.typography.bodySmall) * 1.2f)
            if (lock) {
                if (correct)
                    Text(
                        "Correct", color = Color.Green,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = spaceModifier.align(Alignment.CenterHorizontally)
                    )
                else
                    Text(
                        "Correct: $correctOne",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = spaceModifier.align(Alignment.CenterHorizontally)
                    )
            } else
                Spacer(modifier = spaceModifier)
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
                    config.enableTextQs = enableTextQs.value
                    config.enableSelectQs = enableSelectQs.value
                    config.enableMultipleChoiceQs = enableMultipleChoiceQs.value
                    config.enableCategoryQs = enableCategoryQs.value
                    config.enableOrderQs = enableOrderQs.value
                    dataManager.config = config
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
}
