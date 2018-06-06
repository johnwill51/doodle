package com.doodle.chat.db;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.ConnectionString;
import org.bson.Document;

import java.util.concurrent.CountDownLatch;

public class Driver {

    private final String HOST = "localhost";
    private final String PORT = "27017";
    private final String DB = "chat";

    public void connect() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);

        final ConnectionString connectionString =
                new ConnectionString("mongodb://" + HOST + ":" + PORT + "/" + DB);
        final MongoClient mongoClient = MongoClients.create(connectionString);

        final MongoDatabase database = mongoClient.getDatabase("chat");
        final MongoCollection<Document> collection = database.getCollection("users");

        SingleResultCallback<Void> cb = (result, t) -> System.out.println("Inserted!");

        final Document doc1 = new Document("name", "marzio");
        final Document doc2 = new Document("name", "giulia");
        final Document doc3 = new Document("name", "eliana");
        collection.insertOne(doc1, cb);
        collection.insertOne(doc2, cb);
        collection.insertOne(doc3, cb);

        Block<Document> printDocumentBlock = document -> System.out.println(document.toJson());

        SingleResultCallback<Void> callbackWhenFinished = (result, t) -> {
            System.out.println("Operation Finished!");
            latch.countDown();
        };

        collection.find().forEach(printDocumentBlock, callbackWhenFinished);

        latch.await();
    }
}
