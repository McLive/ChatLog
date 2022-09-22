package eu.mclive.ChatLog;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import eu.mclive.ChatLog.Commands.Chatreport;
import eu.mclive.ChatLog.MySQL.MySQL;
import eu.mclive.ChatLog.MySQL.MySQLHandler;
import eu.mclive.ChatLog.update.UpdateListener;
import eu.mclive.ChatLog.update.UpdateUtil;

public class ChatLog extends JavaPlugin implements Listener {

    public UUIDHandler UUIDHandler;
    public Logger logger = getLogger();
    public MySQL sql;
    public Messages messages;
    public MySQLHandler sqlHandler;
    public Long pluginstart = null;
    private eu.mclive.ChatLog.bstats.Metrics bstats;
    private Utils utils;

    /**
     * Issued ChatLogs since last submit.
     */
    private int issuedChatLogs = 0;

    /**
     * Logged Messages since last submit.
     */
    private int loggedMessages = 0;

    public void onEnable() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Loading MySQL...");
            sql = new MySQL(this);
            sqlHandler = new MySQLHandler(sql, this);
            startRefresh();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "MySQL successfully loaded.");
        } catch (Exception e1) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.RED + "Failled to load MySQL: " + e1.toString());
        }

        messages = new Messages(this);
        UUIDHandler = new UUIDHandler(this);
        utils = new Utils(this);

        this.setupConfig();

        Date now = new Date();
        pluginstart = now.getTime() / 1000L;

        boolean metrics = getConfig().getBoolean("Metrics");

        if (metrics) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Loading bStats...");
            try {
                this.startBstats(new eu.mclive.ChatLog.bstats.Metrics(this));
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "bStats successfully loaded.");
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.RED + "Failed to load bStats.");
            }
        }

        this.cleanup();

        this.registerEvents();
        this.registerCommands();
        this.checkUpdates();

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Plugin started.");
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.RED + "Plugin stopped.");
    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    private void registerCommands() {
        getCommand("chatreport").setExecutor(new Chatreport(this));
    }

    private void registerEvents() {
        if (getConfig().getBoolean("use-AsyncChatEvent")) {
            getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
			            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Using Async ChatEvent:" + ChatColor.AQUA + " true");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Using Async ChatEvent:" + ChatColor.AQUA + " false");
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
        getServer().getPluginManager().registerEvents(new UpdateListener(this), this);
    }

    private void startBstats(eu.mclive.ChatLog.bstats.Metrics bstats) {
        bstats.addCustomChart(new eu.mclive.ChatLog.bstats.Metrics.SingleLineChart("issued_chatlogs", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int value = issuedChatLogs;
                issuedChatLogs = 0;
                return value;
            }
        }));

        bstats.addCustomChart(new eu.mclive.ChatLog.bstats.Metrics.SingleLineChart("logged_messages", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int value = loggedMessages;
                loggedMessages = 0;
                return value;
            }
        }));
    }

    public void incrementIssuedChatLogs() {
        issuedChatLogs++;
    }

    public void incrementLoggedMessages() {
        loggedMessages++;
    }

    public void addMessage(final Player p, final String msg) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            public void run() {
                Date now = new Date();
                Long timestamp = now.getTime() / 1000;
                String server = getConfig().getString("Server");
                String bypassCharacter = getConfig().getString("bypass-character");
                String bypassPermission = getConfig().getString("bypass-permission");
                //System.out.println(server + p + msg + timestamp);
                if (bypassCharacter.isEmpty() || (msg.startsWith(bypassCharacter) && !p.hasPermission(bypassPermission)) || !msg.startsWith(bypassCharacter)) {
                    sqlHandler.addMessage(server, p, msg, timestamp);
                }
            }
        });
    }

    public void cleanup() {
        final String server = getConfig().getString("Server");
        boolean doCleanup = getConfig().getBoolean("Cleanup.enabled");
        int since = getConfig().getInt("Cleanup.since");

        if (doCleanup) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Running Cleanup...");
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
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Skipping Cleanup|" + ChatColor.AQUA + " disabled.");
        }
    }

    public void startRefresh() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                try {
                    sql.refreshConnect();
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.RED + "Failed to reload MySQL: " + e.toString());
                }
            }
        }, 20L * 10, 20L * 1800);
    }

    private void checkUpdates() {
        UpdateUtil.sendUpdateMessage(this);
    }

    public Utils getUtils() {
        return utils;
    }

    public void setUtils(Utils utils) {
        this.utils = utils;
    }
}
