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
	
	private void sendResponse(HttpServletResponse response, String body) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write(body);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot write to client", 1);
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
	
	private void sendBadRequest(HttpServletResponse response, String body) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		sendResponse(response, body);
	}
	
	private void createUser(HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("username");
		if(username == null) {
			sendBadRequest(response, "User unsuccessfully created");
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
	
	private void addTicket(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("addtix");
	}
	
	private void transferTicket(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("transtix");
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (!isPageFound(request, response)) {
			return;
		}
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		// POST /create
		// POST /{userid}/tickets/transfer
		// POST /{userid}/tickets/add
		Connection con = DatabaseManager.getConnection();
		if(pathParts.length == 2 && pathParts[1].equals("create")) {
			createUser(request, response);
		} else if(pathParts.length == 4 && pathParts[2].equals("tickets") && pathParts[3].equals("add")) { 
			addTicket(request, response);
		} else if(pathParts.length == 4 && pathParts[2].equals("tickets") && pathParts[3].equals("transfer")) { 
			transferTicket(request, response);
		} else {
			// page not found
		}
	}
}
