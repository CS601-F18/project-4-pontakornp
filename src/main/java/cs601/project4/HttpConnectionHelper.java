package cs601.project4;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

/**
 * 
 * @author pontakornp
 *
 * Helper class to get a connection via http by get or post method
 * it also has method to get body response from the connection
 */
public class HttpConnectionHelper {
	public static HttpURLConnection getConnection(String host, String path) throws IOException{
		String urlString = host + path;
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		return con;
	}
	
	private static HttpURLConnection getPostConnectionHelper(String host, String path) throws IOException {
		HttpURLConnection con = getConnection(host,path);
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setRequestProperty("Accept", "application/json");
		return con;
	}
	
	public static HttpURLConnection getConnection(String host, String path, String jsonStr) throws IOException{
		HttpURLConnection con = getPostConnectionHelper(host, path);
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(jsonStr);
		w.flush();
		w.close();
		return con;
	}
	
	public static HttpURLConnection getConnection(String host, String path, JsonObject reqObj) throws IOException{
		HttpURLConnection con = getPostConnectionHelper(host, path);
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(reqObj.toString());
		w.flush();
		w.close();
		return con;
	}
	
	public static String getBodyResponse(HttpURLConnection con) throws IOException {
		String bodyResponse = IOUtils.toString(con.getInputStream(), "UTF-8");
		return bodyResponse;
	}
}
