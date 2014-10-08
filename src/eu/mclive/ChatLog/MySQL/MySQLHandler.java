package eu.mclive.ChatLog.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import eu.mclive.ChatLog.ChatLog;

public class MySQLHandler {
	
	private MySQL sql;
	
	public MySQLHandler(MySQL mysql) {
		sql = mysql;
		sql.queryUpdate("CREATE TABLE IF NOT EXISTS messages (id int NOT NULL AUTO_INCREMENT,server varchar(100),name varchar(100),message varchar(400),timestamp varchar(50),PRIMARY KEY (id))");
		sql.queryUpdate("CREATE TABLE IF NOT EXISTS reportmessages (id int NOT NULL AUTO_INCREMENT,server varchar(100),name varchar(100),message varchar(400),timestamp varchar(50),reportid text,PRIMARY KEY (id))");
		//ALTER TABLE reportmessages ADD type VARCHAR(400) AFTER reportid;
		//ALTER TABLE messages ADD type VARCHAR(400) AFTER timestamp;
	}
	
	public void addMessage(String server, Player p, String msg, Long timestamp, String type) {
		Connection conn = sql.getConnection();
		try (PreparedStatement st = conn.prepareStatement("INSERT INTO messages (server, name, message, timestamp, type) VALUES (?,?,?,?,?);")) {
			st.setString(1, server);
			st.setString(2, p.getName());
			st.setString(3, msg);
			st.setLong(4, timestamp);
			st.setString(5, type);
			st.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int checkMessage(String server, String p2, Long pluginstart, Long timestamp) {
		Connection conn = sql.getConnection();
		ResultSet rs = null;
		try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) AS count FROM messages WHERE server = ? && name = ? && timestamp >= ? && timestamp <= ?;")) {
			st.setString(1, server);
			st.setString(2, p2);
			st.setLong(3, pluginstart);
			st.setLong(4, timestamp);
			rs = st.executeQuery();
			rs.first();
			//System.out.println("Von " + p2 + " gesendete Nachrichten seit Pluginstart: " + rs.getInt("count") );
			return rs.getInt("count");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public void setReport(String server, String p2, Long pluginstart, Long timestamp, String reportid) {
		Connection conn = sql.getConnection();
		ResultSet rs = null;
		ChatLog.INSTANCE.logger.info("ReportID: " + reportid);
		try (PreparedStatement st = conn.prepareStatement("SELECT * FROM messages WHERE server = ? && name = ? && timestamp >= ? && timestamp <= ?;")) {
			st.setString(1, server);
			st.setString(2, p2);
			st.setLong(3, pluginstart);
			st.setLong(4, timestamp);
			rs = st.executeQuery();
			while(rs.next()) {
				try (PreparedStatement st2 = conn.prepareStatement("INSERT INTO reportmessages (server, name, message, timestamp, reportid) VALUES (?,?,?,?,?);")) {
					st2.setString(1, server);
					st2.setString(2, p2);
					st2.setString(3, rs.getString("message"));
					st2.setLong(4, rs.getLong("timestamp"));
					st2.setString(5, reportid);
					st2.executeUpdate();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void delete(String server, Long timestamp) {
		Connection conn = sql.getConnection();
		try (PreparedStatement st = conn.prepareStatement("DELETE FROM messages WHERE server = ? AND timestamp < ? ")) {
			st.setString(1, server);
			st.setLong(2, timestamp);
			int rows = st.executeUpdate();
			if(rows > 0) {
				ChatLog.INSTANCE.logger.info("Deleted " + rows + " old messages!");
			} else {
				ChatLog.INSTANCE.logger.info("There were no old messages to delete.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
