package cs601.project4.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import cs601.project4.Config;
import cs601.project4.TicketPurchaseApplicationLogger;

/**
 * database manager class handles the database connection and all sql queries of users, events, and tickets tables;
 * @author pontakornp
 *
 */
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
		try {
			con = DriverManager.getConnection(urlString + timeZoneSettings,
					username,
					password);
			TicketPurchaseApplicationLogger.write(Level.INFO, "Connection established", 0);
	    } catch (SQLException e) {
	        System.out.println("Connection Failed! Check output console");
	        TicketPurchaseApplicationLogger.write(Level.WARNING, "Connection failed", 1);
	        return;
	    }
	} 
	/**
	 * get instance method of database manager class using singleton design
	 * 
	 * @return
	 */
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
	public int insertUser(String username) {
		try {
			PreparedStatement stmt = con.prepareStatement("INSERT INTO users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, username);
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User failed to be created", 1);
				return -1;
			}
			int userId = -1;
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					userId = generatedKeys.getInt(1);
				} else {
					TicketPurchaseApplicationLogger.write(Level.WARNING, "User failed to be created", 1);
					return -1;
				}
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "Username: " + username + " has successfully created", 0);
			return userId;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error insert user", 1);
			return -1;
		}
	}
	
	/**
	 * delete user by user id in users table
	 * 
	 * @param userId
	 * @return
	 */
	public boolean deleteUser(int userId) {
		try {
			PreparedStatement stmt = con.prepareStatement("DELETE FROM users WHERE user_id = ?");
			stmt.setInt(1, userId);
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User not deleted", 1);
				return false;
			}
			return true;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error delete user", 1);
			return false;
		}
	}
	
	/**
	 * select username of a user from users table
	 * 
	 * @param userId
	 * @return
	 */
	public String selectUser(int userId) {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT username FROM users WHERE user_id = ?");
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();
			if(!result.next()) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Username not found", 0);
				return null;
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "User id: " + userId +" exists", 0);
			String username = result.getString("username");
			return username;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error select user", 1);
			return null;
		}
	}
	
	/**
	 * select event id of a user from tickets table
	 * 
	 * @param userId
	 * @return
	 */
	public List<Integer> selectUserEventId(int userId) {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT event_id FROM tickets WHERE user_id = ?");
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();
			List<Integer> list = new ArrayList<Integer>();
			while (result.next()) {
				int eventId = result.getInt("event_id");
				list.add(eventId);
			}
			return list;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "", 1);
			return null;
		}
	}
	
	/**
	 * insert event tickets for a user in tickets table
	 * @param ticket
	 * @param numTickets
	 * @return
	 */
	//reference: https://github.codbm/eugenp/tutorials/blob/master/persistence-modules/core-java-persistence/src/main/java/com/baeldung/jdbc/BatchProcessing.java
	public boolean insertTickets(Ticket ticket, int numTickets) {
		try {
			PreparedStatement stmt = con.prepareStatement("INSERT INTO tickets (user_id, event_id) VALUES (?, ?)");
			for(int i = 1; i <= numTickets; i++) {
				stmt.setInt(1, ticket.getUserId());
				stmt.setInt(2, ticket.getEventId());
				stmt.addBatch();
			}
			int count[] = stmt.executeBatch();
			if(count.length != numTickets) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets has failed to insert", 1);
				//revert back
				if(count.length > 0) {
					if(deleteTickets(ticket, count.length)) {
						TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets inserted incompletely has been reverted back", 0);
					} else {
						TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets inserted incompletely could not be reverted back", 1);
					}
				}
				return false;
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets has been inserted successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error insert tickets", 1);
			return false;
		}
	}
	
	/**
	 * delete event tickets of a user from tickets table
	 * 
	 * @param ticket
	 * @param numTickets
	 * @return
	 */
	public boolean deleteTickets(Ticket ticket, int numTickets) {
		try {
			PreparedStatement stmt = con.prepareStatement("DELETE FROM tickets WHERE event_id = ? AND user_id = ? ORDER BY ticket_id DESC LIMIT ?");
			stmt.setInt(1, ticket.getEventId());
			stmt.setInt(2, ticket.getUserId());
			stmt.setInt(3, numTickets);
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets not deleted", 1);
				return false;
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets has been deleted successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error delete tickets", 1);
			return false;
		}
	}
	
	/**
	 * count number of tickets of an event that user has from tickets table
	 * 
	 * @param user
	 * @param eventId
	 * @param tickets
	 * @return
	 */
	public int countTickets(int userId, int eventId) {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT COUNT(ticket_id) tickets FROM tickets WHERE user_id = ? and event_id = ?");
			stmt.setInt(1, userId);
			stmt.setInt(2, eventId);
			ResultSet result = stmt.executeQuery();
			int ticketCount = 0;
			if(!result.next()) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Ticket does not exists", 1);
				return -1;
			} else {
				ticketCount = result.getInt("tickets");
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "Number of Tickets: " + ticketCount, 0);
			return ticketCount;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Error count tickets, please try again", 1);
			return -1;
		}
	}
	
	/**
	 * swaps one user with another in tickets table
	 * 
	 * @param userId
	 * @param targetUserId
	 * @param eventId
	 * @param numTickets
	 * @return
	 */
	public boolean updateTickets(int userId, int targetUserId, int eventId, int numTickets) {
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE tickets SET user_id = ? WHERE user_id = ? and event_id = ? LIMIT ?");
			stmt.setInt(1, targetUserId);
			stmt.setInt(2, userId);
			stmt.setInt(3, eventId);
			stmt.setInt(4, numTickets);
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets not updated", 1);
				return false;
			}
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets has been updated successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Error update tickets, please try again", 1);
			return false;
		}
	}

}