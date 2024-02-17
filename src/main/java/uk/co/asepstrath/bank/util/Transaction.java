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
    public UUID id = null;
    public Account recipient = null;
    public Account sender = null;
}
