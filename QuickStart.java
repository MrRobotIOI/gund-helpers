package org.example;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QuickStart {
    public static void main(String[] args) throws IOException {
        // Replace the placeholder with your MongoDB deployment's connection string
        Document doc3 = Jsoup.connect("https://informationislnd.com/list-of-master-grade-mg-ver-ka-gundam/").get();

        Element body4 = doc3.body();
        Elements list = body4.getElementsByTag("strong");

        String[] gunds = new String[list.size()];

// insert Mongodb connection string
        String uri = "mongodb+srv:";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("Gundam-api");
            MongoCollection<org.bson.Document> collection = database.getCollection("gundams");

            //Preparing a document
            //an array for documents each its own gundam
            //a document is made using info from scraper then added into array and also into mongodb


            org.bson.Document document = new org.bson.Document();
            for (int i = 0; i <= list.size() - 1; i++) {

                document.append("name", list.get(i).html());
                document.append("verKa", true);

                //Inserting the document into the collection
                database.getCollection("gundams").insertOne(document);
                document = new org.bson.Document();
            }


            System.out.println("Document inserted successfully");
        }
    }
}
