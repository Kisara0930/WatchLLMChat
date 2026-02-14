package com.kisara0930.watchllmchat

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val client: OkHttpClient

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun getSelectedModel(): String {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("selectedModel", Config.MODEL_NAME) ?: Config.MODEL_NAME
    }

    private fun isWebSearchEnabled(): Boolean {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("webSearch", false)
    }

    private fun isBriefReplyEnabled(): Boolean {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("ModelPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("briefReply", false)
    }

    private suspend fun getChatCompletion(text: String): String = withContext(Dispatchers.IO) {
        val modelName = getSelectedModel()
        val webSearchEnabled = isWebSearchEnabled()
        val briefReplyEnabled = isBriefReplyEnabled()

        var prompt = text
        if (briefReplyEnabled) {
            prompt = "简要回复：$text"
        }

        val requestBody = JSONObject()
            .put("model", modelName)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .put("stream", false)
            .put("enable_search", webSearchEnabled) // Enable web search based on preference
            .toString()

        val request = Request.Builder()
            .url("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
            .header("Authorization", "Bearer ${Config.API_KEY}")
            .header("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            throw IOException("Request failed: ${response.code} ${response.message} ($errorBody)")
        }

        val responseBody = response.body?.string()
        responseBody?.let {
            val json = JSONObject(it)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                return@withContext message.getString("content")
            }
        }
        throw IOException("No response from API")
    }

    fun sendMessage(text: String) {
        val userMessage = Message(text = text, author = "user")
        _messages.value = _messages.value + userMessage

        val loadingMessage = Message(text = "", author = "ai", isLoading = true)
        _messages.value = _messages.value + loadingMessage

        viewModelScope.launch {
            try {
                val responseText = getChatCompletion(text)
                val currentList = _messages.value
                val lastMessage = currentList.last()
                val updatedMessage = lastMessage.copy(text = responseText, isLoading = false)
                _messages.value = currentList.dropLast(1) + updatedMessage
            } catch (e: IOException) {
                val currentList = _messages.value
                val lastMessage = currentList.last()
                val updatedMessage = lastMessage.copy(text = "Error: ${e.message}", isLoading = false)
                _messages.value = currentList.dropLast(1) + updatedMessage
            }
        }
    }
}
