package cs601.project4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
	private static DatabaseManager INSTANCE;
	private static Connection con;
	
	private DatabaseManager() {
		Config config = new Config();
		config.setVariables();
		
		String username  = config.getDbUsername();
		String password  = config.getDbPassword();
		try {
			// load driver
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}
		catch (Exception e) {
			System.err.println("Can't find driver");
			System.exit(1);
		}
		String hostname = config.getHostname();
		String db = config.getDb();
		String urlString = "jdbc:mysql://"+ hostname + "/" + db;
		//Must set time zone explicitly in newer versions of mySQL.
		String timeZoneSettings = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		//https://stackoverflow.com/questions/21361781/how-to-connect-to-database-connection-in-java
		con = null;
		
		try {
			con = DriverManager.getConnection(urlString + timeZoneSettings,
					username,
					password);
	    } catch (SQLException e) {
	        System.out.println("Connection Failed! Check output console");
	        TicketPurchaseApplicationLogger.write(Level.WARNING, "Connection failed.", 1);
	        return;
	    }

	    if (con != null) {
	    	TicketPurchaseApplicationLogger.write(Level.INFO, "Connection established.", 0);
	    } else {
	        TicketPurchaseApplicationLogger.write(Level.WARNING, "Failed to make connection.", 1);
	    }
	} 
	
	public static DatabaseManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DatabaseManager();
		}
		return INSTANCE;
	}
	
	public static Connection getConnection() {
		return con;
	}
	
	public static void closeConnection() {
		try {
			con.close();
			TicketPurchaseApplicationLogger.write(Level.INFO, "Successfully close db connection.", 0);
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Failed to close db conection", 1);
		}
	}
}