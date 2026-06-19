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
data class GeminiRequest(val systemInstruction: GeminiInstruction, val contents: List<GeminiContent>)
@Serializable
data class GeminiInstruction(val parts: List<GeminiPart>)
@Serializable
data class GeminiContent(val role: String, val parts: List<GeminiPart>)
@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiResponse(val error: GeminiError? = null, val candidates: List<GeminiCandidate>? = null)
@Serializable
data class GeminiCandidate(val content: GeminiContent)
@Serializable
data class GeminiError(val message: String)

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
        
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun sendMessage(apiKey: String, model: String, systemPrompt: String, history: List<com.example.Message>): String = withContext(Dispatchers.IO) {
        val contents = history.map { 
            // Gemini uses "user" and "model"
            val geminiRole = if (it.role == "assistant") "model" else "user"
            GeminiContent(geminiRole, listOf(GeminiPart(it.content))) 
        }

        val requestBody = json.encodeToString(
            GeminiRequest(GeminiInstruction(listOf(GeminiPart(systemPrompt))), contents)
        )
        
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string()
            if (!response.isSuccessful) throw Exception("Error: ${response.code} $bodyStr")
            val resp = json.decodeFromString<GeminiResponse>(bodyStr ?: "")
            resp.error?.let { throw Exception(it.message) }
            resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Nessuna risposta")
        }
    }

    suspend fun generateVideo(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(2000)
        "[Video generato con Veo per: $prompt]"
    }

    suspend fun generateImage(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(2000)
        "[Immagine generata con Imagen per: $prompt]"
    }
}
