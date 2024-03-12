package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import uk.co.asepstrath.bank.util.AccountCategory;

public class Account {
    protected UUID id = null;
    protected User user = null;
    protected String accountNumber = "";
    protected String sortCode = "";
    protected Locale locale = new Locale("en", "gb"); // default is UK, Note: "uk" is not valid, must be "gb".
    @SerializedName(value = "startingBalance")
    protected BigDecimal balance = BigDecimal.valueOf(0);
    protected String formattedBalance = ""; // yes it is being used in the hbs
    protected AccountCategory accountCategory;
    protected Boolean foreign;

    public Account(BigDecimal balance) {
        this(balance, Boolean.FALSE, AccountCategory.Payment);
    }

    public Account(BigDecimal balance, AccountCategory accountType) {
        this(balance, Boolean.FALSE, accountType);
    }

    public Account(BigDecimal balance, Boolean foreign) {
        this(balance, foreign, AccountCategory.Payment);
    }

    public Account(String accountNumber, String sortCode, BigDecimal balance) {
        this.id = UUID.randomUUID();
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.balance = balance;

        updateFormattedBalance();
    }

    public Account(BigDecimal balance, Boolean foreign, AccountCategory accountType) {
        Random rand = new Random();
        this.id = UUID.randomUUID();
        this.accountNumber = "%08d".formatted(rand.nextInt(100000000));
        this.sortCode = "%02d-%02d-%02d".formatted(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
        this.balance = balance;
        this.accountCategory = accountType;
        this.foreign = foreign;
    }

    public Account(User user, UUID accountId, String accountNumber, String sortCode, BigDecimal balance,
            Boolean foreign,
            AccountCategory accountType) {
        this.id = accountId;
        this.user = user;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
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

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public Boolean isForeign() {
        return foreign;
    }

    public UUID getUUID() {
        return id;
    }

    public User getUser() {
        return user;
    }

    // Note: This will not display on HTML if the char set is not Unicode.
    public String getFormattedBalance() {
        return NumberFormat.getCurrencyInstance(locale).format(balance);
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public String getSortCode() {
        return this.sortCode;
    }

    @Override
    public String toString() {
        String formattedBalanceString = getFormattedBalance();

        return "Balance: " + formattedBalanceString;
    }
}
