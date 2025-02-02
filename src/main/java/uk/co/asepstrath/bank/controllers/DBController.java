package uk.co.asepstrath.bank.controllers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.sql.DataSource;

import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.User;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionCategory;
import uk.co.asepstrath.bank.util.TransactionStatus;

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
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {

            stmt.executeUpdate(
                    "CREATE TABLE `Users`" +
                            "(" +
                            "`Id` UUID PRIMARY KEY UNIQUE, " +
                            "`Name` varchar(30)," +
                            "`Email` varchar(50)," +
                            "`Hash_pass` char(128)," + // Using SHA-512 for encryption
                            "`phoneNo` varchar(12)," +
                            "`address` varchar(50)," +
                            "`IsAdmin` bit," +
                            "PRIMARY KEY (`Id`)" +
                            ")");
            stmt.executeUpdate(
                    "CREATE TABLE `Accounts` " +
                            "(" +
                            "`Id` UUID PRIMARY KEY UNIQUE," +
                            "`User_id` UUID," +
                            "`AccountNumber` varchar(8)," +
                            "`SortCode` varchar(8)," +
                            "`AccountBalance` decimal(15,2)," +
                            "`AccountCategory` varchar(15)," +
                            "`IsForeign` bit," +
                            "PRIMARY KEY (`Id`)," +
                            "FOREIGN KEY (`User_id`) REFERENCES Users(`Id`)" +
                            ")");
            stmt.executeUpdate(
                    "CREATE TABLE `Transaction` " +
                            "(" +
                            "`Timestamp` timestamp," +
                            "`Amount` decimal(15,2)," +
                            "`Ref` varchar(50)," +
                            "`Category` varchar(255)," +
                            "`Status` varchar(30)," +
                            "`Type` varchar(30)," +
                            "`Id` UUID PRIMARY KEY UNIQUE," +
                            "`Recipient` varchar(255)," +
                            "`Sender` varchar(255)," +
                            "PRIMARY KEY ( `Id` )" +
                            ")");
        }
    }

    public List<Transaction> returnTransactions(Account account) throws SQLException {

        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Perform SQL Query
            ResultSet set = statement.executeQuery(
                    "SELECT * FROM Transaction WHERE Recipient='%s' OR Sender='%s'"
                            .formatted(account.getUUID().toString(), account.getUUID().toString()));

            List<Transaction> transactions = new ArrayList<>();
            while (set.next()) {
                Transaction ts = new Transaction();

                ts.time = set.getTimestamp("Timestamp");
                ts.category = TransactionCategory.valueOf(set.getString("Category"));
                ts.id = (UUID) set.getObject("Id");
                ts.amount = BigDecimal.valueOf(set.getDouble("Amount"));
                ts.status = TransactionStatus.valueOf(set.getString("Status"));
                ts.reference = set.getString("Ref");
                try {
                    ts.sender = returnAccount(UUID.fromString(set.getString("Sender")));
                } catch (Exception ignored) {
                }
                try {
                    ts.recipient = returnAccount(UUID.fromString(set.getString("Recipient")));
                } catch (Exception ignored) {
                }

                transactions.add(ts);
            }


            return transactions;
        }
    }

    /**
     * Adds a transaction to the Transactions table in the database
     * 
     * @param ts
     * @throws SQLException
     */
    public void addTransaction(Transaction ts) throws SQLException {
        PreparedStatement prepStmt;
        try (Connection connection = dataSource.getConnection()) {
            prepStmt = connection.prepareStatement(
                    "INSERT into Transaction (timestamp,Amount,Ref,Category,Status,Id,Recipient,Sender)" +
                            "values (?, ?, ?, ?, ?, ?, ?, ?)");
            prepStmt.setTimestamp(1, ts.time);
            prepStmt.setDouble(2, ts.amount.doubleValue());
            prepStmt.setString(3, ts.reference);
            prepStmt.setString(4, ts.category.toString());
            prepStmt.setString(5, ts.status.toString());
            prepStmt.setObject(6, ts.id);

            if(ts.sender == null){
                prepStmt.setObject(7, ts.recipient.getUUID());
                prepStmt.setObject(8, ts.from);
            }
            else if(ts.recipient == null){
                prepStmt.setObject(7, ts.to);
                prepStmt.setObject(8, ts.sender.getUUID());
            }
            else{
                prepStmt.setObject(7, ts.recipient.getUUID());
                prepStmt.setObject(8, ts.sender.getUUID());
            }

            // SEND IT!
            prepStmt.execute();
        }
    }

    public Map<String, Object> getTransactionCategory (Account AccountID) throws SQLException {
        BigDecimal billsCount = BigDecimal.valueOf(0);
        BigDecimal entertainmentCount = BigDecimal.valueOf(0);
        BigDecimal foodCount = BigDecimal.valueOf(0);
        BigDecimal groceryCount = BigDecimal.valueOf(0);
        BigDecimal depositCount = BigDecimal.valueOf(0);
        BigDecimal paymentCount = BigDecimal.valueOf(0);
        BigDecimal transferCount = BigDecimal.valueOf(0);
        BigDecimal withdrawlCount = BigDecimal.valueOf(0);

        List<Transaction> transactions = this.returnTransactions(AccountID);
        for(Transaction transaction: transactions) {
            if (transaction.getCategory() == TransactionCategory.Bills) {
                billsCount = billsCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Entertainment) {
                entertainmentCount = entertainmentCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Food) {
                foodCount = foodCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Grocery) {
                groceryCount = groceryCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Deposit) {
                depositCount = depositCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Payment) {
                paymentCount = paymentCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Withdrawal) {
                withdrawlCount = withdrawlCount.add(transaction.amount);
            } else if (transaction.getCategory() == TransactionCategory.Transfer) {
                transferCount = transferCount.add(transaction.amount);
            }
        }
        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put("Bills", billsCount);
        toReturn.put("Entertainment", entertainmentCount);
        toReturn.put("Food", foodCount);
        toReturn.put("Grocery", groceryCount);
        toReturn.put("Deposit", depositCount);
        toReturn.put("Payment", paymentCount);
        toReturn.put("Withdrawal", withdrawlCount);
        toReturn.put("Transfer", transferCount);

        return toReturn;
    }

    public void addAccount(Account acc) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(
                    String.format("INSERT INTO Accounts " + "VALUES (?, ?, '%s','%s', '%f', '%s', %b)",
                            acc.getAccountNumber(), acc.getSortCode(), acc.getBalance().floatValue(),
                            acc.getAccountCategory(), acc.isForeign()));
            prepStmt.setObject(1, acc.getUUID());
            prepStmt.setObject(2, acc.getUser().getId());
            prepStmt.execute();
        }
    }

    /**
     * Returns a List containing every Account in the Accounts table in the database
     * 
     * @throws SQLException
     */
    public List<Account> returnAllAccounts() throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {

            // Perform SQL Query
            ResultSet set = statement.executeQuery("SELECT * FROM `Accounts`");

            List<Account> accounts = new ArrayList<>();

            while (set.next()) {
                User user;
                try {
                    user = returnUser(UUID.fromString(set.getString("User_id")));
                } catch (SQLException e) {
                    throw new SQLException(e.fillInStackTrace());
                }

                accounts.add(new Account(
                        user,
                        UUID.fromString(set.getString("Id")),
                        set.getString("AccountNumber"), set.getString("SortCode"),
                        set.getBigDecimal("AccountBalance"), set.getBoolean("IsForeign"),
                        AccountCategory.valueOf(set.getString("AccountCategory"))));
            }

            return accounts;
        }
    }

    public Account returnAccount(String accountNumber, String sortCode) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            // Perform SQL Query
            ResultSet set = statement.executeQuery(
                    "SELECT * FROM `Accounts` WHERE AccountNumber='%s' and SortCode='%s'".formatted(accountNumber,
                            sortCode));

            if (!set.next()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
            }
            User user;
            try {
                user = returnUser(UUID.fromString(set.getString("User_id")));
            } catch (SQLException e) {
                throw new SQLException(e.fillInStackTrace());
            }

            Account account = new Account(
                    user,
                    UUID.fromString(set.getString("Id")),
                    set.getString("AccountNumber"), set.getString("SortCode"),
                    set.getBigDecimal("AccountBalance"), set.getBoolean("IsForeign"),
                    AccountCategory.valueOf(set.getString("AccountCategory")));

            return account;
        }
    }


    public Account returnAccount(UUID userID) throws SQLException {
        User user = returnUser(userID);
        return this.returnAccount(user);
    }

    public Account returnAccount(User user) throws SQLException {

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            // Perform SQL Query
            ResultSet set = statement
                    .executeQuery("SELECT * FROM `Accounts` WHERE User_id='%s'".formatted(user.getId().toString()));

            if (!set.next()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
            }

            Account account = new Account(user, UUID.fromString(set.getString("Id")),
                    set.getString("AccountNumber"), set.getString("SortCode"),
                    set.getBigDecimal("AccountBalance"), set.getBoolean("IsForeign"),
                    AccountCategory.valueOf(set.getString("AccountCategory")));
            return account;
        }
    }

    public Account returnAccountFromId(UUID accountID) throws SQLException {
        try(Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            // Perform SQL Query
            ResultSet set = statement.executeQuery("SELECT * FROM `Accounts` WHERE Id='%s'".formatted(accountID.toString()));

            if (!set.next()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "Account Not Found");
            }

            User user = this.returnUser(UUID.fromString(set.getString("User_id")));

            Account account = new Account(user, UUID.fromString(set.getString("Id")),
                    set.getString("AccountNumber"), set.getString("SortCode"),
                    set.getBigDecimal("AccountBalance"), set.getBoolean("IsForeign"),
                    AccountCategory.valueOf(set.getString("AccountCategory"))
            );
            return account;
        }
    }

    /**
     * Returns the user details
     *
     * @param user
     * @throws SQLException
     */
    public UUID loginUser(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            // Perform SQL Query
            ResultSet set = statement.executeQuery("SELECT * FROM `Users` WHERE email='%s' and Hash_pass='%s'"
                    .formatted(user.getEmail(), user.getPasswordHash()));

            if (!set.next()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "User Not Found");
            }

            String userId = set.getString("Id");

            return UUID.fromString(userId);
        }
    }

    public void addUser(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(
                    String.format("INSERT INTO Users " + "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', %b)",
                            user.getId(), user.getName().replace("'", "''"), user.getEmail().replace("'", "''"),
                            user.getPasswordHash(), user.getPhoneNo(),
                            user.getAddress(), user.isAdmin()));
            prepStmt.execute();
        }
    }

    public User returnUser(UUID userID) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            // Perform SQL Query
            ResultSet set = statement.executeQuery("SELECT * FROM `Users` WHERE Id='%s'".formatted(userID.toString()));

            if (!set.next()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "User Not Found");
            }

            User user = new User(userID, set.getString("Email"), set.getString("Hash_Pass"), set.getString("Name"),
                    set.getString("phoneNo"), set.getString("address"), set.getBoolean("IsAdmin"));

            return user;
        }
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

    public void updateBalance(Account updatedAccount) throws SQLException {
        try(Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {

            stmt.executeUpdate("UPDATE Accounts SET AccountBalance=%f WHERE Id='%s'".formatted(updatedAccount.getBalance().floatValue(),updatedAccount.getUUID()));
        }
    }
}
