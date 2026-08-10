package com.example.models.helpers;

public class BankAccountSummary {
    private String bankAccountId;
    private String bankName;
    private String customerId;
    private String customerName;

    public BankAccountSummary(String bankAccountId, String bankName, String customerId, String customerName) {
        this.bankAccountId = bankAccountId;
        this.bankName = bankName;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public String getBankName() {
        return bankName;
    }

    
    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}
