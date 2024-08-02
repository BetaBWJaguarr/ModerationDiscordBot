package beta.com.moderationdiscordbot.filtersortmodule;

import okhttp3.*;

import java.io.IOException;

public class FilterSortModule {

    private static final String BASE_URL = "http://127.0.0.1:5000/filtermanager/";
    private final OkHttpClient client = new OkHttpClient();
    private final String sessionId;

    public FilterSortModule(String sessionId) {
        this.sessionId = sessionId;
    }

    private Request.Builder createRequestBuilder(String endpoint, String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .addHeader("Authorization", sessionId);
    }

    public String postMatch(String jsonPayload) throws IOException {
        Request request = createRequestBuilder("match", jsonPayload).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public String postSort(String jsonPayload) throws IOException {
        Request request = createRequestBuilder("sort", jsonPayload).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public String postMultiFilter(String jsonPayload) throws IOException {
        Request request = createRequestBuilder("multi_filter", jsonPayload).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}
