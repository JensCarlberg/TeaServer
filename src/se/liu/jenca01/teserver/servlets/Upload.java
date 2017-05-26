package se.liu.jenca01.teserver.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import se.liu.jenca01.teserver.BrewedTeas;

@WebServlet("/Upload")
@MultipartConfig
public final class Upload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    Part teas = request.getPart("teas");
        brewedTeas().clearTeas();
        AddTea.reloadTeasFromStream(brewedTeas(), teas.getInputStream());
        setAdminMessage(request, "Reloaded file");
	    response.sendRedirect("admin.jsp");
	}

	private BrewedTeas brewedTeas() {
		return BrewedTeas.instance();
	}

    public void setAdminMessage(HttpServletRequest request, String msg) {
        request.getSession().setAttribute("admin_message", msg);
    }
}
