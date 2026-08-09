package com.example.models;

public class BankAccountByCustomerResult {
    public int bankAccountId;
    public String bankName;

    public BankAccountByCustomerResult(int bankAccountId, String bankName) {
        this.bankAccountId = bankAccountId;
        this.bankName = bankName;
    }

    public int getBankAccountId() {
        return bankAccountId;
    }

    public String getBankName() {
        return bankName;
    }
}