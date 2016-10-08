package eu.mclive.ChatLog;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {
	private ChatLog plugin;

	public ChatListener(ChatLog plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerChat2(final PlayerChatEvent e) {
		if(e.isCancelled()) {
			return;
		}
		final Player p = e.getPlayer();
		final String msg = e.getMessage();
		plugin.addMessage(p, ChatColor.stripColor(msg));
		plugin.incrementLoggedMessages();
	}
	
}
