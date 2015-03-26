package eu.mclive.ChatLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import eu.mclive.ChatLog.Commands.Chatreport;
import eu.mclive.ChatLog.MySQL.MySQL;
import eu.mclive.ChatLog.MySQL.MySQLHandler;

public class ChatLog extends JavaPlugin implements Listener {
	
	public UUIDHandler UUIDHandler;
	public final Logger logger = getLogger();
	public static MySQL sql;
	public static Messages messages;
	public MySQLHandler sqlHandler;
	public static ChatLog INSTANCE;
	public Long pluginstart = null;
	
	public void onEnable() {
		ChatLog.INSTANCE = this;
		try {
			logger.info("Loading MySQL ...");
			sql = new MySQL(this);
			sqlHandler = new MySQLHandler(sql, this);
			startRefresh();
			logger.info("MySQL successfully loaded.");
		} catch (Exception e1) {
			logger.warning("Failled to load MySQL: " + e1.toString());
		}
		
		messages = new Messages(this);
		UUIDHandler = new UUIDHandler(this);
		
		getConfig().options().copyDefaults(true);
        saveConfig();
		
        if(getConfig().getBoolean("use-AsyncChatEvent")) {
        	getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
        } else {
        	logger.info("Using NON-Async ChatEvent.");
        	getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
		
        Date now = new Date();
        pluginstart = new Long(now.getTime()/1000L);
        
        boolean metrics = getConfig().getBoolean("metrics");
		
        if(metrics) {
	        logger.info("Loading Metrics ...");
	        try {
	            Metrics me = new Metrics(this);
	            me.start();
	            logger.info("Metrics successfully loaded.");
	        } catch (IOException e) {
	            logger.warning("Failled to load Metrics.");
	        }
        }
        
        cleanup();
        
        this.registerCommands();
        
		logger.info("Plugin successfully started.");
	}
	
	public void onDisable() {
		logger.info("Plugin successfully stopped.");
	}
	
	public void registerCommands() {
		getCommand("chatreport").setExecutor(new Chatreport(this));
	}
	
	public void addMessage(final Player p, final String msg) {
	    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
	    	public void run() {
	            Date now = new Date();
	            Long timestamp = new Long(now.getTime()/1000);
	            String server = getConfig().getString("server");
	            String bypassChar = getConfig().getString("bypass-with-beginning-char");
	            String bypassPermission = getConfig().getString("bypass-with-permission");
	            //System.out.println(server + p + msg + timestamp);
	            if( bypassChar.isEmpty() || ( msg.startsWith(bypassChar) && !p.hasPermission(bypassPermission) ) || !msg.startsWith(bypassChar) ) {
	            	sqlHandler.addMessage(server, p, msg, timestamp);
	            }
	    	}
	    });
	}
	
	
	public void cleanup() {
		final String server = getConfig().getString("server");
		boolean doCleanup = getConfig().getBoolean("Cleanup.enabled");
		int since = getConfig().getInt("Cleanup.since");
		
		if(doCleanup) {
			logger.info("Doing Cleanup...");
			Calendar cal = Calendar.getInstance();
			Date now = new Date();
			cal.setTime(now);
			cal.add(Calendar.DATE, -since); // subtract days from config
		
			final Long timestamp = cal.getTimeInMillis() / 1000L;

		    Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
		    	public void run() {
		    		sqlHandler.delete(server, timestamp);
		    	}
		    });
		    
		} else {
			logger.info("Skipping Cleanup because it is disabled.");
		}
	}
	
	public void startRefresh() {
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
