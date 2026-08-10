package com.example.models;

import java.time.LocalDateTime;

public class Transaction {

    private String id;
    private LocalDateTime createdAt;
    private TransactionAction action;

    private Long sourceAmount;
    private String sourceBankAccountId;
    private Long sourceResultBalance;   // snapshot

    private Long destinationAmount;
    private String destinationBankAccountId;
    private Long destinationResultBalance;

    // Join result fields (could've made separate model but its fine)
    private String sourceCustomerId;
    private String sourceBankId;
    private String sourceCustomerName;
    private String sourceBankName;

    private String destinationCustomerId;
    private String destinationBankId;
    private String destinationCustomerName;
    private String destinationBankName;

    public Transaction() {}

    public Transaction(
        String id,
        LocalDateTime createdAt,
        TransactionAction action,
        Long sourceAmount,
        String sourceBankAccountId,
        Long sourceResultBalance,
        Long destinationAmount,
        String destinationBankAccountId,
        Long destinationResultBalance
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.action = action;

        this.sourceAmount = sourceAmount;
        this.sourceBankAccountId = sourceBankAccountId;
        this.sourceResultBalance = sourceResultBalance;

        this.destinationAmount = destinationAmount;
        this.destinationBankAccountId = destinationBankAccountId;
        this.destinationResultBalance = destinationResultBalance;
    }

    public String getSourceBankAccountId() {
        return this.sourceBankAccountId;
    }

    public String getDestinationBankAccountId() {
        return this.destinationBankAccountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public TransactionAction getAction() {
        return action;
    }

    public void setAction(TransactionAction action) {
        this.action = action;
    }

    public Long getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(Long sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public String getSourceBankId() {
        return sourceBankId;
    }

    public void setSourceBankId(String sourceBankId) {
        this.sourceBankId = sourceBankId;
    }

    public Long getSourceResultBalance() {
        return sourceResultBalance;
    }

    public void setSourceResultBalance(Long sourceResultBalance) {
        this.sourceResultBalance = sourceResultBalance;
    }

    public String getSourceCustomerId() {
        return sourceCustomerId;
    }

    public void setSourceCustomerId(String sourceCustomerId) {
        this.sourceCustomerId = sourceCustomerId;
    }

    public Long getDestinationAmount() {
        return destinationAmount;
    }

    public void setDestinationAmount(Long destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public String getDestinationBankId() {
        return destinationBankId;
    }

    public void setDestinationBankId(String destinationBankId) {
        this.destinationBankId = destinationBankId;
    }

    public Long getDestinationResultBalance() {
        return destinationResultBalance;
    }

    public void setDestinationResultBalance(Long destinationResultBalance) {
        this.destinationResultBalance = destinationResultBalance;
    }

    public String getDestinationCustomerId() {
        return destinationCustomerId;
    }

    public void setDestinationCustomerId(String destinationCustomerId) {
        this.destinationCustomerId = destinationCustomerId;
    }

    public String getSourceCustomerName() {
        return sourceCustomerName;
    }

    public void setSourceCustomerName(String sourceCustomerName) {
        this.sourceCustomerName = sourceCustomerName;
    }

    public String getSourceBankName() {
        return sourceBankName;
    }

    public void setSourceBankName(String sourceBankName) {
        this.sourceBankName = sourceBankName;
    }

    public String getDestinationCustomerName() {
        return destinationCustomerName;
    }

    public void setDestinationCustomerName(String destinationCustomerName) {
        this.destinationCustomerName = destinationCustomerName;
    }

    public String getDestinationBankName() {
        return destinationBankName;
    }

    public void setDestinationBankName(String destinationBankName) {
        this.destinationBankName = destinationBankName;
    }
}