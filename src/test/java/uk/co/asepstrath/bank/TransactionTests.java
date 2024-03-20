package uk.co.asepstrath.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionCategory;
import uk.co.asepstrath.bank.util.TransactionStatus;

public class TransactionTests {

    private Transaction transaction;

    @Test
    public void testDefaultConstructor() {
        transaction = new Transaction();
        assertNotNull(transaction);
        assertNull(transaction.getTime());
        assertEquals(BigDecimal.valueOf(0),transaction.getAmount());
        assertNull(transaction.getReference());
        assertNull(transaction.getCategory());
        assertNull(transaction.getStatus());
        assertNull(transaction.getId());
        assertNull(transaction.getRecipient());
        assertNull(transaction.getSender());
        assertEquals("£0.00", transaction.getFormattedAmount());
    }

    @Test
    public void testGettersAndSetters() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        BigDecimal amount = BigDecimal.valueOf(100.50);
        String reference = "Test Reference";
        TransactionCategory category = TransactionCategory.Deposit;
        TransactionStatus status = TransactionStatus.OK;
        UUID id = UUID.randomUUID();
        Account recipient = new Account();
        Account sender = new Account();

        transaction = new Transaction(time,amount,reference,category,status,id,recipient,sender,null,null);

        assertEquals(time, transaction.getTime());
        assertEquals(amount, transaction.getAmount());
        assertEquals(reference, transaction.getReference());
        assertEquals(category, transaction.getCategory());
        assertEquals(status, transaction.getStatus());
        assertEquals(id, transaction.getId());
        assertEquals(recipient, transaction.getRecipient());
        assertEquals(sender, transaction.getSender());
    }
}

