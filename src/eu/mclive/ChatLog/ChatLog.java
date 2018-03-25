package eu.mclive.ChatLog;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import eu.mclive.ChatLog.update.UpdateListener;
import eu.mclive.ChatLog.update.UpdateUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import eu.mclive.ChatLog.Commands.Chatreport;
import eu.mclive.ChatLog.MySQL.MySQL;
import eu.mclive.ChatLog.MySQL.MySQLHandler;

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
        utils = new Utils(this);

        this.setupConfig();

        Date now = new Date();
        pluginstart = now.getTime() / 1000L;

        boolean metrics = getConfig().getBoolean("metrics");

        if (metrics) {
            logger.info("Loading bStats ...");
            try {
                this.startBstats(new eu.mclive.ChatLog.bstats.Metrics(this));
                logger.info("bStats successfully loaded.");
            } catch (Exception e) {
                logger.warning("Failed to load bStats.");
            }
        }

        this.cleanup();

        this.registerEvents();
        this.registerCommands();
        this.checkUpdates();

        logger.info("Plugin successfully started.");
    }

    public void onDisable() {
        logger.info("Plugin successfully stopped.");
    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerCommands() {
        getCommand("chatreport").setExecutor(new Chatreport(this));
    }

    private void registerEvents() {
        if (getConfig().getBoolean("use-AsyncChatEvent")) {
            getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
        } else {
            logger.info("Using NON-Async ChatEvent.");
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
        getServer().getPluginManager().registerEvents(new UpdateListener(this), this);
    }

    private void startBstats(eu.mclive.ChatLog.bstats.Metrics bstats) {
        bstats.addCustomChart(new eu.mclive.ChatLog.bstats.Metrics.SingleLineChart("issued_chatlogs") {
            @Override
            public int getValue() {
                int value = issuedChatLogs;
                issuedChatLogs = 0;
                return value;
            }
        });

        bstats.addCustomChart(new eu.mclive.ChatLog.bstats.Metrics.SingleLineChart("logged_messages") {
            @Override
            public int getValue() {
                int value = loggedMessages;
                loggedMessages = 0;
                return value;
            }
        });
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
                String server = getConfig().getString("server");
                String bypassChar = getConfig().getString("bypass-with-beginning-char");
                String bypassPermission = getConfig().getString("bypass-with-permission");
                //System.out.println(server + p + msg + timestamp);
                if (bypassChar.isEmpty() || (msg.startsWith(bypassChar) && !p.hasPermission(bypassPermission)) || !msg.startsWith(bypassChar)) {
                    sqlHandler.addMessage(server, p, msg, timestamp);
                }
            }
        });
    }

    public void cleanup() {
        final String server = getConfig().getString("server");
        boolean doCleanup = getConfig().getBoolean("Cleanup.enabled");
        int since = getConfig().getInt("Cleanup.since");

        if (doCleanup) {
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
                    logger.warning("Failed to reload MySQL: " + e.toString());
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
