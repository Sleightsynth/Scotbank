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
import java.sql.SQLException;
import java.util.ArrayList;
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
    }
    public void onStop () {
        System.out.println("Shutting Down...");
    }
}
