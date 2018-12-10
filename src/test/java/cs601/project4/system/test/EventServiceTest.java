package cs601.project4.system.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;

import cs601.project4.Config;
import cs601.project4.HttpConnectionHelper;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.object.EventJsonConstant;
import cs601.project4.object.EventServicePathConstant;
import cs601.project4.unit.test.SqlQueryTest;

/**
 * 
 * @author pontakornp
 *
 *
 * Performs system test by calling Event Service API paths
 */
public class EventServiceTest {
	private static String host;
	
	@BeforeClass
	public static void initialize() {
		TicketPurchaseApplicationLogger.initialize(SqlQueryTest.class.getName(), "EventServiceTest.txt");
		Config config = new Config();
		config.setVariables();
		String hostname = config.getHostname();
		int port = config.getEventPort();
		host = hostname + ":" + port;
	}
	
	@Test
	public void testGetEventDetailsResponseBody() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertEquals(1 ,jsonObj.get(EventJsonConstant.EVENT_ID).getAsInt());
			assertEquals("blockchain" ,jsonObj.get(EventJsonConstant.EVENT_NAME).getAsString());
			assertEquals(2 ,jsonObj.get(EventJsonConstant.USER_ID).getAsInt());
			assertTrue(jsonObj.get(EventJsonConstant.AVAIL).getAsInt() <= 50);
			assertTrue(jsonObj.get(EventJsonConstant.PURCHASED).getAsInt() >= 0);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetialsResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsValid() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetailsValid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsInvalid() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1000);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetailsNotExist connection error", 1);
		}
	}
	
	@Test
	public void testGetPathInvalid() {
		try {
			String path = "/%s";
			path = String.format(path, "car");
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(404, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetPathInvalid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventListValid() {
		try {
			String path = EventServicePathConstant.GET_EVENT_LIST_PATH;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventListValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventResponseBody() {
		try {
			String path = EventServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 1;
			String eventName = "testCreateEventResponseBody" + (int)(Math.random()*1000);
			int numTickets = 3;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get(EventJsonConstant.EVENT_ID) != null);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateEventResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventValid() {
		try {
			String path = EventServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 2;
			String eventName = "testCreateEventValid" + (int)(Math.random()*1000);
			int numTickets = 4;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateEventValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventInvalid() {
		try {
			String path = EventServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 3;
			String eventName = "testCreateEventInvalid" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			int numTickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateEventInvalid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventJsonInvalid() {
		try {
			String path = EventServicePathConstant.POST_CREATE_EVENT_PATH;
			String userId = "create";
			String eventName = "testCreateEventInvalid" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			int numTickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateEventInvalid connection error", 1);
		}
	}

	@Test
	public void testPurchaseTicketsValid() {
		try {
			String path = EventServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			path = String.format(path, eventId);
			int userId = 5;
			int tickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(EventJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseInvalid() {
		try {
			String path = EventServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 2;
			path = String.format(path, eventId);
			int userId = 1;
			int tickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(EventJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseEventIdInvalid() {
		try {
			String path = EventServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			path = String.format(path, eventId);
			String eventIdString = "purchase";
			int userId = 1;
			int tickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_ID, eventIdString);
			reqObj.addProperty(EventJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseJsonInvalid() {
		try {
			String path = EventServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			path = String.format(path, eventId);
			String userId = "purchase";
			int tickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(EventJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseEmptyJsonInvalid() {
		try {
			String path = EventServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			path = String.format(path, eventId);
			JsonObject reqObj = new JsonObject();
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
}
