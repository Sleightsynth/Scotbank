package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import javax.security.auth.login.AccountException;

public class AccountTests {

  @Test
  public void createAccount() {
    Account a = new Account();
    assertTrue(a != null);
  }

  @Test
  public void checkAccountHasZeroValue() {
    Account a = new Account();
    assertTrue(a.getBalance() == 0);
  }

  @Test
  public void checkAccountSumOk() {
    Account a = new Account();
    a.deposit(20);
    a.deposit(50);
    assertTrue(a.getBalance() == 70);
  }

  @Test
  public void checkAccountWithdrawOk() {
    Account a = new Account();
    a.deposit(40);
    a.withdraw(20);
    assertTrue(a.getBalance() == 20);
  }

  @Test
  public void checkMultiple() {
    Account a = new Account();
    a.deposit(20);
    for(int i = 0; i < 8; i++) {
      if (i < 5) {
        a.deposit(10);
      } else {
        a.withdraw(20);
      }
    }
    assertTrue(a.getBalance() == 10);
  }
}


