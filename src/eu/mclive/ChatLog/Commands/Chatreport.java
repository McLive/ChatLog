package eu.mclive.ChatLog.Commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.mclive.ChatLog.ChatLog;

public class Chatreport implements CommandExecutor {
	private ChatLog plugin;
	
	public Chatreport(ChatLog plugin) {
		this.plugin = plugin;
	}
	
	private HashMap<String, Long> lastReport = new HashMap<>();
	
	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, final String[] args) {
		final String player = sender.getName();

		if (cmd.getName().equalsIgnoreCase("chatreport")) {
			Long last = this.lastReport.get(player);
			Long cooldown = plugin.getConfig().getLong("reportCooldown") * 1000;
			if(last != null && cooldown > 0) {
				Long now = System.currentTimeMillis();
				Long until = last + cooldown;
				if(System.currentTimeMillis() <= until) {
					Long left = (until - now) / 1000;
					sender.sendMessage(plugin.messages.cooldown.replace("%seconds%", left.toString()));
					return true;
				}
			}
			if (args.length == 0) {
				sender.sendMessage("§7§m                                                                     ");
				sender.sendMessage(plugin.messages.help.replace("%cmd%", "/" + commandLabel));
				sender.sendMessage("§7§m                                                                     ");
			}
			if (args.length >= 1) {
				final Date now = new Date();
				final Long timestamp = now.getTime() / 1000;
	            final String server = plugin.getConfig().getString("server");
	            boolean mode = plugin.getConfig().getBoolean("minigames-mode");
	            int timeBack = plugin.getConfig().getInt("timeBack");
	            if(!mode) { //disabled minigame mode? Only get messages from last 15 minutes!
	            	Calendar cal = Calendar.getInstance();
	            	cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE)-timeBack); //15 minutes before
	            	plugin.pluginstart = cal.getTimeInMillis() / 1000L;
	            }
	            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					public void run() {
						List<String> users = new ArrayList<>();
						for (int i = 0; i < args.length; i++) {
							String user = args[i];
							int messagesSent = plugin.sqlHandler.checkMessage(server, user, plugin.pluginstart, timestamp);
							if(messagesSent >= 1 ) {
								users.add(user);
							} else {
								sender.sendMessage(plugin.messages.error.replace("%name%", user));
							}
						}
						String reportid = UUID.randomUUID().toString().replace("-", "");
						if(users != null && users.size() > 0) {
							plugin.sqlHandler.setReport(server, users, plugin.pluginstart, timestamp, reportid);
							String URL = plugin.getConfig().getString("URL");
							sender.sendMessage(plugin.messages.url.replace("%url%", URL + reportid));
			            	lastReport.put(player, System.currentTimeMillis());
						} else {
							sender.sendMessage(plugin.messages.errorNotSaved);
						}
					}
			    });
			}
		}
		return false;
	}
	
}
