package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;

import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import kong.unirest.core.UnirestException;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.controllers.WebsiteController;
import uk.co.asepstrath.bank.util.AccountCategory;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionStatus;
import javax.sql.DataSource;

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
        mvc(new APIController(ds));

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
        APIController api = new APIController(ds);

        try {
            api.populateTables();
        } catch (UnirestException e) {
            log.error("Error during HTTP request", e);
            this.stop();
        } catch (Exception e) {
            log.error("Error during startup", e);
            this.stop();
        }
        api.getTransactions();
    }

// previous attempt

    /*
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        DataSource ds = require(DataSource.class);
        DBController db = new DBController(ds);

        User testUser = new User(UUID.randomUUID(), "admin", db.getSha512Hash("123"),
                "Connor Waiter", "07123 45678", "123 Connor Street", true);
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
                "Connor Waiter", "07123 45678", "123 Connor Street",true);

        ArrayList < Account > accounts = new ArrayList < > ();

        Account account = new Account(testUser, UUID.randomUUID(), "12345678", "12-34-56", BigDecimal.valueOf(100.01),
                Boolean.FALSE, AccountCategory.Payment);
        accounts.add(account);

        try {
            db.createTables();
            db.addUser(testUser);
            db.addAccounts(accounts);
            Transaction transaction = db.tryTransaction(account, account, BigDecimal.valueOf(50), "Hello!");
            log.info("Transaction ref: " + transaction.toString());
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
