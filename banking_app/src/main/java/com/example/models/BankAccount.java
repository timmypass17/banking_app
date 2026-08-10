package com.example.models;

public class BankAccount {
    private String id;
    private String customerId;
    private String bankId;
    private BankAccountType accountType;
    private long balance;
    private boolean isActive;

    public BankAccount(String id, String customerId, String bankId, BankAccountType accountType, long balance, boolean isActive) {
        this.id = id;
        this.customerId = customerId;
        this.bankId = bankId;
        this.accountType = accountType;
        this.balance = balance;
        this.isActive = isActive;
    }

    public BankAccount(String customerId, String bankId, BankAccountType accountType, long balance, boolean isActive) {
        this.customerId = customerId;
        this.bankId = bankId;
        this.accountType = accountType;
        this.balance = balance;
        this.isActive = isActive;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public BankAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(BankAccountType accountType) {
        this.accountType = accountType;
    }

    public long getBalance() {
        return balance;
    }

    public String getBalanceFormatted() {
        return String.format("$%.2f", balance / 100.0);
    }


    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }

        balance += amount;
    }

    public void withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal must be positive");
        }

        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        balance -= amount;
    }
}