package eu.mclive.ChatLog.Commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import eu.mclive.ChatLog.ChatLog;
import eu.mclive.ChatLog.Messages;

public class Chatreport implements CommandExecutor {
    private ChatLog plugin;
	private Messages messages;

    public Chatreport(ChatLog plugin) {
        this.plugin = plugin;
		this.messages = plugin.messages;
    }

    private HashMap<String, Long> lastReport = new HashMap<>();

    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, final String[] args) {
        final String player = sender.getName();
		plugin.UUIDHandler.resetMessage();
        if (cmd.getName().equalsIgnoreCase("chatreport"))
			if (sender.hasPermission("chatlog.command"))
		{
            Long last = this.lastReport.get(player);
            Long cooldown = plugin.getConfig().getLong("Cooldown") * 1000;
            if (last != null && cooldown > 0) {
                Long now = System.currentTimeMillis();
                Long until = last + cooldown;
                if (System.currentTimeMillis() <= until) {
                    Long left = (until - now) / 1000;
					if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                    sender.sendMessage(plugin.messages.prefix + plugin.messages.command_cooldown.replace("%seconds%", left.toString()));
					}
                    return true;
                }
            }
            if (args.length == 0) {
				if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                sender.sendMessage(plugin.messages.help_above);
				}
				if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                sender.sendMessage(plugin.messages.help.replace("%cmd%", "/" + commandLabel));
				}
				if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                sender.sendMessage(plugin.messages.help_below);
				}
            }
            if(args.length == 1 && args[0].equalsIgnoreCase("reload"))
            {
                if (sender.hasPermission("chatlog.reload"))
                {
					                            sender.sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Plugin reloading...");
				try {
					plugin.reloadConfig();
                    messages.MessagesReload();
					plugin.saveConfig();
                    sender.sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Plugin successfully reloaded.");
                    sender.sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "You may need to restart the server for certain changes to take affect.");
				} catch (Exception e) {
					sender.sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.RED + "An error occurred while trying to reload the plugin.");
					e.printStackTrace();
				}
                    return true;
                }
                else
                {
					if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.no_permission);
					}
                    return true;
                }
            }
            if (args.length >= 1) {
                final Date now = new Date();
                final Long timestamp = now.getTime() / 1000;
                final String server = plugin.getConfig().getString("Server-Name");
                boolean mode = plugin.getConfig().getBoolean("minigames-mode");
                int ChatHistory = plugin.getConfig().getInt("Chat-History");
                if (!mode) { //disabled minigame mode? Only get messages from last 15 minutes!
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - ChatHistory); //15 minutes before
                    plugin.pluginstart = cal.getTimeInMillis() / 1000L;
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        List<String> users = new ArrayList<>();
                        for (int i = 0; i < args.length; i++) {
                            String user = args[i];
                            int messagesSent = plugin.sqlHandler.checkMessage(server, user, plugin.pluginstart, timestamp);
                            if (messagesSent >= 1) {
                                users.add(user);
                            } else {
								if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                                sender.sendMessage(plugin.messages.prefix + plugin.messages.no_messages_found.replace("%name%", user));
								}
                            }
                        }
                        String reportid = UUID.randomUUID().toString().replace("-", "");
                        if (users != null && users.size() > 0) {
                            plugin.sqlHandler.setReport(server, users, plugin.pluginstart, timestamp, reportid);
                            String URL = plugin.getConfig().getString("URL");
							if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.url.replace("%url%", URL + reportid));
							}
                            lastReport.put(player, System.currentTimeMillis());
                            plugin.incrementIssuedChatLogs();
                        } else {
							if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.no_report_saved);
							}
                        }
                    }
                });
            }
        }
                else
                {
					if (messages.no_messages_found != null && !messages.no_messages_found.isEmpty()) {
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.no_permission);
					}
                    return true;
                }
        return false;
    }

}
