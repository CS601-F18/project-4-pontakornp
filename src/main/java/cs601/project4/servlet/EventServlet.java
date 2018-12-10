package cs601.project4.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
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
import cs601.project4.HttpConnectionHelper;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketManagementApplicationLogger;
import cs601.project4.object.Event;
import cs601.project4.object.EventJsonConstant;
import cs601.project4.object.UserServicePathConstant;

/**
 * EventServlet class handles the API request from internal users e.g. FrontEndService and UserService
 * @author pontakornp
 *
 */
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
		if(pathParts.length == 2 && pathParts[1].equals("list")) {
			getEventList(request, response);
		} else if(pathParts.length == 2 && StringUtils.isNumeric(pathParts[1])) {
			int eventId = Integer.parseInt(pathParts[1]);
			getEventDetails(request, response, eventId);
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
		
		if(pathParts.length == 2 && pathParts[1].equals("create")) {
			createEvent(request, response);
		} else if(pathParts.length == 3 && pathParts[1].equals("purchase") && StringUtils.isNumeric(pathParts[2])) {
			int eventId = Integer.parseInt(pathParts[2]);
			purchaseTickets(request, response, eventId);
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	/**
	 * GET /list
	 * GET method to get the list of events available
	 * @param request
	 * @param response
	 */
	private void getEventList(HttpServletRequest request, HttpServletResponse response) {
		List<Event> events = DatabaseManager.getInstance().selectEvents();
		JsonArray jsonArr = new JsonArray();
		if(events != null) {
			for(Event event: events) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty(EventJsonConstant.EVENT_ID, event.getEventId());
				jsonObj.addProperty(EventJsonConstant.EVENT_NAME, event.getEventName());
				jsonObj.addProperty(EventJsonConstant.USER_ID, event.getUserId());
				jsonObj.addProperty(EventJsonConstant.AVAIL, event.getNumTicketAvail());
				jsonObj.addProperty(EventJsonConstant.PURCHASED, event.getNumTicketPurchased());
				jsonArr.add(jsonObj);
			}
		}
		BaseServlet.sendResponse(response, jsonArr.toString());
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
			TicketManagementApplicationLogger.write(Level.WARNING, "Event not found", 1);
			BaseServlet.sendBadRequestResponse(response, "Event not found");
			return;
		}
		JsonObject eventObj = new JsonObject();
		eventObj.addProperty(EventJsonConstant.EVENT_ID, eventId);
		eventObj.addProperty(EventJsonConstant.EVENT_NAME, event.getEventName());
		eventObj.addProperty(EventJsonConstant.USER_ID, event.getUserId());
		eventObj.addProperty(EventJsonConstant.AVAIL, event.getNumTicketAvail());
		eventObj.addProperty(EventJsonConstant.PURCHASED, event.getNumTicketPurchased());
		BaseServlet.sendResponse(response, eventObj.toString());
	}
	
	/**
	 * Helper method of create event - create client to call User Service to check if user exists or not
	 * @param userId
	 * @return true or false
	 */
	private boolean doesUserExist(int userId) {
		try {
			Config config = new Config();
			config.setVariables();
			String hostname = config.getUserHostname();
			int port = config.getUserPort();
			String path = UserServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, userId);
			String host = hostname + ":" + port;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				TicketManagementApplicationLogger.write(Level.WARNING, "User not found", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "User service connection error", 1);
			return false;
		}
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - request body is not json string", 1);
				return null;
			}
			Event event = JsonParserHelper.parseJsonStringToObject(jsonStr, Event.class);
			if(event == null || event.getEventName() == null || 
					!StringUtils.isAlphanumericSpace(event.getEventName() ) || StringUtils.isBlank(event.getEventName()) ||
					event.getUserId() <= 0 || event.getNumTickets() <= 0 ) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - invalid request body ", 1);
				return null;
			}
			event.setEventName(event.getEventName().trim());
			return event;
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - cannot get event details from request body", 1);
			return null;
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
		if(event == null) {
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
			TicketManagementApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - user cannot be created", 1);
			BaseServlet.sendBadRequestResponse(response, "Event unsuccessfully created");
			return;
		}
		JSONObject eventObj = new JSONObject();
		eventObj.put(EventJsonConstant.EVENT_ID, eventId);
		String body = eventObj.toString();
		BaseServlet.sendResponse(response, body);
	}


	
	/**
	 * add tickets by calling User service API to update
	 * @param jsonObj
	 * @return true or false
	 */
	private boolean addTickets(JsonObject reqObj) {
		try {
			Config config = new Config();
			config.setVariables();
			String hostname = config.getUserHostname();
			int port = config.getUserPort();
			String host = hostname + ":" + port;
			String path = UserServicePathConstant.POST_ADD_TICKETS_PATH;
			path = String.format(path, reqObj.get(EventJsonConstant.USER_ID).getAsInt());
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				TicketManagementApplicationLogger.write(Level.WARNING, "User service fails to add ticket", 1);
				return false;
			}
			return true;
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "User service connection error", 1);
			return false;
		}
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
				TicketManagementApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body is not json string", 1);
				return null;
			}
			JsonObject reqObj = JsonParserHelper.parseJsonStringToJsonObject(jsonStr);
			if(reqObj.get(EventJsonConstant.USER_ID) == null || 
					reqObj.get(EventJsonConstant.EVENT_ID) == null || 
					reqObj.get(EventJsonConstant.TICKETS) == null) {
				TicketManagementApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - request body invalid", 1);
				return null;
			}
			reqObj.get(EventJsonConstant.USER_ID).getAsInt();
			reqObj.get(EventJsonConstant.EVENT_ID).getAsInt();
			reqObj.get(EventJsonConstant.TICKETS).getAsInt();
			return reqObj;
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Cannot get event details to update from request body", 1);
		} catch (NumberFormatException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Cannot get event details to update from request body", 1);
		}
		return null;
	}
	
	/**
	 * Helper method for purchase tickets by checking if there's enough ticket to be purchased
	 * @param reqObj
	 * @param event
	 * @return
	 */
	private boolean updateEventObject(JsonObject reqObj, Event event) {
		int numTicketToPurchase = reqObj.get(EventJsonConstant.TICKETS).getAsInt();
		int numTicketAvail = event.getNumTicketAvail();
		int numTicketPurchased = event.getNumTicketPurchased();
		if(numTicketToPurchase > numTicketAvail) {
			TicketManagementApplicationLogger.write(Level.INFO, "Tickets could not be purchased - not enough available number of tickets", 0);
			return false;
		}
		event.setNumTicketAvail(numTicketAvail - numTicketToPurchase);
		event.setNumTicketPurchased(numTicketPurchased + numTicketToPurchase);
		return true;
	}
	
	/**
	 * Helper method for purchase ticket by updating number of tickets in the database
	 * @param event
	 * @return true or false
	 */
	private boolean updateEvent(Event event) {
		boolean isUpdated = DatabaseManager.getInstance().updateEvent(event);
		if(!isUpdated) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - event not updated", 1);
			return false;
		}
		return true;
	}
	
	/**
	 * Helper method for purchase ticket to rollback what has been updated in the database to be in the previous state
	 * @param event
	 */
	private void rollbackUpdatedEvent(Event event) {
		boolean isRollback = DatabaseManager.getInstance().updateEvent(event);
		if(!isRollback) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Updated event fails to rollback", 1);
		} else {
			TicketManagementApplicationLogger.write(Level.INFO, "Updated event has rollbacked successfully", 0);
		}
	}
	
	/**
	 * Helper method for purchase ticket to manage adding tickets, it will performs rollback if add ticket fails
	 * @param reqObj
	 * @param event
	 * @param numTicketAvailBeforeUpdate
	 * @param numTicketPurchasedBeforeUpdate
	 * @return true or false
	 */
	private boolean manageAddTickets(JsonObject reqObj, Event event, int numTicketAvailBeforeUpdate, int numTicketPurchasedBeforeUpdate) {
		//if add ticket fail, return 400
		if(!addTickets(reqObj)) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - tickets not added", 1);
			event.setNumTicketAvail(numTicketAvailBeforeUpdate);
			event.setNumTicketPurchased(numTicketPurchasedBeforeUpdate);
			//rollback the number of tickets updated in events table
			rollbackUpdatedEvent(event);
			return false;
		}
		return true;
	}
	
	/**
	 * Helper method for purchase ticket that calls update evnet object, update event and manage add tickets methods
	 * return true if every methods perform successfully
	 * return false otherwise
	 * return false i
	 * @param reqObj
	 * @param event
	 * @return true or false
	 */
	private boolean purchaseTicketsHelper(JsonObject reqObj, Event event) {
		if(!updateEventObject(reqObj, event)) {
			return false;
		}
		int numTicketAvailBeforeUpdate = event.getNumTicketAvail();
		int numTicketPurchasedBeforeUpdate = event.getNumTicketPurchased();
		if(!updateEvent(event)) {
			return false;
		}
		if(!manageAddTickets(reqObj, event, numTicketAvailBeforeUpdate, numTicketPurchasedBeforeUpdate)) {
			return false;
		}
		return true;
	}
	
	/**
	 * POST /purchase/{eventid}
	 * post method to purchase ticket by event id
	 * return 200 response if it pass all the following conditions:
	 * 1. json object from request body is valid
	 * 2. event exists from eventid specified
	 * 3. event has enough available ticket to purchase
	 * 3. ticket has been added successfully by user service
	 * return 400 response otherwise
	 * 
	 * note that rollback on event details that has been updated will be performed, 
	 * if call user service API to add ticket is not successful
	 * 
	 * @param request
	 * @param response
	 * @param eventId
	 */
	private synchronized void purchaseTickets(HttpServletRequest request, HttpServletResponse response, int eventId) {
		//check if json from request body is valid
		JsonObject reqObj = purchaseTicketsHelper(request);
		if(reqObj == null || reqObj.get(EventJsonConstant.EVENT_ID).getAsInt() != eventId) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//check if event exists
		Event event = DatabaseManager.getInstance().selectEvent(eventId);
		if(event == null) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Tickets could not be purchased - event not found", 1);
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		if(!purchaseTicketsHelper(reqObj, event)) {
			BaseServlet.sendBadRequestResponse(response, "Tickets could not be purchased");
			return;
		}
		//tickets added successfully, return 200
		TicketManagementApplicationLogger.write(Level.INFO, "Event tickets purchased", 0);
		BaseServlet.sendResponse(response, "Event tickets purchased");
	}
}
