package cs601.project4.system.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;

import cs601.project4.Config;
import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.object.EventJsonConstant;
import cs601.project4.object.EventServicePathConstant;
import cs601.project4.unit.test.SqlQueryTest;

public class EventServletTest {
	private static String eventUrl;
	
	@BeforeClass
	public static void initialize() {
		Config config = new Config();
		config.setVariables();
		String hostname = config.getHostname();
		String port = config.getEventPort();
		eventUrl = hostname + ":" + port;
		TicketPurchaseApplicationLogger.initialize(SqlQueryTest.class.getName(), "EventServletTest.txt");
	}
	
	private String getBodyResponse(HttpURLConnection con) throws IOException {
		String bodyResponse = IOUtils.toString(con.getInputStream(), "UTF-8");
		return bodyResponse;
	}
	
	private HttpURLConnection getConnection(String path, JsonObject reqObj) throws IOException{
		String urlString = eventUrl + path;
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setRequestProperty("Accept", "application/json");
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(reqObj.toString());
		w.flush();
		w.close();
		return con;
	}
	
	@Test
	public void testGetEventDetailsResponseBody() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			String urlString = eventUrl + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			String responseStr = getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertEquals(1 ,jsonObj.get(EventJsonConstant.EVENT_ID).getAsInt());
			assertEquals("blockchain" ,jsonObj.get(EventJsonConstant.EVENT_NAME).getAsString());
			assertEquals(2 ,jsonObj.get(EventJsonConstant.USER_ID).getAsInt());
			assertEquals(50 ,jsonObj.get(EventJsonConstant.AVAIL).getAsInt());
			assertEquals(0 ,jsonObj.get(EventJsonConstant.PURCHASED).getAsInt());
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetialsResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsValid() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			String urlString = eventUrl + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetailsValid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsNotExist() {
		try {
			String path = EventServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1000);
			String urlString = eventUrl + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
			String urlString = eventUrl + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
			String urlString = eventUrl + path;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
			HttpURLConnection con = getConnection(path, reqObj);
			String responseStr = getBodyResponse(con);
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
			HttpURLConnection con = getConnection(path, reqObj);
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
			HttpURLConnection con = getConnection(path, reqObj);
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
			HttpURLConnection con = getConnection(path, reqObj);
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
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
}
