package uk.co.asepstrath.bank;

import javax.sql.DataSource;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import io.jooby.Context;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import org.w3c.dom.NodeList;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Token;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
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
                .addHeader("Accept","application/json")
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
            String newEmail = name.replaceAll("\\.+", "")
                    .replaceAll("\\s+", ".").toLowerCase();
            newEmail = newEmail.concat(".2022@uni.strath.ac.uk");

            // new sort code

            String newSortCode = "%02d-%02d-%02d".formatted(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));

            // new account number

            String newAccountNumber = "%08d".formatted(rand.nextInt(100000000));

            // new password
            int n = rand.nextInt(1000);
            String newPassword = db.getSha512Hash(String.valueOf(n));
            User testUser = new User(UUID.randomUUID(), newEmail, newPassword,
                    name, "07123 45678", "45 Waterloo Street", false);

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

        User testUser = new User(UUID.randomUUID(), "Admin", db.getSha512Hash("Admin"),
                "Admin", "07123 45678", "45 Waterloo Street", true);

        Account account = new Account(testUser, UUID.randomUUID(), "00-00-00", "00000000", BigDecimal.valueOf(0),
                Boolean.FALSE, AccountCategory.Payment);

        db.addUser(testUser);
        db.addAccount(account);
    }

    public void getTransactions () throws IOException, SQLException {
        int count = 0;
        for (int i = 0; i < 16; i++) {
            String authToken = this.generateToken();

            Request req = new Request.Builder()
                    .url("https://api.asep-strath.co.uk/api/transactions?size=1000&page=" + i)
                    .get()
                    .addHeader("Accept","application/json")
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
            Gson gson = new Gson();

            TransactionResponsePage apiResponse = gson.fromJson(responseBody, TransactionResponsePage.class);

            System.out.printf("Processing transactions. Page %d/16\n",i+1);

            for (int o = 0; o < apiResponse.results.size()-1; o++) {

                /*if(count==318){
                    System.out.println();
                }*/
                Transaction transaction = apiResponse.results.get(o);
                transaction.status = TransactionStatus.OK;
                try{
                    UUID senderAccountID = UUID.fromString(transaction.from);
                    transaction.sender = db.returnAccountFromId(senderAccountID);
                    transaction.sender.withdraw(transaction.amount);
                    db.updateBalance(transaction.sender);
                } catch (Exception ignored){
                    transaction.reference = transaction.from;
                }
                try{
                    UUID recipientAccountID = UUID.fromString(transaction.to);
                    transaction.recipient = db.returnAccountFromId(recipientAccountID);
                    transaction.recipient.deposit(transaction.amount);
                    db.updateBalance(transaction.recipient);
                } catch (Exception ignored){
                    transaction.reference = transaction.to;
                }
                transaction.time = new Timestamp(new java.util.Date().getTime());
                //System.out.println("Transaction num: "+count+" UUID: "+transaction.id);
                db.addTransaction(transaction);
                count++;
            }
        }
        System.out.println(count+" transactions processed");
    }
}
