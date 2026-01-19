package org.swim.ollamaChat;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.swim.ollamaChat.config.OllamaChatConfig;
import org.swim.ollamaChat.listener.ChatListener;
import org.swim.ollamaChat.ollama.OllamaApiClient;

public final class OllamaChat extends JavaPlugin {
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        OllamaChatConfig config = OllamaChatConfig.from(getConfig());
        OllamaApiClient apiClient = new OllamaApiClient(
            config.apiBaseUrl(),
            config.model(),
            config.requestTimeout()
        );

        chatListener = new ChatListener(this, apiClient, config, getLogger());
        getServer().getPluginManager().registerEvents(chatListener, this);
        getLogger().info("OllamaChat 已啟用，助手名稱: " + config.assistantName());

    }

    @Override
    public void onDisable() {
        if (chatListener != null) {
            HandlerList.unregisterAll(chatListener);
            chatListener = null;
        }
    }
}
