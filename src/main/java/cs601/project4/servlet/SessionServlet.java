package cs601.project4.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		HttpSession session = request.getSession();		
		String visits = (String) session.getAttribute("visits");
		System.out.println(visits);
		
		int intVisits = 1;
		if(visits != null) {
			intVisits = Integer.parseInt(visits);
			intVisits++;
		}
		
		session.setAttribute("visits", String.valueOf(intVisits));
		
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.println("<html><title>SessionServlet</title><body>There have been " + 
							intVisits + " visits.</body></html>");
	}
}