package uk.co.asepstrath.bank.controllers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.User;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Transaction;

/**
 * This class communicates directly with the database, providing methods to
 * access the data in the various tables
 */
public class DBController {

  /**
   * This is the link to the database
   */
  private static DataSource dataSource;

  /**
   * Creates an instance of the DBController
   * 
   * @param ds
   */
  public DBController(DataSource ds) {
    dataSource = ds;
  }

  /**
   * Creates the four database tables: Accounts, Users, Session, Transaction
   * 
   * @throws SQLException
   */
  public void createTables() throws SQLException {
    // Open Connection to DB
    Connection connection = dataSource.getConnection();
    Statement stmt = connection.createStatement();

    stmt.executeUpdate(
        "CREATE TABLE `Users`"
            + "("
            + "`Id` UUID PRIMARY KEY UNIQUE, "
            + "`Name` varchar(30),"
            + "`Email` varchar(50),"

            + "`Hash_pass` char(128)," // Using SHA-512 for encryption
            + "`phoneNo` varchar(12),"
            + "`address` varchar(50),"

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
   * 
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
   * 
   * @param accounts
   * @throws SQLException
   */
  public void addAccounts(List<Account> accounts) throws SQLException {
    for (Account acc : accounts) {
      this.addAccount(acc);
    }
  }

  public void addAccount(Account acc) throws SQLException {
    Connection connection = dataSource.getConnection();
    PreparedStatement prepStmt = connection.prepareStatement(
        String.format("INSERT INTO Accounts " + "VALUES (?, ?, '%s','%s', '%f')",
            acc.getAccountNumber(), acc.getSortCode(), acc.getBalance().floatValue()));
    prepStmt.setObject(1, acc.getUUID());
    prepStmt.setObject(2, acc.getUser_id());
    prepStmt.execute();
  }

  /**
   * Returns a List containing every Account in the Accounts table in the database
   * 
   * @throws SQLException
   */
  public List<Account> returnAllAccounts() throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery("SELECT * FROM `Accounts`");

    List<Account> accounts = new ArrayList<>();

    while (set.next()) {
      accounts.add(new Account(set.getString("AccountNumber"), set.getString("SortCode"),
          set.getBigDecimal("AccountBalance")));
    }

    return accounts;
  }

  /**
   * Returns the account with the specified name
   * 
   * @param name
   * @throws SQLException
   */
  public Account returnAccount(String name) throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery("SELECT * FROM `Accounts` WHERE name='%s'".formatted(name));
    if (!set.next()) {
      throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
    }

    Account account = new Account(set.getString("AccountNumber"), set.getString("SortCode"),
        set.getBigDecimal("AccountBalance"));

    return account;

  }

  public Account returnAccount(String accountNumber, String sortCode) throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery(
        "SELECT * FROM `Accounts` WHERE AccountNumber='%s' and SortCode='%s'".formatted(accountNumber, sortCode));

    if (!set.next()) {
      throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
    }

    Account account = new Account(set.getString("AccountNumber"), set.getString("SortCode"),
        set.getBigDecimal("AccountBalance"));

    return account;
  }

  public Account returnAccount(UUID userID) throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery("SELECT * FROM `Accounts` WHERE User_id='%s'".formatted(userID.toString()));

    if (!set.next()) {
      throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
    }

    Account account = new Account(userID, set.getString("AccountNumber"),
        set.getString("SortCode"), set.getBigDecimal("AccountBalance"), Boolean.FALSE, AccountCategory.Payment);

    return account;
  }

  /**
   * Returns the user details
   * 
   * @param user
   * @throws SQLException
   */
  public UUID loginUser(User user) throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery("SELECT * FROM `Users` WHERE email='%s' and Hash_pass='%s'"
        .formatted(user.getEmail(), user.getPasswordHash()));

    if (!set.next()) {
      throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
    }

    String userId = set.getString("Id");
    // Account account = new Account(set.getString("Name"),
    // set.getBigDecimal("AccountBalance"));

    return UUID.fromString(userId);
  }

  public void addUser(User user) throws SQLException {
    Connection connection = dataSource.getConnection();
    PreparedStatement prepStmt = connection.prepareStatement(
        String.format("INSERT INTO Users " + "VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
            user.getId(), user.getName(), user.getEmail(), user.getPasswordHash(), user.getPhoneNo(),
            user.getAddress()));
    prepStmt.execute();
  }

  public User returnUser(UUID userID) throws SQLException {
    Connection connection = dataSource.getConnection();
    // Create Statement (batch of SQL Commands)
    Statement statement = connection.createStatement();
    // Perform SQL Query
    ResultSet set = statement.executeQuery("SELECT * FROM `Users` WHERE Id='%s'".formatted(userID.toString()));

    if (!set.next()) {
      throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
    }

    User user = new User(userID, set.getString("Email"), set.getString("Hash_Pass"), set.getString("Name"),
        set.getString("phoneNo"), set.getString("address"));

    return user;
  }

  public String getSha512Hash(String password) {
    try {

      // Static getInstance method is called with hashing SHA-512
      MessageDigest md = MessageDigest.getInstance("SHA-512");

      // digest() method is called to calculate message digest
      // of an input digest() return array of byte
      byte[] messageDigest = md.digest(password.getBytes());

      // Convert byte array into signum representation
      BigInteger no = new BigInteger(1, messageDigest);

      // Convert message digest into hex value
      StringBuilder hashtext = new StringBuilder(no.toString(16));
      while (hashtext.length() < 32) {
        hashtext.insert(0, "0");
      }
      return hashtext.toString();
    }

    // For specifying wrong message digest algorithms
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
