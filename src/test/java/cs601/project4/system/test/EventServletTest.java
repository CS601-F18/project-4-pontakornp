package cs601.project4.system.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
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
			assertEquals(1 ,jsonObj.get(EventJsonConstant.USER_ID).getAsInt());
			assertEquals(5 ,jsonObj.get(EventJsonConstant.AVAIL).getAsInt());
			assertEquals(0 ,jsonObj.get(EventJsonConstant.PURCHASED).getAsInt());
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "testGetEventDetialsResponseBody connection error", 1);
		}
	}
	
	
}
