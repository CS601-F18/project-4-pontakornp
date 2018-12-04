package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class EventServlet extends HttpServlet{
	/**
	 * do GET operation according to specified path
	 * GET /list
	 * GET /{eventid}
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		if(pathParts.length == 2 && pathParts[1] == "list") {
			//createEvent
		} else if(pathParts.length == 2 && StringUtils.isNumeric(pathParts[1])) {
			int eventId = Integer.parseInt(pathInfo.substring(1));
			//getEventDetails
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
	
	/**
	 * do POST operation according to specified path
	 * POST /create
	 * POST /purchase/{eventid}
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		if (!BaseServlet.isPageFound(request, response)) {
			return;
		}
		response.setContentType("application/json; charset=utf-8");
		String pathInfo = request.getPathInfo().trim();
		String[] pathParts = pathInfo.split("/");
		
		if(pathParts.length == 2 && pathParts[1] == "create") {
			
		} else if(pathParts.length == 3 && pathParts[1] == "purchase" && StringUtils.isNumeric(pathParts[2])) {
			
		} else {
			BaseServlet.sendPageNotFoundResponse(response, "Page not found");
		}
	}
}
