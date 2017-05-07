package se.liu.jenca01.teserver.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import se.liu.jenca01.teserver.BrewedTeas;

/**
 * Servlet implementation class JsonData
 */
@WebServlet("/presentation")
public class JsonData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			writeJsonData(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException();
		}
		response.flushBuffer();
	}

	private void writeJsonData(ServletOutputStream outputStream) throws IOException {
		JSONObject obj = BrewedTeas.instance().jsonSumTeaDay();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		obj.writeJSONString(writer);
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
