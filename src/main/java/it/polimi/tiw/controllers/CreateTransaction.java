package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateTransaction")

public class CreateTransaction extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public CreateTransaction() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		
		User user = (User)session.getAttribute("user");

		if (session.isNew() || user == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}
		
		String err = null;
		
		// Get and parse all parameters from request
		Integer idOrigin = null;
		String userDestination = null;
		Integer idDestination = null;
		Double amount = null;
		String description = null;
		Date date = null;
		
		try {
			idOrigin = Integer.parseInt(request.getParameter("idAccount"));
			userDestination = StringEscapeUtils.escapeJava(request.getParameter("userDestination"));
			idDestination = Integer.parseInt(request.getParameter("idDestination"));
			amount = Double.parseDouble(request.getParameter("amount"));
			description = StringEscapeUtils.escapeJava(request.getParameter("description"));
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			err = "Parameters are not correct";
		}
		
		if (userDestination == null || idDestination == null || amount == null || description == null ){
			err = "Parameters are null";
		}
		
		AccountDAO accountDAO = new AccountDAO(connection);
		
		Account destAcc = null;
		Account origAcc = null;
		
		try {
			destAcc = accountDAO.findAccountById(idDestination);
			origAcc = accountDAO.findAccountById(idOrigin);
		} catch (SQLException e1) {
			e1.printStackTrace();
			err = "Cannot connect to database";
		}
		
		//check new transaction error
		if (destAcc == null) {
        	err = "Destination account does not exist";			
        }
        else if (idOrigin == idDestination) {
        	err = "Destination account must be different from origin account!";
        }
        else if (!(destAcc.getUser().equals(userDestination))) {
        	err = "Destination account or user are not correct";
        }
        else if(!(origAcc.getUser().equals(user.getUsername()))) {
        	err = "Source account does not belong to the current user";
		}
        else if (amount <= 0) 	{
        	err = "Amount is negative!";
        }
        else if (amount > origAcc.getBalance()) {
        	err = "Insufficient balance!";
        }
	
        String path;
        ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		    
		double oldBalanceOrig = 0;
        double oldBalanceDest = 0;
        double newBalanceOrig = 0;
        double newBalanceDest = 0;
        
        if (err == null) {
        	oldBalanceOrig = origAcc.getBalance();
            oldBalanceDest = destAcc.getBalance();
        
        	//no errors, create transaction in db and redirect to confirmation
    		TransactionDAO transactionDAO = new TransactionDAO(connection);
    		try { 
    			transactionDAO.createTransaction(idOrigin, userDestination, idDestination, amount, description, date);
    		
    			newBalanceOrig = accountDAO.findAccountById(idOrigin).getBalance();
    				newBalanceDest = accountDAO.findAccountById(idDestination).getBalance();
    			    		
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    		
    		
    		path = "WEB-INF/TransactionConfirmation.html";
    		
    		ctx.setVariable("origAcc", origAcc);
            ctx.setVariable("destAcc", destAcc);
            ctx.setVariable("oldBalanceOrig", oldBalanceOrig);
            ctx.setVariable("oldBalanceDest", oldBalanceDest);
            ctx.setVariable("newBalanceOrig", newBalanceOrig);
            ctx.setVariable("newBalanceDest", newBalanceDest);
            ctx.setVariable("amount", amount);
            
        }else {
        	//error, redirect to failed page
        	path = "WEB-INF/FailedTransaction.html";
            ctx.setVariable("err", err);
            ctx.setVariable("origAcc", origAcc);
        }

        templateEngine.process(path, ctx, response.getWriter());
    }

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}