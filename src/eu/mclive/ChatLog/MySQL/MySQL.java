package eu.mclive.ChatLog.MySQL;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import eu.mclive.ChatLog.ChatLog;

public class MySQL {

    private ChatLog plugin;

    private String host;
    private int port;
    private String user;
    private String password;
    private String database;

    private Connection conn;

    public MySQL(ChatLog plugin) throws Exception {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "mysql.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String db = "database.";
        cfg.addDefault(db + "host", "leer");
        cfg.addDefault(db + "port", 3306);
        cfg.addDefault(db + "user", "leer");
        cfg.addDefault(db + "password", "leer");
        cfg.addDefault(db + "database", "leer");
        cfg.options().copyDefaults(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        host = cfg.getString(db + "host");
        port = cfg.getInt(db + "port");
        user = cfg.getString(db + "user");
        password = cfg.getString(db + "password");
        database = cfg.getString(db + "database");

        conn = openConnection();
    }

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
        return conn;
    }

    public void refreshConnect() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
    }

    public Connection getConnection() {
        try {
            if (!conn.isValid(1)) {
                System.out.println("[ChatLog] Lost MySQL-Connection! Reconnecting...");
                try {
                    conn = this.openConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = this.conn.prepareStatement("SELECT 1")) {
            stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("[ChatLog] SELECT 1 - failled. Reconnecting...");
            try {
                conn = this.openConnection();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return conn;
    }

    public boolean hasConnecion() {
        try {
            return conn != null || conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    public void queryUpdate(String query) {
        Connection connection = conn;
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeRessources(ResultSet rs, PreparedStatement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn = null;
        }
    }

}
