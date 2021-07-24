package com.notmarek.animu;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnimuApi {
    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://animu.notmarek.com";
    String apiUrl = baseUrl + "/fancy";
    private String token;
    public AnimuApi(String token) {
        this.token = token;
    }
    public String getUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", this.token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public JSONArray getFilesFromPath(String path) throws ExecutionException, InterruptedException, JSONException {
        AnimuAsync async = new AnimuAsync(this);
        String response = async.execute(this.apiUrl + path).get();
        return new JSONArray(response);
    }

    public JSONObject updateInfo() throws ExecutionException, InterruptedException, JSONException {
        AnimuAsync async = new AnimuAsync(this);
        String response = async.execute(this.baseUrl + "/app.json").get();
        return new JSONObject(response);
    }

    public JSONArray search(String query) throws ExecutionException, InterruptedException, JSONException {
        AnimuAsync async = new AnimuAsync(this);
        String response = async.execute(this.apiUrl + "/search?q=" + URLEncoder.encode(query)).get();
        // TODO: write the backend function
        return new JSONArray(response);
    }
}

class AnimuAsync extends AsyncTask<String, Void, String> {
    private AnimuApi api;
    private String result = "";
    private boolean finished = false;
    AnimuAsync(AnimuApi api) {
        this.api = api;
    }
    @Override
    protected String doInBackground(String... urls) {
        try {
            return this.api.getUrl(urls[0]);
        } catch (IOException e) {
            return "error";
        }
    }
    protected boolean isFinished() {
        return finished;
    }
    protected String getResult() {
        return this.result;
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        this.finished = true;
        this.result = s;
    }
}
