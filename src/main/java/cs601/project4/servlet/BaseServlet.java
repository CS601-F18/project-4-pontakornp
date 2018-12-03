package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs601.project4.TicketPurchaseApplicationLogger;

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
			TicketPurchaseApplicationLogger.write(Level.WARNING, "Cannot write to client", 1);
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
