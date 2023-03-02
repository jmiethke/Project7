package com.c323.proj7.jmiethke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText et_Loc;
    TextView tv_Description, tv_Weather, tv_Temp, tv_FeelsLike;
    ImageView iv_weather;

    private Geocoder geocoder;
    private List<Address> addressList;
    private List<Address> locationInput;
    private Double lat, lng;
    ProgressDialog progressDialog;
    private String jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_Loc = findViewById(R.id.editTextLoc);
        tv_Description = findViewById(R.id.tv_Description);
        tv_Weather = findViewById(R.id.tv_TodaysWeather);
        tv_Temp = findViewById(R.id.tv_Temp);
        tv_FeelsLike = findViewById(R.id.tv_FeelsLike);
        iv_weather = findViewById(R.id.iv_weather);
        startingBackGroundWork();
    }


    /**
     * Sets up a LocationManager and LocationListener. Initializes the first address
     * in the addressList to be New York City so if the button "Get Weather Data" is pressed,
     * information about NYC will appear.
     */
    public void startingBackGroundWork(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationManager myLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            lat = location.getLatitude(); lng = location.getLongitude();
                        }
                    };
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    }
                    else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 13);
                        }
                    }
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    addressList = geocoder.getFromLocationName("New York City", 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (addressList != null) {
                            lat = addressList.get(0).getLatitude();
                            lng = addressList.get(0).getLongitude();
                        }
                    }
                });
            }
        });
    }


    /**
     * A method setup for buttonWeather's onClick. Uses New York City as default if nothing has been
     * typed. Uses input in the EditText to get weather data from openweathermap's API.
     * @param view
     */
    public void getWeatherData(View view) {
        String weatherLat = null, weatherLng = null;
        String weatherState = null;
        String weatherCity = null;
        String userLocInput = et_Loc.getText().toString();
        locationInput = null;
        if (TextUtils.isEmpty(userLocInput)) {
            weatherLat = ""+lat;
            weatherLng = ""+lng;
        }
        else if (userLocInput.contains(",")) {
            int indexComma = userLocInput.indexOf(",");
            String inputLat = userLocInput.substring(0,indexComma);
            userLocInput = userLocInput.substring(indexComma+1);
            String inputLng = userLocInput;
            try {
                locationInput = geocoder.getFromLocation(Double.parseDouble(inputLat), Double.parseDouble(inputLng), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (locationInput == null) {
                 Toast.makeText(this, "Enter a valid set of Lat/Long", Toast.LENGTH_SHORT).show();
            }
            else {
                 weatherLat = inputLat;
                 weatherLng = inputLng;
                }
            }
        else {
            try {
                locationInput = geocoder.getFromLocationName(userLocInput,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (locationInput == null) {
                Toast.makeText(this, "Enter a valid city.", Toast.LENGTH_SHORT).show();
            }
            else {
                weatherCity = userLocInput;
                weatherState = locationInput.get(0).getAdminArea();
            }
        }

        String apiURL;
        if (weatherCity == null) {
            apiURL = "https://api.openweathermap.org/data/2.5/weather?lat="+weatherLat+"&lon="+weatherLng+"&appid=c96f4470f0a3f150b98deb4758d4be8b";
        }
        else {
            apiURL = "https://api.openweathermap.org/data/2.5/weather?q="+weatherCity+","+weatherState+"&appid=c96f4470f0a3f150b98deb4758d4be8b";
        }

        progressDialog = ProgressDialog.show(this, "Weather Data", "Connecting, please wait...", true, true);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadDataTask().execute(apiURL);
        } else {
            Toast.makeText(this, "No network connection available!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * A class that downloads data from a URL and then parses it. Uses AsyncTask to do it in the
     * background.
     */
    private class DownloadDataTask extends AsyncTask<String, Void, String> {

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
            parseData(jsonData);
        }
    }

    /**
     * Parses the datainto jsonObjects and then updates the corresponding textViews.
     * @param data a String that represents the data downloaded.
     */
    private void parseData(String data) {
        try {
            JSONObject jsonRootObject = new JSONObject(data);
            JSONArray jsonWeatherArray = jsonRootObject.getJSONArray("weather");
            JSONObject jsonWeatherObject = jsonWeatherArray.getJSONObject(0);
            JSONObject jsonMainObject = (JSONObject) jsonRootObject.get("main");
            Double temp = Double.parseDouble(jsonMainObject.getString("feels_like"));
            temp = temp - 273.15;
            temp = Math.round(temp*100.0) / 100.0;
            tv_Weather.setText("Today's Weather: "+jsonWeatherObject.getString("main"));
            tv_FeelsLike.setText("Feels Like: "+temp + "°C");
            tv_Description.setText("Description :" +jsonWeatherObject.getString("description"));
            temp = Double.parseDouble(jsonMainObject.getString("temp"));
            temp = temp - 273.15;
            temp = Math.round(temp*100.0) / 100.0;
            tv_Temp.setText("Temperature: "+ temp + "°C");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void goToMovieActivity(View view) {
        startActivity(new Intent(MainActivity.this, Activity2.class));
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
     * Starts activity two if "Movie Application" was selected in the menu.
     * @param item the menu item that was clicked.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_weather:
                break;
            case R.id.action_movie:
                startActivity(new Intent(MainActivity.this, Activity2.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}