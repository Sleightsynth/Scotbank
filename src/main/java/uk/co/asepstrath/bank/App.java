package uk.co.asepstrath.bank;

import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.controllers.WebsiteController;
import uk.co.asepstrath.bank.util.AccountCategory;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class App extends Jooby {
    {
        /*
         * This section is used for setting up the Jooby Framework modules
         */

        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));

        assets("/assets/*", "/assets");
        assets("/service_worker.js", "/service_worker.js");

        /*
         * Now we set up our controllers and their dependencies
         */
        DataSource ds = require(DataSource.class);
        Logger log = getLog();
        mvc(new WebsiteController(ds, log));
        mvc(new APIController(ds, log));

        /*
         * Finally we register our application lifecycle methods
         */
        onStarted(() -> onStart());
        onStop(() -> onStop());
    }

    public static void main(final String[] args) {
        runApp(args, App::new);
    }

    /*
     * <<<<<<< HEAD
     * This will host any files in src/main/resources/assets on
     * <host>/assets For example in the dice template (dice.hbs) it
     * references "assets/dice.png" which is in resources/assets folder
     * =======
     * This function will be called when the application starts up,
     * it should be used to ensure that the DB is properly setup
     * >>>>>>> UAC
     */

    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        DataSource ds = require(DataSource.class);
        DBController db = new DBController(ds);

        try {
            db.createTables();

            HttpResponse<String> accountResponse = Unirest.get("https://api.asep-strath.co.uk/api/accounts").asString();
            int statusCode = accountResponse.getStatus();
            log.info("API Response Status Code: " + statusCode);

            if (statusCode == 200) {
                String responseBody = accountResponse.getBody();
                log.info("API Response Body: " + responseBody);

                JSONArray jsonArray = new JSONArray(responseBody);
                ArrayList<Account> accounts = new ArrayList<>();
                for (int i = 0; i < 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    UUID uuid = UUID.fromString(jsonObject.getString("id")); // Assuming "id" is the UUID
                    String name = jsonObject.getString("name");
                    BigDecimal startingBalance = jsonObject.getBigDecimal("startingBalance");
                    boolean roundUpEnabled = jsonObject.getBoolean("roundUpEnabled");

                    // Create Account object and add to list


                    //random account number
                    byte[] array2 = new byte[7]; // length is bounded by 7
                    new Random().nextBytes(array2);
                    String generatedAccountNo = new String(array2, StandardCharsets.UTF_8);

                    //random email
                    byte[] array = new byte[7]; // length is bounded by 7
                    new Random().nextBytes(array);
                    String generatedEmail = new String(array, StandardCharsets.UTF_8);

                    //random password
                    Random rand = new Random();
                    int n = rand.nextInt(50);
                    n += 1;

                    //random sortcode
                    byte[] array1 = new byte[7]; // length is bounded by 7
                    new Random().nextBytes(array1);
                    String generatedSortCode = new String(array1, Charset.forName("UTF-8"));


                    User testUser = new User(uuid, generatedEmail, db.getSha512Hash(String.valueOf(n)),
                            name, generatedEmail, generatedEmail, true);
                    db.addUser(testUser);

                    accounts.add(new Account(testUser, uuid, generatedAccountNo, generatedSortCode, startingBalance,
                            Boolean.FALSE, AccountCategory.Payment));
                    db.addAccounts(accounts);
                }

                // Process accounts and add to database
                //db.addAccounts(accounts);

                // Log accounts
                for (Account account : accounts) {
                    System.out.println(account);
                }
            } else {
                log.error("Failed to fetch accounts from API. Status code: " + statusCode);
                this.stop();
            }
        } catch (UnirestException e) {
            log.error("Error during HTTP request", e);
            this.stop();
        } catch (Exception e) {
            log.error("Error during startup", e);
            this.stop();
        }
    }

// previous attempt

    /*
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        DataSource ds = require(DataSource.class);
        DBController db = new DBController(ds);

        try {
            db.createTables();

        HttpResponse<Account> accountResponse = Unirest.get("https://api.asep-strath.co.uk/api/accounts").asObject(Account.class);
        //Account accountObject = accountResponse.getBody();

        if (accountResponse.getBody() != null) {
            JSONArray jsonArray = new JSONArray(accountResponse.getBody());
            ArrayList<Account> accounts = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                String name = jsonObject.getString("name");
                BigDecimal startingBalance = new BigDecimal(jsonObject.getString("startingBalance"));
                boolean roundUpEnabled = jsonObject.getBoolean("roundUpEnabled");

                User testUser = new User(uuid, "connor.waiter.2022@uni.strath.ac.uk", db.getSha512Hash("123"),
                        name, "07123 45678", "123 Connor Street", true);
                db.addUser(testUser);

                accounts.add(new Account(testUser, uuid, "12345678", "12-34-56", startingBalance,
                        Boolean.FALSE, AccountCategory.Payment));
            }

            db.addAccounts(accounts);

            for (Account account : accounts) {
                System.out.println(account);
            }
        } else {
            log.error("Error! Couldn't get data from API :/");
            this.stop();
        }

        } catch(SQLException e){
            log.error("Database Creation Error!", e);
            this.stop();
        }
    }
*/

/* Test below - current issue:

            [2024-03-05 23:40:44,912]-[pool-2-thread-1] INFO  uk.co.asepstrath.bank.App - Starting Up...
            [2024-03-05 23:40:45,039]-[pool-2-thread-1] INFO  uk.co.asepstrath.bank.App - API Response Status Code: 200
            [2024-03-05 23:40:45,040]-[pool-2-thread-1] INFO  uk.co.asepstrath.bank.App - API Response Body: Empty

*/

/*    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        DataSource ds = require(DataSource.class);
        DBController db = new DBController(ds);

        try {
            db.createTables();

            HttpResponse<Account[]> accountResponse = Unirest.get("https://api.asep-strath.co.uk/api/accounts").asObject(Account[].class);

            int statusCode = accountResponse.getStatus();
            log.info("API Response Status Code: " + statusCode);

            String responseBody = accountResponse.getBody() != null ? accountResponse.getBody().toString() : "Empty";
            log.info("API Response Body: " + responseBody);

            if (statusCode == 200) {
                // Process the response if successful
            } else {
                log.error("Failed to fetch accounts from API. Status code: " + statusCode);
                this.stop();
            }
        } catch (UnirestException e) {
            log.error("Error during HTTP request", e);
            this.stop();
        } catch (Exception e) {
            log.error("Error during startup", e);
            this.stop();
        }
    }
*/


// Old Code Below
/*
        User testUser = new User(UUID.randomUUID(), "connor.waiter.2022@uni.strath.ac.uk", db.getSha512Hash("123"),
                "Connor Waiter", "07123 45678", "123 Connor Street", true);

        ArrayList<Account> accounts = new ArrayList<>();

        accounts.add(new Account(testUser, UUID.randomUUID(), "12345678", "12-34-56", BigDecimal.valueOf(100.01),
                Boolean.FALSE, AccountCategory.Payment));

        try {
            db.createTables();
            db.addUser(testUser);
            db.addAccounts(accounts);
        } catch (Exception e) {
            log.error("Database Creation Error", e);
            this.stop();
        }
*/


        /*
         * This function will be called when the application shuts down
         */

    public void onStop () {
        System.out.println("Shutting Down...");
    }
}
