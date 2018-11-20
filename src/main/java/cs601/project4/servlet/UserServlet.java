package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import cs601.project4.DatabaseManager;
import cs601.project4.TicketPurchaseApplicationLogger;


public class UserServlet extends HttpServlet {
	/**
	 * do GET operation according to specified path
	 * GET /{userid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!isPageFound(request, response)) {
			return;
		}
		String pathInfo = request.getPathInfo();
		String username = pathInfo.substring(1);
		DatabaseManager dbManager = DatabaseManager.getInstance();
		Connection con = dbManager.getConnection();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        int useridres = 0;
		String usernameres = "";
		int eventidres = 0;
		JSONObject userObj = new JSONObject();
		JSONArray arrObj = new JSONArray();
		JSONObject ticketObj = new JSONObject();
		try {
//			PreparedStatement stmt = con.prepareStatement("SELECT users.userid, users.username, tickets.eventid FROM users INNER JOIN tickets ON users.userid = tickets.userid WHERE users.username = ?");
			PreparedStatement stmt = con.prepareStatement("SELECT userid, username FROM users WHERE username = ?");
			stmt.setString(1, username);
//			execute a query, which returns a ResultSet object
			ResultSet result = stmt.executeQuery();
			if(!result.next()) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Username not found", 0);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				String body = "User not found";
				sendResponse(response, body);
				return;
			} else {
				// put userid and username to json object
				useridres = result.getInt("userid");
				usernameres = result.getString("username");
				userObj.put("userid", useridres);
				userObj.put("username", usernameres);
			}
			if(!userObj.has("userid")) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "User has no tickets", 0);
				String body = userObj.toString();
				sendResponse(response, body);
				return;
			}
			stmt = con.prepareStatement("SELECT eventid FROM tickets WHERE userid = ?");
			stmt.setInt(1, useridres);
			result = stmt.executeQuery();
			while (result.next()) {
				eventidres = result.getInt("eventid");
				System.out.println(useridres);
				System.out.println(usernameres);
				System.out.println(eventidres);
				// put tickets info to json object
				ticketObj.put("eventid", eventidres);
				arrObj.put(ticketObj);
			}
			userObj.put("tickets", arrObj);
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error", 1);
		}
		String body = userObj.toString();
		sendResponse(response, body);
	}
	
	/**
	 * do POST operation according to specified path
	 * POST /create
	 * POST /{userid}/tickets/add
	 * POST /{userid}/tickets/transfer
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (!isPageFound(request, response)) {
			return;
		}
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		
		Connection con = DatabaseManager.getConnection();
		if(pathParts.length == 2 && pathParts[1].equals("create")) {
			createUser(request, response);
		} else if(pathParts.length == 4 && pathParts[2].equals("tickets") && pathParts[3].equals("add")) { 
			int userid = Integer.parseInt(pathParts[1]);
			addTicket(request, response, userid);
		} else if(pathParts.length == 4 && pathParts[2].equals("tickets") && pathParts[3].equals("transfer")) { 
			int userid = Integer.parseInt(pathParts[1]);
			transferTicket(request, response, userid);
		} else {
			// page not found
		}
	}
	
	private boolean isPageFound(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null || pathInfo.length() <= 1) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			String body = "Page not found";
			sendResponse(response, body);
			return false;
		}
		return true;
	}
	
	private void createUser(HttpServletRequest request, HttpServletResponse response) {
		
		String username = request.getParameter("username");
		if(username == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "User unsuccessfully created");
			return;
		}
		Connection con = DatabaseManager.getConnection();
		try {
			PreparedStatement smtp = con.prepareStatement("INSERT INTO users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			smtp.setString(1, username);
			int count = smtp.executeUpdate();
			if(count == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				sendResponse(response, "User unsuccessfully created");
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Creating user failed. No row affected.", 1);
				return;
			}
			int userid = 0;
			try (ResultSet generatedKeys = smtp.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					userid = generatedKeys.getInt(1);
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					sendResponse(response, "User unsuccessfully created");
					TicketPurchaseApplicationLogger.write(Level.WARNING, "Creating user failed. No userid obtained.", 1);
					return;
				}
			}
			JSONObject userObj = new JSONObject();
			userObj.put("userid", userid);
			String body = "User created\n";
			body += userObj.toString();
			sendResponse(response, body);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "User unsuccessfully created");
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error", 1);
		}
	}

	private boolean isUserExist(int userid) throws SQLException{
		Connection con = DatabaseManager.getConnection();
		// check if user who will transfer the ticket exists
		PreparedStatement smtp = con.prepareStatement("SELECT userid FROM users WHERE userid = ?");
		smtp.setInt(1, userid);
		ResultSet result = smtp.executeQuery();
		if(!result.next()) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User does not exist", 1);
			return false;
		}
		return true;
	}
	
	private boolean isTicketAdded(int userid, int eventid, int tickets) throws SQLException {
		Connection con = DatabaseManager.getConnection();
		PreparedStatement smtp = null;
		for(int i = 1; i <= tickets; i++) {
			smtp = con.prepareStatement("INSERT INTO tickets (userid, eventid) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			smtp.setInt(1, userid);
			smtp.setInt(2, eventid);
			int count = smtp.executeUpdate();
			if(count == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Adding ticket failed. No row affected.", 1);
				return false;
			} else {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Ticket added. Event id: "+ eventid + ", User id: " + userid, 0);
			}
		}
		return true;
	}

	private void addTicket(HttpServletRequest request, HttpServletResponse response, int userid) {
		// check if input is valid
		if(request.getParameter("eventid") == null || request.getParameter("tickets") == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be added");
			return;
		}
		try {
			// check if userid exists and is ticket added
			int eventid = Integer.parseInt(request.getParameter("eventid"));
			int tickets = Integer.parseInt(request.getParameter("tickets"));
			if(!isUserExist(userid)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				sendResponse(response, "Tickets could not be added");
				return;
			} else {
				if(!isTicketAdded(userid, eventid, tickets)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					sendResponse(response, "Tickets could not be added");
					return;
				}
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be added");
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Failed to execute SQL query.", 1);
		}
		sendResponse(response, "Event tickets added");
	}
	
	private boolean doesUserHasEnoughTickets(int userid, int eventid, int tickets) throws SQLException{
		Connection con = DatabaseManager.getConnection();
		// check if user has ticket
		PreparedStatement smtp = con.prepareStatement("SELECT COUNT(ticketid) tickets FROM tickets WHERE userid = ? and eventid = ?");
		smtp.setInt(1, userid);
		smtp.setInt(2, eventid);
		ResultSet result = smtp.executeQuery();
		int ticketCount = 0;
		if(!result.next()) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Ticket does not exists", 1);
			return false;
		} else {
			ticketCount = result.getInt("tickets");
		}
		// check if ticket to be transferred is not 0 and the user has enough tickets to transfer
		if(tickets == 0 || ticketCount < tickets) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Number of tickets not valid", 1);
			return false;
		}
		return true;
	}
	
	private boolean isTicketUpdated(int userid, int targetuser, int eventid, int tickets) throws SQLException{
		Connection con = DatabaseManager.getConnection();
		// update userid to be the userid that gets tickets transferred
		PreparedStatement stmt = con.prepareStatement("UPDATE tickets SET userid = ? WHERE userid = ? and eventid = ? LIMIT ?");
		stmt.setInt(1, targetuser);
		stmt.setInt(2, userid);
		stmt.setInt(3, eventid);
		stmt.setInt(4, tickets);
		int count = stmt.executeUpdate();
		if(count == 0) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Userid not updated", 1);
			return false;
		}
		return true;
	}
	
	private void transferTicket(HttpServletRequest request, HttpServletResponse response, int userid) {
		// check if input is valid
		if(request.getParameter("eventid") == null || request.getParameter("tickets") == null || request.getParameter("targetuser") == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be transfered");
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Invalid body request", 1);
			return;
		}
		try {
			// check if users transferring the ticket and receiving the ticket exist
			int targetuser = Integer.parseInt(request.getParameter("targetuser"));
			int eventid = Integer.parseInt(request.getParameter("eventid"));
			int tickets = Integer.parseInt(request.getParameter("tickets"));
			if(!isUserExist(userid) || !isUserExist(targetuser) || !doesUserHasEnoughTickets(userid, eventid, tickets) || !isTicketUpdated(userid, targetuser, eventid, tickets)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				sendResponse(response, "Tickets could not be transfered");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be transfered");
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Invalid SQL", 1);
			return;
		}
		sendResponse(response, "Event tickets transferred");
	}
	
	private void sendResponse(HttpServletResponse response, String body) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write(body);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot write to client", 1);
		}
	}
}