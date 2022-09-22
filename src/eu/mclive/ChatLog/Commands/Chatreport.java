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
            Long cooldown = plugin.getConfig().getLong("Cooldown") * 1000;
            if (last != null && cooldown > 0) {
                Long now = System.currentTimeMillis();
                Long until = last + cooldown;
                if (System.currentTimeMillis() <= until) {
                    Long left = (until - now) / 1000;
                    sender.sendMessage(plugin.messages.prefix + plugin.messages.command_cooldown.replace("%seconds%", left.toString()));
                    return true;
                }
            }
            if (args.length == 0) {
                sender.sendMessage(plugin.messages.help_above);
                sender.sendMessage(plugin.messages.help.replace("%cmd%", "/" + commandLabel));
                sender.sendMessage(plugin.messages.help_below);
            }
            if (args.length >= 1) {
                final Date now = new Date();
                final Long timestamp = now.getTime() / 1000;
                final String server = plugin.getConfig().getString("Server");
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
                                sender.sendMessage(plugin.messages.prefix + plugin.messages.no_messages_found.replace("%name%", user));
                            }
                        }
                        String reportid = UUID.randomUUID().toString().replace("-", "");
                        if (users != null && users.size() > 0) {
                            plugin.sqlHandler.setReport(server, users, plugin.pluginstart, timestamp, reportid);
                            String URL = plugin.getConfig().getString("URL");
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.url.replace("%url%", URL + reportid));
                            lastReport.put(player, System.currentTimeMillis());
                            plugin.incrementIssuedChatLogs();
                        } else {
                            sender.sendMessage(plugin.messages.prefix + plugin.messages.no_report_saved);
                        }
                    }
                });
            }
        }
        return false;
    }

}
