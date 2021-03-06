package cs601.project4.service;

import java.util.logging.Level;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import cs601.project4.database.DatabaseManager;
import cs601.project4.helper.Config;
import cs601.project4.helper.TicketManagementApplicationLogger;
import cs601.project4.servlet.FrontEndServlet;

/**
 * 
 * @author pontakornp
 *
 *
 * Main class to run Frontend Service
 */
public class FrontEndService {
    public static void main(String args[]) {
    	TicketManagementApplicationLogger.initialize(FrontEndService.class.getName(), "FrontEndServiceLog.txt");
    	DatabaseManager.getInstance();
    	Config config = new Config();
    	config.setVariables();
    	int port = config.getFrontEndPort();
    	// Create a basic jetty server object that will listen on port specified in config file.
        Server server = new Server(port);
        // Create context handler and mount it to the server
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        // Pass in the class for the Servlet to instantiate an instance of that Servlet and mount it on a given context path
        handler.addServlet(FrontEndServlet.class, "/*");
        // Start the server
        try {
			server.start();
			server.join(); // wait for the thread to die
		} catch (Exception e) {
			TicketManagementApplicationLogger.write(Level.INFO, "There's error from the server", 0);
		}
    }
}
