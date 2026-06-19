package com.example.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class OpenAIGenerateRequest(val model: String, val messages: List<OpenAIMessage>, val stream: Boolean = false)
@Serializable
data class OpenAIMessage(val role: String, val content: String)
@Serializable
data class OpenAIResponse(val error: APIError? = null, val choices: List<OpenAIChoice>? = null)
@Serializable
data class OpenAIChoice(val message: OpenAIMessage)
@Serializable
data class APIError(val message: String)

@Serializable
data class OpenAIImageRequest(val model: String = "dall-e-3", val prompt: String, val n: Int = 1, val size: String = "1024x1024")
@Serializable
data class OpenAIImageResponse(val error: APIError? = null, val data: List<OpenAIImageUrl>? = null)
@Serializable
data class OpenAIImageUrl(val url: String)

@Serializable
data class OpenAIAudioRequest(val model: String = "tts-1", val input: String, val voice: String = "alloy")

object OpenAIService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun sendMessage(apiKey: String, model: String, systemPrompt: String, history: List<com.example.Message>): String = withContext(Dispatchers.IO) {
        val messages = mutableListOf(OpenAIMessage("system", systemPrompt))
        history.forEach {
            messages.add(OpenAIMessage(it.role, it.content))
        }

        val requestBody = json.encodeToString(OpenAIGenerateRequest(model, messages))
        
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string()
            if (!response.isSuccessful) throw Exception("Error: ${response.code} $bodyStr")
            val resp = json.decodeFromString<OpenAIResponse>(bodyStr ?: "")
            resp.error?.let { throw Exception(it.message) }
            resp.choices?.firstOrNull()?.message?.content ?: throw Exception("Nessuna risposta")
        }
    }

    suspend fun generateImage(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = json.encodeToString(OpenAIImageRequest(prompt = prompt))
        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string()
            if (!response.isSuccessful) throw Exception("Error: ${response.code} $bodyStr")
            val resp = json.decodeFromString<OpenAIImageResponse>(bodyStr ?: "")
            resp.error?.let { throw Exception(it.message) }
            val url = resp.data?.firstOrNull()?.url ?: throw Exception("Nessuna immagine generata")
            url // return the URL string directly, the chat UI can display it if we modify it or we just return it as a message
        }
    }

    suspend fun generateAudio(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(2000)
        "[Generazione Audio OpenAI non ancora supportata in riproduzione: $prompt]"
    }
}
