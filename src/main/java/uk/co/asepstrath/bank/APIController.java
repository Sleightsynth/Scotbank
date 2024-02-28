package uk.co.asepstrath.bank;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.POST;
import io.jooby.annotation.Path;

import org.slf4j.Logger;
import org.json.JSONObject;
import javax.sql.DataSource;
import java.math.BigDecimal;

@Path("/api")
public class APIController {

  private final DataSource dataSource;
  private final Logger logger;

  /*
   * This constructor can take in any dependencies the controller may need to
   * respond to a request
   */
  public APIController(DataSource ds, Logger log) {
    dataSource = ds;
    logger = log;
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
}
