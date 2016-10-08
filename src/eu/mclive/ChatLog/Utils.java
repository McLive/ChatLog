package eu.mclive.ChatLog;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by McLive on 08.10.2016.
 */
public class Utils {
    private ChatLog plugin;

    public Utils(ChatLog plugin) {
        this.plugin = plugin;
    }

    public void logMessage(Player p, String msg) {
        plugin.addMessage(p, ChatColor.stripColor(msg));
        plugin.incrementLoggedMessages();
    }
}
