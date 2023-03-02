package com.c323.proj7.jmiethke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Activity3 extends AppCompatActivity {
    String title, lang, genre, releaseDate, imageURL;
    String[] genres;
    TextView tv_genre, tv_title, tv_lang, tv_releaseDate, tv_description;
    ImageView imageView;

    /**
     * Sets up the TextViews and ImageView with the information from the intent.
     * @param savedInstanceState not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        imageView = findViewById(R.id.iv_act3_posterImage);
        tv_genre = findViewById(R.id.tv_act3_genre);
        tv_title = findViewById(R.id.tv_act3_title);
        tv_lang = findViewById(R.id.tv_act3_lang);
        tv_releaseDate = findViewById(R.id.tv_act3_releaseDate);
        tv_description = findViewById(R.id.tv_act3_description);
        Intent intent = getIntent();
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("object"));
            title = object.getString("title");
            lang = object.getString("original_language");
            releaseDate = object.getString("release_date");
            tv_title.setText("Title: "+title);
            tv_lang.setText("Language: "+lang);
            tv_releaseDate.setText("Release Date: "+releaseDate);
            tv_description.setText("Description: "+object.get("overview"));

            genres = intent.getStringArrayExtra("genres");
            genre = "Genre: ";
            for (int i = 0; i< genres.length; i++) {
                if (i == (genres.length - 1)) {
                    genre = genre + genres[i];
                } else {
                    genre = genre + genres[i] + ", ";
                }
            }
            tv_genre.setText(genre);

            imageURL = intent.getStringExtra("imageURL");
            LoadImage loadImage = new LoadImage(imageView);
            loadImage.execute(imageURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A class to download images from the themoviedb's API. Sets the relevant image to
     * the imageview.
     */
    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public LoadImage(ImageView imageView) {
            this.imageView = imageView;
        }

        /**
         * Opens a httpURLConnection to download the image using an Input Stream.
         * @param strings A String Array where the first string is the relevant URL.
         * @return a bitmap that represents the image downloaded.
         */
        @Override
        protected Bitmap doInBackground(String... strings) {
            InputStream is = null;
            Bitmap bitmap = null;
            URL myURL = null;
            try {
                myURL = new URL(strings[0]);
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
                bitmap = BitmapFactory.decodeStream(is);
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        /**
         * Sets the bitmap to the imageView.
         * @param bitmap a bitmap that was downloaded.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
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
     * Starts Activity One if "Weather Application" was selected.
     * Starts Activity Two if "Movie Application" was selected.
     * @param item the menu item that was clicked.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch(id) {
            case R.id.action_weather:
                startActivity(new Intent(Activity3.this, MainActivity.class));
                break;
            case R.id.action_movie:
                startActivity(new Intent(Activity3.this, Activity2.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}