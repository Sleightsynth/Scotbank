package uk.co.asepstrath.bank.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import uk.co.asepstrath.bank.Account;

/*
 * The fields are ordered according to the table values in the database.
 * This interface is only here for definition sake.
 * */
public class Transaction {
    @SerializedName("Timestamp")
    public Timestamp time = null;
    public BigDecimal amount = null;
    public String reference = null;
    @SerializedName("type")
    public TransactionCategory category = null;
    public TransactionStatus status = null;

    public UUID id = null;
    public Account recipient = null;
    public Account sender = null;
    protected Locale locale = new Locale("en", "gb"); // default is UK, Note: "uk" is not valid, must be "gb".
    protected String formattedAmount = ""; // yes it is being used in the hbs
    public String to;
    public String from;

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

    public TransactionCategory getCategory() {
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
    public String getFormattedAmount(){
        return NumberFormat.getCurrencyInstance(locale).format(amount);
    }

    @Override
    public String toString() {
        return reference;
    }
}
