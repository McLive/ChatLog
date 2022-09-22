package eu.mclive.ChatLog.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.ChatColor;

import org.bukkit.entity.Player;

import eu.mclive.ChatLog.ChatLog;

public class MySQLHandler {
    private ChatLog plugin;
    private MySQL sql;

    public MySQLHandler(MySQL mysql, ChatLog plugin) {
        sql = mysql;
        sql.queryUpdate("CREATE TABLE IF NOT EXISTS messages (id int NOT NULL AUTO_INCREMENT, server varchar(100), name varchar(100), message varchar(400), timestamp varchar(50), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        sql.queryUpdate("CREATE TABLE IF NOT EXISTS reportmessages (id int NOT NULL AUTO_INCREMENT, server varchar(100), name varchar(100), message varchar(400), timestamp varchar(50), reportid text, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci");
        this.plugin = plugin;
    }

    public void addMessage(String server, Player p, String msg, Long timestamp) {
        String name;

        if (plugin.getConfig().getBoolean("use-UUIDs")) {
            UUID uuid = p.getUniqueId();
            name = uuid.toString().replace("-", "");
        } else {
            name = p.getName();
        }

        Connection conn = sql.getConnection();
        try (PreparedStatement st = conn.prepareStatement("INSERT INTO messages (server, name, message, timestamp) VALUES (?,?,?,?);")) {
            st.setString(1, server);
            st.setString(2, name);
            st.setString(3, msg);
            st.setLong(4, timestamp);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int checkMessage(String server, String p2, Long pluginstart, Long timestamp) {
        String name = null;

        if (plugin.getConfig().getBoolean("use-UUIDs")) {
            //player could be offline
            name = plugin.UUIDHandler.getUUID(p2);
        } else {
            name = p2;
        }

        Connection conn = sql.getConnection();
        ResultSet rs = null;
        try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) AS count FROM messages WHERE server = ? && name = ? && timestamp >= ? && timestamp <= ?;")) {
            st.setString(1, server);
            st.setString(2, name);
            st.setLong(3, pluginstart);
            st.setLong(4, timestamp);
            rs = st.executeQuery();
            rs.next();
            //System.out.println("Von " + p2 + " gesendete Nachrichten seit Pluginstart: " + rs.getInt("count") );
            return rs.getInt("count");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setReport(String server, List<String> users, Long pluginstart, Long timestamp, String reportid) {
        Connection conn = sql.getConnection();
        ResultSet rs = null;
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "ReportID: " + ChatColor.YELLOW + reportid);
        for (String user : users) {
            if (plugin.getConfig().getBoolean("use-UUIDs")) {
                //player could be offline
                user = plugin.UUIDHandler.getUUID(user);
            }
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM messages WHERE server = ? && name = ? && timestamp >= ? && timestamp <= ?;")) {
                st.setString(1, server);
                st.setString(2, user);
                st.setLong(3, pluginstart);
                st.setLong(4, timestamp);
                rs = st.executeQuery();
                while (rs.next()) {
                    try (PreparedStatement st2 = conn.prepareStatement("INSERT INTO reportmessages (server, name, message, timestamp, reportid) VALUES (?,?,?,?,?);")) {
                        st2.setString(1, server);
                        st2.setString(2, user);
                        st2.setString(3, rs.getString("message"));
                        st2.setLong(4, rs.getLong("timestamp"));
                        st2.setString(5, reportid);
                        st2.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(String server, Long timestamp) {
        Connection conn = sql.getConnection();
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM messages WHERE server = ? AND timestamp < ? ")) {
            st.setString(1, server);
            st.setLong(2, timestamp);
            int rows = st.executeUpdate();
            if (rows > 0) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Cleanup Deleted " + ChatColor.AQUA + rows + ChatColor.GREEN + "messages!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Cleanup complete.");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.YELLOW + "Cleanup had no messages to delete.");
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[ChatLog] " + ChatColor.GREEN + "Cleanup complete.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
