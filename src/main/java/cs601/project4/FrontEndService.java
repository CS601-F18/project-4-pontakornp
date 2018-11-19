package cs601.project4;

import java.util.logging.Level;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import cs601.project4.servlet.EventServlet;
import cs601.project4.servlet.FrontEndServlet;
import cs601.project4.servlet.SessionServlet;
import cs601.project4.servlet.UserServlet;

public class FrontEndService {
    public static void main(String args[]) {
    	TicketPurchaseApplicationLogger.initialize(FrontEndService.class.getName(), "frontEndServiceLog.txt");
    	// Create a basic jetty server object that will listen on port 8080.
        Server server = new Server(8080);
        // Create context handler and mount it to the server
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        // Pass in the class for the Servlet to instantiate an instance of that Servlet and mount it on a given context path
        
        // how to handle many paths using same class??????
        
//        handler.addServlet(new ServletHolder(SessionServlet.class), "/");
//        handler.addServlet(SessionServlet.class, "/session");
        handler.addServlet(FrontEndServlet.class, "/events/*");
        handler.addServlet(FrontEndServlet.class, "/users/*");
        // Start the server
        try {
			server.start();
			server.join(); // wait for the thread to die
		} catch (Exception e) {
			TicketPurchaseApplicationLogger.write(Level.INFO, "There's error from the server", 0);
		}
    }
}
