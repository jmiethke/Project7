package com.c323.proj7.jmiethke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MovieAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> movieList;
    Map genreMap;
    private String getImage, apiImage;
    public MovieAdapter(@NonNull Context context, Map map, List<String> movies) {
        super(context, R.layout.list_item, R.id.tv_list_item, movies);
        this.context = context;
        this.movieList = movies;
        this.genreMap = map;
    }

    /**
     * Inflates a new view if it is null. Sets the ImageView and TextView to the relevant
     * information.
     * @param position
     * @param convertView
     * @param parent
     * @return the inflated view.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View singleItem = convertView;
        MovieViewHolder holder = null;
        if (singleItem == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            singleItem = layoutInflater.inflate(R.layout.list_item, parent, false);
            holder = new MovieViewHolder(singleItem);
            singleItem.setTag(holder);
        } else {
            holder = (MovieViewHolder) singleItem.getTag();
        }
        apiImage = null;
        getImage = "https://image.tmdb.org/t/p/w154";
        try {
            JSONObject object = new JSONObject(movieList.get(position));
            apiImage = getImage+object.getString("poster_path");
            LoadImage loadImage = new LoadImage(holder.itemImage);
            loadImage.execute(apiImage);
            holder.movieName.setText(object.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return singleItem;
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
}
