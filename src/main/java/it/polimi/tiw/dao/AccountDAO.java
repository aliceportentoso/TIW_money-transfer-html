package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }


    public List<Account> findAccountsByUser(String user) throws SQLException {

        List<Account> accounts = new ArrayList<>();

        String query = "SELECT * from account where user = ? ORDER BY balance DESC";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, user);
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Account account = new Account();//Create java Bean
                    account.setUser(result.getString("user"));
                    account.setIdAccount(result.getInt("idAccount"));
                    account.setBalance((result.getDouble("balance")));
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }


    public Account findAccountById(int idAccount) throws SQLException {

        Account account; //Create java Bean

        String query = "SELECT * from account where idAccount = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, idAccount);            
            try (ResultSet result = pstatement.executeQuery()) {
            	result.next();
                account = new Account();
                account.setUser(result.getString("user"));
                account.setIdAccount(result.getInt("idAccount"));
                account.setBalance(result.getDouble("balance"));
            } catch (SQLException e) {
                System.out.println("invalid account");
                return null;
            }
        }
        
        return account;

    }
}
