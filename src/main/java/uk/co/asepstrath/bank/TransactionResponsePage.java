package uk.co.asepstrath.bank;

import uk.co.asepstrath.bank.util.Transaction;

import java.util.ArrayList;

public class TransactionResponsePage {
    ArrayList <Transaction> results = new ArrayList<>();
    private float page;
    private float totalPages;
    private float size;
    private boolean hasNext;
    private boolean hasPrevious;

    // Getter Methods

    public float getPage() {
        return page;
    }

    public float getTotalPages() {
        return totalPages;
    }

    public float getSize() {
        return size;
    }

    public boolean getHasNext() {
        return hasNext;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }
}
