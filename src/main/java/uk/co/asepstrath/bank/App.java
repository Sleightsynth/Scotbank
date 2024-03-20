package uk.co.asepstrath.bank;

import javax.sql.DataSource;

import org.slf4j.Logger;

import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import kong.unirest.core.UnirestException;
import uk.co.asepstrath.bank.controllers.WebsiteController;

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
            api.getTransactions();
        } catch (UnirestException e) {
            log.error("Error during HTTP request", e);
            this.stop();
        } catch (Exception e) {
            log.error("Error during startup", e);
            this.stop();
        }
    }

        /*
         * This function will be called when the application shuts down
         */

    public void onStop () {
        System.out.println("Shutting Down...");
    }
}
