package se.liu.jenca01.teserver.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.liu.jenca01.teserver.BrewedTeas;

/**
 * Servlet implementation class TeaBrewed
 */
@WebServlet(description = "Store  timestamp for a tea brew", urlPatterns = { "/Testart" }, loadOnStartup = 1)
public final class TeaBrewed extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private File logFile;
	BrewedTeas brewedTeas = BrewedTeas.instance();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public TeaBrewed() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		brewedTeas.setLogFile(getLogFile());
		if (getLogFile().exists())
			new Thread(new Runnable() {
				@Override
				public void run() {
					brewedTeas.load();
				}
			}).start();
	}

	private synchronized File getLogFile() {
		if (logFile == null) {
			ServletContext servletContext = getServletContext();
			String realPath = servletContext.getRealPath("brewed-teas.log");
			System.out.println("Log file at " + realPath);
			logFile = new File(realPath);
		}
		return logFile;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String tea = request.getParameter("Te");
		String amount = request.getParameter("Volym");
        brewedTeas.addTea(tea, getVolume(amount));
		response.sendRedirect("showLatestTeas.jsp");
	}

	private double getVolume(String amount) {
		if (amount == null || "".equals(amount)) return 3;
		try {
			return Double.parseDouble(amount);
		} catch (Exception ignore) {
			return 3;
		}
	}
}
