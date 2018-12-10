package cs601.project4.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cs601.project4.Config;
import cs601.project4.HttpConnectionHelper;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.object.Event;
import cs601.project4.object.EventJsonConstant;
import cs601.project4.object.FrontEndJsonConstant;
import cs601.project4.object.FrontEndServicePathConstant;
import cs601.project4.object.UserJsonConstant;

public class FrontEndServlet extends HttpServlet{
	private Config config = new Config();
	/**
	 * do GET operation according to specified path
	 * GET /events
	 * GET /events/{eventid}
	 * GET /users/{userid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		config.setVariables();
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1].equals("events")) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Call get event list", 0);
//			getEventList(request, response);
		} else if(pathParts.length == 3 && pathParts[1].equals("events") && StringUtils.isNumeric(pathParts[2])) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Call get event details", 0);
			int eventId = Integer.parseInt(pathParts[2]);
//			getEventDetails(request, response, eventId);
		} else if(pathParts.length == 3 && pathParts[1].equals("users") && StringUtils.isNumeric(pathParts[2])) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Call get user details", 0);
			int userId = Integer.parseInt(pathParts[2]);
			//getUserDetails
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	/**
	 * do POST operation according to specified path
	 * POST /events/create
	 * POST /events/{eventid}/purchase/{userid}
	 * POST /user/{userid}/tickets/transfer
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		config.setVariables();
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 3 && pathParts[1].equals("events") && pathParts[2].equals("create")) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Call create event", 0);
//			createEvent(request, response);
		} else if(pathParts.length == 5 && pathParts[1].equals("events") && pathParts[3].equals("purchase")) {
			if(StringUtils.isNumeric(pathParts[2]) && StringUtils.isNumeric(pathParts[4])) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Call purchase tickets", 0);
				int eventId = Integer.parseInt(pathParts[2]);
				int userId = Integer.parseInt(pathParts[4]);
				//purchaseTickets
			} else {
				BaseServlet.sendPageNotFoundResponse(response, "Page not found");
			}
		}else if(pathParts.length == 5 && pathParts[1].equals("user") && pathParts[3].equals("tickets") && pathParts[4].equals("transfer")) {
			if(StringUtils.isNumeric(pathParts[2])) {
				TicketPurchaseApplicationLogger.write(Level.INFO, "Call transfer tickets", 0);
				int userId = Integer.parseInt(pathParts[2]);
				//transferTickets
			} else {
				BaseServlet.sendPageNotFoundResponse(response, "Page not found");
			}
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	private void getEventList(HttpServletRequest request, HttpServletResponse response) {
		try {
			String hostname = config.getHostname();
			int port = config.getEventPort();
			String path = FrontEndServicePathConstant.GET_EVENT_LIST_PATH;
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			if(con.getResponseCode() != 200) {
				BaseServlet.sendBadRequestResponse(response, "No events found");
				return;
			}
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonArray jsonArr = JsonParserHelper.parseJsonStringToJsonArray(responseStr);
			if(jsonArr == null || jsonArr.size() == 0) {
				BaseServlet.sendBadRequestResponse(response, "No events found");
				return;
			}
			BaseServlet.sendResponse(response, jsonArr.toString());
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User service connection error", 1);
		}
	}
	
	private JsonObject getEventFromEventService(int eventId) throws IOException {
		String hostname = config.getHostname();
		int port = config.getEventPort();
		String path = FrontEndServicePathConstant.GET_EVENT_DETAILS_PATH;
		path = String.format(path, eventId);
		String host = hostname + ":" + port;
		HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
		if(con.getResponseCode() != 200) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event not found - event service return error " + con.getResponseCode(), 1);
			return null;
		}
		String responseStr = HttpConnectionHelper.getBodyResponse(con);
		JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
		if(jsonObj == null || jsonObj.size() == 0) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event not found - nothing pass from event service", 1);
			return null;
		}
		return jsonObj;
	}
	
	private void getEventDetails(HttpServletRequest request, HttpServletResponse response, int eventId) {
		try {
			JsonObject jsonObj =  getEventFromEventService(eventId);
			if(jsonObj == null) {
				BaseServlet.sendBadRequestResponse(response, "Event not found");
				return;
			}
			BaseServlet.sendResponse(response, jsonObj.toString());
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User service connection error", 1);
		}
	}
	
	private JsonObject getUserDetailsHelper(JsonObject userObj, int userId) throws IOException {
		JsonArray ticketArr = userObj.get(FrontEndJsonConstant.TICKETS).getAsJsonArray();
		Map<Integer, JsonObject> eventDetails = new HashMap<Integer, JsonObject>();
		JsonArray resArr = new JsonArray();
		for(JsonElement elem: ticketArr) {
			JsonObject obj = elem.getAsJsonObject();
			int eventId = obj.get(FrontEndJsonConstant.EVENT_ID).getAsInt();
			if(!eventDetails.containsKey(eventId)) {
				JsonObject eventObj = getEventFromEventService(eventId);
				resArr.add(eventObj);
				eventDetails.put(eventId, eventObj);
			} else {
				JsonObject eventObj = eventDetails.get(eventId);
				resArr.add(eventObj);
			}
		}
		JsonObject resObj = new JsonObject();
		resObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
		String username = userObj.get(FrontEndJsonConstant.USERNAME).getAsString();
		resObj.addProperty(FrontEndJsonConstant.USERNAME, username);
		resObj.add(FrontEndJsonConstant.TICKETS, resArr);
		return resObj;
	}
	
	private void getUserDetails(HttpServletRequest request, HttpServletResponse response, int userId) {
		try {
			String hostname = config.getHostname();
			int port = config.getUserPort();
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, userId);
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			if(con.getResponseCode() != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User not found - user service return error " + con.getResponseCode(), 1);
				BaseServlet.sendBadRequestResponse(response, "User not found");
				return;
			}
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject userObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			if(userObj == null || userObj.size() == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User not found - nothing pass from user service", 1);
				BaseServlet.sendBadRequestResponse(response, "Event not found");
				return;
			}
			JsonObject resObj = getUserDetailsHelper(userObj, userId);
			BaseServlet.sendResponse(response, resObj.toString());
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User service connection error", 1);
			BaseServlet.sendBadRequestResponse(response, "Event not found");
		}
	}
	
	/**
	 * Helper method of create event to handle validation of json request
	 * @param request
	 * @return Json object
	 */
	private String createEventHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			if(!JsonParserHelper.isJsonString(jsonStr)) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - request body is not json string", 1);
				return null;
			}
			Event event = JsonParserHelper.parseJsonStringToObject(jsonStr, Event.class);
			if(event == null || event.getEventName() == null || !StringUtils.isAlphanumeric(event.getEventName()) || 
					event.getUserId() <= 0 || event.getNumTickets() <= 0 ) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - invalid request body ", 1);
				return null;
			}
			return jsonStr;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - cannot get event details from request body", 1);
			return null;
		}
	}
	
	private JsonObject createEventByEventService(String jsonStr) {
		try {
			String hostname = config.getHostname();
			int port = config.getEventPort();
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, jsonStr);
			if(con.getResponseCode() != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service return error", 1);
				return null;
			}
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			if(jsonObj == null || jsonObj.size() == 0) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - empty json response", 1);
				return null;
			}
			return jsonObj;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service connection error", 1);
		}
		return null;
	}
	private void createEvent(HttpServletRequest request, HttpServletResponse response) {
		String jsonStr = createEventHelper(request);
		if(jsonStr == null) {
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		JsonObject jsonObj = createEventByEventService(jsonStr);
		if(jsonObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		BaseServlet.sendResponse(response, jsonObj.toString());
	}
	
	private JsonObject purchaseTicketsHelper(HttpServletRequest request, int eventId, int userId) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			if(!JsonParserHelper.isJsonString(jsonStr)) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body is not json string", 1);
				return null;
			}
			JsonObject reqObj = JsonParserHelper.parseJsonStringToJsonObject(jsonStr);
			if(reqObj.get(FrontEndJsonConstant.TICKETS) == null) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body invalid", 1);
				return null;
			}
			reqObj.get(FrontEndJsonConstant.TICKETS).getAsInt();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
			return reqObj;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - cannot get number of ticket from request body", 1);
		} catch (NumberFormatException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get event details to update from request body", 1);
		}
		return null;
	}
	
	private boolean purchaseTicketByEventService(JsonObject jsonObj, int eventId) {
		try {
			String hostname = config.getHostname();
			int port = config.getEventPort();
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			path = String.format(path, eventId);
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, jsonObj);
			if(con.getResponseCode() != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service return error", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service connection error", 1);
		}
		return false;
	}
	
	private void purchaseTickets(HttpServletRequest request, HttpServletResponse response, int eventId, int userId) {
		JsonObject jsonObj = purchaseTicketsHelper(request, eventId, userId);
		if(jsonObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		boolean areTicketsPurchased = purchaseTicketByEventService(jsonObj, eventId);
		if(!areTicketsPurchased) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		BaseServlet.sendResponse(response, "Tickets purchased");
	}
	
	/**
	 * Helper method for transferTicket - if the request body is valid and return JsonObject
	 * @param request
	 * @param response
	 * @return JsonObject or null
	 */
	private JsonObject transferTicketsHelper(HttpServletRequest request) {
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
	
	private boolean transferTicketsByUserService(JsonObject jsonObj, int userId) {
		try {
			String hostname = config.getHostname();
			int port = config.getUserPort();
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, userId);
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, jsonObj);
			if(con.getResponseCode() != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service return error", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event service connection error", 1);
		}
		return false;
	}
	
	private void transferTickets(HttpServletRequest request, HttpServletResponse response, int userId) {
		JsonObject jsonObj = transferTicketsHelper(request);
		if(jsonObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transferred");
			return;
		}
		boolean areTicketsTransferred = transferTicketsByUserService(jsonObj, userId);
		if(!areTicketsTransferred) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be transferred");
			return;
		}
		BaseServlet.sendResponse(response, "Event tickets transferred");
	}
}
