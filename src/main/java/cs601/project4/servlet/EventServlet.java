package cs601.project4.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cs601.project4.Config;
import cs601.project4.DatabaseManager;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.object.Event;
import cs601.project4.object.UserServicePathConstant;

public class EventServlet extends HttpServlet{
	/**
	 * do GET operation according to specified path
	 * GET /list
	 * GET /{eventid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1] == "list") {
			//createEvent
		} else if(pathParts.length == 2 && StringUtils.isNumeric(pathParts[1])) {
			int eventId = Integer.parseInt(pathInfo.substring(1));
			//getEventDetails
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	/**
	 * do POST operation according to specified path
	 * POST /create
	 * POST /purchase/{eventid}
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		
		if(pathParts.length == 2 && pathParts[1] == "create") {
			
		} else if(pathParts.length == 3 && pathParts[1] == "purchase" && StringUtils.isNumeric(pathParts[2])) {
			int eventId = Integer.parseInt(pathParts[2]);
			purchaseTickets(request, response, eventId);
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	/**
	 * GET /list
	 * GET method to give the list of events available
	 * @param request
	 * @param response
	 */
	private void getEventList(HttpServletRequest request, HttpServletResponse response) {
		List<Event> events = DatabaseManager.getInstance().selectEvents();
		JsonArray arrObj = new JsonArray();
		if(events != null) {
			for(Event event: events) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("eventid", event.getEventId());
				jsonObj.addProperty("eventname", event.getEventName());
				jsonObj.addProperty("userid", event.getUserId());
				jsonObj.addProperty("avail", event.getNumTicketAvail());
				jsonObj.addProperty("purchased", event.getNumTicketPurchased());
				arrObj.add(jsonObj);
			}
		}
		BaseServlet.sendResponse(response, arrObj.toString());
	}
	
	/**
	 * GET /{eventid}
	 * GET method to get event details of an event
	 * 
	 * @param request
	 * @param response
	 */
	private void getEventDetails(HttpServletRequest request, HttpServletResponse response, int eventId) {
		Event event = DatabaseManager.getInstance().selectEvent(eventId);
		if(event == null) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event not found", 1);
			BaseServlet.sendBadRequestResponse(response, "Event not found");
			return;
		}
		JsonObject eventObj = new JsonObject();
		eventObj.addProperty("eventid", eventId);
		eventObj.addProperty("eventname", event.getEventName());
		eventObj.addProperty("userid", event.getUserId());
		eventObj.addProperty("avail", event.getNumTicketAvail());
		eventObj.addProperty("purchased", event.getNumTicketPurchased());
		BaseServlet.sendResponse(response, eventObj.toString());
	}
	
	
	
	/**
	 * Helper method of create event to handle validation of json request
	 * @param request
	 * @return Event object
	 */
	private Event createEventHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			if(!JsonParserHelper.isJsonString(jsonStr)) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - request body is not json string", 1);
				return null;
			}
			Event event = JsonParserHelper.parseJsonStringToObject(jsonStr, Event.class);
			return event;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get event details from request body", 1);
			return null;
		}
	}
	
	/**
	 * Helper method of create event - create client to call User Service to check if user exists or not
	 * @param userId
	 * @return
	 */
	private boolean doesUserExist(int userId) {
		try {
			Config config = new Config();
			config.setVariables();
			String hostname = config.getHostname() + ":" + config.getUserPort();
			String path = UserServicePathConstant.GET_USER_DETAILS_PATH;
			String port = config.getUserPort();
			path = String.format(path, userId);
			String urlString = hostname + ":" + port + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User not found", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User service connection error", 1);
			return false;
		}
	}
	
	/**
	 * POST /create
	 * POST method to create event from event details passing along the body request
	 * @param request
	 * @param response
	 */
	private void createEvent(HttpServletRequest request, HttpServletResponse response) {
		Event event = createEventHelper(request);
		if(event == null || 
				event.getEventName() == null ||
				event.getUserId() <= 0 || 
				event.getNumTickets() <= 0 ) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event details invalid", 1);
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		if(!doesUserExist(event.getUserId())) {
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		int numTickets = event.getNumTickets();
		event.setNumTicketAvail(numTickets);
		event.setNumTicketPurchased(0);
		int eventId = DatabaseManager.getInstance().insertEvent(event);
		if(eventId == -1) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - user cannot be created", 1);
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		JSONObject eventObj = new JSONObject();
		eventObj.put("eventid", eventId);
		String body = eventObj.toString();
		BaseServlet.sendResponse(response, body);
	}

	/**
	 * Helper method of purchase event tickets to handle validation of json request
	 * @param request
	 * @return Event object
	 */
	private JsonObject purchaseTicketsHelper(HttpServletRequest request) {
		try {
			String jsonStr = IOUtils.toString(request.getReader());
			if(!JsonParserHelper.isJsonString(jsonStr)) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body is not json string", 1);
				return null;
			}
			JsonObject reqObj = JsonParserHelper.parseJsonStringToJsonObject(jsonStr);
			if(reqObj.get("eventid") == null || reqObj.get("tickets") == null) {
				reqObj.get("userid").getAsInt();
				reqObj.get("eventid").getAsInt();
				reqObj.get("tickets").getAsInt();
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body invalid", 1);
				return null;
			}
			return reqObj;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get event details to update from request body", 1);
		} catch (ClassCastException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot get event details to update from request body", 1);
		}
		return null;
	}
	
	/**
	 * add tickets by calling User service API to update
	 * @param jsonObj
	 * @return
	 */
	private boolean addTickets(JsonObject jsonObj) {
		try {
			Config config = new Config();
			config.setVariables();
			String hostname = config.getHostname() + ":" + config.getUserPort();
			String path = UserServicePathConstant.POST_ADD_TICKET_PATH;
			String port = config.getUserPort();
			path = String.format(path, jsonObj.get("userid").getAsInt());
			String urlString = hostname + ":" + port + path;
			URL url = new URL(urlString);
			
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "User not found", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User service connection error", 1);
			return false;
		}
	}
	
	private synchronized void purchaseTickets(HttpServletRequest request, HttpServletResponse response, int eventId) {
		//check if json from request body is valid
		JsonObject reqObj = purchaseTicketsHelper(request);
		if(reqObj == null) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//check if event id from request body match with the event id from the path
		if(eventId != reqObj.get("eventid").getAsInt()) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - event ids mismatched", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
		}
		//check if event exists
		Event event = DatabaseManager.getInstance().selectEvent(eventId);
		if(event == null) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - event not found", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		int numTicketToPurchase = reqObj.get("tickets").getAsInt();
		int numTicketAvail = event.getNumTicketAvail();
		int numTicketPurchased = event.getNumTicketPurchased();
		if(numTicketToPurchase > numTicketAvail) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "Tickets could not be purchased - not enough available number of tickets", 0);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		event.setNumTicketAvail(numTicketAvail - numTicketToPurchase);
		event.setNumTicketPurchased(numTicketPurchased + numTicketToPurchase);
		//update number of tickets in events table
		boolean areEventUpdated = DatabaseManager.getInstance().updateEvent(event);
		if(!areEventUpdated) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - event not updated", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//call User API to check if user exists
		if(!doesUserExist(event.getUserId())) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - user not found", 1);
			//rollback the number of tickets updated in events table
			event.setNumTicketAvail(numTicketAvail);
			event.setNumTicketPurchased(numTicketPurchased);
			boolean areEventUpdatedRollback = DatabaseManager.getInstance().updateEvent(event);
			if(!areEventUpdatedRollback) {
				TicketPurchaseApplicationLogger.write(Level.WARNING, "Event updated do not rollback", 1);
			}
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//if add ticket fail, return 400
		if(!addTickets(reqObj)) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - tickets not added", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//tickets added successfully, return 200
		TicketPurchaseApplicationLogger.write(Level.INFO, "Event tickets purchased", 0);
		BaseServlet.sendResponse(response, "Event tickets purchased");
	}
}
