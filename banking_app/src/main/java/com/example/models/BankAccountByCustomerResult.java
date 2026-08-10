package com.example.models;

public class BankAccountByCustomerResult {
    public String bankAccountId;
    public String bankName;

    public BankAccountByCustomerResult() {}

    public BankAccountByCustomerResult(String bankAccountId, String bankName) {
        this.bankAccountId = bankAccountId;
        this.bankName = bankName;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getBankName() {
        return bankName;
    }
}