package eu.mclive.ChatLog;

import java.io.File;
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
import org.bukkit.scheduler.BukkitScheduler;

import eu.mclive.ChatLog.MySQL.MySQL;
import eu.mclive.ChatLog.MySQL.MySQLHandler;

public class ChatLog extends JavaPlugin implements Listener {
	
	public final Logger logger = Logger.getLogger("Minecraft");
	public static MySQL sql;
	public MySQLHandler sqlHandler;
	public static ChatLog INSTANCE;
	Long pluginstart = null;
	
	public void onEnable() {
		ChatLog.INSTANCE = this;
		try {
			this.logger.info("[ChatLog] Now Currently Loading MySQL");
			sql = new MySQL(this);
			sqlHandler = new MySQLHandler(sql);
			//startRefresh();
			this.logger.info("[ChatLog] 'MySQL' has successfully loaded!");
		} catch (Exception e1) {
			this.logger.info("[ChatLog] WARNING, FAILED TO LOAD MySQL " + e1.toString());
		}
		
	    if (!new File(getDataFolder(), "config.yml").exists()) {
	    	saveDefaultConfig();
	    }
	    getConfig().addDefault("server", "server");
	    saveConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
		
        Date now = new Date();
        pluginstart = new Long(now.getTime()/1000);
		
		System.out.println("[ChatLog] Plugin erfolgreich gestartet!");
	}
	
	public void onDisable() {
		System.out.println("[ChatLog] Plugin erfolgreich gestoppt!");
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		final Player p = e.getPlayer();
		final String msg = e.getMessage();
	    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
	    	public void run() {
	            Date now = new Date();
	            Long timestamp = new Long(now.getTime()/1000);
	            String server = ChatLog.this.getConfig().getString("server");
	            System.out.println(server + p + msg + timestamp);
	            ChatLog.INSTANCE.sqlHandler.addMessage(server, p, msg, timestamp);
	    	}
	    });
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Diesen Befehl können nur Spieler ausführen!");
			return true;
		}

		final Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("chatreport")) {
			if (args.length == 0 || args.length > 1) {
				p.sendMessage("Type " + commandLabel + " <playername> to get all messgaes sent by that Player.");
			} else if (args.length == 1) {
				final String p2 = args[0];
				Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
					public void run() {
			            Date now = new Date();
			            Long timestamp = new Long(now.getTime()/1000);
			            String server = ChatLog.this.getConfig().getString("server");
			            int messagesSent = ChatLog.INSTANCE.sqlHandler.checkMessage(server, p2, pluginstart, timestamp);
			            if(messagesSent >= 1) {
			            	String reportid = UUID.randomUUID().toString().replace("-", "");
			            	ChatLog.INSTANCE.sqlHandler.setReport(server, p2, pluginstart, timestamp, reportid);
			            	p.sendMessage("URL: http://freecraft.eu/chatreport/" + reportid);
			            	//SELECT * FROM `messages` WHERE 'e01c88e99e8f4530a292aaba7e236e03'  LIKE CONCAT('%',`reportids` , '%');
				            Bukkit.getScheduler().runTask(INSTANCE, new Runnable(){
				            	@Override
				            	public void run() {
				            		System.out.println("Test!");
				            	}
				            });
			            } else {
			            	p.sendMessage("Dieser Spieler hat noch keine Nachrichten gesendet!");
			            }
			    	}
			    });
				/*
			     Player p2 = Bukkit.getPlayer(args[0]);
			     if (p2 != null) {
			    	 String reportid = UUID.randomUUID().toString().replace("-", "");
			     } else {
			     	p.sendMessage("Dieser Spieler ist offline!");
			     }
			     */
			}
		}
		return false;
	}
	
}
