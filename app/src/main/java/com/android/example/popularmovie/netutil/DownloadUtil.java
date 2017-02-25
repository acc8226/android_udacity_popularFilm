package com.android.example.popularmovie.netutil;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hp on 2017/2/25.
 */

public final class DownloadUtil {

    private static final String LOG_TAG = DownloadUtil.class.getSimpleName();


    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    public static String download(URL url) throws IOException {
        InputStream in = null;
        HttpURLConnection conn = null;
        String result = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 5000ms.
            conn.setReadTimeout(5000);
            // Timeout for connection.connect() arbitrarily set to 5000ms.
            conn.setConnectTimeout(5000);
            // For this use case, set HTTP method to GET.
            conn.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            conn.setDoInput(true);
            // Open communications link (network traffic occurs here).
            conn.connect();

            in = conn.getInputStream();
            if (in != null) {
                // Converts Stream to String
                result = readStream(in);
            }
        } finally {
            // Close Stream and disconnect HTTP connection.
            if (in != null) {
                try{
                    in.close();
                }catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;

    }

    /**
     * Converts the contents of an InputStream to a String.(use BufferedReader)
     */
    public static String readStream(InputStream in) throws IOException {
        //read contents into string builder
        final StringBuilder stringBuilder = new StringBuilder();
        // Read InputStream using the UTF-8 charset.
        final BufferedReader bf = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = bf.readLine()) != null) {
            // Adding a newline isn't necessary (it won't affect parsing)
            // But it does make debugging a *lot* easier if you print out the completed
            // buffer for debugging.
            stringBuilder.append(line).append('\n');
        }
        return stringBuilder.toString();
    }
}
