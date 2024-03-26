package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Transaction> findTransactionsByAccount(int idAccount) throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String query = "SELECT idTransaction, amount, date, idOrigin, idDestination, description, A1.user, A2.user " + 
                       "FROM transaction JOIN account A1 ON idOrigin 		= A1.idAccount " +
                       					"JOIN account A2 ON idDestination 	= A2.idAccount " +
                       "WHERE idOrigin = ? OR idDestination = ? ORDER BY date DESC";
        
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, idAccount);
            pstatement.setInt(2, idAccount);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    Transaction transaction = new Transaction();//Create java Bean
                    transaction.setIdTransaction(result.getInt("idTransaction"));
                    transaction.setDate(result.getDate("date"));
                    transaction.setAmount(result.getDouble("amount"));
                    transaction.setIdOrigin(result.getInt("idOrigin"));
                    transaction.setIdDestination(result.getInt("idDestination"));
                    transaction.setDescription(result.getString("description"));
                    transaction.setUserOrigin(result.getString("A1.user"));
                    transaction.setUserDestination(result.getString("A2.user"));
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }


    public void createTransaction(int idOrigin, String userDestination, int idDestination, double amount, String description, java.util.Date date)
            throws SQLException {
    	 System.out.println("Transaction you want to create - " + "idOrigin: " + idOrigin + 
    			 "userDestination: " + userDestination + "idDestination: " + idDestination + 
    			 "amount: " + amount + "description: " + description + "date: " + date);
     
        String transQuery = "INSERT into transaction (idTransaction, date, amount, idOrigin, idDestination, description) VALUES(NULL, NOW(), ?, ?, ?, ?)";
        String origQuery = "UPDATE account SET balance = balance - ? WHERE idAccount = ? AND balance >= 0";
        String destQuery = "UPDATE account SET balance = balance + ? WHERE idAccount = ? AND balance >= 0";

        try (PreparedStatement insertTransaction = connection.prepareStatement(transQuery);
             PreparedStatement updateOrigBalance = connection.prepareStatement(origQuery);
             PreparedStatement updateDestBalance = connection.prepareStatement(destQuery)) {

            connection.setAutoCommit(false);
            insertTransaction.setDouble(1, amount);
            insertTransaction.setInt(2, idOrigin);
            insertTransaction.setInt(3, idDestination);
            insertTransaction.setString(4, description);
            updateOrigBalance.setDouble(1, amount);
            updateOrigBalance.setInt(2, idOrigin);
            updateDestBalance.setDouble(1, amount);
            updateDestBalance.setInt(2, idDestination);
            updateOrigBalance.executeUpdate();
            updateDestBalance.executeUpdate();
            insertTransaction.executeUpdate();

            connection.commit();
            
        }
    }
}

