package uk.co.asepstrath.bank;

import uk.co.asepstrath.bank.example.ExampleController;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

public class App extends Jooby {

  {
    /*
     * This section is used for setting up the Jooby Framework modules
     */

    install(new UniRestExtension());
    install(new HandlebarsModule());
    install(new HikariModule("mem"));

    /*
     * This will host any files in src/main/resources/assets on <host>/assets
     * For example in the dice template (dice.hbs) it references "assets/dice.png"
     * which is in resources/assets folder
     */
    assets("/assets/*", "/assets");
    assets("/service_worker.js", "/service_worker.js");

    /*
     * Now we set up our controllers and their dependencies
     */
    DataSource ds = require(DataSource.class);
    Logger log = getLog();

    mvc(new ExampleController(ds, log));

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
   * This function will be called when the application starts up,
   * it should be used to ensure that the DB is properly setup
   */
  public void onStart() {
    Logger log = getLog();
    log.info("Starting Up...");
    ArrayList<Account> accounts = new ArrayList<>();
    accounts.add(new Account("Rachel", BigDecimal.valueOf(50.00)));
    accounts.add(new Account("Monica", BigDecimal.valueOf(100.00)));
    accounts.add(new Account("Phoebe", BigDecimal.valueOf(76.00)));
    accounts.add(new Account("Joey", BigDecimal.valueOf(23.90)));
    accounts.add(new Account("Chandler", BigDecimal.valueOf(3.00)));
    accounts.add(new Account("Ross", BigDecimal.valueOf(54.32)));

    // Fetch DB Source
    DataSource ds = require(DataSource.class);
    // Open Connection to DB
    try (Connection connection = ds.getConnection()) {
      Statement stmt = connection.createStatement();

      stmt.executeUpdate(
          "CREATE TABLE `Users`"
              + "("
              + "`Id` UUID PRIMARY KEY UNIQUE, "
              + "`Name` varchar(30),"
              + "`Email` varchar(50),"
              + "`Hash_pass` varchar(32)," // use md5 for hashing
              + "PRIMARY KEY (`Id`)"
              + ")");
      stmt.executeUpdate(
          "CREATE TABLE `Session`"
              + "("
              + "`Id` UUID PRIMARY KEY UNIQUE, "
              + "`User_id` UUID," // This can be Null.
              + "`Expiry` timestamp,"
              + "PRIMARY KEY (`Id`),"
              + "FOREIGN KEY (`User_id`) REFERENCES Users(`Id`)"
              + ")");

      stmt.executeUpdate(
          "CREATE TABLE `Accounts` "
              + "("
              + "`Id` UUID PRIMARY KEY UNIQUE,"
              + "`User_id` UUID,"
              + "`Name` varchar(255),"
              + "`AccountBalance` decimal,"
              + "PRIMARY KEY (`Id`),"
              + "FOREIGN KEY (`User_id`) REFERENCES Users(`Id`)"
              + ")");
      stmt.executeUpdate(
          "CREATE TABLE `Transcation` "
              + "("
              + "`Timestamp` timestamp,"
              + "`Amount` decimal,"
              + "`Ref` varchar(30),"
              + "`Category` varchar(255),"
              + "`Status` varchar(30),"
              + "`Type` varchar(30),"
              + "`Id` UUID PRIMARY KEY UNIQUE,"
              + "`Recipient` varchar(255),"
              + "`Sender` varchar(255),"
              + "PRIMARY KEY ( `Id` ),"
              + "FOREIGN KEY ( `Recipient` ) REFERENCES Accounts(`Id`),"
              + "FOREIGN KEY ( `Sender` ) REFERENCES Accounts(`Id`)"
              + ")");

      for (Account acc : accounts) {
        PreparedStatement prepStmt = connection.prepareStatement(
            String.format("INSERT INTO Accounts " + "VALUES (?, NULL, '%s', '%f')", acc.getName(),
                acc.getBalance().floatValue()));
        prepStmt.setObject(1, acc.getUUID());
        prepStmt.execute();
      }

      stmt.executeUpdate("CREATE TABLE `Example` (`Key` varchar(255),`Value` varchar(255))");
      stmt.executeUpdate("INSERT INTO Example " + "VALUES ('WelcomeMessage', 'Welcome to A Bank')");
    } catch (SQLException e) {
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
