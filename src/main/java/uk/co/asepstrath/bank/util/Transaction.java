package uk.co.asepstrath.bank.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
import uk.co.asepstrath.bank.Account;

/*
 * The fields are ordered according to the table values in the database.
 * This interface is only here for definition sake.
 * */
public class Transaction {
    public Timestamp time = null;
    public BigDecimal amount = null;
    public String reference = null;
    public AccountCategory category = null;
    public TransactionStatus status = null;

    // NOTE: For handlebards to pick up on properties of a class, it must have
    // getters!
    public Timestamp getTime() {
        return time;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public AccountCategory getCategory() {
        return category;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public UUID getId() {
        return id;
    }

    public Account getRecipient() {
        return recipient;
    }

    public Account getSender() {
        return sender;
    }

    public UUID id = null;
    public Account recipient = null;
    public Account sender = null;

    @Override
    public String toString() {
        return reference;
    }
}
