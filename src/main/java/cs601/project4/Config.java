package cs601.project4;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * 
 * @author pontakornp
 * 
 * 
 * Manages config file of tickets management API service
 */
public class Config {
	private String hostname;
	private int frontEndPort;
	private int eventPort;
	private int userPort;
	private String dbUsername;
	private String dbPassword;
	private String dbHostname;
	private String db;

	/**
	 * Set variables from the config.json file and assign them to variables in this class.
	 */
	public boolean setVariables() {
		Config config = new Config();
		try {
			JsonReader jsonReader = new JsonReader(new FileReader("config.json"));
			Gson gson = new Gson();
			config = gson.fromJson(jsonReader, Config.class);
			this.hostname = config.hostname;
			this.frontEndPort = config.frontEndPort;
			this.eventPort = config.eventPort;
			this.userPort = config.userPort;
			this.dbUsername = config.dbUsername;
			this.dbPassword = config.dbPassword;
			this.dbHostname = config.dbHostname;
			this.db = config.db;
		} catch(IOException ioe) {
			System.out.println("Please try again with correct config file.");
			return false;
		}
		return true;
	}

	public String getHostname() {
		return hostname;
	}


	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getFrontEndPort() {
		return frontEndPort;
	}


	public void setFrontEndPort(int frontEndPort) {
		this.frontEndPort = frontEndPort;
	}


	public int getEventPort() {
		return eventPort;
	}


	public void setEventPort(int eventPort) {
		this.eventPort = eventPort;
	}


	public int getUserPort() {
		return userPort;
	}


	public void setUserPort(int userPort) {
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


	public String getDbHostname() {
		return dbHostname;
	}


	public void setDbHostname(String dbHostname) {
		this.dbHostname = dbHostname;
	}


	public String getDb() {
		return db;
	}


	public void setDb(String db) {
		this.db = db;
	}
}