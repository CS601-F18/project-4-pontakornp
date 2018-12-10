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
import cs601.project4.object.UserJsonConstant;
import cs601.project4.object.UserServicePathConstant;
import cs601.project4.unit.test.SqlQueryTest;

/**
 * 
 * @author pontakornp
 *
 *
 * Performs system test by calling User Service API paths
 */
public class UserServiceTest {
	private static String host;
	
	@BeforeClass
	public static void initialize() {
		TicketPurchaseApplicationLogger.initialize(SqlQueryTest.class.getName(), "UserServiceTest.txt");
		Config config = new Config();
		config.setVariables();
		String hostname = config.getHostname();
		int port = config.getUserPort();
		host = hostname + ":" + port;
		
	}
	
	@Test
	public void testGetUserResponseBody() {
		try {
			String path = UserServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject jsonObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertEquals(1, jsonObj.get(UserJsonConstant.USER_ID).getAsInt());
			assertEquals("hello", jsonObj.get(UserJsonConstant.USERNAME).getAsString());
			assertTrue(jsonObj.get(UserJsonConstant.TICKETS).getAsJsonArray().size() > 0);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserBody connection error", 1);
		}
	}
	
	@Test
	public void testGetUserValid() {
		try {
			String path = UserServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserValid connection error", 1);
		}
	}
	
	@Test
	public void testGetUserInvalid() {
		try {
			String path = UserServicePathConstant.GET_USER_DETAILS_PATH;
			path = String.format(path, 1000);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserNotExist connection error", 1);
		}
	}
	
	@Test
	public void testGetPathInvalid() {
		try {
			String path = "/%s";
			path = String.format(path, "abc");
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(404, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetPathInvalid connection error", 1);
		}
	}

	@Test
	public void testCreateUserResponseBody() {
		try {
			String path = UserServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testCreateUser" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			String responseStr = HttpConnectionHelper.getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get(UserJsonConstant.USER_ID) != null);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserBody connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserValid() {
		try {
			String path = UserServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testCreateUser" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserInvalid() {
		try {
			String path = UserServicePathConstant.POST_CREATE_USER_PATH;
			String username = "testCreteUser" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.USERNAME, username);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserInvalid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsValid() {
		try {
			String path = UserServicePathConstant.POST_ADD_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsUserIdInvalid() {
		try {
			String path = UserServicePathConstant.POST_ADD_TICKETS_PATH;
			path = String.format(path, 1000);
			int eventId = 1;
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsEventIdInvalid() {
		try {
			String path = UserServicePathConstant.POST_ADD_TICKETS_PATH;
			path = String.format(path, 1);
			String eventId = "abc";
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsNumTicketsInvalid() {
		try {
			String path = UserServicePathConstant.POST_ADD_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 0;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsValid() {
		try {
			String path = UserServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(UserJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsValid connection error", 1);
		}
	}

	@Test
	public void testTransferTicketsEventInvalid() {
		try {
			String path = UserServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			String eventId = "abc";
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(UserJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsEventInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsNumTicketsZero() {
		try {
			String path = UserServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 0;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(UserJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsNumTicketsInvalid() {
		try {
			String path = UserServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			path = String.format(path, 1);
			int eventId = 1;
			int numTickets = 100;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(UserJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsTargetUserInvalid() {
		try {
			String path = UserServicePathConstant.POST_TRANSFER_TICKETS_PATH;
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 1000;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty(UserJsonConstant.EVENT_ID, eventId);
			reqObj.addProperty(UserJsonConstant.TICKETS, numTickets);
			reqObj.addProperty(UserJsonConstant.TARGET_USER, targetUserId);
			HttpURLConnection con = HttpConnectionHelper.getConnection(host, path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsTargetUserInvalid connection error", 1);
		}
	}
}
