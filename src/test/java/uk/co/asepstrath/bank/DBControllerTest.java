package uk.co.asepstrath.bank;
import io.jooby.exception.StatusCodeException;
import io.jooby.test.JoobyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.App;
import uk.co.asepstrath.bank.controllers.DBController;
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
    public void returnAccountaUUIDTest(){
        try {
            List<Account> accountList = dbController.returnAllAccounts();
            assertNotNull(accountList);
            Account account1 = accountList.get(0);
            Account account2 = dbController.returnAccount(account1.getUUID());
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
        //UUID uuid = new UUID();
        //User user = new User();
    }
}
