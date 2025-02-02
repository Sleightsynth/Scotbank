package uk.co.asepstrath.bank;
import io.jooby.exception.StatusCodeException;
import io.jooby.test.JoobyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionCategory;
import uk.co.asepstrath.bank.util.TransactionStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@JoobyTest(App.class)
public class DBControllerTest {
    static DBController dbController;
    @BeforeEach
    public void before(App app){
        DataSource ds = app.require(DataSource.class);
        dbController = new DBController(ds);
    }
    @Test
    public void addTransacionTest(){
        //dbController.addTransaction();
    }
    @Test
    public void returnAccountNumberSortCodeTest(){
        try {
            List<Account> accountList = dbController.returnAllAccounts();
            assertNotNull(accountList);
            Account account1 = accountList.get(0);
            Account account2 = dbController.returnAccount(account1.getAccountNumber(), account1.getSortCode());
            assertNotNull(account2);
            assertEquals(account1.getAccountNumber(), account2.getAccountNumber());
            assertEquals(account1.getAccountCategory(), account2.getAccountCategory());
            assertEquals(account1.getSortCode(), account2.getSortCode());
            assertEquals(account1.getBalance(), account2.getBalance());
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    public void returnAccountUUIDTest(){
        try {
            List<Account> accountList = dbController.returnAllAccounts();
            assertNotNull(accountList);
            Account account1 = accountList.get(0);
            Account account2 = dbController.returnAccount(account1.getUser());
            assertNotNull(account2);
            assertEquals(account1.getAccountNumber(), account2.getAccountNumber());
            assertEquals(account1.getAccountCategory(), account2.getAccountCategory());
            assertEquals(account1.getSortCode(), account2.getSortCode());
            assertEquals(account1.getBalance(), account2.getBalance());
        } catch (SQLException e) {
            fail();
        }
    }

    @Test
    public void returnAccountExceptionTest(){
        assertThrows(StatusCodeException.class, () -> dbController.returnAccount("33333333", "888888"));
    }
    @Test
    public void returnUserTest(){
        UUID uuid = UUID.randomUUID();
        User user1 = new User(uuid, "e@mail.com", dbController.getSha512Hash("password"), "name", "07111111111", "thePlace",true);
        assertNotNull(user1);
        try {dbController.addUser(user1);}
        catch (SQLException e) {fail();}
        User user2 = null;
        try {user2 = dbController.returnUser(uuid);}
        catch (SQLException e) {fail();}
        assertNotNull(user2);
        assertEquals(user1.getEmail(), user2.getEmail());
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getPhoneNo(), user2.getPhoneNo());
        assertEquals(user1.getPasswordHash(), user2.getPasswordHash());
        assertEquals(user1.getAddress(), user2.getAddress());
        assertEquals(user1.getName(), user2.getName());
    }
    @Test
    public void AccountNotFound(){
        UUID uuid = UUID.randomUUID();
        assertThrows(StatusCodeException.class, () -> dbController.returnUser(uuid));
    }

    @Test
    public void loginUserTest() throws SQLException {
        UUID uuid = UUID.randomUUID();
        User user1 = new User(uuid, "e@mail.com", "password", "name", "07111111111", "thePlace", true);
        assertNotNull(user1);
        dbController.addUser(user1);
        UUID returnUUID = dbController.loginUser(user1);
        assertNotNull(returnUUID);
        assertEquals(returnUUID, uuid);
    }
    @Test
    public void loginUserNoUser(){
        UUID uuid = UUID.randomUUID();
        User user1 = new User(uuid, "egjgjhghg@mail.com", dbController.getSha512Hash("password"), "name", "07111111111", "thePlace",false);
        assertThrows(StatusCodeException.class, () -> dbController.loginUser(user1));

    }

    @Test
    public void returnTransactionsTest() throws SQLException {
        UUID uuid = UUID.randomUUID();
        User user1 = new User(uuid, "e@mail.com", "password", "name", "07111111111", "thePlace", true);
        assertNotNull(user1);
        dbController.addUser(user1);
        Account account = new Account(user1, UUID.randomUUID(), "", "", BigDecimal.valueOf(0), false, AccountCategory.Payment);
        assertNotNull(account);
        dbController.addAccount(account);
        Transaction transaction = new Transaction(new Timestamp(new java.util.Date().getTime()), BigDecimal.valueOf(0), "", TransactionCategory.Payment, TransactionStatus.OK, UUID.randomUUID(), account, account, "","");
        dbController.addTransaction(transaction);
        List<Transaction> tran = dbController.returnTransactions(account);
        assertNotNull(tran);
        assertEquals(1, tran.size());
    }
}
