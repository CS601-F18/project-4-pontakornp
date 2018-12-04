package cs601.project4.service;

import java.util.logging.Level;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.database.DatabaseManager;
import cs601.project4.servlet.UserServlet;

public class EventService {
	public static void main(String args[]) {
    	TicketPurchaseApplicationLogger.initialize(UserService.class.getName(), "EventServiceLog.txt");
    	DatabaseManager.getInstance();
    	// Create a jetty server object that will listen on port 8081.
        Server server = new Server(8081);
        // Create context handler and mount it to the server
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        // Pass in the class for the Servlet to instantiate an instance of that Servlet and mount it on a given context path
        handler.addServlet(UserServlet.class, "/*");
        try {
			server.start();
			server.join(); // wait for the thread to die
		} catch (Exception e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "There's error from the server", 1);
		}
    }
}
