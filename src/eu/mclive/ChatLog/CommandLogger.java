package eu.mclive.ChatLog;

import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandLogger implements Listener {
	private ChatLog plugin;

	public CommandLogger(ChatLog plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		final Player p = e.getPlayer();
		final String command = e.getMessage();
		plugin.logger.info(p.getName() + ": " + command);
		
		if(e.isCancelled() || p.hasPermission("ChatLog.commandlog.bypass")) {
			plugin.logger.info("Event was cancelled!");
			return;
		}
		
		List<String> blacklist = plugin.getConfig().getStringList("Commands.blacklist");
		boolean found = false;
		for(String blocked : blacklist) {
			if(command.toLowerCase().startsWith("/" + blocked.toLowerCase())) {
				found = true;
				break;
			}
		}
		
		if(found == false) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					Date now = new Date();
					Long timestamp = new Long(now.getTime()/1000);
					String server = plugin.getConfig().getString("server");
					//System.out.println(server + p + msg + timestamp);
					plugin.sqlHandler.addMessage(server, p, command, timestamp, "command");
				}
			});
		}
		
	}
}
