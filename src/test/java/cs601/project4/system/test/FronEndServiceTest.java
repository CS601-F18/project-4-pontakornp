package cs601.project4.system.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cs601.project4.constant.EventJsonConstant;
import cs601.project4.constant.FrontEndJsonConstant;
import cs601.project4.constant.FrontEndServicePathConstant;
import cs601.project4.constant.UserJsonConstant;
import cs601.project4.constant.UserServicePathConstant;
import cs601.project4.helper.Config;
import cs601.project4.helper.HttpConnectionHelper;
import cs601.project4.helper.JsonParserHelper;
import cs601.project4.helper.TicketManagementApplicationLogger;
import cs601.project4.unit.test.SqlQueryTest;

/**
 * 
 * @author pontakornp
 *
 *
 * Performs system test by calling Frontend Service API paths
 */
public class FronEndServiceTest {
	private static String host;
		
	@BeforeClass
	public static void initialize() {
		TicketManagementApplicationLogger.initialize(SqlQueryTest.class.getName(), "FrontEndServiceTest.txt");
		Config config = new Config();
		config.setVariables();
		String hostname = config.getFrontEndHostname();
		int port = config.getFrontEndPort();
		host = hostname + ":" + port;
	}
	
	@Test
	public void testGetEventDetailsResponseBody() {
		try {
			String path = FrontEndServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertEquals(1 ,jsonObj.get(FrontEndJsonConstant.EVENT_ID).getAsInt());
			assertEquals("blockchain" ,jsonObj.get(FrontEndJsonConstant.EVENT_NAME).getAsString());
			assertEquals(2 ,jsonObj.get(FrontEndJsonConstant.USER_ID).getAsInt());
			assertTrue(jsonObj.get(FrontEndJsonConstant.AVAIL).getAsInt() <= 50);
			assertTrue(jsonObj.get(FrontEndJsonConstant.PURCHASED).getAsInt() >= 0);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetEventDetialsResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsValid() {
		try {
			String path = FrontEndServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetEventDetailsValid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventDetailsInvalid() {
		try {
			String path = FrontEndServicePathConstant.GET_EVENT_DETAILS_PATH;
			path = String.format(path, 1000);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetEventDetailsNotExist connection error", 1);
		}
	}
	
	@Test
	public void testGetPathInvalid() {
		try {
			String path = "/events/%s";
			path = String.format(path, "car");
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(404, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetPathInvalid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventListResponseBody() {
		try {
			String path = FrontEndServicePathConstant.GET_EVENT_LIST_PATH;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonArray resArr = JsonParserHelper.parseJsonStringToJsonArray(responseStr);
			assertTrue(resArr.size() > 0);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetEventListValid connection error", 1);
		}
	}
	
	@Test
	public void testGetEventListValid() {
		try {
			String path = FrontEndServicePathConstant.GET_EVENT_LIST_PATH;
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetEventListValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventResponseBody() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 1;
			String eventName = "testFrontEndCreateEventResponseBody" + (int)(Math.random()*1000);
			int numTickets = 3;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
			reqObj.addProperty(FrontEndJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(FrontEndJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get(EventJsonConstant.EVENT_ID) != null);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventValid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 2;
			String eventName = "testFrontEndCreateEventValid-" + (int)(Math.random()*1000);
			int numTickets = 4;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
			reqObj.addProperty(FrontEndJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(FrontEndJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventWithSpaceValid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 2;
			String eventName = "test front-end create-event valid" + (int)(Math.random()*1000);
			int numTickets = 4;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventWithSymbolValid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 3;
			String eventName = "testFrontEndCreateEventValid" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			int numTickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
			reqObj.addProperty(FrontEndJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(FrontEndJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventWithSymbolValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateEventBlankInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			int userId = 3;
			String eventName = "";
			int numTickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(EventJsonConstant.USER_ID, userId);
			reqObj.addProperty(EventJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(EventJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventBlankInvalid connection error", 1);
		}
	}
	
	
	@Test
	public void testCreateEventJsonInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_EVENT_PATH;
			String userId = "create";
			String eventName = "testFrontEndCreateEventInvalid" + (int)(Math.random()*1000);
			int numTickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USER_ID, userId);
			reqObj.addProperty(FrontEndJsonConstant.EVENT_NAME, eventName);
			reqObj.addProperty(FrontEndJsonConstant.NUM_TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateEventInvalid connection error", 1);
		}
	}

	@Test
	public void testPurchaseTicketsValid() {
		try {
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			int userId = 5;
			path = String.format(path, eventId, userId);
			int tickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 2;
			int userId = 1;
			path = String.format(path, eventId, userId);
			int tickets = 5;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseNumTicketsInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			int userId = 1;
			path = String.format(path, eventId, userId);
			int tickets = 0;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseJsonInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			int userId = 2;
			path = String.format(path, eventId, userId);
			String tickets = "invalid";
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, tickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testPurchaseEmptyJsonInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_PURCHASE_TICKETS_PATH;
			int eventId = 1;
			int userId = 2;
			path = String.format(path, eventId, userId);
			JsonObject reqObj = new JsonObject();
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testPurchaseTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testGetUserResponseBody() {
		try {
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			JsonArray jsonArr = jsonObj.get(FrontEndJsonConstant.TICKETS).getAsJsonArray();
			assertEquals(1, jsonObj.get(UserJsonConstant.USER_ID).getAsInt());
			assertEquals("hello", jsonObj.get(UserJsonConstant.USERNAME).getAsString());
			assertTrue(jsonArr.size() > 0);
			assertTrue(jsonArr.get(0).getAsJsonObject().size() == 5);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetUserBody connection error", 1);
		}
	}
	
	@Test
	public void testGetUserValid() {
		try {
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetUserValid connection error", 1);
		}
	}
	
	@Test
	public void testGetUserInvalid() {
		try {
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1000);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetUserNotExist connection error", 1);
		}
	}

	@Test
	public void testGetUserNoTicketResponseBody() {
		try {
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 76);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertEquals(76, jsonObj.get(UserJsonConstant.USER_ID).getAsInt());
			assertEquals("404707ad-c", jsonObj.get(UserJsonConstant.USERNAME).getAsString());
			assertTrue(jsonObj.get(UserJsonConstant.TICKETS).getAsJsonArray().size() == 0);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetUserNoTicketResponseBody connection error", 1);
		}
	}
	
	@Test
	public void testGetUserNoTicketValid() {
		try {
			String path = FrontEndServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 76);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testGetUserNoTicketValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserResponseBody() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testFrontEndCreateUser" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get(FrontEndJsonConstant.USER_ID) != null);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateUserBody connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserValid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testFrontEndCreateUser-" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateUserValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserSymbolValid() {
		try {
			String path = FrontEndServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testFrontEndCreateUser" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testCreateUserSymbolValid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsValid() {
		try {
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(FrontEndJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testTransferTicketsValid connection error", 1);
		}
	}

	@Test
	public void testTransferTicketsEventInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			String eventId = "abc";
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(FrontEndJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testTransferTicketsEventInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsNumTicketsZero() {
		try {
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 0;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(FrontEndJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsNumTicketsInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 100;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(FrontEndJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsTargetUserInvalid() {
		try {
			String path = FrontEndServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 1000;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(FrontEndJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(FrontEndJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(FrontEndJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "testTransferTicketsTargetUserInvalid connection error", 1);
		}
	}
}
