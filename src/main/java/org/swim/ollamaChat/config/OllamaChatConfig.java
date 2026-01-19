package org.swim.ollamaChat.config;

import java.time.Duration;
import org.bukkit.configuration.file.FileConfiguration;

public record OllamaChatConfig(
    String assistantName,
    String apiBaseUrl,
    String model,
    Duration requestTimeout
) {
    public static OllamaChatConfig from(FileConfiguration config) {
        String assistantName = config.getString("assistant-name", "Ollama").trim();
        if (assistantName.isBlank()) {
            assistantName = "Ollama";
        }

        String apiBaseUrl = config.getString("api-base-url", "http://localhost:11434/api").trim();
        if (apiBaseUrl.isBlank()) {
            apiBaseUrl = "http://localhost:11434/api";
        }

        String model = config.getString("model", "llama3.2").trim();
        if (model.isBlank()) {
            model = "llama3.2";
        }

        int timeoutSeconds = config.getInt("request-timeout-seconds", 30);
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 30;
        }

        return new OllamaChatConfig(
            assistantName,
            apiBaseUrl,
            model,
            Duration.ofSeconds(timeoutSeconds)
        );
    }
}
