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
	}
	
	public void addMessage(String server, Player p, String msg, Long timestamp) {
		Connection conn = sql.getConnection();
		try (PreparedStatement st = conn.prepareStatement("INSERT INTO messages (server, name, message, timestamp) VALUES (?,?,?,?);")) {
			st.setString(1, server);
			st.setString(2, p.getName());
			st.setString(3, msg);
			st.setLong(4, timestamp);
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

}
