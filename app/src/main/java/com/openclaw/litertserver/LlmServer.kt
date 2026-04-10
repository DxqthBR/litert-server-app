package com.openclaw.litertserver

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatRequest(val model: String, val messages: List<Message>, val stream: Boolean = false)

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class ChatResponse(val id: String, val choices: List<Choice>)

@Serializable
data class Choice(val message: Message)

class LlmServer(private val inference: LiteRTInference) {
    private var server: NettyApplicationEngine? = null

    fun start(port: Int = 8080) {
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; ignoreUnknownKeys = true })
            }
            routing {
                post("/v1/chat/completions") {
                    val request = call.receive<ChatRequest>()
                    val prompt = request.messages.lastOrNull()?.content ?: ""
                    
                    // Here we would integrate with inference.generateResponse
                    // For now, returning a static response to prove the server works
                    val response = ChatResponse(
                        id = "chatcmpl-123",
                        choices = listOf(Choice(Message("assistant", "Hello from LiteRT! You said: $prompt")))
                    )
                    call.respond(response)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
    }
}
