package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class AccountTests {
  @Test
  public void createAccount() {
    Account a = new Account();
    assertNotNull(a);
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
}
