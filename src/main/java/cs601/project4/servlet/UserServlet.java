package cs601.project4.servlet;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cs601.project4.DatabaseManager;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.object.Ticket;
import cs601.project4.object.User;
import cs601.project4.object.UserJsonConstant;


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
		if(pathParts.length == 2 && StringUtils.isNumeric(pathParts[1])) {
			int userId = Integer.parseInt(pathInfo.substring(1));
			getUserDetails(request, response, userId);
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
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
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1].equals("create")) {
			createUser(request, response);
		} else if(pathParts.length == 4 && StringUtils.isNumeric(pathParts[1]) && pathParts[2].equals("tickets")) { 
			if(pathParts[3].equals("add")) {
				int userId = Integer.parseInt(pathParts[1]);
				addTicket(request, response, userId);
			} else if(pathParts[3].equals("transfer")) {
				int userId = Integer.parseInt(pathParts[1]);
				transferTicket(request, response, userId);
			} else {
				BaseServlet.sendPageNotFoundResponse(response, "Page not found");
			}
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
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
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User not found", 1);
			BaseServlet.sendBadRequestResponse(response, "User not found");
			return;
		}
		JsonObject userObj = new JsonObject();
		userObj.addProperty(UserJsonConstant.USER_ID, userId);
		userObj.addProperty(UserJsonConstant.USERNAME, username);
		List<Integer> eventIdList = DatabaseManager.getInstance().selectUserEventId(userId);
		if(eventIdList == null || eventIdList.isEmpty()) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "User does not own any ticket", 0);
			return;
		}
		JsonArray arrObj = new JsonArray();
		for(int eventId: eventIdList) {
			// put tickets info to json object
			JsonObject ticketObj = new JsonObject();
			ticketObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			arrObj.add(ticketObj);
		}
		userObj.add(UserJsonConstant.TICKETS, arrObj);
		BaseServlet.sendResponse(response, userObj.toString());
	}
	
	/**
	 * Helper method for createUser - if the request body is valid and return User object
	 * @param request
	 * @return User object or null
	 */
	private User createUserHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			if(!JsonParserHelper.isJsonString(jsonStr)) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - request body is not json string", 1);
				return null;
			}
			User user = JsonParserHelper.parseJsonStringToObject(jsonStr, User.class);
			if(user == null || user.getUsername() == null || !StringUtils.isAlphanumeric(user.getUsername())) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - username is null or non-alphanumeric", 1);
				return null;
			}
			return user;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get username from request body", 1);
			return null;
		}
	}
	
	/**
	 * POST /create
	 * @param request
	 * @param response
	 */
	private void createUser(HttpServletRequest request, HttpServletResponse response) {
		User user = createUserHelper(request);
		if(user == null) {
			BaseServlet.sendBadRequestResponse(response, "User unsuccessfully created");
			return;
		}
		String username = user.getUsername();
		int userId = DatabaseManager.getInstance().insertUser(username);
		if(userId == -1) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - user cannot be created", 1);
			BaseServlet.sendBadRequestResponse(response, "User unsuccessfully created");
			return;
		}
		JsonObject userObj = new JsonObject();
		userObj.addProperty(UserJsonConstant.USER_ID, userId);
		String body = userObj.toString();
		BaseServlet.sendResponse(response, body);
	}
	
	/**
	 * Helper method for add ticket - if the request body is valid and return Json Object
	 * @param request
	 * @param response
	 * @return JsonObject or null
	 */
	private JsonObject addTicketHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			JsonObject reqObj = JsonParserHelper.parseJsonStringToJsonObject(jsonStr);
			if(reqObj == null) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be added - request body is not json string", 1);
				return null;
			}
			if(reqObj.get(UserJsonConstant.EVENT_ID) == null || reqObj.get(UserJsonConstant.TICKETS) == null) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be added - request body invalid", 1);
				return null;
			}
			reqObj.get(UserJsonConstant.EVENT_ID).getAsInt();
			reqObj.get(UserJsonConstant.TICKETS).getAsInt();
			return reqObj;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get add ticket details from request body", 1);
		} catch (NumberFormatException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get add ticket details from request body", 1);
		}
		return null;
	}
	
	/**
	 * POST /{userid}/tickets/add
	 * @param request
	 * @param response
	 * @param userid
	 */
	private void addTicket(HttpServletRequest request, HttpServletResponse response, int userId) {
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - user does not exist", 0);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be added");
			return;
		}
		JsonObject reqObj = addTicketHelper(request);
		if(reqObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be added");
			return;
		}
		int eventId = reqObj.get(UserJsonConstant.EVENT_ID).getAsInt();
		int numTickets = reqObj.get(UserJsonConstant.TICKETS).getAsInt();
		if(numTickets <= 0) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be added - invalid number of tickets", 0);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be added");
			return;
		}
		Ticket ticket = new Ticket();
		ticket.setEventId(eventId);
		ticket.setUserId(userId);
		boolean areTicketAdded = DatabaseManager.getInstance().insertTickets(ticket, numTickets);
		if(!areTicketAdded) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be added - fail to add", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be added");
			return;
		}
		BaseServlet.sendResponse(response, "Event tickets added");
	}
	
	/**
	 * Helper method for transferTicket - if the request body is valid and return JsonObject
	 * @param request
	 * @param response
	 * @return JsonObject or null
	 */
	private JsonObject transferTicketHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			JsonObject reqObj = JsonParserHelper.parseJsonStringToJsonObject(jsonStr);
			if(reqObj == null) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be transferred - request body is not json string", 1);
				return null;
			}
			if(reqObj.get(UserJsonConstant.EVENT_ID) == null || 
					reqObj.get(UserJsonConstant.TICKETS) == null || 
					reqObj.get(UserJsonConstant.TARGET_USER) == null) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be transferred - request body invalid", 1);
				return null;
			}
			reqObj.get(UserJsonConstant.EVENT_ID).getAsInt();
			reqObj.get(UserJsonConstant.TICKETS).getAsInt();
			reqObj.get(UserJsonConstant.TARGET_USER).getAsInt();
			return reqObj;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get transfer ticket details from request body", 1);
		} catch (NumberFormatException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get transfer ticket details from request body", 1);
		}
		return null;
	}
	
	private boolean areTicketsEnough(int userId, int eventId, int numTickets) {
		int userTickets = DatabaseManager.getInstance().countTickets(userId, eventId);
		if(numTickets <= 0 || userTickets < numTickets) {
			return false;
		}
		return true;
	}
	
	/**
	 * POST /{userid}/tickets/transfer
	 * @param request
	 * @param response
	 * @param userid
	 */
	private synchronized void transferTicket(HttpServletRequest request, HttpServletResponse response, int userId) {
		String username = DatabaseManager.getInstance().selectUser(userId);
		if(username == null) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be transferred - user does not exist", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transfered");
			return;
		}
		JsonObject reqObj = transferTicketHelper(request);
		if(reqObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transfered");
			return;
		}
		int targetUserId = reqObj.get(UserJsonConstant.TARGET_USER).getAsInt();
		String targetUsername = DatabaseManager.getInstance().selectUser(targetUserId);
		if(targetUsername == null) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be transferred - target user does not exist", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transfered");
			return;
		}
		int eventId = reqObj.get(UserJsonConstant.EVENT_ID).getAsInt();
		int numTickets = reqObj.get(UserJsonConstant.TICKETS).getAsInt();
		if(!areTicketsEnough(userId, eventId, numTickets)) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be transfered - invalid number of tickets", 0);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transfered");
			return;
		}
		boolean areTicketsTransferred = DatabaseManager.getInstance().updateTickets(userId, targetUserId, eventId, numTickets);
		if(!areTicketsTransferred) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be transferred - fail to transfer", 0);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transfered");
			return;
		}
		BaseServlet.sendResponse(response, "Event tickets transferred");
	}
}