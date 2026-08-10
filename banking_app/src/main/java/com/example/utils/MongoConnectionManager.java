package com.example.utils;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoConnectionManager {

    /*
     * The MongoClient Managed as a singleton -- created once and
     * reused throughout the application. This differs from JDBC
     * Connection objects, which are single-socket connections.
     *
     * MongoClient acts as a connection pool (it manages multiple
     * connections).
     */
    private static MongoClient mongoClient;

    private static final CodecRegistry POJO_CODEC_REGISTRY = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(
                    PojoCodecProvider.builder()
                            .automatic(true)
                            .build()
            )
    );

    public static MongoDatabase getDatabase() {
        return getMongoClient()
                .getDatabase("bank_db")
                .withCodecRegistry(POJO_CODEC_REGISTRY);
    }

    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            String connectionUri = System.getenv("MONGODB_URI");

            if (connectionUri == null || connectionUri.isEmpty()) {
                connectionUri = loadUriFromPropertiesFile();
            }

            if (connectionUri == null || connectionUri.isEmpty()) {
                throw new IllegalStateException(
                        "MongoDB connection URI not found in environment variables or properties file");
            }
            mongoClient = MongoClients.create(connectionUri);

            // Proactively verify the connection, since MongoClient creation
            // alone does NOT guarantee the server is reachable
            verifyConnection(mongoClient);
        }

        return mongoClient;
    }

    private static String loadUriFromPropertiesFile() {
        Properties properties = new Properties();

        // try-with-resources ensures the InputStream is closed automatically
        try (InputStream input = MongoConnectionManager.class
                .getClassLoader()
                .getResourceAsStream("mongo.properties")) {

            if (input == null) {
                System.out.println("mongo.properties file not found on classpath.");
                return null;
            }

            properties.load(input);
            return properties.getProperty("MONGODB_URI");

        } catch (IOException e) {
            System.out.println("Failed to read mongo.properties: " + e.getMessage());
            return null;
        }
    }

    private static void verifyConnection(MongoClient client) {
        try {
            // Running a lightweight "ping" command confirms the server is
            // reachable and credentials are valid
            MongoDatabase adminDatabase = client.getDatabase("admin");
            adminDatabase.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB.");

        } catch (MongoException e) {
            // This is where connection issues actually surface -- NOT at
            // MongoClients.create() -- e.g. bad credentials, unreachable host,
            // or an IP not whitelisted in Atlas Network Access settings
            System.out.println("Failed to connect to MongoDB: " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        // Should be called once, at application shutdown
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
