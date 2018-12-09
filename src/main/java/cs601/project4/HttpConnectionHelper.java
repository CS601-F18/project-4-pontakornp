package cs601.project4;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

public class HttpConnectionHelper {
	public static String getBodyResponse(HttpURLConnection con) throws IOException {
		String bodyResponse = IOUtils.toString(con.getInputStream(), "UTF-8");
		return bodyResponse;
	}
	
	public static HttpURLConnection getConnection(String host, String path, JsonObject reqObj) throws IOException{
		String urlString = host + path;
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
}
