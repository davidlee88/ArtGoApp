package com.ArtGo.ArtGoApp.utils; /**
 * Created by MoaoranLi on 2016/4/19.
 */


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class RestClient {

    private static final String BASE_URI = "https://microsoft-apiapp95aeaaf2c782424e88fb244f90cafcd9.azurewebsites.net/api/Locations";
    //private static URL url;

    public static JSONArray getAllCategoriesData() {
        URL url;
        JSONArray response = new JSONArray();
        BufferedReader reader = null;
        String requestURL = BASE_URI + "/getTypes";
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                InputStream stream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String textResult = "";
                while ((textResult = reader.readLine()) != null) {
                    buffer.append(textResult);
                }
                String finalJson = buffer.toString();
                response = new JSONArray(finalJson);
            } else {
                response = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return response;
    }

    public static JSONArray getLocationsByCategory(String typeId) {
        URL url;
        JSONArray response = new JSONArray();
        BufferedReader reader = null;
        String requestURL = BASE_URI + "/getLocationByType/" + typeId;
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                InputStream stream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String textResult = "";
                while ((textResult = reader.readLine()) != null) {
                    buffer.append(textResult);
                }
                String finalJson = buffer.toString();
                response = new JSONArray(finalJson);
            } else {
                response = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return response;
    }

    public static JSONObject getLocationDetailById(String id) {
        URL url;
        JSONObject response = new JSONObject();
        BufferedReader reader = null;
        String requestURL = BASE_URI + "/getLocationDetailById/" + id;
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                InputStream stream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String textResult = "";
                while ((textResult = reader.readLine()) != null) {
                    buffer.append(textResult);
                }
                String finalJson = buffer.toString();
                String finalStr = finalJson.replace("[", "").replace("]", "");
                response = new JSONObject(finalStr);

            } else {
                response = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return response;
    }


}
