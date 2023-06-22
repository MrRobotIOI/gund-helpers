import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import org.json.*;

import static com.mongodb.client.model.Filters.eq;
//uses youtube data api to get videos of gundam
//can be reconfigured to retireve different documents from MongoDB ith different filters
public class gundVideo {

    public static void main(String[] args) {

        try {
            String uri = "mongodb+srv:";
            // Replace the uri string with your MongoDB deployment's connection string

            try (MongoClient mongoClient = MongoClients.create(uri)) {
                MongoDatabase database = mongoClient.getDatabase("Gundam-api");
                MongoCollection<Document> collection = database.getCollection("gundams");
                Bson projectionFields = Projections.fields(
                        Projections.include("name"),
                        Projections.excludeId());
                MongoCursor<Document> cursor = collection.find()
                        .projection(projectionFields)
                        .sort(Sorts.descending("name")).iterator();

                String jsonString;
                org.json.JSONObject obj;
                String[] prodlist = new String[25];
                int i= 0;
                while (cursor.hasNext()) {
                    jsonString = cursor.next().toJson();

                    obj = new org.json.JSONObject(jsonString);
                    System.out.println(obj.getString("name"));
                    prodlist[i++]=obj.getString("name");

                }
              for( int a= 0; a<=22; a++) {
                    String name = prodlist[a];

                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest getRequest = HttpRequest.newBuilder()
                    // insert api key
                            .uri(new URI("https://youtube.googleapis.com/youtube/v3/search?part=snippet&q=" + name.replaceAll("\\s", "+") + "&key=<insert api key>"))
                            .build();
                    HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

                    String jsonString2 = getResponse.body();
                    org.json.JSONObject obj2 = new org.json.JSONObject(jsonString2);
                    JSONArray arr = obj2.getJSONArray("items");
                    JSONObject obj3 = arr.getJSONObject(4);
                JSONObject obj4 = obj3.getJSONObject("id");
                String embedid= obj4.getString("videoId");
                        System.out.println(embedid);


// insert Mongodb connection string
                    uri = "mongodb+srv:";
                    try (MongoClient mongoClient2 = MongoClients.create(uri)) {
                        MongoDatabase database2 = mongoClient2.getDatabase("Gundam-api");
                        MongoCollection<Document> collection2 = database2.getCollection("gundams");


                        Document query = new Document().append("name", name);
                        Bson updates;



                            updates = Updates.combine(
                                    // Updates.unset("price"),
                                    //Updates.set("price","N/A")


                                    Updates.set("embedId",embedid )
                            );
                        UpdateOptions options = new UpdateOptions().upsert(true);
                        try {
                            UpdateResult result = collection2.updateOne(query, updates, options);
                        } catch (MongoException me) {
                            System.err.println("Unable to update due to an error: " + me);
                            System.out.println(name);
                        }
                        catch (JSONException me2){
                            System.out.println(me2+" "+name);
                        }
                    }
                    finally {
                        cursor.close();
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

