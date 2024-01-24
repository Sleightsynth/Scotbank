package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTests {

  @Test
  public void createAccount() {
    Account a = new Account();
    Assertions.assertTrue(a != null);
  }

  @Test
  public void checkAccountHasZeroValue() {
    Account a = new Account();
    Assertions.assertTrue(a.getBalance() == 0);
  }

  @Test
  public void checkAccountSumOk() {
    Account a = new Account();
    a.deposit(20);
    a.deposit(50);
    Assertions.assertTrue(a.getBalance() == 70);
  }

}
