package se.liu.jenca01.teserver.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import se.liu.jenca01.teserver.BrewedTeas;

/**
 * Servlet implementation class GetUsedTeaNames
 */
@WebServlet(description = "Get a list of all already used tea names", urlPatterns = { "/GetUsedTeaNames" }, loadOnStartup=1)
public class GetUsedTeaNames extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetUsedTeaNames() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	System.out.println("GetUsedTeaNames initiated.");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonBrewedTeaNames = BrewedTeas.instance().jsonBrewedTeaNames();
		sendResult(response, HttpServletResponse.SC_OK, jsonBrewedTeaNames.toJSONString());
	}

	private void sendResult(HttpServletResponse response, int resultCode, String msg) throws IOException {
		response.setStatus(resultCode);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.println(msg);
		writer.flush();
	}
}
