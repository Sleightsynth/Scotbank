package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Account {
  private String name = "";
  private Locale locale = new Locale("en", "gb"); // default is UK, Note: "uk" is not valid, must be "gb".
  private BigDecimal balance = BigDecimal.valueOf(0);
  private String formattedBalance = ""; // yes it is being used in the hbs

  public Account(String name, BigDecimal balance) {
    this.name = name;
    this.balance = balance;

    updateFormattedBalance();
  }

  public Account() {
    // For JUnit Testing
  }

  protected void updateFormattedBalance() {
    formattedBalance = getFormattedBalance();
  }

  public void withdraw(double amount) {
    this.withdraw(BigDecimal.valueOf(amount));
  }

  public void deposit(double amount) {
    this.deposit(BigDecimal.valueOf(amount));
  }

  public void withdraw(BigDecimal amount) {
    if (amount.doubleValue() < 0 || (amount.compareTo(balance) > 0))
      throw new ArithmeticException();
    balance = balance.subtract(amount);
    updateFormattedBalance();
  }

  public void deposit(BigDecimal amount) {
    if (amount.doubleValue() < 0)
      throw new ArithmeticException();
    balance = balance.add(amount);
    updateFormattedBalance();
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public String getName() {
    return name;

  }

  // Note: This will not display on HTML if the char set is not Unicode.
  public String getFormattedBalance() {
    return NumberFormat.getCurrencyInstance(locale).format(balance);
  }

  @Override
  public String toString() {
    String formattedBalanceString = getFormattedBalance();

    return "Name: " + name + ", Balance: " + formattedBalanceString;
  }
}
