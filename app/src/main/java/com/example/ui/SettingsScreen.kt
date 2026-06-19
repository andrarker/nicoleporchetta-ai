package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataStoreManager: DataStoreManager,
    activeProvider: String,
    activeModel: String,
    onBack: () -> Unit,
    onProviderModelSelected: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    val systemPrompt by dataStoreManager.systemPromptFlow.collectAsState(initial = DataStoreManager.DEFAULT_PROMPT)
    val openAIKey by dataStoreManager.openAIFlow.collectAsState(initial = "")
    val anthropicKey by dataStoreManager.anthropicFlow.collectAsState(initial = "")
    val geminiKey by dataStoreManager.geminiFlow.collectAsState(initial = "")

    var localPrompt by remember(systemPrompt) { mutableStateOf(systemPrompt) }
    var localOpenAI by remember(openAIKey) { mutableStateOf(openAIKey) }
    var localAnthropic by remember(anthropicKey) { mutableStateOf(anthropicKey) }
    var localGemini by remember(geminiKey) { mutableStateOf(geminiKey) }

    val providers = listOf("OpenAI", "Anthropic", "Google")
    var providerExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    val modelsMap = mapOf(
        "OpenAI" to listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "o3", "o3-mini", "dall-e-3", "gpt-image-1"),
        "Anthropic" to listOf("claude-opus-4-5", "claude-sonnet-4-5", "claude-haiku-4-5"),
        "Google" to listOf("gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash", "imagen-3.0-generate-002", "veo-3.1-generate-preview")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Provider e Modello", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = providerExpanded,
                    onExpandedChange = { providerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = activeProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Provider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = providerExpanded,
                        onDismissRequest = { providerExpanded = false }
                    ) {
                        providers.forEach { prov ->
                            DropdownMenuItem(
                                text = { Text(prov) },
                                onClick = {
                                    val newModel = modelsMap[prov]?.first() ?: ""
                                    onProviderModelSelected(prov, newModel)
                                    providerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = modelExpanded,
                    onExpandedChange = { modelExpanded = it }
                ) {
                    OutlinedTextField(
                        value = activeModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modello") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        modelsMap[activeProvider]?.forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod) },
                                onClick = {
                                    onProviderModelSelected(activeProvider, mod)
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

            item {
                Text("Chiavi API", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                ApiKeyInput("OpenAI", localOpenAI) { 
                    localOpenAI = it
                    coroutineScope.launch { dataStoreManager.saveOpenAIKey(it) }
                }
                ApiKeyInput("Anthropic / Claude", localAnthropic) {
                    localAnthropic = it
                    coroutineScope.launch { dataStoreManager.saveAnthropicKey(it) }
                }
                ApiKeyInput("Google / Gemini", localGemini) {
                    localGemini = it
                    coroutineScope.launch { dataStoreManager.saveGeminiKey(it) }
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

            item {
                Text("System Prompt", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = localPrompt,
                    onValueChange = { localPrompt = it },
                    label = { Text("System Prompt personalizzato") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedButton(
                    onClick = { coroutineScope.launch { dataStoreManager.saveSystemPrompt(localPrompt) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salva Prompt")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ApiKeyInput(label: String, value: String, onSave: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("API Key $label") },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (visible) Icons.Filled.Create else Icons.Filled.Lock
                IconButton(onClick = { visible = !visible }) { Icon(icon, "Torna visibilità API Key") }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            text = "salvata solo sul dispositivo",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        ElevatedButton(
            onClick = { onSave(text) },
            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
        ) {
            Text("Salva")
        }
    }
}
