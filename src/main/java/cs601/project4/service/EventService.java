package cs601.project4.service;

import java.util.logging.Level;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import cs601.project4.Config;
import cs601.project4.DatabaseManager;
import cs601.project4.TicketPurchaseApplicationLogger;
import cs601.project4.servlet.EventServlet;

public class EventService {
	public static void main(String args[]) {
    	TicketPurchaseApplicationLogger.initialize(UserService.class.getName(), "EventServiceLog.txt");
    	DatabaseManager.getInstance();
    	Config config = new Config();
    	config.setVariables();
    	int port = config.getEventPort();
    	// Create a jetty server object that will listen on port specified in config file
        Server server = new Server(port);
        // Create context handler and mount it to the server
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        // Pass in the class for the Servlet to instantiate an instance of that Servlet and mount it on a given context path
        handler.addServlet(EventServlet.class, "/*");
        try {
			server.start();
			server.join(); // wait for the thread to die
		} catch (Exception e) {
			TicketPurchaseApplicationLogger.write(Level.WARNING, "There's error from the server", 1);
		}
    }
}
