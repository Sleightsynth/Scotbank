package uk.co.asepstrath.bank.controllers;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.StatusCode;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;
import io.jooby.exception.StatusCodeException;
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

    @GET("/")
    public ModelAndView homepage(Context ctx) {
        return getUserAndAddToTemplate("homePage.hbs", getUUIDOrNull(ctx));
    }

    @GET("/accounts")
    public ModelAndView printAllAccounts(Context ctx) {
        UUID userId = getUUIDOrNull(ctx);

        if (userId == null)
            throw new StatusCodeException(StatusCode.UNAUTHORIZED);
        try {
            User user = dbController.returnUser(userId);

            if (!user.isAdmin())
                throw new StatusCodeException(StatusCode.UNAUTHORIZED);

            List<Account> accounts = dbController.returnAllAccounts();

            if (accounts.isEmpty()) {
                throw new StatusCodeException(StatusCode.NOT_FOUND, "No accounts found");
            }
            ModelAndView model = new ModelAndView("managerView.hbs").put("accounts", accounts);
            return getUserAndAddToTemplate(model, userId);

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @POST("/login/save")
    public void login(String username, String password, Context ctx) {
        try {
            String passwordHash = dbController.getSha512Hash(password);
            User user = new User(username, passwordHash);

            UUID userId = dbController.loginUser(user);
            Session session = ctx.session();
            session.put("User_Id", userId.toString());
            ctx.sendRedirect("/profile");
        } catch (StatusCodeException se) {
            // Username or password is incorrect, should probably redirect to a new view
            // saying their login is incorrect

            // logger.error("Login Error Occurred", se);
            //
            // ctx.sendRedirect("/login");
            ctx.send(StatusCode.UNAUTHORIZED);
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/logout")
    public void logout(Context ctx) {
        Session session = ctx.session();

        session.destroy();

        ctx.sendRedirect("/");
    }

    @GET("/register")
    public ModelAndView registerpage(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null) {
            return getUserAndAddToTemplate("register.hbs", uuid);
        }
        ctx.sendRedirect("/profile");
        throw new StatusCodeException(StatusCode.FOUND);
    }

    @GET("/login")
    public ModelAndView loginpage(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null) {
            return getUserAndAddToTemplate("login.hbs", uuid);
        }

        // User logged in
        ctx.sendRedirect("/profile");

        // Was complaining about no return statement so had to return a status code
        throw new StatusCodeException(StatusCode.FOUND);
    }

    @GET("/profile")
    public ModelAndView profilepage(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null) {
            ctx.sendRedirect("/login");

            throw new StatusCodeException(StatusCode.I_AM_A_TEAPOT);
        }

        try {
            User user = dbController.returnUser(uuid);
            Account account = dbController.returnAccount(user);

            ModelAndView modelAndView = new ModelAndView("profile.hbs").put("account", account);

            return getUserAndAddToTemplate(modelAndView, uuid);
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/transactionForm")
    public ModelAndView transactionpage(Context ctx) {
        return getUserAndAddToTemplate("transactionForm.hbs", getUUIDOrNull(ctx));
    }

    @GET("/contact")
    public ModelAndView contactpage(Context ctx) {
        return getUserAndAddToTemplate("contact.hbs", getUUIDOrNull(ctx));
    }

    @GET("/savingsPot")
    public ModelAndView savingspage(Context ctx) {
        return getUserAndAddToTemplate("savingsPot.hbs", getUUIDOrNull(ctx));
    }

    @GET("/spendingSummary")
    public ModelAndView spendingPage(Context ctx) {
        return getUserAndAddToTemplate("spendingSummary.hbs", getUUIDOrNull(ctx));
    }

    @GET("/overview")
    public ModelAndView overview(Context ctx) {

        UUID uuid = getUUIDOrNull(ctx);

        if (uuid == null) {
            ctx.sendRedirect("/login");

            throw new StatusCodeException(StatusCode.I_AM_A_TEAPOT);
        }

        try {
            User user = dbController.returnUser(uuid);
            Account account = dbController.returnAccount(user);
            List<Transaction> transactions = dbController.returnTransactions(account);
            logger.info("size of transactions = " + transactions.size());
            ModelAndView modelAndView = new ModelAndView("overview.hbs").put("account", account).put("transactions",
                    transactions);

            return getUserAndAddToTemplate(modelAndView, uuid);
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    /**
     * Returns the user's id if logged in or null if not
     *
     * @param ctx the session context
     * @return User's UUID or null
     */
    private static UUID getUUIDOrNull(Context ctx) {
        Session session = ctx.session();
        Instant sessionCreated = session.getCreationTime();
        long sessionLifeSpan = Duration.between(sessionCreated, Instant.now()).toSeconds();

        // Expire the session if it is older than 10 minutes
        if (sessionLifeSpan > 600) {
            session.destroy();
        }

        try {
            return UUID.fromString(String.valueOf(session.get("User_Id")));
        } catch (IllegalArgumentException e) { // If the user is not logged in
            return null;
        }
    }

    private ModelAndView getUserAndAddToTemplate(String hbsFileName, UUID uuid) {
        return this.getUserAndAddToTemplate(new ModelAndView(hbsFileName), uuid);
    }

    private ModelAndView getUserAndAddToTemplate(ModelAndView model, UUID uuid) {
        User user;
        if (uuid == null) {
            return model.put("userLoggedIn", Boolean.FALSE);
        }
        try {
            user = dbController.returnUser(uuid);
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred", e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }

        return model.put("userLoggedIn", Boolean.TRUE).put("user", user);
    }
}