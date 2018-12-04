package cs601.project4.servlet;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.database.DatabaseManager;
import cs601.project4.database.Event;

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
			
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	public Event createEventHelper(HttpServletRequest request) {
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
	
	public void createEvent(HttpServletRequest request, HttpServletResponse response) {
		Event event = createEventHelper(request);
		if(event == null || 
				event.getEventName() == null ||
				event.getUserId() <= 0 || 
				event.getNumTickets() <= 0 ) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Event unsuccessfully created - event details invalid", 1);
			BaseServlet.sendBadRequestResponse(response, "User unsuccessfully created");
			return;
		}
		int numTickets = event.getNumTickets();
		event.setNumTicketAvail(numTickets);
		event.setNumTicketPurchased(0);
		int eventId = DatabaseManager.getInstance().insertEvent(event);
		if(eventId == -1) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "User unsuccessfully created - user cannot be created", 1);
			BaseServlet.sendBadRequestResponse(response, "User unsuccessfully created");
			return;
		}
		JSONObject eventObj = new JSONObject();
		eventObj.put("eventid", eventId);
		String body = eventObj.toString();
		BaseServlet.sendResponse(response, body);
	}
}
