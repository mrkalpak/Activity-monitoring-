package com.example.finalyearproject_android.DietSug;

import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIService extends AsyncTask<String, Void, String> {

    private OnDataReceivedListener onDataReceivedListener;

    public interface OnDataReceivedListener {
        void onDataReceived(String result);
    }

    public void setOnDataReceivedListener(OnDataReceivedListener listener) {
        this.onDataReceivedListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String jsonData = params[1];

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                // Set up the HTTP request method and headers
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                // Write the JSON data to the request body
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(jsonData.getBytes());
                outputStream.flush();
                outputStream.close();

                // Get the response from the server
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response
                    // This assumes the response is in text format; adjust if your API returns JSON
                    return readStream(urlConnection.getInputStream());
                } else {
                    // Handle the error response
                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readStream(java.io.InputStream inputStream) {
        java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (onDataReceivedListener != null) {
            onDataReceivedListener.onDataReceived(result);
        }
    }
}
