package uk.co.asepstrath.bank;

import static org.junit.jupiter.api.Assertions.*;
import static uk.co.asepstrath.bank.util.AccountCategory.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionStatus;
import java.sql.Timestamp;
import java.util.UUID;

public class TransactionTest {

    @Test
    public void testTransaction() {
        Transaction transaction = new Transaction();

        // Test initial state
        assertNull(transaction.time);
        assertNull(transaction.amount);
        assertNull(transaction.reference);
        assertNull(transaction.category);
        assertNull(transaction.status);
        assertNull(transaction.id);
        assertNull(transaction.recipient);
        assertNull(transaction.sender);

        // Set values
        transaction.time = new Timestamp(System.currentTimeMillis());
        transaction.amount = new BigDecimal("100.00");
        transaction.reference = "Test Reference";
        transaction.category = Payment;
        transaction.status = TransactionStatus.OK;
        transaction.id = UUID.randomUUID();
        transaction.recipient = new Account();
        transaction.sender = new Account();

        // Test set values
        assertNotNull(transaction.time);
        assertEquals(0, transaction.amount.compareTo(new BigDecimal("100.00")));
        assertEquals("Test Reference", transaction.reference);
        assertEquals(Payment, transaction.category);
        assertEquals(TransactionStatus.OK, transaction.status);
        assertNotNull(transaction.id);
        assertNotNull(transaction.recipient);
        assertNotNull(transaction.sender);
    }
}
