package uk.co.asepstrath.bank;
//initiate branch
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.util.AccountCategory;

public class AccountTests {
  @Test
  public void createAccount() {
    Account a = new Account();
    assertNotNull(a);

    Account b = new Account(BigDecimal.valueOf(50.01), Boolean.FALSE, AccountCategory.Payment);
    assertNotNull(b);

    Account c = new Account(BigDecimal.valueOf(50.01), Boolean.TRUE);
    assertNotNull(c);

    Account d = new Account(BigDecimal.valueOf(50.01), AccountCategory.Bills);
    assertNotNull(d);

    Account e = new Account(BigDecimal.valueOf(50.01));
    assertNotNull(e);
  }

  @Test
  public void checkAccountHasZeroValue() {
    Account a = new Account();
    assertEquals(a.getBalance().compareTo(BigDecimal.valueOf(0)), 0);
  }

  @Test
  public void checkAccountSumOk() {
    Account a = new Account();
    a.deposit(50);
    a.deposit(20);
    System.out.println(a.getBalance()); // TODO: This should print to a logger
    assertEquals(a.getBalance().compareTo(BigDecimal.valueOf(70)), 0);
  }

  @Test
  public void checkAccountWithdrawOk() {
    Account a = new Account();
    a.deposit(40);
    a.withdraw(20);
    assertEquals(a.getBalance().compareTo(BigDecimal.valueOf(20)), 0);
  }

  @Test
  public void checkMultiple() {
    Account a = new Account();
    a.deposit(BigDecimal.valueOf(20));
    for (int i = 0; i < 8; i++) {
      if (i < 5) {
        a.deposit(10);
      } else {
        a.withdraw(20);
      }
    }
    assertEquals(a.getBalance().compareTo(BigDecimal.valueOf(10)), 0);
  }

  @Test
  public void checkDecimals() {
    Account a = new Account();
    a.deposit(5.45);
    a.deposit(17.56);
    assertEquals(a.getBalance().compareTo(BigDecimal.valueOf(23.01)), 0);
  }

  @Test
  public void checkNoOverdraft() {
    Account a = new Account();
    a.deposit(20);
    assertThrows(ArithmeticException.class, () -> a.withdraw(BigDecimal.valueOf(40)));
  }

  @Test
  public void checkAccountCategory() {
    Account a = new Account(BigDecimal.valueOf(50.01), Boolean.FALSE, AccountCategory.Food);
    assertNotNull(a);

    assertEquals(AccountCategory.Food, a.getAccountCategory());
  }

  @Test
  public void checkIsForeign() {
    Account a = new Account(BigDecimal.valueOf(50.01), Boolean.FALSE, AccountCategory.Entertainment);
    assertNotNull(a);

    assertEquals(Boolean.FALSE, a.isForeign());
  }

  @Test
  public void checkGetUUID() {
    Account a = new Account(BigDecimal.valueOf(50.01), Boolean.FALSE, AccountCategory.Grocery);
    assertNotNull(a);

    assertNotNull(a.getUUID());
  }

  @Test
  public void checkCategoryToString() {
    Account a = new Account(BigDecimal.valueOf(50.01), Boolean.FALSE, AccountCategory.Payment);
    assertNotNull(a);

    assertEquals("Payment", a.getAccountCategory().toString());
  }
}
