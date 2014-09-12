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
	
	public MySQL(ChatLog fcb) throws Exception{
		plugin = fcb;
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
		System.out.println("[OITC] 'MySQL' has refrehed the Connection!");
	}
	
	public Connection getConnection(){
		return conn;
	}
	
	public boolean hasConnecion(){
		try {
			return conn != null || conn.isValid(1);
		} catch (SQLException e) {
			return false;
		}
	}
	
	public void queryUpdate(String query){
		Connection connection = conn;
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement(query);
			st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.closeRessources(null, st);
		}
	}
	
	public void closeRessources(ResultSet rs, PreparedStatement st){
		if(rs !=null){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(st != null){
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeConnection(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			conn = null;
		}
	}
	
}
