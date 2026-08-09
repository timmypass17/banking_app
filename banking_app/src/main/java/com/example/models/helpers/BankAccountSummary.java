package com.example.models.helpers;

public class BankAccountSummary {
    private int bankAccountId;
    private String bankName;
    private int customerId;
    private String customerName;

    public BankAccountSummary(int bankAccountId, String bankName, int customerId, String customerName) {
        this.bankAccountId = bankAccountId;
        this.bankName = bankName;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public int getBankAccountId() {
        return bankAccountId;
    }

    public String getBankName() {
        return bankName;
    }

    
    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}
