package cs601.project4.system.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;

import cs601.project4.JsonParserHelper;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.unit.test.SqlQueryTest;

public class UserServletTest {
//	HttpURLConnection con;
	String host = "http://localhost:8082";
	
	@BeforeClass
	public static void initialize() {
		TicketPurchaseApplicationLogger.initialize(SqlQueryTest.class.getName(), "UserServletTest.txt");
	}
	
	@Test
	public void testGetUserResponseBody() {
		try {
			String urlString = host + "/1";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			String responseStr = getBodyResponse(con);
			JSONObject jsonObj = new JSONObject(responseStr);
			assertTrue(jsonObj.get("userid") instanceof Integer);
			assertTrue(jsonObj.get("username") instanceof String);
			assertTrue(jsonObj.get("tickets") instanceof JSONArray);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserBody connection error", 1);
		}
	}
	
	@Test
	public void testGetUserValid() {
		try {
			String urlString = host + "/1";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
			String urlString = host + "/abc";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserInvalid connection error", 1);
		}
	}
	
	@Test
	public void testGetUserNotExist() {
		try {
			String urlString = host + "/1000";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetUserNotExist connection error", 1);
		}
	}
	
	public String getBodyResponse(HttpURLConnection con) throws IOException {
		String bodyResponse = IOUtils.toString(con.getInputStream(), "UTF-8");
		return bodyResponse;
	}
	
	public HttpURLConnection getConnection(String path, JsonObject reqObj) throws IOException{
		String urlString = host + path;
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setRequestProperty("Accept", "application/json");
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		
//		DataOutputStream s = new DataOutputStream(con.getOutputStream());
//		System.out.println(reqObj.toString());
//		s.writeBytes(reqObj.toString());
//		s.flush();
//		s.close();
		w.write(reqObj.toString());
		w.flush();
		w.close();
		return con;
	}

	@Test
	public void testCreateUserResponseBody() {
		try {
			String path = "/create";
			String username = "testCreateUser" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("username", username);
			HttpURLConnection con = getConnection(path, reqObj);
			String responseStr = getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get("userid") != null);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserBody connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserValid() {
		try {
			String path = "/create";
			String username = "testCreateUser" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("username", username);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserInvalid() {
		try {
			String path = "/create";
			String username = "testCreteUser" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("username", username);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserInvalid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsValid() {
		try {
			String path = "/1/tickets/add";
			int eventId = 1;
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			System.out.println(reqObj.toString());
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsUserInvalid() {
		try {
			String path = "/1000/tickets/add";
			int eventId = 1;
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsEventInvalid() {
		try {
			String path = "/1/tickets/add";
			String eventId = "abc";
			int numTickets = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testAddTicketsNumTicketsInvalid() {
		try {
			String path = "/1/tickets/add";
			int eventId = 1;
			int numTickets = 0;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testAddTicketsValid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsValid() {
		try {
			String path = "/1/tickets/transfer";
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			reqObj.addProperty("targetuser", targetUserId);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsValid connection error", 1);
		}
	}

	@Test
	public void testTransferTicketsEventInvalid() {
		try {
			String path = "/1/tickets/transfer";
			String eventId = "abc";
			int numTickets = 2;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			reqObj.addProperty("targetuser", targetUserId);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsEventInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsNumTicketsZero() {
		try {
			String path = "/1/tickets/transfer";
			int eventId = 1;
			int numTickets = 0;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			reqObj.addProperty("targetuser", targetUserId);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	public void testTransferTicketsNumTicketsInvalid() {
		try {
			String path = "/1/tickets/transfer";
			int eventId = 1;
			int numTickets = 100;
			int targetUserId = 2;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			reqObj.addProperty("targetuser", targetUserId);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsNumTicketsInvalid connection error", 1);
		}
	}
	
	@Test
	public void testTransferTicketsTargetUserInvalid() {
		try {
			String path = "/1/tickets/transfer";
			int eventId = 1;
			int numTickets = 2;
			int targetUserId = 1000;
			JsonObject reqObj = new JsonObject();
			reqObj.addProperty("eventid", eventId);
			reqObj.addProperty("tickets", numTickets);
			reqObj.addProperty("targetuser", targetUserId);
			HttpURLConnection con = getConnection(path, reqObj);
			int responseCode = con.getResponseCode();
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testTransferTicketsTargetUserInvalid connection error", 1);
		}
	}
}
