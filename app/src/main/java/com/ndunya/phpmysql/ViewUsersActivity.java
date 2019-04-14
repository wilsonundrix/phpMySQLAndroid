package com.ndunya.phpmysql;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewUsersActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        new AsyncFetch().execute();

    }

    // Create class AsyncFetch
    @SuppressLint("StaticFieldLeak")
    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(ViewUsersActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://192.168.43.252/android/view_users.inc.php");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and receive data
                conn.setDoInput(true);
                conn.setDoOutput(true);


            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        System.out.println(line);
                    }

                    // Pass data to onPostExecute method
                    System.out.println(result.toString());
                    return (result.toString());

                } else {
                    return ("Connection error");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            List<DataUser> data = new ArrayList<>();

            pdLoading.dismiss();
            if (result.equals("No Users")) {
                Toast.makeText(ViewUsersActivity.this, "No Users found.", Toast.LENGTH_LONG).show();
            } else {

                try {

                    JSONArray jArray = new JSONArray(result);

                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        DataUser userData = new DataUser();
                        userData.email = json_data.getString("email");
                        userData.username = json_data.getString("username");
                        data.add(userData);
                    }

                    // Setup and Handover data to recycler view
                    RecyclerView mRVUser = findViewById(R.id.usersList);
                    AdapterUser mAdapter = new AdapterUser(ViewUsersActivity.this, data);
                    mRVUser.setAdapter(mAdapter);
                    mRVUser.setLayoutManager(new LinearLayoutManager(ViewUsersActivity.this));

                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(result);
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(ViewUsersActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(ViewUsersActivity.this, result, Toast.LENGTH_LONG).show();
                }

            }

        }

    }
}