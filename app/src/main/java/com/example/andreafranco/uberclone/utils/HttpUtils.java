package com.example.andreafranco.uberclone.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.example.andreafranco.uberclone.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpUtils {

    private static final String TAG = HttpUtils.class.getSimpleName();
    private static final String LOG_TAG = HttpUtils.class.getSimpleName();
    private static final Uri.Builder sUriBuilder = buildUriRoute();

    private static Uri.Builder buildUriRoute() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("directions")
                .appendPath("json")
                .appendQueryParameter("sensor", "false");
        return builder;
    }

    /**
     * User the query parameter for creating url and getting the List of images
     * @param origin
     * @param destination
     * @return
     */
    public static PolylineOptions fetchImageListData(LatLng origin, LatLng destination, Context context) {
        // Origin of route
        String str_origin = origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = destination.latitude + "," + destination.longitude;

        // Api Key
        String api_key = context.getString(R.string.google_maps_key);

        sUriBuilder.appendQueryParameter("origin", str_origin);
        sUriBuilder.appendQueryParameter("destination", str_dest);
        sUriBuilder.appendQueryParameter("key", api_key);
        URL url = createUrl(sUriBuilder.build().toString());

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error during connection", e);
        }

        PolylineOptions polylineOptions = extractDataFromJson(jsonResponse);
        return polylineOptions;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = null;
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error with connection code " + httpURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    private static URL createUrl(String query) {
        URL url = null;
        try {
            url = new URL(URLDecoder.decode(query, "UTF-8"));
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating URL", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error creating URL", e);
        }
        return url;
    }

    private static PolylineOptions extractDataFromJson(String jsonResponse) {
        if (jsonResponse == null || TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        PolylineOptions finalPolylines = null;

        try {
            jObject = new JSONObject(jsonResponse);
            Log.d(TAG,jsonResponse.toString());
            DataParserUtils parser = new DataParserUtils();
            Log.d(TAG, parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            finalPolylines = getPolyLinesFromData(routes);

        } catch (Exception e) {
            Log.d("ParserTask",e.toString());
            e.printStackTrace();
        }

        return finalPolylines;
    }

    private static PolylineOptions getPolyLinesFromData(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        return lineOptions;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected static int getSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    private static Bitmap createBitmap(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return bitmap;
    }
}
