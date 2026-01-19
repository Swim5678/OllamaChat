package org.swim.ollamaChat.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.swim.ollamaChat.config.OllamaChatConfig;
import org.swim.ollamaChat.ollama.OllamaApiClient;

public final class ChatListener implements Listener {
    private final Plugin plugin;
    private final OllamaApiClient apiClient;
    private final OllamaChatConfig config;
    private final Logger logger;
    private final Pattern mentionPattern;

    public ChatListener(Plugin plugin, OllamaApiClient apiClient, OllamaChatConfig config, Logger logger) {
        this.plugin = plugin;
        this.apiClient = apiClient;
        this.config = config;
        this.logger = logger;
        this.mentionPattern = Pattern.compile(
            "@" + Pattern.quote(config.assistantName()),
            Pattern.CASE_INSENSITIVE
        );
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String originalMessage = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (originalMessage.isBlank()) {
            return;
        }

        String prompt = sanitizePrompt(originalMessage);
        if (prompt.isBlank()) {
            return;
        }

        UUID playerId = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        apiClient.requestReply(prompt)
            .thenAccept(reply -> sendReply(playerId, playerName, originalMessage, reply))
            .exceptionally(throwable -> {
                handleFailure(playerId, throwable);
                return null;
            });
    }

    private String sanitizePrompt(String message) {
        String sanitized = mentionPattern.matcher(message).replaceAll("").trim();
        if (sanitized.isBlank()) {
            return message;
        }
        return sanitized;
    }

    private void sendReply(UUID playerId, String playerName, String originalMessage, String reply) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Component response = buildResponse(playerName, originalMessage, reply);
            Bukkit.getServer().sendMessage(response);
        });
    }

    private Component buildResponse(String playerName, String originalMessage, String reply) {
        TextComponent quote = Component.text()
            .append(Component.text("↪ ").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(playerName).color(NamedTextColor.GRAY))
            .append(Component.text(": ").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(originalMessage).color(NamedTextColor.GRAY))
            .decoration(TextDecoration.ITALIC, true)
            .build();

        TextComponent answer = Component.text()
            .append(Component.text(config.assistantName()).color(NamedTextColor.AQUA))
            .append(Component.text(": ").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(reply).color(NamedTextColor.WHITE))
            .build();

        return Component.text()
            .append(quote)
            .append(Component.newline())
            .append(answer)
            .build();
    }

    private void handleFailure(UUID playerId, Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
        logger.log(Level.WARNING, "Ollama API 呼叫失敗", cause);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(Component.text("AI 暫時無法回覆，請稍後再試。", NamedTextColor.RED));
            }
        });
    }
}
