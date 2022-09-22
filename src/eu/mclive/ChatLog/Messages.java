package eu.mclive.ChatLog;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

    private ChatLog plugin;
    public String prefix;
    public String url;
    public String help_above;
    public String help;
    public String help_below;
    public String command_cooldown;
    public String no_player_found;
    public String no_messages_found;
    public String no_report_saved;
    public String cmd_color;
    public String playername;
    public String seperator;
    private File file;
    private FileConfiguration cfg;

    public Messages(ChatLog plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        this.cfg = YamlConfiguration.loadConfiguration(file);

        cfg.addDefault("Prefix", "&2[ChatLog] ");
        cfg.addDefault("URL", "&eURL: &a%url%");
        cfg.addDefault("Help-Above", "§7§m                                                                     ");
        cfg.addDefault("Help", "&e%cmd% <playername> &7- &agets the Chatlog from a player.");
        cfg.addDefault("Help-Below", "§7§m                                                                     ");
        cfg.addDefault("Command-Cooldown", "&cYou have to wait &b%seconds% &cseconds.");
        cfg.addDefault("No-Player-Found", "&cThat player does not exist.");
        cfg.addDefault("No-Messages-Found", "&cNo messages found from &b%name%");
        cfg.addDefault("No-Report-Saved", "&cNo report saved");

        cfg.options().copyDefaults(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefix = addcolors(cfg.getString("Prefix"));
        url = addcolors(cfg.getString("URL"));
        help_above = addcolors(cfg.getString("Help-Above"));
        help = addcolors(cfg.getString("Help1"));
        help_below = addcolors(cfg.getString("Help-Below"));
        command_cooldown = addcolors(cfg.getString("Command-Cooldown"));
        no_player_found = addcolors(cfg.getString("No-Player-Found"));
        no_messages_found = addcolors(cfg.getString("No-Messages-Found"));
        no_report_saved = addcolors(cfg.getString("No-Report-Saved"));

    }

    private String addcolors(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    public String help() {
        return addcolors(cfg.getString("help"));
    }

}
