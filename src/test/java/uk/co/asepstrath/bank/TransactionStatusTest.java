package uk.co.asepstrath.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.util.TransactionStatus;

public class TransactionStatusTest {

    @Test
    public void testEnumValues() {
        assertEquals(3, TransactionStatus.values().length);
        assertEquals(TransactionStatus.OK, TransactionStatus.values()[0]);
        assertEquals(TransactionStatus.PROCESS_DUE, TransactionStatus.values()[1]);
        assertEquals(TransactionStatus.FAILED, TransactionStatus.values()[2]);
    }

    @Test
    public void testEnumValueOf() {
        assertEquals(TransactionStatus.OK, TransactionStatus.valueOf("OK"));
        assertEquals(TransactionStatus.PROCESS_DUE, TransactionStatus.valueOf("PROCESS_DUE"));
        assertEquals(TransactionStatus.FAILED, TransactionStatus.valueOf("FAILED"));
    }
}
