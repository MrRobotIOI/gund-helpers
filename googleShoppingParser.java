import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.*;
//Ended up never using this, but it uses serpapi to get google shopping json data
public class googleShoppingParser {

    public static void main(String[] args) {

        try {
            // insert api key
            URL url = new URL("https://serpapi.com/search.json?engine=google_shopping&q=RX-78-2+Gundam+Ver.+Ka&google_domain=google.com&gl=ca&hl=en&tbm=shop&api_key=<insert api key>");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Getting the response code
            int responsecode = conn.getResponseCode();

            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {

                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }

                //Close the scanner
                scanner.close();

                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline);

                //Get the required object from the above created object
                JSONObject obj = (JSONObject) data_obj.get("search_information");

                //Get the required data using its key
                System.out.println(obj);

                JSONArray arr = (JSONArray) data_obj.get("shopping_results");

                for (int i = 0; i < arr.size(); i++) {

                    JSONObject new_obj = (JSONObject) arr.get(i);


                    System.out.println("link: " + new_obj.get("product_link"));


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

