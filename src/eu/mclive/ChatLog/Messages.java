package eu.mclive.ChatLog;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {
	
	private ChatLog plugin;
	public String help;
	public String cmd_color;
	public String playername;
	public String seperator;
	public String help2;
	public String error;
	public String url;
	
	public Messages(ChatLog cl) {
		this.plugin = cl;
		
		File file = new File(plugin.getDataFolder(), "messages.yml");
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		cfg.addDefault("help", "&e%cmd% <playername> &7- &agets the Chatlog from a player.");
		cfg.addDefault("error", "&cNo messages found from %name%");
		cfg.addDefault("url", "&eURL: &a%url%");

		cfg.options().copyDefaults(true);
		try {
			cfg.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		help = addcolors(cfg.getString("help"));
		error = addcolors(cfg.getString("error"));
		url = addcolors(cfg.getString("url"));
		
	}
	
	private String addcolors(String msg) {
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}

}
