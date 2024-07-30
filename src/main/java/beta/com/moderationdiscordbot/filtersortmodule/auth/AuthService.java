package beta.com.moderationdiscordbot.filtersortmodule.auth;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class AuthService {

    private static final String LOGIN_URL = "http://127.0.0.1:500/auth/login";
    private final OkHttpClient client = new OkHttpClient();

    public String login(String email, String password) throws IOException {
        String jsonPayload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getString("session_id");
        }
    }
}
