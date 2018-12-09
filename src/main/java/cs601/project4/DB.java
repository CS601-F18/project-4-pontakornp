package cs601.project4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
	public static void main(String[] args) throws SQLException {
		String username  = "root";
		String password  = "root";
		String db  = "ticket_management";

		try {
			// load driver
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}
		catch (Exception e) {
			System.err.println("Can't find driver");
			System.exit(1);
		}

		// format "jdbc:mysql://[hostname][:port]/[dbname]"
		//note: if connecting through an ssh tunnel make sure to use 127.0.0.1 and
		//also to that the ports are set up correctly
//		String urlString = "jdbc:mysql://sql.cs.usfca.edu/"+db;
		String urlString = "jdbc:mysql://127.0.0.1:8889/" + db;
		//Must set time zone explicitly in newer versions of mySQL.
		String timeZoneSettings = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

		System.out.println("test");
//		Connection con = DriverManager.getConnection(urlString + timeZoneSettings,
//				username,
//				password);
//		
		DatabaseManager dbManager = DatabaseManager.getInstance();
		Connection con = dbManager.getConnection();
		System.out.println(con);
		//create a statement object
		Statement stmt = con.createStatement();
		//execute a query, which returns a ResultSet object
		ResultSet result = stmt.executeQuery (
				"SELECT * " + 
				"FROM users;");
		System.out.println("ha");

		//iterate over the ResultSet
		while (result.next()) {
			//for each result, get the value of the columns name and id
			String nameres = result.getString("username");
			String passres = result.getString("password");
//			int idres = result.getInt("id");
			System.out.printf("username: %s pass: %s\n", nameres, passres);
		}

//		String name = "Robert";
//		int id = 123;
//
//		//reuse the statement to insert a new value into the table
//		stmt.executeUpdate("INSERT INTO customers (name, id) VALUES (\"" + name + "\", \"" + id + "\")");
//		System.out.println("\n***\n");
//
//		//print the updated table
//		result = stmt.executeQuery (
//				"SELECT * " + 
//				"FROM customers;");
//		while (result.next()) {
//			String nameres = result.getString("name");
//			int idres = result.getInt("id");
//			System.out.printf("name: %s id: %d\n", nameres, idres);
//		}

		con.close();
	}
}
