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
data class AnthropicRequest(val model: String, val max_tokens: Int = 1024, val system: String, val messages: List<AnthropicMessage>)
@Serializable
data class AnthropicMessage(val role: String, val content: String)
@Serializable
data class AnthropicResponse(val error: AnthropicError? = null, val content: List<AnthropicContent>? = null)
@Serializable
data class AnthropicContent(val text: String)
@Serializable
data class AnthropicError(val type: String, val message: String)

object AnthropicService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
        
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun sendMessage(apiKey: String, model: String, systemPrompt: String, history: List<com.example.Message>): String = withContext(Dispatchers.IO) {
        val messages = history.map { AnthropicMessage(it.role, it.content) }

        val requestBody = json.encodeToString(AnthropicRequest(model, 2048, systemPrompt, messages))
        
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string()
            if (!response.isSuccessful) throw Exception("Error: ${response.code} $bodyStr")
            val resp = json.decodeFromString<AnthropicResponse>(bodyStr ?: "")
            resp.error?.let { throw Exception(it.message) }
            resp.content?.firstOrNull()?.text ?: throw Exception("Nessuna risposta")
        }
    }
}
