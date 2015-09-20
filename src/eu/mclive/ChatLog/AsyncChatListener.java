package eu.mclive.ChatLog;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatListener implements Listener {
	private ChatLog plugin;

	public AsyncChatListener(ChatLog plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerChat(final AsyncPlayerChatEvent e) {
		if(e.isCancelled()) {
			return;
		}
		final Player p = e.getPlayer();
		final String msg = e.getMessage();
		plugin.addMessage(p, ChatColor.stripColor(msg));
	}
	
}
