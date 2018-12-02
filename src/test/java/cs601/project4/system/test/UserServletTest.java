package cs601.project4.system.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	public void testGetUserBody() {
		try {
			String urlString = host + "/1";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			String responseStr = getBodyResponse(con);
//			int responseCode = con.getResponseCode();
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
	
	//reference: https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
	public String getBodyResponse(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
	
	@Test
	public void testCreateUserBody() {
		try {
			String urlString = host + "/create";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			String username = "testCreateUser" + (int)(Math.random()*1000);
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			con.setRequestProperty("Accept", "application/json");
			JSONObject reqObj = new JSONObject();
			reqObj.put("username", username);
			OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream());
			w.write(reqObj.toString());
			w.flush();
			w.close();
			String responseStr = getBodyResponse(con);
			JsonObject resObj = JsonParserHelper.parseJsonStringToJsonObject(responseStr);
			assertTrue(resObj.get("userid") != null);
		} catch (IOException e) {
			e.printStackTrace();
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserBody connection error", 1);
		}
	}
	
	public int getCreateUserResponseCode(String username) throws IOException{
		String urlString = host + "/create";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setRequestProperty("Accept", "application/json");
		JSONObject reqObj = new JSONObject();
		reqObj.put("username", username);
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(reqObj.toString());
		w.flush();
		w.close();
		int responseCode = con.getResponseCode();
		return responseCode;
	}
	
	@Test
	public void testCreateUserValid() {
		try {
			String username = "testCreateUser" + (int)(Math.random()*1000);
			int responseCode = getCreateUserResponseCode(username);
			assertEquals(200, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserValid connection error", 1);
		}
	}
	
	@Test
	public void testCreateUserInvalid() {
		try {
			String username = "testCreteUser" +"!@#$%^&*(()_+,.'" + (int)(Math.random()*1000);
			int responseCode = getCreateUserResponseCode(username);
			assertEquals(400, responseCode);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testCreateUserInvalid connection error", 1);
		}
	}
}
