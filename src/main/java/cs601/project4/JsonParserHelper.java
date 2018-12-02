package cs601.project4;

import java.io.BufferedReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonParserHelper {
	public static JsonObject parseReaderToJsonObject(BufferedReader reader) {
		JsonParser parser = new JsonParser(); 
		JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
		return jsonObj;
	}
	
	public static JsonObject parseJsonStringToJsonObject(String jsonStr) {
		JsonParser parser = new JsonParser(); 
		JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
		return jsonObj;
	}
}
