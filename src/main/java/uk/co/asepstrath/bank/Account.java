package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import uk.co.asepstrath.bank.util.AccountCategory;

public class Account {
  protected UUID id = null;
  protected UUID user_id=null;
  protected String name = "";
  protected Locale locale = new Locale("en", "gb"); // default is UK, Note: "uk" is not valid, must be "gb".
  protected BigDecimal balance = BigDecimal.valueOf(0);
  protected String formattedBalance = ""; // yes it is being used in the hbs
  protected AccountCategory accountCategory;
  protected Boolean foreign;

  // What happens when we pull a UUID from somewhere?

  public Account(String name, BigDecimal balance) {
    this(name, balance, Boolean.FALSE, AccountCategory.Payment);
  }

  public Account(String name, BigDecimal balance, AccountCategory accountType) {
    this(name, balance, Boolean.FALSE, accountType);
  }

  public Account(String name, BigDecimal balance, Boolean foreign) {
    this(name, balance, foreign, AccountCategory.Payment);
  }

  public Account(String name, BigDecimal balance, Boolean foreign, AccountCategory accountType) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.balance = balance;
    this.accountCategory = accountType;
    this.foreign = foreign;

    updateFormattedBalance();
  }

  public Account(String name, UUID user_id, BigDecimal balance, Boolean foreign, AccountCategory accountType) {
    this.id = UUID.randomUUID();
    this.user_id = user_id;
    this.name = name;
    this.balance = balance;
    this.accountCategory = accountType;
    this.foreign = foreign;

    updateFormattedBalance();
  }

  public Account() {
    // For JUnit Testing
  }

  protected void updateFormattedBalance() {
    formattedBalance = getFormattedBalance();
  }

  public String getUsername() {
    return name;
  }

  public void setUsername(String username) {
    this.name = username;
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

  public AccountCategory getAccountCategory() {
    return accountCategory;
  }

  public Boolean isForeign() {
    return foreign;
  }

  public UUID getUUID() {
    return id;
  }

  // Note: This will not display on HTML if the char set is not Unicode.
  public String getFormattedBalance() {
    return NumberFormat.getCurrencyInstance(locale).format(balance);
  }

  public UUID getUser_id() {
    return user_id;
  }

  @Override
  public String toString() {
    String formattedBalanceString = getFormattedBalance();

    return "Name: " + name + ", Balance: " + formattedBalanceString;
  }
}
