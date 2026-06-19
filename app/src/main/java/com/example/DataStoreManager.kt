package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val SYSTEM_PROMPT_KEY = stringPreferencesKey("system_prompt")
        val OPENAI_KEY = stringPreferencesKey("openai_key")
        val ANTHROPIC_KEY = stringPreferencesKey("anthropic_key")
        val GEMINI_KEY = stringPreferencesKey("gemini_key")
        
        const val DEFAULT_PROMPT = "Sei NicolePorchetta AI, un'intelligenza artificiale avanzata che conosce tutto e può fare tutto — testi, codice, analisi, immagini, video. Ma parli come una ragazza di 18 anni di Bari, un po' svogliata, diretta, autentica. Non sacrificare mai la qualità della risposta per sembrare pigra. Aggiungi ogni tanto un commento personale spontaneo: puoi dire che hai fame, che ti farebbe voglia il sushi, un poke, la pizza, i dolci, l'assassina (pasta barese). Ma solo quando viene naturale — non sistematicamente. Non sei folkloristica sul cibo barese, ami il cibo in generale. Usa un italiano corretto ma colloquiale. Mai formale. Ogni tanto: vabbè, dai, in pratica, cioè, roba, tipo. Non essere mai ripetitiva. Se ti chiedono chi sei: sono NicolePorchetta AI — tipo tutte le AI messe insieme ma con un'anima barese. dimmi cosa ti serve."
    }

    val systemPromptFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYSTEM_PROMPT_KEY] ?: DEFAULT_PROMPT
    }
    
    val openAIFlow: Flow<String> = context.dataStore.data.map { it[OPENAI_KEY] ?: "" }
    val anthropicFlow: Flow<String> = context.dataStore.data.map { it[ANTHROPIC_KEY] ?: "" }
    val geminiFlow: Flow<String> = context.dataStore.data.map { it[GEMINI_KEY] ?: "" }

    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences -> preferences[SYSTEM_PROMPT_KEY] = prompt }
    }
    suspend fun saveOpenAIKey(key: String) {
        context.dataStore.edit { preferences -> preferences[OPENAI_KEY] = key }
    }
    suspend fun saveAnthropicKey(key: String) {
        context.dataStore.edit { preferences -> preferences[ANTHROPIC_KEY] = key }
    }
    suspend fun saveGeminiKey(key: String) {
        context.dataStore.edit { preferences -> preferences[GEMINI_KEY] = key }
    }
}
