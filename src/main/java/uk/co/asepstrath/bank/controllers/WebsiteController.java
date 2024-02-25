package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.annotation.*;
import io.jooby.exception.StatusCodeException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import uk.co.asepstrath.bank.User;
import uk.co.asepstrath.bank.util.Transaction;
import uk.co.asepstrath.bank.util.TransactionStatus;

/*
    Example Controller is a Controller from the MVC paradigm.
    The @Path Annotation will tell Jooby what /path this Controller can respond to,
    in this case the controller will respond to requests from <host>/example
 */
@Path("/")
public class WebsiteController {
    // private final DataSource dataSource;
    private final Logger logger;
    private final DBController dbController;

    /*
     * This constructor can take in any dependencies the controller may need to
     * respond to a request
     */
    public WebsiteController(DataSource ds, Logger log) {
        logger = log;
        dbController = new DBController(ds);
    }

    /**
     * This "attempts" to complete a transaction.
     *
     * @param sender    Payment from
     * @param recipient Payment to
     * @return
     */
    private UUID tryTransaction(Account sender, Account recipient, BigDecimal amount, String reference) {
        if (sender == null || recipient == null)
            return null; // TODO: This should probably be an unchecked error.

        // Defining things so verbosely might look like a terrible idea,
        // but I implore you to reflect on what it accomplishes.
        // If you dislike this, I suggest you refactor it. :)

        Transaction ts = new Transaction();
        ts.category = recipient.getAccountCategory();
        ts.time = new Timestamp(System.currentTimeMillis());
        ts.status = recipient.isForeign() ? TransactionStatus.PROCESS_DUE : TransactionStatus.OK;
        ts.sender = sender;
        ts.recipient = recipient;
        ts.amount = amount;
        ts.id = UUID.randomUUID();
        ts.reference = reference;

        // Now try committing a transaction.
        // Note: Roleplay is happening here. Say we do have foreign accounts, the sender
        // cannot been foreign if the transaction is happening through our service.
        try {
            sender.withdraw(amount);

            if (recipient.isForeign()) {
                // recipient.deposit(int amount) does not exist here.
                // Maybe a new class is needed to handle this
            } else {
                sender.deposit(amount);
            }
        } catch (ArithmeticException e) {
            ts.status = TransactionStatus.FAILED;
        }

        try {
            dbController.addTransaction(ts);
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
        return ts.id;
    }

    @GET("/")
    public ModelAndView homepage() {
        return new ModelAndView("homePage.hbs");
    }

    @GET("/accounts")
    public ModelAndView printAllAccounts() {
        try {
            List<Account> accounts = dbController.returnAllAccounts();

            if (accounts.isEmpty()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "No accounts found");
            }

            return new ModelAndView("accountTable.hbs").put("accounts", accounts);

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/login")
    public ModelAndView login() {
        return new ModelAndView("login.hbs");
    }

    @POST("/login/save")
    public void login(String username, String password, Context ctx) {
        try {
            String passwordHash = dbController.getSha256Hash(password);

            User user = new User(username, passwordHash);

            UUID userId = dbController.loginUser(user);
            Session session = ctx.session();
            session.put("User_Id",userId.toString());
            ctx.sendRedirect("/profile");

        } catch(StatusCodeException se){
            //Username or password is incorrect, should probably redirect to a new view saying their login is incorrect

            logger.error("Login Error Occurred", se);

            ctx.sendRedirect("/login");
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/register")
    public ModelAndView registerpage() {
        return new ModelAndView("register.hbs");
    }

    @GET("/login")
    public ModelAndView loginpage(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null){
            return new ModelAndView("login.hbs");
        }

        //User logged in
        ctx.sendRedirect("/profile");

        //Was complaining about no return statement so had to return a status code
        throw new StatusCodeException(StatusCode.FOUND);
    }

    @GET("/profile")
    public ModelAndView profilepage(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null){
            ctx.sendRedirect("/login");

            throw new StatusCodeException(StatusCode.I_AM_A_TEAPOT);
        }

        try{
            Account account = dbController.returnAccount(uuid);

            return new ModelAndView("profile.hbs").put("account",account);
        }catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/transactionForm")
    public ModelAndView transactionpage() {
        return new ModelAndView("transactionForm.hbs");
    }

    /*
     * This is the simplest action a controller can perform
     * The @GET annotation denotes that this function should be invoked when a GET
     * HTTP request is sent to <host>/example
     * The returned string will then be sent to the requester
     */
    @GET("/accounts/{name}")
    public String getSingleAccount(@PathParam("name") String name) {
        try {
            Account account = dbController.returnAccount(name);

            return account.toString();
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/overview")
    public ModelAndView overview() {
        return new ModelAndView("overview.hbs");
    }

    /**
     * Returns the user's id if logged in or null if not
     * @param ctx the session context
     * @return User's UUID or null
     */
    private static UUID getUUIDOrNull(Context ctx){
        Session session = ctx.session();
        Instant sessionCreated = session.getCreationTime();
        long sessionLifeSpan = Duration.between(sessionCreated, Instant.now()).toMinutes();

        //Expire the session if it is older than 10 minutes
        if (sessionLifeSpan > 10){
            session.clear();
        }

        try {
            return UUID.fromString(String.valueOf(session.get("User_Id")));
        } catch (IllegalArgumentException e){ //If the user is not logged in
            return null;
        }
    }
}
