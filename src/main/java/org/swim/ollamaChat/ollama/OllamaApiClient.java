package org.swim.ollamaChat.ollama;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class OllamaApiClient {
    private final HttpClient httpClient;
    private final URI chatUri;
    private final String model;
    private final Duration requestTimeout;
    private final Gson gson;

    public OllamaApiClient(String apiBaseUrl, String model, Duration requestTimeout) {
        this.chatUri = buildChatUri(apiBaseUrl);
        this.model = model;
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();
        this.gson = new Gson();
    }

    private static URI buildChatUri(String apiBaseUrl) {
        String normalized = apiBaseUrl.endsWith("/") ? apiBaseUrl : apiBaseUrl + "/";
        return URI.create(normalized + "chat");
    }

    public CompletableFuture<String> requestReply(String prompt) {
        ChatRequest payload = new ChatRequest(
                model,
                List.of(new ChatMessage("user", prompt)),
                false
        );
        HttpRequest request = HttpRequest.newBuilder(chatUri)
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseResponse);
    }

    private String parseResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Ollama API 回應狀態碼: " + response.statusCode());
        }

        ChatResponse parsed = gson.fromJson(response.body(), ChatResponse.class);
        if (parsed == null || parsed.message == null || parsed.message.content == null) {
            throw new IllegalStateException("Ollama API 回應格式無效");
        }

        String content = parsed.message.content.trim();
        if (content.isBlank()) {
            throw new IllegalStateException("Ollama API 回應內容為空");
        }

        return content;
    }

    private record ChatRequest(String model, List<ChatMessage> messages, boolean stream) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record ChatResponse(ChatMessage message) {
    }
}
