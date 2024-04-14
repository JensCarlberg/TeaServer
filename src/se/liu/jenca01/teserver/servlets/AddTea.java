package se.liu.jenca01.teserver.servlets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.liu.jenca01.teserver.BrewedTeas;
import se.liu.jenca01.teserver.Tea;

/**
 * Servlet implementation class AddTea
 */
@WebServlet(description = "Add a tea from the TeaTimer app", urlPatterns = { "/AddTea" }, loadOnStartup=1)
public class AddTea extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static File logFile;
	private static String lineSeparator;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddTea() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	getLogFile();
    	System.out.println("AddTea initiated.");
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Tea tea = new Tea.Builder().fromRequest(request).build();
			BrewedTeas.instance().addTea(tea);
			sendResult(response, HttpServletResponse.SC_OK);
			logTea(tea);
		} catch (InstantiationException e) {
			e.printStackTrace();
			sendResult(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	private void sendResult(HttpServletResponse response, int resultCode) throws IOException {
		sendResult(response, resultCode, null);
	}

	private void sendResult(HttpServletResponse response, int resultCode, String msg) throws IOException {
		response.setStatus(resultCode);
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		writer.println(String.format("Time: %s", System.currentTimeMillis()));
		if (msg != null && msg.length() !=  0)
			writer.println(String.format("Message: %s", msg));
		writer.flush();
	}

    private synchronized void logTea(Tea tea) {
    	logTeaToFile(tea, getLogFile());
    }

    private static synchronized void logTeaToFile(Tea tea, File file) {
    	OutputStream outStream = null;
    	Writer writer = null;
        try {
        	outStream = new FileOutputStream(file, true);
        	writer = new OutputStreamWriter(outStream, UTF_8);
            writer.write(tea.toString());
            writer.write(lineSeparator());
        } catch (IOException e) {
            System.err.println("Could not write tea to log file");
            e.printStackTrace();
        } finally {
        	if (writer != null) try { writer.close(); } catch (Exception ignore) { }
        	if (outStream != null) try { outStream.close(); } catch (Exception ignore) { }
        }
    }

	private static String lineSeparator() {
		if (lineSeparator == null)
			lineSeparator = System.getProperty("line.separator");
		return lineSeparator;
	}

	public synchronized File getLogFile() {
		if (logFile == null) {
			ServletContext servletContext = getServletContext();
            String realPath = servletContext.getRealPath("/brewed-teas-full.log");
			System.out.println("Full log file at " + realPath);
			logFile = new File(realPath);
		}
		return logFile;
	}

	private static synchronized List<String> getAllTeasFromLogFile() {
		try {
			return Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public static String getAllTeas() {
		List<String> allTeasFromLogFile = getAllTeasFromLogFile();
		String join = String.join("\n", allTeasFromLogFile);
		return join;
	}
	
	public static void removeLogFile() {
		if (logFile == null) return;
		try {
			File newLogFile = new File(logFile.getPath());
			logFile.renameTo(new File(logFile.getParent(), "brewed-teas-full." + getTimestamp() + ".log"));
			logFile = newLogFile;
		} catch (Exception e) {
			System.err.println("Could not rename full tea file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static String getTimestamp() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss_S").format(new Date());
	}

	public static void copyLogFileToStream(ServletOutputStream out) {
        try (FileInputStream in = new FileInputStream(logFile)) {
            byte[] buffer = new byte[16384];
            int read = 0;
            while ((read = in.read(buffer)) > 0)
                out.write(buffer, 0, read);
        } catch (Exception e) {
        	System.err.println("Failed sending full tea file");
            e.printStackTrace();
        }
	}

	public static void reloadTeas(String fileName) {
		try {
			BrewedTeas brewedTeas = BrewedTeas.instance();
			File reloadFrom = new File(brewedTeas.getlogFileDir(), fileName);
			InputStream fis = new FileInputStream(reloadFrom);
			reloadTeasFromStream(brewedTeas, fis);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void reloadTeasFromStream(BrewedTeas brewedTeas, InputStream fis) throws IOException {
		removeLogFile();
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			Tea tea = Tea.parse(line);
			if (tea != null) {
				brewedTeas.addTea(tea);
				logTeaToFile(tea, logFile);
			}
		}
	}

	public static void reloadTeasFromArray(String[] teas) {
		BrewedTeas brewedTeas = BrewedTeas.instance();
		for (String line: teas) {
			String trimmedLine = line.trim();
			Tea tea = Tea.parse(trimmedLine);
			if (tea != null) {
				brewedTeas.addTea(tea);
				logTeaToFile(tea, logFile);
			}
		}
	}
}
