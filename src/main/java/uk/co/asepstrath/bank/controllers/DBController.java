package uk.co.asepstrath.bank.controllers;

import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.util.Transaction;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class communicates directly with the database, providing methods to access the data in the various tables
 */
public class DBController {

    /**
     * This is the link to the database
     */
    private static DataSource dataSource;

    /**
     * Creates an instance of the DBController
     * @param ds
     */
    public DBController(DataSource ds) {
        dataSource = ds;
    }

    /**
     * Creates the four database tables: Accounts, Users, Session, Transaction
     * @throws SQLException
     */
    public void createTables() throws SQLException{
        // Open Connection to DB
        Connection connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();

        stmt.executeUpdate(
                "CREATE TABLE `Users`"
                        + "("
                        + "`Id` UUID PRIMARY KEY UNIQUE, "
                        + "`Name` varchar(30),"
                        + "`Email` varchar(50),"
                        + "`Hash_pass` varchar(32)," // use md5 for hashing
                        + "PRIMARY KEY (`Id`)"
                        + ")");
        stmt.executeUpdate(
                "CREATE TABLE `Session`"
                        + "("
                        + "`Id` UUID PRIMARY KEY UNIQUE, "
                        + "`User_id` UUID," // This can be Null.
                        + "`Expiry` timestamp,"
                        + "PRIMARY KEY (`Id`),"
                        + "FOREIGN KEY (`User_id`) REFERENCES Users(`Id`)"
                        + ")");

        stmt.executeUpdate(
                "CREATE TABLE `Accounts` "
                        + "("
                        + "`Id` UUID PRIMARY KEY UNIQUE,"
                        + "`User_id` UUID,"
                        + "`AccountNumber` varchar(8),"
                        + "`SortCode` varchar(8),"
                        + "`Name` varchar(255),"
                        + "`AccountBalance` decimal(15,2),"
                        + "PRIMARY KEY (`Id`),"
                        + "FOREIGN KEY (`User_id`) REFERENCES Users(`Id`)"
                        + ")");
        stmt.executeUpdate(
                "CREATE TABLE `Transcation` "
                        + "("
                        + "`Timestamp` timestamp,"
                        + "`Amount` decimal(15,2),"
                        + "`Ref` varchar(30),"
                        + "`Category` varchar(255),"
                        + "`Status` varchar(30),"
                        + "`Type` varchar(30),"
                        + "`Id` UUID PRIMARY KEY UNIQUE,"
                        + "`Recipient` varchar(255),"
                        + "`Sender` varchar(255),"
                        + "PRIMARY KEY ( `Id` ),"
                        + "FOREIGN KEY ( `Recipient` ) REFERENCES Accounts(`Id`),"
                        + "FOREIGN KEY ( `Sender` ) REFERENCES Accounts(`Id`)"
                        + ")");
    }

    /**
     * Adds a transaction to the Transactions table in the database
     * @param ts
     * @throws SQLException
     */
    public void addTransaction(Transaction ts) throws SQLException {
        PreparedStatement prepStmt;
        Connection connection = dataSource.getConnection();
        prepStmt = connection.prepareStatement(
                "INSERT into Transaction (TIME,AMOUNT,REFERENCE,CATEGORY,STATUS,ID,RECIPIENT,SENDER)"
                        + "values (?, ?, ?, ?, ?, ?, ?, ?)");

        prepStmt.setTimestamp(1, ts.time);
        prepStmt.setDouble(2, ts.amount.doubleValue());
        prepStmt.setString(3, ts.reference);
        prepStmt.setString(4, ts.category.toString());
        prepStmt.setString(5, ts.status.toString());
        prepStmt.setObject(6, ts.id);
        prepStmt.setObject(7, ts.recipient.getUUID());
        prepStmt.setObject(8, ts.sender.getUUID());

        // SEND IT!
        prepStmt.execute();
    }

    /**
     * Populates the Accounts table in the database with the list provided
     * @param accounts
     * @throws SQLException
     */
    public void addAccounts(List<Account> accounts) throws SQLException{
        for (Account acc : accounts) {
            this.addAccount(acc);
        }
    }

    public void addAccount(Account acc) throws SQLException{
        Connection connection = dataSource.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(
                String.format("INSERT INTO Accounts " + "VALUES (?, NULL, '%s', '%s' ,'%s', '%f')",
                        acc.getAccountNumber(),acc.getSortCode(), acc.getName(), acc.getBalance().floatValue()));
        prepStmt.setObject(1, acc.getUUID());
        prepStmt.execute();
    }

    /**
     * Returns a List containing every Account in the Accounts table in the database
     * @throws SQLException
     */
    public List<Account> returnAllAccounts() throws SQLException{
        Connection connection = dataSource.getConnection();
        // Create Statement (batch of SQL Commands)
        Statement statement = connection.createStatement();
        // Perform SQL Query
        ResultSet set = statement.executeQuery("SELECT * FROM `Accounts`");

        List<Account> accounts = new ArrayList<>();

        while (set.next()) {
            accounts.add(new Account(set.getString("Name"), set.getString("AccountNumber"), set.getString("SortCode"), set.getBigDecimal("AccountBalance")));
        }

        return accounts;
    }

    /**
     * Returns the account with the specified name
     * @param name
     * @throws SQLException
     */
    public Account returnAccount(String name) throws SQLException{
        Connection connection = dataSource.getConnection();
        // Create Statement (batch of SQL Commands)
        Statement statement = connection.createStatement();
        // Perform SQL Query
        ResultSet set =
                statement.executeQuery("SELECT * FROM `Accounts` WHERE name='%s'".formatted(name));

        if (!set.next()) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
        }

        Account account = new Account(set.getString("Name"), set.getBigDecimal("AccountBalance"));

        return account;

    }
}
