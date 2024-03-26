package it.polimi.tiw.dao;

import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User checkLogin(String username, String password) throws SQLException {
        String query = "SELECT  username, name, surname FROM user  WHERE username = ? AND password =?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, username);
            pstatement.setString(2, password);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) 
                    return null;
                else {
                    result.next();
                    User user = new User();
                    user.setUsername(result.getString("username"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    return user;
                }
            }
        }
    }
    
    public User existingUser(String username) throws SQLException {
        String query = "SELECT  username FROM user  WHERE username = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) 
                    return null;
                else {
                    result.next();
                    return this.getUserByUsername(result.getString("username"));
                }
            }
        }
    }
    
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT  username, name, surname FROM user  WHERE username = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) 
                    return null;
                else {
                    result.next();
                    User user = new User();
                    user.setUsername(result.getString("username"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    return user;
                }
            }
        }
    }   
    
    
    public User newUser(String name, String surname, String username, String email, String pwd)//int idOrigin, String userDestination, int idDestination, double amount, String description, java.util.Date date)
            throws SQLException {
    	     
    	String insert = "INSERT into user (username, name, surname, email, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement newuser = connection.prepareStatement(insert)){;
          
            connection.setAutoCommit(false);

        	newuser.setString(1, username);
        	newuser.setString(2, name);
        	newuser.setString(3, surname);
        	newuser.setString(4, email);
        	newuser.setString(5, pwd);
        	newuser.executeUpdate();
        	
            connection.commit();
            return this.getUserByUsername(username); 
        }
    }
    
}



