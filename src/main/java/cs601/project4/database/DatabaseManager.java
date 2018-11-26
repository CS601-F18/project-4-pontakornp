package cs601.project4.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import cs601.project4.Config;
import cs601.project4.TicketPurchaseApplicationLogger;

public class DatabaseManager {
	private static DatabaseManager INSTANCE;
	private static Connection con;
	
	private DatabaseManager() {
		Config config = new Config();
		config.setVariables();
		String username  = config.getDbUsername();
		String password  = config.getDbPassword();
		try {
			// load driver
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}
		catch (Exception e) {
			System.err.println("Can't find driver");
			System.exit(1);
		}
		String hostname = config.getHostname();
		String db = config.getDb();
		String urlString = "jdbc:mysql://"+ hostname + "/" + db;
		//Must set time zone explicitly in newer versions of mySQL.
		String timeZoneSettings = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		//https://stackoverflow.com/questions/21361781/how-to-connect-to-database-connection-in-java
		con = null;
		try {
			con = DriverManager.getConnection(urlString + timeZoneSettings,
					username,
					password);
	    } catch (SQLException e) {
	        System.out.println("Connection Failed! Check output console");
	        TicketPurchaseApplicationLogger.write(Level.WARNING, "Connection failed.", 1);
	        return;
	    }

	    if (con != null) {
	    	TicketPurchaseApplicationLogger.write(Level.INFO, "Connection established.", 0);
	    } else {
	        TicketPurchaseApplicationLogger.write(Level.WARNING, "Failed to make connection.", 1);
	    }
	} 
	
	public static DatabaseManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DatabaseManager();
		}
		return INSTANCE;
	}
	
	public static Connection getConnection() {
		return con;
	}
	
	public static void closeConnection() {
		try {
			con.close();
			TicketPurchaseApplicationLogger.write(Level.INFO, "Successfully close db connection.", 0);
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Failed to close db conection", 1);
		}
	}
	
	/** 
	 * performs SQL insert user to users table 
	 * if SQL query is successful, return User object with userid  
	 * otherwise return null
	 * 
	 * @param user - User object
	 * @return User object or null
	 */
	public User insertUser(User user) {
		try {
			PreparedStatement smtp = con.prepareStatement("INSERT INTO users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			smtp.setString(1, user.getUsername());
			int count = smtp.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Creating user failed. No row affected.", 1);
				return null;
			}
			int userid = 0;
			try (ResultSet generatedKeys = smtp.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					userid = generatedKeys.getInt(1);
					user.setUserid(userid);
				} else {
					TicketPurchaseApplicationLogger.write(Level.WARNING, "Creating user failed. No userid obtained.", 1);
					return null;
				}
			}
			return user;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "insert user SQL error", 1);
			return null;
		}
	}
	
	public User selectUser(User user) {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT username FROM users WHERE userid = ?");
			stmt.setInt(1, user.getUserid());
			// execute a query, which returns a ResultSet object
			ResultSet result = stmt.executeQuery();
			if(!result.next()) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Username not found", 0);
				return null;
			}
			String username = result.getString("username");
			user.setUsername(username);
			return user;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User does not exist", 1);
			return null;
		}
	}
	
	//reference: https://github.com/eugenp/tutorials/blob/master/persistence-modules/core-java-persistence/src/main/java/com/baeldung/jdbc/BatchProcessing.java
	public boolean insertTickets(Ticket ticket, int tickets) {
		try {
			PreparedStatement smtp = con.prepareStatement("INSERT INTO tickets (userid, eventid) VALUES (?, ?)");
			for(int i = 1; i <= tickets; i++) {
				smtp.setInt(1, ticket.getUserid());
				smtp.setInt(2, ticket.getEventid());
				smtp.addBatch();
			}
			smtp.executeBatch();
			con.commit();
			return true;
		} catch (Exception e) {
			try {
				con.rollback();
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Error insert tickets, rollback performed.", 1);
	        } catch (SQLException ex) {
	        	TicketPurchaseApplicationLogger.write(Level.WARNING, "Error insert tickets, cannot rollback", 1);
	        }
			return false;
		}
	}
}