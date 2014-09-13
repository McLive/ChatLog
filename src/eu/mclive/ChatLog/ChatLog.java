package eu.mclive.ChatLog;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import eu.mclive.ChatLog.MySQL.MySQL;
import eu.mclive.ChatLog.MySQL.MySQLHandler;

public class ChatLog extends JavaPlugin implements Listener {
	
	public final Logger logger = getLogger();
	public static MySQL sql;
	public MySQLHandler sqlHandler;
	public static ChatLog INSTANCE;
	Long pluginstart = null;
	
	public void onEnable() {
		ChatLog.INSTANCE = this;
		try {
			logger.info("Loading MySQL ...");
			sql = new MySQL(this);
			sqlHandler = new MySQLHandler(sql);
			startRefresh();
			logger.info("MySQL successfully loaded.");
		} catch (Exception e1) {
			logger.warning("Failled to load MySQL: " + e1.toString());
		}
		
		getConfig().options().copyDefaults(true);
        saveConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
		
        Date now = new Date();
        pluginstart = new Long(now.getTime()/1000);
		
        logger.info("Loading Metrics ...");
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            logger.info("Metrics successfully loaded.");
        } catch (IOException e) {
            logger.warning("Failled to load Metrics.");
        }
        
		logger.info("Plugin successfully started.");
	}
	
	public void onDisable() {
		logger.info("Plugin successfully stopped.");
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		final Player p = e.getPlayer();
		final String msg = e.getMessage();
	    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
	    	public void run() {
	            Date now = new Date();
	            Long timestamp = new Long(now.getTime()/1000);
	            String server = getConfig().getString("server");
	            //System.out.println(server + p + msg + timestamp);
	            ChatLog.INSTANCE.sqlHandler.addMessage(server, p, msg, timestamp);
	    	}
	    });
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only Players can run this command!");
			return true;
		}

		final Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("chatreport")) {
			if (args.length == 0 || args.length > 1) {
				p.sendMessage("§7§m                                                                     ");
				p.sendMessage("§e/" + commandLabel + " <playername> §7- §agets the Chatlog from a player.");
				p.sendMessage("§7§m                                                                     ");
			} else if (args.length == 1) {
				final String p2 = args[0];
				Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
					public void run() {
			            Date now = new Date();
			            Long timestamp = new Long(now.getTime()/1000);
			            String server = getConfig().getString("server");
			            int messagesSent = sqlHandler.checkMessage(server, p2, pluginstart, timestamp);
			            if(messagesSent >= 1) {
			            	ChatLog.this.logger.info("[" + p.getName() + "] getting ChatLog from " + p2);
			            	String reportid = UUID.randomUUID().toString().replace("-", "");
			            	sqlHandler.setReport(server, p2, pluginstart, timestamp, reportid);
			            	String URL = getConfig().getString("URL");
			            	p.sendMessage("§eURL: §a" + URL + reportid);
			            } else {
			            	p.sendMessage("§cNo messages found from " + p2);
			            }
			    	}
			    });
			}
		}
		return false;
	}
	public void startRefresh(){
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				try {
					sql.refreshConnect();
				} catch (Exception e) {
					logger.warning("Failled to reload MySQL: " + e.toString());
				}
			}
		}, 20L*10, 20L * 1800);
	}
}
