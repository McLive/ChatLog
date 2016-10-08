package eu.mclive.ChatLog;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {
    private ChatLog plugin;

    public ChatListener(ChatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(final PlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        plugin.getUtils().logMessage(e.getPlayer(), e.getMessage());
    }
}
