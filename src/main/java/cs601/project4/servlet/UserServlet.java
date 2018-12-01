package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.database.DatabaseManager;
import cs601.project4.database.Ticket;


public class UserServlet extends HttpServlet {
	/**
	 * do GET operation according to specified path
	 * GET /{userid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1].matches("\\d+")) {
			int userid = Integer.parseInt(pathInfo.substring(1));
			getUserDetails(request, response, userid);
		}
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
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
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
	
	/**
	 * GET /{userid}
	 * @param request
	 * @param response
	 * @param userid
	 */
	private void getUserDetails(HttpServletRequest request, HttpServletResponse response, int userId) {
		String username = DatabaseManager.getInstance().selectUser(userId);
        JSONObject userObj = new JSONObject();
        userObj.put("userid", userId);
		userObj.put("username", username);
		List<Integer> eventIdList = DatabaseManager.getInstance().selectUserEventId(userId);
		JSONArray arrObj = new JSONArray();
		for(int eventId: eventIdList) {
			// put tickets info to json object
			JSONObject ticketObj = new JSONObject();
			ticketObj.put("eventid", eventId);
			arrObj.put(ticketObj);
		}
		userObj.put("tickets", arrObj);
		if(userObj.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "User does not own any ticket");
			return;
		}
		sendResponse(response, userObj.toString());
	}
	
	/**
	 * POST /create
	 * @param request
	 * @param response
	 */
	private void createUser(HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("username");
		if(username == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "User unsuccessfully created");
			return;
		}
		int userId = DatabaseManager.getInstance().insertUser(username);
		if(userId == -1) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "User unsuccessfully created");
			return;
		}
		JSONObject userObj = new JSONObject();
		userObj.put("userid", userId);
		String body = userObj.toString();
		sendResponse(response, body);
	}
	
	/**
	 * POST /{userid}/tickets/add
	 * @param request
	 * @param response
	 * @param userid
	 */
	private void addTicket(HttpServletRequest request, HttpServletResponse response, int userId) {
		// check if input is valid
		String eventIdStr = request.getParameter("eventid");
		String numTicketsStr = request.getParameter("tickets");
		if(eventIdStr == null || numTicketsStr == null || !eventIdStr.matches("\\d+") || !numTicketsStr.matches("\\d+")) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be added - request input invalid", 1);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be added");
			return;
		}
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - user does not exist", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be added");
			return;
		}
		int eventId = Integer.parseInt(eventIdStr);
		int numTickets = Integer.parseInt(numTicketsStr);
		Ticket ticket = new Ticket();
		ticket.setEventId(eventId);
		ticket.setUserId(userId);
		boolean areTicketAdded = DatabaseManager.getInstance().insertTickets(ticket, numTickets);
		if(!areTicketAdded) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - fail to add", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be added");
			return;
		}
		sendResponse(response, "Event tickets added");
	}
	
	/**
	 * POST /{userid}/tickets/transfer
	 * @param request
	 * @param response
	 * @param userid
	 */
	private void transferTicket(HttpServletRequest request, HttpServletResponse response, int userId) {
		String eventIdStr = request.getParameter("eventid");
		String numTicketsStr = request.getParameter("tickets");
		String targetUserIdStr = request.getParameter("targetUserId");
		
		// check if input is valid
		if(eventIdStr == null || numTicketsStr == null || targetUserIdStr == null || !eventIdStr.matches("\\d+") || !numTicketsStr.matches("\\d+") || !targetUserIdStr.matches("\\d+")) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be added - request input invalid", 1);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be transfered");
			return;
		}
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - user does not exist", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			sendResponse(response, "Tickets could not be transfered");
			return;
		}
		
		int targetUserId = Integer.parseInt(request.getParameter("targetuser"));
		int eventId = Integer.parseInt(request.getParameter("eventid"));
		int numTickets = Integer.parseInt(request.getParameter("tickets"));
		boolean areTicketsTransferred = DatabaseManager.getInstance().updateTickets(userId, targetUserId, eventId, numTickets);
		if(!areTicketsTransferred) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - fail to transfer", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		sendResponse(response, "Event tickets transferred");
	}
	
	/**
	 * GET and POST helper method
	 * @param request
	 * @param response
	 * @return
	 */
	private boolean isPageFound(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null || pathInfo.length() <= 1) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			sendResponse(response, "Page not found");
			return false;
		}
		return true;
	}
	
	/**
	 * getUserDetails helper method
	 * @param userid
	 * @param userObj
	 * @throws SQLException
	 */
	private void selectUser(int userid, JSONObject userObj) throws SQLException {
		Connection con = DatabaseManager.getConnection();
		PreparedStatement stmt = con.prepareStatement("SELECT userid, username FROM users WHERE userid = ?");
		stmt.setInt(1, userid);
		// execute a query, which returns a ResultSet object
		ResultSet result = stmt.executeQuery();
		if(!result.next()) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Username not found", 0);
			return;
		}
		// put userid and username to json object
		int useridres = result.getInt("userid");
		String usernameres = result.getString("username");
		userObj.put("userid", useridres);
		userObj.put("username", usernameres);
		stmt = con.prepareStatement("SELECT eventid FROM tickets WHERE userid = ?");
		stmt.setInt(1, useridres);
		result = stmt.executeQuery();
		JSONArray arrObj = new JSONArray();
		while (result.next()) {
			int eventidres = result.getInt("eventid");
			// put tickets info to json object
			JSONObject ticketObj = new JSONObject();
			ticketObj.put("eventid", eventidres);
			arrObj.put(ticketObj);
		}
		userObj.put("tickets", arrObj);
		
		
	}
	
	/**
	 * POST helper method
	 * @param userid
	 * @return
	 * @throws SQLException
	 */
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
	
	/**
	 * addTicket helper method
	 * @param userid
	 * @param eventid
	 * @param tickets
	 * @return
	 * @throws SQLException
	 */
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

	/**
	 * transferTicket helper method
	 * @param userid
	 * @param eventid
	 * @param tickets
	 * @return
	 * @throws SQLException
	 */
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
	
	/**
	 * transferTicket helper method
	 * @param userid
	 * @param targetuser
	 * @param eventid
	 * @param tickets
	 * @return
	 * @throws SQLException
	 */
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
	
	/**
	 * send response
	 * @param response
	 * @param body
	 */
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