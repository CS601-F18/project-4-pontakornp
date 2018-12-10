package cs601.project4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import cs601.project4.object.Event;
import cs601.project4.object.Ticket;

/**
 * 
 * @author pontakornp
 *
 *
 * Database manager class handles the database connection and all sql queries of users, events, and tickets tables;
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
		String dbHostname = config.getDbHostname();
		String db = config.getDb();
		String urlString = "jdbc:mysql://"+ dbHostname + "/" + db;
		//Must set time zone explicitly in newer versions of mySQL.
		String timeZoneSettings = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		//https://stackoverflow.com/questions/21361781/how-to-connect-to-database-connection-in-java
		try {
			con = DriverManager.getConnection(urlString + timeZoneSettings,
					username,
					password);
			TicketManagementApplicationLogger.write(Level.INFO, "Connection established", 0);
	    } catch (SQLException e) {
	        System.out.println("Connection Failed! Check output console");
	        TicketManagementApplicationLogger.write(Level.WARNING, "Connection failed", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "User failed to be created", 1);
				return -1;
			}
			ResultSet result = stmt.getGeneratedKeys();
			if(result.next()) {
				TicketManagementApplicationLogger.write(Level.INFO, "Username: " + username + " has successfully created", 0);
				int userId = result.getInt(1);
				return userId;
			} else {
				TicketManagementApplicationLogger.write(Level.WARNING, "User failed to be created", 1);
				return -1;
			}
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error insert user", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "User not deleted", 1);
				return false;
			}
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error delete user", 1);
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
				TicketManagementApplicationLogger.write(Level.INFO, "Username not found", 0);
				return null;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "User id: " + userId +" exists", 0);
			String username = result.getString("username");
			return username;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error select user", 1);
			return null;
		}
	}
	
	/**
	 * select list of event ids of a user from tickets table
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
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error select list of event id", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Tickets has failed to insert", 1);
				//revert back
				if(count.length > 0) {
					if(deleteTickets(ticket, count.length)) {
						TicketManagementApplicationLogger.write(Level.INFO, "Tickets inserted incompletely has been reverted back", 0);
					} else {
						TicketManagementApplicationLogger.write(Level.WARNING, "Tickets inserted incompletely could not be reverted back", 1);
					}
				}
				return false;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Tickets has been inserted successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error insert tickets", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Tickets not deleted", 1);
				return false;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Tickets has been deleted successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error delete tickets", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Ticket does not exists", 1);
				return -1;
			} else {
				ticketCount = result.getInt("tickets");
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Number of Tickets: " + ticketCount, 0);
			return ticketCount;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error count tickets", 1);
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Tickets not updated", 1);
				return false;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Tickets has been updated successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error update tickets", 1);
			return false;
		}
	}
	
	/**
	 * insert event details to events table
	 * 
	 * @param event
	 * @return
	 */
	public int insertEvent(Event event) {
		try {
			PreparedStatement stmt = con.prepareStatement("INSERT INTO events (event_name, user_id, num_ticket_avail, num_ticket_purchased) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, event.getEventName());
			stmt.setInt(2, event.getUserId());
			stmt.setInt(3, event.getNumTicketAvail());
			stmt.setInt(4,  event.getNumTicketPurchased());
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event not inserted", 1);
				return -1;
			}
			ResultSet result = stmt.getGeneratedKeys();
			if(result.next()) {
				TicketManagementApplicationLogger.write(Level.INFO, "Event has been inserted successfully", 0);
				int eventId = result.getInt(1);
				return eventId;
			} else {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event failed to be inserted", 1);
				return -1;
			}
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error insert event", 1);
			return -1;
		}
	}
	
	/**
	 * delete event by event id from events table
	 * 
	 * @param userId
	 * @return
	 */
	public boolean deleteEvent(int eventId) {
		try {
			PreparedStatement stmt = con.prepareStatement("DELETE FROM events WHERE event_id = ?");
			stmt.setInt(1, eventId);
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event not deleted", 1);
				return false;
			}
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error delete event", 1);
			return false;
		}
	}
	
	/**
	 * select event list from events table
	 * 
	 * @return list of event object
	 */
	public List<Event> selectEvents() {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM events");
			ResultSet result = stmt.executeQuery();
			List<Event> list = new ArrayList<Event>();
			while (result.next()) {
				Event event = new Event();
				event.setEventId(result.getInt("event_id"));
				event.setEventName(result.getString("event_name"));
				event.setUserId(result.getInt("user_id"));
				event.setNumTicketAvail(result.getInt("num_ticket_avail"));
				event.setNumTicketPurchased(result.getInt("num_ticket_purchased"));
				list.add(event);
			}
			if(list.isEmpty()) {
				TicketManagementApplicationLogger.write(Level.INFO, "Events not found", 0);
				return null;
			}
			return list;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error select all event", 1);
			return null;
		}
	} 
	
	/**
	 * select event details from events table
	 * 
	 * @param eventId
	 * @return event object
	 */
	public Event selectEvent(int eventId) {
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM events WHERE event_id = ?");
			stmt.setInt(1, eventId);
			ResultSet result = stmt.executeQuery();
			if(!result.next()) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event not found", 1);
				return null;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Event id: " + eventId +" exists", 0);
			Event event = new Event();
			event.setEventId(eventId);
			event.setEventName(result.getString("event_name"));
			event.setUserId(result.getInt("user_id"));
			event.setNumTicketAvail(result.getInt("num_ticket_avail"));
			event.setNumTicketPurchased(result.getInt("num_ticket_purchased"));
			return event;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error select event", 1);
			return null;
		}
	}
	
	/**
	 * update number of ticket available and number of tickets purchased of an event in events table
	 * 
	 * @param event
	 * @return true or false
	 */
	public boolean updateEvent(Event event) {
		try {
			PreparedStatement stmt = con.prepareStatement("UPDATE events SET num_ticket_avail = ?, num_ticket_purchased = ? WHERE event_id = ?");
			stmt.setInt(1, event.getNumTicketAvail());
			stmt.setInt(2, event.getNumTicketPurchased());
			stmt.setInt(3, event.getEventId());
			int count = stmt.executeUpdate();
			if(count == 0) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event not updated", 1);
				return false;
			}
			TicketManagementApplicationLogger.write(Level.INFO, "Event has been updated successfully", 0);
			return true;
		} catch (SQLException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "SQL error update event", 1);
			return false;
		}
	}
}