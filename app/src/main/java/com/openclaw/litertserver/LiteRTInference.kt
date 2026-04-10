package com.openclaw.litertserver

import android.content.Context
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import kotlinx.coroutines.flow.collect

class LiteRTInference(private val context: Context) {
    private var engine: Engine? = null

    fun initialize(modelPath: String) {
        if (!File(modelPath).exists()) {
            throw Exception("Model file not found at $modelPath")
        }
        val config = EngineConfig(modelPath)
        engine = Engine(config)
        engine?.initialize()
    }

    fun generateResponse(prompt: String): Flow<String> = flow {
        engine?.createConversation()?.let { conversation ->
            conversation.sendMessageAsync(prompt).collect { chunk ->
                emit(chunk)
            }
        }
    }

    fun close() {
        engine?.close()
    }
}
