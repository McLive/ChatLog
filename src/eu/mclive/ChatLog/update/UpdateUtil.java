package eu.mclive.ChatLog.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Updater inspired from ViaVersion
 * https://github.com/ViaVersion/ViaVersion/tree/master/common/src/main/java/com/viaversion/viaversion/update
 */
public class UpdateUtil {

    public final static String PREFIX = ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.DARK_GREEN;
    private final static String URL = "http://api.spiget.org/v2/resources/";
    private final static int PLUGIN = 1128;
    private final static String LATEST_VERSION = "/versions/latest";

	public static void scheduleUpdateChecker(final Plugin plugin) {
		boolean updateCheckEnabled = plugin.getConfig().getBoolean("update-check");
    if (!updateCheckEnabled) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Update check: " + ChatColor.AQUA + "disabled.");
        return;
    }

    long updateInterval = plugin.getConfig().getLong("update-interval");
	if (updateInterval <= 0) {
		updateInterval = 1;
    }
	long delay = 5 * 20;
	long interval = updateInterval * 24 * 60 * 60 * 20;
    new BukkitRunnable() {
        @Override
        public void run() {
            checkForUpdates(plugin);
        }
    }.runTaskTimer(plugin, delay, interval);
	Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Update check: " + ChatColor.AQUA + "enabled.");
}

    public static void checkForUpdates(final Plugin plugin) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    final String message = getUpdateMessage(true, plugin);
                    if (message != null) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.getLogger().warning(message);
                        }
                    });
                } else {
                    Bukkit.getLogger().info("No update available.");
                }
            }
        });
}

    public static void sendUpdateMessage(final UUID uuid, final Plugin plugin) {
		if (plugin.getConfig().getBoolean("update-check"))
        new BukkitRunnable() {

            @Override
            public void run() {
                final String message = getUpdateMessage(false, plugin);
                if (message != null) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                p.sendMessage(PREFIX + message);
                            }
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void sendUpdateMessage(final Plugin plugin) {
		if (plugin.getConfig().getBoolean("update-check"))
        new BukkitRunnable() {

            @Override
            public void run() {
                final String message = getUpdateMessage(true, plugin);
                if (message != null) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            plugin.getLogger().warning(message);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private static String getUpdateMessage(boolean console, Plugin plugin) {
        if (plugin.getDescription().getVersion().equals("${project.version}")) {
            return "You are using a debug/custom version, consider updating.";
        }
        String newestString = getNewestVersion(plugin);
        if (newestString == null) {
            if (console) {
                return "Could not check for updates, check your connection.";
            } else {
                return null;
            }
        }
        Version current;
        try {
            current = new Version(plugin.getDescription().getVersion());
        } catch (IllegalArgumentException e) {
            return "You are using a custom version, consider updating.";
        }
        Version newest = new Version(newestString);
        if (current.compareTo(newest) < 0)
            return "There is a newer version available: " + newest.toString() + ", you're on: " + current.toString();
        else if (console && current.compareTo(newest) != 0) {
            if (current.getTag().toLowerCase().startsWith("dev") || current.getTag().toLowerCase().startsWith("snapshot")) {
                return "You are running a development version, please report any bugs to GitHub.";
            } else {
                return "You are running a newer version than is released!";
            }
        }
        return null;
    }

    private static String getNewestVersion(Plugin plugin) {
        try {
            URL url = new URL(URL + PLUGIN + LATEST_VERSION + "?" + System.currentTimeMillis());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "ChatLog " + plugin.getDescription().getVersion());
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            String content = "";
            while ((input = br.readLine()) != null) {
                content = content + input;
            }
            br.close();
            JSONParser parser = new JSONParser();
            JSONObject statistics;
            try {
                statistics = (JSONObject) parser.parse(content);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            return (String) statistics.get("name");
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}