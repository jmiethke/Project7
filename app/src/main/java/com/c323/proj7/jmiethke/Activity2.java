package com.c323.proj7.jmiethke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity2 extends AppCompatActivity {
    ListView listView;
    private ProgressDialog progressDialog;
    private String jsonData, getBiggerImage;
    private Map<String, String> genreMap;
    JSONArray movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        getBiggerImage = "https://image.tmdb.org/t/p/w342";
        genreMap = new HashMap<>();
        getMovieList();
    }

    /**
     * Initializes the URLs used to access themoviedb.org's APIs. Then it downloads the movie list
     * and populates the ListView with them.
     */
    private void getMovieList() {
        String movieListURL = "https://api.themoviedb.org/3/discover/movie?api_key=c3e9e6aa6726811f4ec911ca89380f13&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1";
        String genreURL = "https://api.themoviedb.org/3/genre/movie/list?api_key=c3e9e6aa6726811f4ec911ca89380f13";
        progressDialog = ProgressDialog.show(this, "Movie List", "Connecting, please wait...", true, true);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadMovieData().execute(genreURL);
            new DownloadMovieData().execute(movieListURL);
        } else {
            Toast.makeText(this, "No network connection available!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A class that downloads data from a URL and then parses it. Uses AsyncTask to do it in the
     * background.
     */
    private class DownloadMovieData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonData = downloadFromURL(urls[0]);
            return jsonData;
        }

        /**
         * Opens a httpURLConnection and downloads the data from using an Input Stream.
         * All data appended and returned in string result.
         * @param url A string that represents the URL you will download data from.
         * @return A string that holds the data downloaded from the URL
         */
        private String downloadFromURL(String url) {
            InputStream is = null;
            StringBuffer result = new StringBuffer();
            URL myURL = null;
            try {
                myURL = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Http error code: " + responseCode);
                }
                is = connection.getInputStream();
                progressDialog.dismiss();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonRootObject = new JSONObject(jsonData);
                if (jsonRootObject.has("genres")) {
                    generateGenreMap(jsonData);
                } else {
                    parseData(jsonData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * Generates a map that holds the genre data.
         * @param data A string that holds genre data.
         */
        private void generateGenreMap(String data) {
            try {
                JSONObject jsonRootObject = new JSONObject(data);
                JSONArray results = jsonRootObject.getJSONArray("genres");
                JSONObject object;
                String id, name;
                for (int i = 0; i<results.length(); i++) {
                    object = results.getJSONObject(i);
                    id = object.getString("id");
                    name = object.getString("name");
                    genreMap.put(id, name);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * Uses the data provided to generate a movie list. Populates a ListView with
         * the title and the poster image. Sets up onClick listener for each item.
         * @param data a String that holds the data downloaded from the URL.
         */
        private void parseData(String data) {
            try {
                JSONObject jsonRootObject = new JSONObject(data);
                movieList = jsonRootObject.getJSONArray("results");
                List<String> movies = new ArrayList<>();
                for (int i=0;i<movieList.length();i++){
                    try {
                        JSONObject object = movieList.getJSONObject(i);
                        movies.add(object.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listView = findViewById(R.id.listView);
                MovieAdapter movieAdapter = new MovieAdapter(getApplicationContext(), genreMap, movies);
                listView.setAdapter(movieAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    /**
                     * If the item is select, starts Activity 3 with the intent of showing more information
                     * about the item.
                     * @param parent
                     * @param view
                     * @param position
                     * @param id
                     */
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), Activity3.class);
                        String object = movies.get(position);
                        intent.putExtra("object", object);
                        try {
                            JSONObject jsonObject = new JSONObject(object);
                            JSONArray genreArrays = jsonObject.getJSONArray("genre_ids");
                            String[] genres = new String[genreArrays.length()];
                            String k;
                            for (int i = 0; i<genreArrays.length(); i++) {
                                k = ""+genreArrays.getInt(i);
                                genres[i] = genreMap.get(k);
                            }
                            intent.putExtra("genres", genres);
                            intent.putExtra("imageURL", getBiggerImage+jsonObject.getString("poster_path"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }
                });
        } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inflates the menu given.
     * @param menu a menu.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Starts activity one if "Weather Application" was selected in the menu.
     * @param item the menu item that was clicked.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch(id) {
            case R.id.action_weather:
                startActivity(new Intent(Activity2.this, MainActivity.class));
                break;
            case R.id.action_movie:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}