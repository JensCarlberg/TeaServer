package se.liu.jenca01.teserver.servlets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.liu.jenca01.teserver.BrewedTeas;

/**
 * Servlet implementation class Admin
 */
@WebServlet("/Admin")
public final class Admin extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Admin() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String submit = reqValues(request, new String[] {"clear", "save", "reload", "reloadNewFormat", "remove", "get", "getNewFormat" });
		switch (submit) {
		case "clear":
			brewedTeas().clearTeas();
			setAdminMessage(request, "Listan med téer tömd.");
			break;
		case "save":
			brewedTeas().clearTeas();
			brewedTeas().removeLogFile();
			String parameter = request.getParameter("all-teas");
			String[] split = parameter.split("\n");
			AddTea.reloadTeasFromArray(split);
			setAdminMessage(request, "Sparat.");
			response.sendRedirect("edit.jsp");
			return;
		case "reload":
			brewedTeas().reloadTeas();
			setAdminMessage(request, "Filen med téer omladdad.");
			break;
		case "reloadNewFormat":
			brewedTeas().clearTeas();
			brewedTeas().removeLogFile();
			AddTea.reloadTeas("Teas.log");
			setAdminMessage(request, "Filen med téerna i fullformat omladdad.");
			break;
		case "remove":
			brewedTeas().removeLogFile();
			setAdminMessage(request, "Filen med téer borttagen.");
			break;
		case "get":
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			brewedTeas().copyLogFileToStream(response.getOutputStream());
			response.flushBuffer();
			return;
		case "getNewFormat":
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			AddTea.copyLogFileToStream(response.getOutputStream());
			response.flushBuffer();
			return;
		default:
			System.err.println("Unknown request");
		}
		response.sendRedirect("admin.jsp");
	}

	private BrewedTeas brewedTeas() {
		return BrewedTeas.instance();
	}

	public void setAdminMessage(HttpServletRequest request, String msg) {
		request.getSession().setAttribute("admin_message", msg);
	}

	public String reqValues(HttpServletRequest request, String[] names) {
		StringBuffer result = new StringBuffer();
		for (String name : names)
			result.append(reqValue(request, name));
		return result.toString();
	}

	public String reqValue(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value == null) return "";
		return name;
	}

}
