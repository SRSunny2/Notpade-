package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.TextPart
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    BOT
}

enum class AiModelOption(
    val modelId: String,
    val label: String,
    val roleDescription: String
) {
    GENERAL("gemini-3.8-flash", "General (3.8 Flash)", "Versatile Diary Copilot & Expense Tracker"),
    FAST("gemini-3.1-flash-lite", "Fast (3.1 Lite)", "Ultra-fast quick reflections & summaries"),
    COMPLEX("gemini-4-argon", "Complex (4 Argon)", "Complex interactive analysis & budgeting"),
    REASONING("gemini-4-carbon", "Reasoning (4 Carbon)", "Deep life reflections & financial audits")
}

class GeminiAiService {
    private val TAG = "GeminiAiService"

    private val systemPrompt =
        "You are MyPocket Diary AI, a friendly, intelligent personal diary assistant and financial advisor specialized in helping users organize their daily diary notes and manage expenses in Bangladeshi Taka (৳). Be empathetic, organized, concise, and helpful. Always format financial references in ৳ with 2 decimal places."

    suspend fun transcribeAudio(audioBytes: ByteArray): String {
        val audioContent = Content(
            role = "user",
            parts = listOf(
                InlineDataPart(audioBytes, "audio/mp4"),
                TextPart("Transcribe this spoken audio exactly into plain text. Return only the transcript with no surrounding notes or commentary.")
            )
        )

        return withTimeout(60_000L) {
            try {
                // Try requested gemini-3.5-transcribe model
                val model = Firebase.ai.generativeModel(
                    modelName = "gemini-3.5-transcribe"
                )
                val response: GenerateContentResponse = model.generateContent(listOf(audioContent))
                val text = response.text?.trim()
                if (!text.isNullOrBlank()) text else throw IllegalStateException("Empty transcription returned.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "gemini-3.5-transcribe failed, falling back to gemini-3.8-flash: ${e.message}")
                val fallbackModel = Firebase.ai.generativeModel(
                    modelName = "gemini-3.8-flash"
                )
                val response: GenerateContentResponse = fallbackModel.generateContent(listOf(audioContent))
                response.text?.trim() ?: "Could not transcribe audio."
            }
        }
    }

    fun streamChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        modelOption: AiModelOption
    ): Flow<String> = flow {
        val historyContents = history.map { msg ->
            Content(
                role = if (msg.sender == MessageSender.USER) "user" else "model",
                parts = listOf(TextPart(msg.text))
            )
        }

        val systemInstructionContent = Content(
            parts = listOf(TextPart(systemPrompt))
        )

        try {
            val model = Firebase.ai.generativeModel(
                modelName = modelOption.modelId,
                systemInstruction = systemInstructionContent
            )
            val chat = model.startChat(historyContents)
            chat.sendMessageStream(userMessage).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "${modelOption.modelId} chat stream failed, falling back to gemini-3.8-flash: ${e.message}")
            val fallbackModel = Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                systemInstruction = systemInstructionContent
            )
            val fallbackChat = fallbackModel.startChat(historyContents)
            fallbackChat.sendMessageStream(userMessage).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        }
    }
}
