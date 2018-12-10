package cs601.project4;

import java.io.BufferedReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * @author pontakornp
 *
 *
 * Helper class to parse between json object, json string, or some object that has getter setter class
 * it also has method to check if a string is a json string
 */
public class JsonParserHelper {
	public static JsonObject parseReaderToJsonObject(BufferedReader reader) {
		try {
			JsonParser parser = new JsonParser();
			if(!parser.parse(reader).isJsonObject()) {
				return null;
			}
			JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
			return jsonObj;
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static JsonObject parseJsonStringToJsonObject(String jsonStr) {
		try {
			JsonParser parser = new JsonParser(); 
			if(!parser.parse(jsonStr).isJsonObject()) {
				return null;
			}
			JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
			return jsonObj;
		} catch (JsonSyntaxException e) {
			return null;
		}
	}
	
	public static JsonArray parseJsonStringToJsonArray(String jsonStr) {
		try {
			JsonParser parser = new JsonParser(); 
			if(!parser.parse(jsonStr).isJsonArray()) {
				return null;
			}
			JsonArray jsonArr = parser.parse(jsonStr).getAsJsonArray();
			return jsonArr;
		} catch (JsonSyntaxException e) {
			return null;
		}
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

	public static boolean isJsonString(String jsonStr) {
		JsonParser parser = new JsonParser();
		if(!parser.parse(jsonStr).isJsonObject()) {
			return false;
		}
		return true;
	}
}
