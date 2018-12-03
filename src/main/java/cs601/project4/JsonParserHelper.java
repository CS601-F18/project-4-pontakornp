package cs601.project4;

import java.io.BufferedReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonParserHelper {
	public static JsonObject parseReaderToJsonObject(BufferedReader reader) {
		JsonParser parser = new JsonParser(); 
//		JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
//		return jsonObj;
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
		Gson gson = new Gson();
		T obj = gson.fromJson(jsonStr, objClass);
		return obj;
	}
	
	public static <T> String parseObjectToJsonString(T object) {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(object);
		return jsonStr;
	}
	
	//reference: https://stackoverflow.com/questions/10174898/how-to-check-whether-a-given-string-is-valid-json-in-java/10174938#10174938
//	public static boolean isJsonString(String jsonStr) {
//        try {
//            new JSONObject(jsonStr);
//        } catch (JSONException ex) {
//            try {
//                new JSONArray(jsonStr);
//            } catch (JSONException ex1) {
//                return false;
//            }
//        }
//        return true;
//    }
//	public static boolean isJsonString(String jsonStr) {
//		try{
//			JsonParser parser = new JsonParser();
//			parser.parse(jsonStr);
//		} 
//			catch(JsonSyntaxException jse){
//			System.out.println("Not a valid Json String:"+jse.getMessage());
//			return false;
//		}
//		return true;
//	}
	
	public static boolean isJsonString(String jsonStr) {
		JsonParser parser = new JsonParser();
		if(!parser.parse(jsonStr).isJsonObject()) {
			return false;
		}
		return true;
	}
	
	
}
