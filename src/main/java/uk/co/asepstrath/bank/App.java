package uk.co.asepstrath.bank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;

import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import uk.co.asepstrath.bank.controllers.DBController;
import uk.co.asepstrath.bank.controllers.WebsiteController;
import uk.co.asepstrath.bank.util.AccountCategory;

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

        User testUser = new User(UUID.randomUUID(), "connor.waiter.2022@uni.strath.ac.uk", db.getSha512Hash("123"),
                "Connor Waiter", "07123 45678", "123 Connor Street");

        ArrayList < Account > accounts = new ArrayList < > ();

        accounts.add(new Account(testUser.getId(), "12345678", "12-34-56", BigDecimal.valueOf(100.01),
                Boolean.FALSE, AccountCategory.Payment));

        try {
            db.createTables();
            db.addUser(testUser);
            db.addAccounts(accounts);
        } catch (Exception e) {
            log.error("Database Creation Error", e);
            this.stop();
        }
    }

    /*
     * This function will be called when the application shuts down
     */
    public void onStop() {
        System.out.println("Shutting Down...");
    }
}