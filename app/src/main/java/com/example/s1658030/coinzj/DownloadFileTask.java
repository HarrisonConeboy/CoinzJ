package com.example.s1658030.coinzj;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//Class is identical to that shown in lectures

public class DownloadFileTask extends AsyncTask<String,Void,String> {

    @Override
    protected String doInBackground(String... urls) {
        try {
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e) {
            return "Unable to load content. Check your connection";
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException {
        return readStream(downloadUrl(new URL(urlString)));
    }

    @NonNull
    private String readStream(InputStream stream)
        throws IOException {

        //Read input from stream, build result as a string
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        DownloadCompleteRunner.downloadComplete(result);
    }

}
