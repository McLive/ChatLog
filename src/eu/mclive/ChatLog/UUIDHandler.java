package eu.mclive.ChatLog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UUIDHandler implements Listener {
    private ChatLog plugin;
	private boolean messageSent;

    public UUIDHandler(ChatLog plugin) {
        this.plugin = plugin;
		this.messageSent = false;
    }

    public String getUUID(String player) {
        Player p = Bukkit.getServer().getPlayer(player);
        if (p != null) {
            UUID uuid = Bukkit.getServer().getPlayer(player).getUniqueId();
			if (!messageSent) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " is online! UUID: " + ChatColor.YELLOW + uuid.toString().replace("-", ""));
			messageSent = true;
			}
            return uuid.toString().replace("-", "");
        } else {
			if (!messageSent) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.AQUA + player + ChatColor.RED + " is offline! Fetching UUID from" + ChatColor.YELLOW +" https://api.minetools.eu/");
			messageSent = true;
			}
            final JSONParser jsonParser = new JSONParser();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + player).openConnection();
                try {
                    JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

                    String uuid = (String) response.get("id");

                    if (uuid != null) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.AQUA + player + "'s " + "UUID: " + ChatColor.YELLOW + uuid.replace("-", ""));
                        return uuid.replace("-", "");
                    } else {
                        //UUID should only be used on premium servers. It'll return null if player is not premium.
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.AQUA + ChatColor.RED + " That player does not exist.");
                        return null;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
    }
		
    public void resetMessage() {
        messageSent = false;
    }
}