package uk.co.asepstrath.bank;

public class Account {
  private int balance = 0;

  public void deposit(int amount) {
    // TODO: This should probably throw an error.
    if (amount < 0)
      return;
    balance += amount;
  }

  public int getBalance() {
    return balance;
  }

}
