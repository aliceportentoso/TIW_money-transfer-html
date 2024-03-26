package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckSignUp")
public class CheckSignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckSignUp() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// obtain and escape params
		String name = null;
		String surname = null;
		String username = null;
		String email = null;
		String pwd = null;
		String pwd2 = null;
		
		String err = null;
		
		try {
			name =  StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			pwd2 = StringEscapeUtils.escapeJava(request.getParameter("pwd2"));
							
			if (name == null || surname == null || username == null || email == null || pwd == null || pwd2 == null
				|| name.isEmpty() || surname.isEmpty() || username.isEmpty()
				|| email.isEmpty() || pwd.isEmpty() || pwd2.isEmpty())
				throw new Exception("Missing or empty credential value");	
			
			} catch (Exception e) {
			// for debugging only 
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
			}
		
		// check db username
		User user = new User();
		UserDAO userDAO = new UserDAO(connection);
		try {
			user = userDAO.existingUser(username);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to check username");
			return;
		}
		
		if (user!= null)
			err = "This username is token, choose an other one.";
		else if (!(pwd.equals(pwd2)))
			err = "Password and Repeat Password fields must be equal.";

	
		//se esiste errore: già un username uguale, no correttezza email, password diverse
		String path;
		
		if (err != null) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("err2", err);
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
		} else {
			
			try {
				user = userDAO.newUser(name, surname, username, email, pwd);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to create new user in db");
				return;
			}
			
			request.getSession().setAttribute("user", user);
			path = getServletContext().getContextPath() + "/Home";
			response.sendRedirect(path);
		}

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}