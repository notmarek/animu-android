package com.notmarek.animu;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnimuApi {
    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://animu.notmarek.com";
    String apiUrl = baseUrl + "/fancy";
    private String token;
    public AnimuApi(String token) {
        this.token = token;
    }

    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", this.token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", this.token)
                .post(body)
                .build();
        try (Response response = this.client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public JSONArray getFilesFromPath(String path) throws ExecutionException, InterruptedException, JSONException {
        AnimuAsyncGet async = new AnimuAsyncGet(this);
        String response = async.execute(this.apiUrl + path).get();
        return new JSONArray(response);
    }

    public JSONObject updateInfo() throws ExecutionException, InterruptedException, JSONException {
        AnimuAsyncGet async = new AnimuAsyncGet(this);
        String response = async.execute(this.baseUrl + "/app.json").get();
        return new JSONObject(response);
    }

    public JSONArray search(String query) throws ExecutionException, InterruptedException, JSONException {
        AnimuAsyncGet async = new AnimuAsyncGet(this);
        String response = async.execute(this.apiUrl + "/search?q=" + URLEncoder.encode(query)).get();
        // TODO: write the backend function
        return new JSONArray(response);
    }

    public JSONObject requestTorrent(String magnet) throws ExecutionException, InterruptedException, JSONException {
        AnimuAsyncPost async = new AnimuAsyncPost(this);
        String response = async.execute(this.apiUrl + "/torrents/request", "{\"link\":\"" + magnet + "\"}").get();
        return new JSONObject(response);
    }

}

class AnimuAsyncGet extends AsyncTask<String, Void, String> {
    private AnimuApi api;
    AnimuAsyncGet(AnimuApi api) {
        this.api = api;
    }
    @Override
    protected String doInBackground(String... urls) {
        try {
            return this.api.get(urls[0]);
        } catch (IOException e) {
            return "error";
        }
    }
}
class AnimuAsyncPost extends AsyncTask<String, Void, String> {
    private AnimuApi api;
    AnimuAsyncPost(AnimuApi api) {
        this.api = api;
    }
    @Override
    protected String doInBackground(String... urls) {
        try {
            return this.api.post(urls[0], urls[1]);
        } catch (IOException e) {
            return "error";
        }
    }
}
