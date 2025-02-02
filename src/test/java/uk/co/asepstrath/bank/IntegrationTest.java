package uk.co.asepstrath.bank;

import static org.junit.jupiter.api.Assertions.*;

import io.jooby.StatusCode;
import io.jooby.test.JoobyTest;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

@JoobyTest(App.class)
public class IntegrationTest {
    static OkHttpClient client = new OkHttpClient();

    @Test
    public void shouldNotDisplayAccountsData(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/accounts").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.UNAUTHORIZED_CODE, rsp.code());
        }
    }

    @Test
    public void shouldDisplayHomePage(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }

    @Test
    public void shouldCallApi(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/api/transactions").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }
}
