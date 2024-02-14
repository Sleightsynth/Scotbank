package uk.co.asepstrath.bank.util;

public enum AccountCategory {
  Grocery,
  Bills,
  Food,
  Entertainment,
  Payment;

  public String toString() {
    return this.name();
  }
}
