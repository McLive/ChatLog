package eu.mclive.ChatLog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UUIDHandler implements Listener {
	private ChatLog plugin;

	public UUIDHandler(ChatLog plugin) {
		this.plugin = plugin;
	}
	
	public String getUUID(String player) {
		
		Player p = Bukkit.getServer().getPlayer(player);
		if(p != null) {
			UUID uuid = Bukkit.getServer().getPlayer(player).getUniqueId();
			plugin.logger.info(p.getName() + " is online! UUID: " + uuid.toString().replace("-", ""));
			return uuid.toString().replace("-", "");
		} else {
			plugin.logger.info(player + " is offline! Fetching UUID from api.minetools.eu");
			final JSONParser jsonParser = new JSONParser();
			try {
				HttpURLConnection connection = (HttpURLConnection) new URL("http://api.minetools.eu/uuid/" + player).openConnection();
				try {
					JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
					
					String uuid = (String) response.get("uuid");
					
					if(uuid.length() > 4) {
						plugin.logger.info("UUID from " + player + ": " + uuid.toString().replace("-", ""));
						return uuid.toString().replace("-", "");
					} else {
						//UUID should only be used on premium servers. It'll return null if player is not premium.
						plugin.logger.info("Minetools returned null! " + player + " is no premium user!");
						return null;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
}
