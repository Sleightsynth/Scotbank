package uk.co.asepstrath.bank;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import io.jooby.Context;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

@Path("/api")
public class APIController {

    private final DBController db;
    private final OkHttpClient client = new OkHttpClient();

    /*
     * This constructor can take in any dependencies the controller may need to
     * respond to a request
     */
    public APIController(DataSource ds) {
        db = new DBController(ds);
    }

    @GET("/transactions")
    public String test() {
        /*
         * Account test2 = new Account("API Testerson",new BigDecimal(12));
         * JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
         * jo.put("name", "jon doe");
         * jo.put("age", "22");
         * jo.put("city", "chicago");
         */
        return "Hi from API";
    }

    @POST
    @Path("/login")
    public String attemptLogin(Context ctx) {
        String username = ctx.form().get("username").value();
        String password = ctx.form().get("password").value();

        return String.format("Got your form! %s %s", username, password);

    }

    public String generateToken() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request req = new Request.Builder()
                .url("https://api.asep-strath.co.uk/oauth2/token")
                .post(formBody)
                .addHeader("Authorization", Credentials.basic("scotbank", "this1password2is3not4secure"))
                .build();

        try (Response rsp = client.newCall(req).execute()) {
            ResponseBody body = rsp.body();

            if (body == null) {
                throw new IOException("Response body is null!");
            }

            String apiResponse = body.string();

            Gson g = new Gson();

            // De-serialize to an object
            Token token = g.fromJson(apiResponse, Token.class);

            return token.access_token;
        }
    }

    public void populateTables() throws IOException, SQLException {
        db.createTables();

        String authToken = this.generateToken();

        Request req = new Request.Builder()
                .url("https://api.asep-strath.co.uk/api/accounts")
                .get()
                .addHeader("Authorization", authToken)
                .build();

        int statusCode;
        String responseBody;

        try (Response rsp = client.newCall(req).execute()) {
            statusCode = rsp.code();
            ResponseBody body = rsp.body();
            if (body == null) {
                throw new IOException("Response body is null!");
            }

            responseBody = body.string();
        }

        // any 200 code is an OK code.
        if (statusCode < 200 || statusCode > 300) {
            throw new IOException("Status code is not 200! Code: " + statusCode);
        }

        JSONArray jsonArray = new JSONArray(responseBody);

        // first one works, second works for 2 of them
        // for (int i = 0; i < 1; i++) {
        for (int i = 0; i < jsonArray.length(); i++) { // there should be 100 accounts to go through
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            UUID uuid = UUID.fromString(jsonObject.getString("id")); // Assuming "id" is the UUID
            String name = jsonObject.getString("name");
            BigDecimal startingBalance = jsonObject.getBigDecimal("startingBalance");
            boolean roundUpEnabled = jsonObject.getBoolean("roundUpEnabled");

            Random rand = new Random();

            // new email
            String newEmail = name.replaceAll("\\s+", "");
            newEmail = newEmail.concat(".2022@uni.strath.ac.uk");

            // new sort code

            String newSortCode = "%02d-%02d-%02d".formatted(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));

            // new account number

            String newAccountNumber = "%08d".formatted(rand.nextInt(100000000));

            // new password
            int n = rand.nextInt(1000);
            String newPassword = db.getSha512Hash(String.valueOf(n));
            System.out.println("The UUID:" + uuid);
            User testUser = new User(UUID.randomUUID(), newEmail, newPassword,
                    name, "07123 45678", "123 Connor Street", false);

            Account account = new Account(testUser, uuid, newSortCode, newAccountNumber, startingBalance,
                    Boolean.FALSE, AccountCategory.Payment);

            // Get results
            System.out.println(" ");
            System.out.println("UUID: " + uuid);
            System.out.println("Email: " + newEmail);
            System.out.println("Name: " + name);
            System.out.println("Password: " + n);
            System.out.println("Sort Code: " + newSortCode);
            System.out.println("Account Number: " + newAccountNumber);
            System.out.println("Starting Balance: " + startingBalance);
            System.out.println(" ");

            db.addUser(testUser);
            db.addAccount(account);
        }

        User testUser = new User(UUID.randomUUID(), "admin", db.getSha512Hash("admin"),
                "admin", "07123 45678", "123 Connor Street", true);

        Account account = new Account(testUser, UUID.randomUUID(), "00-00-00", "00000000", BigDecimal.valueOf(0),
                Boolean.FALSE, AccountCategory.Payment);

        db.addUser(testUser);
        db.addAccount(account);
    }
    public static void getTransactions () throws IOException, ParserConfigurationException, SAXException {
        //int i = 1;
        int count = 0;
        NodeList nodeList;
        for (int i = 1; i < 20; i++) {
            String apiURL = ("https://api.asep-strath.co.uk/api/transactions?size=1000&page=" + i);
            URL url = new URL(apiURL);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            nodeList = doc.getElementsByTagName("results");
            ArrayList<Transaction> transactions = new ArrayList<>();
            for (int o = 0; o < nodeList.getLength(); o++) {
                Transaction transaction = new Transaction();
                Node node = nodeList.item(o).getFirstChild();
                String timestamp = null;
                String amount = null;
                String sender = null;
                String recipient = null;
                String category = null;

                while (node != null) {
                    switch (node.getNodeName()) {
                        case "timestamp":
                            if (node.getFirstChild() != null) {
                                timestamp = node.getFirstChild().getNodeValue();
                            }
                            break;
                        case "amount":
                            if (node.getFirstChild() != null) {
                                amount = node.getFirstChild().getNodeValue();
                            }
                            break;
                        case "from":
                            if (node.getFirstChild() != null) {
                                sender = node.getFirstChild().getNodeValue();
                            }
                            break;
                        case "id":
                            if (node.getFirstChild() != null) {
                                recipient = node.getFirstChild().getNodeValue();
                            }
                            break;
                        case "type":
                            if (node.getFirstChild() != null) {
                                category = node.getFirstChild().getNodeValue();
                            }
                            break;
                    }
                    node = node.getNextSibling();
                }
                System.out.println(" ");
                System.out.println("Timestamp: " + timestamp);
                System.out.println("Amount: " + amount);
                System.out.println("Sender: " + sender);
                System.out.println("Recipient: " + recipient);
                System.out.println("Category: " + category);
                count += 1;
                System.out.println("Counter: " + count);
                System.out.println(" ");
                //transactions.add(transaction);
            }
            //db.util.


        }
        System.out.println("Out of loop! :D");
    }
}
