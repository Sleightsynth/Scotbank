package uk.co.asepstrath.bank;

import java.math.BigDecimal;

public class Account {
  private String name;
  private BigDecimal balance = BigDecimal.valueOf(0);

  public Account(String name, BigDecimal balance) {
    this.name = name;
    this.balance = balance;
  }

  public Account() {
    // For JUnit Testing
  }

  public void withdraw(double amount) {
    this.withdraw(BigDecimal.valueOf(amount));
  }

  public void deposit(double amount) {
    this.deposit(BigDecimal.valueOf(amount));
  }

  public void withdraw(BigDecimal amount) {
    if (amount.doubleValue() < 0 | (amount.compareTo(balance) > 0))
      throw new ArithmeticException();
    balance = balance.subtract(amount);
  }

  public void deposit(BigDecimal amount) {
    if (amount.doubleValue() < 0)
      throw new ArithmeticException();
    balance = balance.add(amount);
  }

  public BigDecimal getBalance() {
    return balance;
  }

  @Override
  public String toString() {
    return "Name: " + name + ", Balance: £" + balance;
  }
}
