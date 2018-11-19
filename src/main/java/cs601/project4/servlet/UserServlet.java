package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.project4.DatabaseManager;
import cs601.project4.TicketPurchaseApplicationLogger;


public class UserServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null || pathInfo.length() <= 1) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			String body = "Page not found";
			sendResponse(response, body);
			return;
		}
		System.out.println(pathInfo);
//		String[] pathParts = pathInfo.split("/");
//		if(pathParts.length != 1) {
//			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//			String body = "User unsuccessfully created";
//			sendResponse(response, body);
//			return;
//		}
		String username = pathInfo.substring(1);
		DatabaseManager dbManager = DatabaseManager.getInstance();
		Connection con = dbManager.getConnection();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
//        String selectStmt = "SELECT * "
//        		+ "FROM users";
        StringBuilder selectStmtBuilder = new StringBuilder();
        selectStmtBuilder.append("SELECT * ");
        selectStmtBuilder.append("FROM users ");
        selectStmtBuilder.append("WHERE username = '" + username + "'");
//        String selectStmt = "SELECT * "
//        		+ "FROM users"
//        		+ "";
        String selectStmt = selectStmtBuilder.toString();
		//create a statement object
		String body = "";
		try {
			
			PreparedStatement stmt = con.prepareStatement(selectStmt);
//			execute a query, which returns a ResultSet object
			ResultSet result = stmt.executeQuery();
//			Statement stmt = con.createStatement();
			//execute a query, which returns a ResultSet object
//			ResultSet result = stmt.executeQuery (
//					"SELECT * " + 
//					"FROM users;");
			//iterate over the ResultSet
			while (result.next()) {
				//for each result, get the value of the columns name and id
				String usernameres = result.getString("username");
				String passwordres = result.getString("password");
				System.out.printf("username: %s password: %s\n", usernameres, passwordres);
				body = "{'username' : '"+ usernameres + "'}";
			}
		} catch (SQLException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "SQL error", 1);
		}
		if(body.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			body = "User not found";
		}
		sendResponse(response, body);
	}
	
	private void sendResponse(HttpServletResponse response, String body) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write(body);
		} catch (IOException e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot write to client", 1);
		}
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		String part1 = pathParts[1]; // {value}
		String part2 = pathParts[2]; // test
		// ...
//		POST /create
//		GET /{userid}
//		POST /{userid}/tickets/add
//		POST /{userid}/tickets/transfer
		
		//POST /echo
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
		
		PrintWriter out = response.getWriter();

		String msg = request.getParameter("usermsg");
		
		out.println("<html><title>EchoServlet</title><body>You said: " + msg + "</body></html>");

	}
}
