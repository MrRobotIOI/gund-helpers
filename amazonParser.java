import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
//can be reconfigured to retireve different documents from MongoDB ith different filters
public class amazonParser {
    public static void main(String[] args) {


        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb+srv:";
        // Replace the uri string with your MongoDB deployment's connection string

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("Gundam-api");
            MongoCollection<Document> collection = database.getCollection("gundams");
            Bson projectionFields = Projections.fields(
                    Projections.include("name"),
                    Projections.excludeId());
            //find can have empty parameteres  meaning no filters
            MongoCursor<Document> cursor = collection.find(eq("price", "N/A"))
                    .projection(projectionFields)
                    .sort(Sorts.descending("name")).iterator();
            try {
                String jsonString;
                JSONObject obj;
                String[] prodlist = new String[25];
                int i = 0;
                while (cursor.hasNext()) {
                    jsonString = cursor.next().toJson();

                    obj = new JSONObject(jsonString);
                    System.out.println(obj.getString("name"));
                    prodlist[i++] = obj.getString("name");

                }
                //loops for the amount of gundam found
                for (int a = 0; a <= 4; a++) {
                    String name = prodlist[a];

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest getRequest = HttpRequest.newBuilder()
                        // insert api key
                            .uri(new URI("https://api.scraperapi.com/structured/amazon/search?api_key=<insert key here>&query=" + name.replaceAll("\\s", "+") + "&country=ca&tld="))
                            .build();
                    HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

                    String jsonString2 = getResponse.body();
                    JSONObject obj2 = new JSONObject(jsonString2);
                    JSONArray arr = obj2.getJSONArray("results");
                    JSONObject obj3 = arr.getJSONObject(0);

			// insert Mongodb connection string
                    uri = "mongodb+srv:";
                    try (MongoClient mongoClient2 = MongoClients.create(uri)) {
                        MongoDatabase database2 = mongoClient2.getDatabase("Gundam-api");
                        MongoCollection<Document> collection2 = database2.getCollection("gundams");


                        Document query = new Document().append("name", name);
                        Bson updates;


                        if (obj3.isNull("price_string")) {
                            updates = Updates.combine(
                                    // Updates.unset("price"),
                                    //Updates.set("price","N/A")
                                    Updates.addToSet("links", obj3.getString("url"))


                            );
                        } else {
                            updates = Updates.combine(
                                    // Updates.unset("price"),
                                    //Updates.set("price","N/A")
                                    Updates.addToSet("links", obj3.getString("url")),

                                    Updates.set("price", obj3.getString("price_string"))
                            );
                        }
                        UpdateOptions options = new UpdateOptions().upsert(true);
                        try {
                            UpdateResult result = collection2.updateOne(query, updates, options);
                        } catch (MongoException me) {
                            System.err.println("Unable to update due to an error: " + me);
                            System.out.println(name);
                        } catch (JSONException me2) {
                            System.out.println(me2 + " " + name);
                        }
                    }
                }

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                cursor.close();
            }

        }
    }
}