package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.project4.TicketManagementApplicationLogger;

/**
 * 
 * @author pontakornp
 *
 *
 * Base servlet class provides common method that each service can call
 * i.e. send responses such as page not found, bad request, and custom responses
 * it can also check if page is found or not
 */
public class BaseServlet extends HttpServlet {
	/**
	 * send response
	 * @param response
	 * @param body
	 */
	public static void sendResponse(HttpServletResponse response, String body) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.write(body);
		} catch (IOException e) {
			TicketManagementApplicationLogger.write(Level.WARNING, "Cannot write to client", 1);
		}
	}
	
	/**
	 * GET and POST helper method
	 * @param request
	 * @param response
	 * @return
	 */
	public static boolean isPageFound(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null || pathInfo.length() <= 1) {
			sendPageNotFoundResponse(response, "Page not found");
			return false;
		}
		return true;
	}
	
	/**
	 * send page not found response
	 * @param response
	 * @param body
	 */
	public static void sendPageNotFoundResponse(HttpServletResponse response,String body) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		sendResponse(response, body);
	}
	
	/**
	 * send bad request response
	 * @param response
	 * @param body
	 */
	public static void sendBadRequestResponse(HttpServletResponse response,String body) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		sendResponse(response, body);
	}
}
