package uk.co.asepstrath.bank;

import java.math.BigDecimal;

public class Account {
  private BigDecimal balance = BigDecimal.valueOf(0);

  public void withdraw(BigDecimal amount) {
    // TODO: This should probably throw an error instead.
    if (amount.doubleValue() < 0)
      return;
    balance = balance.subtract(amount);
  }

  public void deposit(BigDecimal amount) {
    // TODO: This should probably throw an error instead.
    if (amount.doubleValue() < 0)
      return;
    balance = balance.add(amount);
  }

  public BigDecimal getBalance() {
    return balance;
  }

}
