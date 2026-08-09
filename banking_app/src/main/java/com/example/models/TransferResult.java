package com.example.models;

public class TransferResult {
    private long sourceNewBalance;
    private long destinationNewBalance;

    public TransferResult(long sourceNewBalance, long destinationNewBalance) {
        this.sourceNewBalance = sourceNewBalance;
        this.destinationNewBalance = destinationNewBalance;
    }

    public long getSourceNewBalance() {
        return this.sourceNewBalance;
    }

    public long getDestinationNewBalance() {
        return this.destinationNewBalance;
    }
}
