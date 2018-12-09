package cs601.project4;

import java.io.BufferedReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonParserHelper {
	public static JsonObject parseReaderToJsonObject(BufferedReader reader) {
		JsonParser parser = new JsonParser();
		if(!parser.parse(reader).isJsonObject()) {
			return null;
		}
		JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
		return jsonObj;
	}
	
	public static JsonObject parseJsonStringToJsonObject(String jsonStr) {
		JsonParser parser = new JsonParser(); 
		if(!parser.parse(jsonStr).isJsonObject()) {
			return null;
		}
		JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
		return jsonObj;
	}
	
	public static <T> T parseJsonStringToObject(String jsonStr, Class<T> objClass) {
		try {
			JsonParser parser = new JsonParser(); 
			if(!parser.parse(jsonStr).isJsonObject()) {
				return null;
			}
			Gson gson = new Gson();
			T obj = gson.fromJson(jsonStr, objClass);
			return obj;
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static <T> String parseObjectToJsonString(T object) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(object);
		return jsonStr;
	}
	
	public static boolean isJsonString(String jsonStr) {
		JsonParser parser = new JsonParser();
		if(!parser.parse(jsonStr).isJsonObject()) {
			return false;
		}
		return true;
	}
}
