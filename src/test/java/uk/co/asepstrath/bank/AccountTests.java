package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

}
