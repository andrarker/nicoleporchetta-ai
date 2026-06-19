package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.AnthropicService
import com.example.network.GeminiService
import com.example.network.OpenAIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class OutputMode {
    TEXT, IMAGE, VIDEO, AUDIO
}

class ChatViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeProvider = MutableStateFlow("OpenAI")
    val activeProvider: StateFlow<String> = _activeProvider.asStateFlow()

    private val _activeModel = MutableStateFlow("gpt-4o")
    val activeModel: StateFlow<String> = _activeModel.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setProviderAndModel(provider: String, model: String) {
        _activeProvider.value = provider
        _activeModel.value = model
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
    
    fun dismissError() {
        _errorMessage.value = null
    }

    fun detectOutputMode(message: String): OutputMode {
        val lower = message.lowercase()

        // VIDEO
        val videoKeywords = listOf(
            "genera video", "crea video", "fai un video", "video di",
            "animazione", "clip di", "veo", "genera un video", "fammi vedere un video"
        )
        if (videoKeywords.any { lower.contains(it) }) return OutputMode.VIDEO

        // IMMAGINE
        val imageKeywords = listOf(
            "genera immagine", "crea immagine", "disegna", "illustra",
            "fai una foto", "genera una foto", "immagine di", "foto di",
            "dipingi", "mostrami", "visualizza", "dall-e", "imagen",
            "genera un'immagine", "crea un'immagine", "fammi vedere",
            "come appare", "come sarebbe", "come è fatto"
        )
        if (imageKeywords.any { lower.contains(it) }) return OutputMode.IMAGE

        // AUDIO
        val audioKeywords = listOf(
            "leggi ad alta voce", "pronuncia", "ascolta", "audio di",
            "voce", "tts", "text to speech", "dimmi a voce"
        )
        if (audioKeywords.any { lower.contains(it) }) return OutputMode.AUDIO

        // Default: testo
        return OutputMode.TEXT
    }

    fun sendMessage(content: String, imageUrl: String? = null) {
        if (content.isBlank() && imageUrl == null) return
        
        val outputMode = detectOutputMode(content)
        
        val userMessage = Message(role = "user", content = content, imageUrl = imageUrl)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        val pendingMessageId = System.currentTimeMillis().toString()
        if (outputMode != OutputMode.TEXT) {
            val loadingMsg = when(outputMode) {
                OutputMode.IMAGE -> "ok, genero l'immagine..."
                OutputMode.VIDEO -> "ci vorrà un attimo, sto generando il video..."
                OutputMode.AUDIO -> "eccolo..."
                else -> ""
            }
            _messages.value = _messages.value + Message(
                id = pendingMessageId,
                role = "assistant",
                content = loadingMsg,
                model = _activeModel.value
            )
        }

        viewModelScope.launch {
            try {
                val systemPrompt = dataStoreManager.systemPromptFlow.first()
                val provider = _activeProvider.value
                val model = _activeModel.value
                
                // Crea messaggi filtrati (se stiamo gestendo solo testo finora)
                val history = _messages.value.filter { !it.isLoading && it.imageUrl == null && it.id != pendingMessageId }

                val responseContext = when (outputMode) {
                    OutputMode.VIDEO -> {
                        val key = dataStoreManager.geminiFlow.first()
                        if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                        GeminiService.generateVideo(key, content)
                    }
                    OutputMode.IMAGE -> {
                        if (provider == "OpenAI") {
                            val key = dataStoreManager.openAIFlow.first()
                            if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                            OpenAIService.generateImage(key, content)
                        } else {
                            val key = dataStoreManager.geminiFlow.first()
                            if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                            GeminiService.generateImage(key, content)
                        }
                    }
                    OutputMode.AUDIO -> {
                        val key = dataStoreManager.openAIFlow.first()
                        if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                        OpenAIService.generateAudio(key, content)
                    }
                    OutputMode.TEXT -> {
                        when (provider) {
                            "Anthropic", "Claude" -> {
                                val key = dataStoreManager.anthropicFlow.first()
                                if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                                AnthropicService.sendMessage(key, model, systemPrompt, history)
                            }
                            "Google", "Gemini" -> {
                                val key = dataStoreManager.geminiFlow.first()
                                if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                                GeminiService.sendMessage(key, model, systemPrompt, history)
                            }
                            else -> { // OpenAI
                                val key = dataStoreManager.openAIFlow.first()
                                if (key.isBlank()) throw Exception("aspè, hai messo la chiave API nelle impostazioni? vai su ⚙️")
                                OpenAIService.sendMessage(key, model, systemPrompt, history)
                            }
                        }
                    }
                }

                if (outputMode != OutputMode.TEXT) {
                     _messages.value = _messages.value.map { 
                         if (it.id == pendingMessageId) it.copy(content = responseContext) else it
                     }
                } else {
                    _messages.value = _messages.value + Message(
                        role = "assistant",
                        content = responseContext,
                        model = model
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.message ?: "qualcosa è andato storto, non è colpa mia. riprova"
                _errorMessage.value = if (msg.contains("aspè")) msg 
                else if (msg.contains("Network") || msg.contains("timeout")) "c'è stato un problema di rete, riprova dai"
                else "qualcosa è andato storto, non è colpa mia. riprova"
                
                if (outputMode != OutputMode.TEXT) {
                     _messages.value = _messages.value.filter { it.id != pendingMessageId }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
