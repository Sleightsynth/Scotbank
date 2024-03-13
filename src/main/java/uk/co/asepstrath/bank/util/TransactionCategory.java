package uk.co.asepstrath.bank.util;

import com.google.gson.annotations.SerializedName;


public enum TransactionCategory {
    @SerializedName("GROCERY")
    Grocery,
    @SerializedName("BILLS")
    Bills,
    @SerializedName("FOOD")
    Food,
    @SerializedName("ENTERTAINMENT")
    Entertainment,
    @SerializedName("PAYMENT")
    Payment,
    @SerializedName("TRANSFER")
    Transfer,
    @SerializedName("DEPOSIT")
    Deposit,

    @SerializedName("WITHDRAWAL")
    Withdrawal;

    public String toString() {
        return this.name();
    }
}


