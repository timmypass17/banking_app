package com.example.models;

import java.time.LocalDateTime;

public class Transaction {

    private int id;
    private LocalDateTime createdAt;
    private TransactionAction action;

    private Long sourceAmount;
    private Integer sourceBankAccountId;
    private Long sourceResultBalance;   // snapshot

    private Long destinationAmount;
    private Integer destinationBankAccountId;
    private Long destinationResultBalance;

    // Join result fields (could've made separate model but its fine)
    private Integer sourceCustomerId;
    private Integer sourceBankId;
    private String sourceCustomerName;
    private String sourceBankName;

    private Integer destinationCustomerId;
    private Integer destinationBankId;
    private String destinationCustomerName;
    private String destinationBankName;

    public Transaction(
        int id,
        LocalDateTime createdAt,
        TransactionAction action,
        Long sourceAmount,
        Integer sourceBankAccountId,
        Long sourceResultBalance,
        Long destinationAmount,
        Integer destinationBankAccountId,
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

    public Integer getSourceBankAccountId() {
        return this.sourceBankAccountId;
    }

    public Integer getDestinationBankAccountId() {
        return this.destinationBankAccountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Integer getSourceBankId() {
        return sourceBankId;
    }

    public void setSourceBankId(Integer sourceBankId) {
        this.sourceBankId = sourceBankId;
    }

    public Long getSourceResultBalance() {
        return sourceResultBalance;
    }

    public void setSourceResultBalance(Long sourceResultBalance) {
        this.sourceResultBalance = sourceResultBalance;
    }

    public Integer getSourceCustomerId() {
        return sourceCustomerId;
    }

    public void setSourceCustomerId(Integer sourceCustomerId) {
        this.sourceCustomerId = sourceCustomerId;
    }

    public Long getDestinationAmount() {
        return destinationAmount;
    }

    public void setDestinationAmount(Long destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public Integer getDestinationBankId() {
        return destinationBankId;
    }

    public void setDestinationBankId(Integer destinationBankId) {
        this.destinationBankId = destinationBankId;
    }

    public Long getDestinationResultBalance() {
        return destinationResultBalance;
    }

    public void setDestinationResultBalance(Long destinationResultBalance) {
        this.destinationResultBalance = destinationResultBalance;
    }

    public Integer getDestinationCustomerId() {
        return destinationCustomerId;
    }

    public void setDestinationCustomerId(Integer destinationCustomerId) {
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