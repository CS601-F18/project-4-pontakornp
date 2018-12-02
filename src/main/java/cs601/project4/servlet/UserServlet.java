package cs601.project4.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.database.DatabaseManager;
import cs601.project4.database.Ticket;


public class UserServlet extends HttpServlet {
	/**
	 * do GET operation according to specified path
	 * GET /{userid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1].matches("\\d+")) {
			int userid = Integer.parseInt(pathInfo.substring(1));
			getUserDetails(request, response, userid);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "User unsuccessfully created");
		}
	}
	
	/**
	 * do POST operation according to specified path
	 * POST /create
	 * POST /{userid}/tickets/add
	 * POST /{userid}/tickets/transfer
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=UTF-8");
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
		if(username == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "User does not own any ticket");
			return;
		}
        JSONObject userObj = new JSONObject();
        userObj.put("userid", userId);
		userObj.put("username", username);
		List<Integer> eventIdList = DatabaseManager.getInstance().selectUserEventId(userId);
		if(eventIdList == null || eventIdList.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "User does not own any ticket");
			return;
		}
		JSONArray arrObj = new JSONArray();
		for(int eventId: eventIdList) {
			// put tickets info to json object
			JSONObject ticketObj = new JSONObject();
			ticketObj.put("eventid", eventId);
			arrObj.put(ticketObj);
		}
		userObj.put("tickets", arrObj);
		BaseServlet.sendResponse(response, userObj.toString());
	}
	
	/**
	 * POST /create
	 * @param request
	 * @param response
	 */
	private void createUser(HttpServletRequest request, HttpServletResponse response) {
		String json = null;
		String username = null;
		try {
			BufferedReader reader = request.getReader();
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
			if(jsonObj.get("username") != null) {
				username = jsonObj.get("username").getAsString();
			}
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get username from request body", 1);
		}
		if(username == null || username.equals("") || !StringUtils.isAlphanumeric(username)) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - username not alphanumeric", 1);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "User unsuccessfully created");
			return;
		}
		int userId = DatabaseManager.getInstance().insertUser(username);
		if(userId == -1) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - user cannot be created", 1);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "User unsuccessfully created");
			return;
		}
		JSONObject userObj = new JSONObject();
		userObj.put("userid", userId);
		String body = userObj.toString();
		BaseServlet.sendResponse(response, body);
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
			BaseServlet.sendResponse(response, "Tickets could not be added");
			return;
		}
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - user does not exist", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "Tickets could not be added");
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
			BaseServlet.sendResponse(response, "Tickets could not be added");
			return;
		}
		BaseServlet.sendResponse(response, "Event tickets added");
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
			BaseServlet.sendResponse(response, "Tickets could not be transfered");
			return;
		}
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - user does not exist", 0);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			BaseServlet.sendResponse(response, "Tickets could not be transfered");
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
		BaseServlet.sendResponse(response, "Event tickets transferred");
	}
}