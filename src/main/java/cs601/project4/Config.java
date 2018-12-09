package cs601.project4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * 
 * @author pontakornp
 * 
 * Manages config file of chat and search applications
 * 
 */
public class Config {
	private String frontendPort;
	private String eventPort;
	private String userPort;
	private String dbUsername;
	private String dbPassword;
	private String hostname;
	private String db;

	/**
	 * Set variables from the config.json file and assign them to variables in this class.
	 */
	public boolean setVariables() {
		Charset cs = Charset.forName("ISO-8859-1");
		Path path = Paths.get("config.json");
		Config config = new Config();
		try {
			JsonReader jsonReader = new JsonReader(new FileReader("config.json"));
			Gson gson = new Gson();
			config = gson.fromJson(jsonReader, Config.class);
			this.frontendPort = config.frontendPort;
			this.eventPort = config.eventPort;
			this.userPort = config.userPort;
			this.dbUsername = config.dbUsername;
			this.dbPassword = config.dbPassword;
			this.hostname = config.hostname;
			this.db = config.db;
		} catch(IOException ioe) {
			System.out.println("Please try again with correct config file.");
			return false;
		}
		return true;
	}


	public String getFrontendPort() {
		return frontendPort;
	}


	public void setFrontendPort(String frontendPort) {
		this.frontendPort = frontendPort;
	}


	public String getEventPort() {
		return eventPort;
	}


	public void setEventPort(String eventPort) {
		this.eventPort = eventPort;
	}


	public String getUserPort() {
		return userPort;
	}


	public void setUserPort(String userPort) {
		this.userPort = userPort;
	}


	public String getDbUsername() {
		return dbUsername;
	}


	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}


	public String getDbPassword() {
		return dbPassword;
	}


	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}


	public String getHostname() {
		return hostname;
	}


	public void setHostname(String hostname) {
		this.hostname = hostname;
	}


	public String getDb() {
		return db;
	}


	public void setDb(String db) {
		this.db = db;
	}
}