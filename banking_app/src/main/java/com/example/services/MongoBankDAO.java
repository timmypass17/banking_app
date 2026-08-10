package com.example.services;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Transaction;
import com.example.models.TransactionAction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;
import com.example.utils.MongoConnectionManager;
import com.mongodb.MongoException;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class MongoBankDAO implements BankDAO {

    @Override
    public List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(String customerId) {

        List<BankAccountByCustomerResult> bankAccounts = new ArrayList<>();

        MongoCollection<Document> collection =
                MongoConnectionManager
                    .getDatabase()
                    .getCollection("bankAccounts");

        try {
            collection.aggregate(Arrays.asList(

                    // WHERE customerId = ?
                    Aggregates.match(
                        Filters.eq("customerId", customerId)
                    ),

                    // JOIN banks ON bankAccounts.bankId = banks._id
                    Aggregates.lookup(
                        "banks",
                        "bankId",
                        "_id",
                        "bank"
                    ),

                    // Turn the bank array into a single object
                    Aggregates.unwind("$bank"),

                    // SELECT bankAccountId, bankName
                    Aggregates.project(
                        Projections.fields(
                            Projections.computed(
                                "bankAccountId",
                                "$_id"
                            ),
                            Projections.computed(
                                "bankName",
                                "$bank.name"
                            ),
                            Projections.excludeId()
                        )
                    )

            )).forEach(document -> {
                String bankAccountId =
                        document.getString("bankAccountId");

                String bankName =
                        document.getString("bankName");

                bankAccounts.add(
                        new BankAccountByCustomerResult(
                                bankAccountId,
                                bankName
                        )
                );
            });

        } catch (MongoException me) {
            me.printStackTrace();
        }

        return bankAccounts;
    }

    @Override
    public Optional<BankAccount> getBankAccountById(String bankAccountId) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        BankAccount bankAccount = collection
                .find(Filters.eq("_id", bankAccountId))
                .first();

        return Optional.ofNullable(bankAccount);
    }

    @Override
    public Optional<Bank> getBankById(String bankId) {

        MongoCollection<Bank> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("banks", Bank.class);

        Bank bank = collection
                .find(Filters.eq("_id", bankId))
                .first();

        return Optional.ofNullable(bank);
    }

    @Override
    public long deposit(String customerId, String bankAccountId, long amount) {

        MongoClient client = MongoConnectionManager.getMongoClient();

        try (ClientSession session = client.startSession()) {

            TransactionOptions transactionOptions =
                    TransactionOptions.builder()
                            .writeConcern(WriteConcern.MAJORITY)
                            .build();

            return session.withTransaction(() -> {

                long balance = deposit(
                        session,
                        bankAccountId,
                        amount
                );

                createDepositTransaction(
                        session,
                        bankAccountId,
                        amount,
                        balance
                );

                return balance;

            }, transactionOptions);
        }
    }

    private long deposit(
        ClientSession session,
        String bankAccountId,
        long amount) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        BankAccount account = collection
                .find(session, Filters.eq("_id", bankAccountId))
                .first();

        if (account == null) {
            throw new IllegalArgumentException("Bank account not found");
        }

        long newBalance = account.getBalance() + amount;

        collection.updateOne(
                session,
                Filters.eq("_id", bankAccountId),
                Updates.set("balance", newBalance)
        );

        return newBalance;
    }

    public void createDepositTransaction(
        ClientSession session,
        String accountId,
        long amount,
        long resultBalance) {

        MongoCollection<Transaction> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("transactions", Transaction.class);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                TransactionAction.DEPOSIT,
                null,
                null,
                null,
                amount,
                accountId,
                resultBalance

        );

        collection.insertOne(session, transaction);
    }

    @Override
    public long withdraw(String bankAccountId, long amount, String customerId) {

        MongoClient client = MongoConnectionManager.getMongoClient();

        try (ClientSession session = client.startSession()) {

            return session.withTransaction(() -> {

                long balance = withdraw(
                        session,
                        bankAccountId,
                        amount,
                        customerId
                );

                createWithdrawTransaction(
                        session,
                        bankAccountId,
                        amount,
                        balance
                );

                return balance;
            });
        }
    }

    private long withdraw(
        ClientSession session,
        String bankAccountId,
        long amount,
        String customerId) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        BankAccount account = collection
                .findOneAndUpdate(
                        session,
                        Filters.and(
                                Filters.eq("_id", bankAccountId),
                                Filters.eq("customerId", customerId),
                                Filters.gte("balance", amount)
                        ),
                        Updates.inc("balance", -amount),
                        new FindOneAndUpdateOptions()
                                .returnDocument(ReturnDocument.AFTER)
                );

        if (account == null) {
            throw new InvalidAmountException(
                    "Bank account not found or insufficient funds " +
                    "or account does not belong to current user: " +
                    bankAccountId
            );
        }

        return account.getBalance();
    }

    public void createWithdrawTransaction(
        ClientSession session,
        String accountId,
        long amount,
        long resultBalance) {

        MongoCollection<Transaction> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("transactions", Transaction.class);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                TransactionAction.WITHDRAW,
                amount,
                accountId,
                resultBalance,
                null,
                null,
                null
        );

        collection.insertOne(session, transaction);
    }

    @Override
    public TransferResult transferMoney(
            long amount,
            String sourceAccountId,
            String destinationAccountId,
            String customerId) {

        MongoClient client = MongoConnectionManager.getMongoClient();

        try (ClientSession session = client.startSession()) {

            return session.withTransaction(() -> {

                long sourceBalance = withdraw(
                        session,
                        sourceAccountId,
                        amount,
                        customerId
                );

                long destinationBalance = deposit(
                        session,
                        destinationAccountId,
                        amount
                );

                createTransferTransaction(
                        session,
                        amount,
                        sourceAccountId,
                        sourceBalance,
                        destinationAccountId,
                        destinationBalance
                );

                return new TransferResult(
                        sourceBalance,
                        destinationBalance
                );
            });
        }
    }

    public void createTransferTransaction(
            ClientSession session,
            long amount,
            String sourceAccountId,
            long sourceResultBalance,
            String destinationAccountId,
            long destinationResultBalance) {

        MongoCollection<Transaction> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("transactions", Transaction.class);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                TransactionAction.TRANSFER,
                amount,
                sourceAccountId,
                sourceResultBalance,
                amount,
                destinationAccountId,
                destinationResultBalance
        );

        collection.insertOne(session, transaction);
    }

    @Override
    public List<BankAccountSummary> getAllBankAccountsSummary() {

        List<BankAccountSummary> bankAccounts = new ArrayList<>();

        MongoCollection<Document> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts");

        collection.aggregate(Arrays.asList(

                // JOIN banks
                Aggregates.lookup(
                        "banks",
                        "bankId",
                        "_id",
                        "bank"
                ),

                // JOIN customers
                Aggregates.lookup(
                        "customers",
                        "customerId",
                        "_id",
                        "customer"
                ),

                // Convert lookup arrays to objects
                Aggregates.unwind("$bank"),
                Aggregates.unwind("$customer"),

                // SELECT the fields we need
                Aggregates.project(
                        Projections.fields(
                                Projections.computed(
                                        "bankAccountId",
                                        "$_id"
                                ),
                                Projections.computed(
                                        "bankName",
                                        "$bank.name"
                                ),
                                Projections.computed(
                                        "customerId",
                                        "$customer._id"
                                ),
                                Projections.computed(
                                        "customerName",
                                        "$customer.firstName"
                                ),
                                Projections.excludeId()
                        )
                )

        )).forEach(document -> {

            bankAccounts.add(
                    new BankAccountSummary(
                            document.getString("bankAccountId"),
                            document.getString("bankName"),
                            document.getString("customerId"),
                            document.getString("customerName")
                    )
            );
        });

        return bankAccounts;
    }

    @Override
    public List<Bank> getAllBanks() {
        MongoCollection<Bank> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("banks", Bank.class);

        return collection.find().into(new ArrayList<>());
    }

    @Override
    public Optional<BankAccount> createBankAccount(BankAccount bankAccount) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        String id = UUID.randomUUID().toString();
        bankAccount.setId(id);

        try {
            collection.insertOne(bankAccount);

            System.out.println(
                    "MongoBankAccountDAO: Successfully created bank account!"
            );

            return Optional.of(bankAccount);

        } catch (MongoException me) {
            System.out.println(
                    "MongoBankAccountDAO: Failed to create bank account!"
            );
            me.printStackTrace();

            return Optional.empty();
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(
            String customerId,
            TransactionAction action,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        // TODO: Harder to implement this since I mainly rely on SQL structure
        // - noSQL schema probably looks way different
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionHistory'");
    }

    @Override
    public boolean closeAccount(String bankAccountId) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        UpdateResult result = collection.updateOne(
                Filters.and(
                        Filters.eq("_id", bankAccountId),
                        Filters.eq("isActive", true)
                ),
                Updates.set("isActive", false)
        );

        return result.getModifiedCount() > 0;
    }

    @Override
    public boolean reactivateAccount(String bankAccountId) {

        MongoCollection<BankAccount> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("bankAccounts", BankAccount.class);

        UpdateResult result = collection.updateOne(
                Filters.and(
                        Filters.eq("_id", bankAccountId),
                        Filters.eq("isActive", false)
                ),
                Updates.set("isActive", true)
        );

        return result.getModifiedCount() > 0;
    }
}
