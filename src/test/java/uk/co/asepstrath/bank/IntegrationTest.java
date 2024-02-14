package uk.co.asepstrath.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void shouldDisplayAccountsData(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/accounts").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }

    @Test
    public void shouldDisplaySingleAccountData(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/accounts/Joey").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }

    @Test
    public void shouldDisplayHomePage(int serverPort) throws IOException {
        Request req = new Request.Builder().url("http://localhost:" + serverPort + "/").build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }
}
