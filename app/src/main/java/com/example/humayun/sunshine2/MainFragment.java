package com.example.humayun.sunshine2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by humayun on 22-04-2016.
 */

public class MainFragment extends Fragment {


    ArrayAdapter<String> mWeatherForecast;
    public MainFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        fetchWeatherTask.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] weatherList = {
        //        "Rainy- 1001 - 25/50",
        //        "Cloudy- 1002 - 25/50"
        };

        List<String> weatherForestcast= new ArrayList<String>(Arrays.asList(weatherList));

        mWeatherForecast = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());//weatherForestcast);

        ListView listView = (ListView) rootView.findViewById(R.id.fragment_main_view);
        listView.setAdapter(mWeatherForecast);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mWeatherForecast.getItem(position);
                Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,forecast);
                startActivity(intent);
 //               Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }


    public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String  formatHighLows(double high, double low, String unitType) {

            if (unitType.equals(getString(R.string.pref_units_imperial))) {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
                Log.d(LOG_TAG, "Unit type not found: " + unitType);
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] parseData(String forecastFragment, int numDays) throws JSONException{

            JSONObject forecastWeather = new JSONObject(forecastFragment);
            JSONArray forecastArray = forecastWeather.getJSONArray("list");

            Time dateTime = new Time();
            dateTime.setToNow();
            int julianDay = Time.getJulianDay(System.currentTimeMillis(),dateTime.gmtoff);
            dateTime = new Time();

            String[] resultStr = new String[numDays];

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));

            for(int i = 0; i < forecastArray.length(); i++){

                String day;
                JSONObject dailyTemp = forecastArray.getJSONObject(i);
                JSONObject dailyminTemp =  dailyTemp.getJSONObject("temp");
                double min = dailyminTemp.getDouble("min");
                double max = dailyminTemp.getDouble("max");

                JSONObject weather = dailyTemp.getJSONArray("weather").getJSONObject(0);
                String descp = weather.getString("description");
                String main = weather.getString("main");

                long dayTime;
                dayTime = dateTime.setJulianDay(julianDay+i);
                SimpleDateFormat simpleDate = new SimpleDateFormat("EEE MMM dd");
                day = simpleDate.format(dayTime);
                String unitype = "metric";

                String highLow = formatHighLows(max,min,unitype);

                resultStr[i] = day +" - "+ main + " - " + highLow;

            }

            for(String s: resultStr){
                Log.v(LOG_TAG,"Forecast Entry :" +s);
            }

            return resultStr;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null){
                mWeatherForecast.clear();
                for(String s:result){
                    mWeatherForecast.add(s);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader =  null;
            String forecastJsonStr = null;

            String mode = "json";
            String units = "metric";
            int count = 7;

            try {

                final String baseurl = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String FORECAST_PARAM = "q";
                final String MODE = "mode";
                final String UNITS = "units";
                final String COUNT = "cnt";
                final String APPID = "APPID";



                Uri uri = Uri.parse(baseurl).buildUpon()
                        .appendQueryParameter(FORECAST_PARAM,params[0])
                        .appendQueryParameter(MODE,mode)
                        .appendQueryParameter(UNITS,units)
                        .appendQueryParameter(COUNT,Integer.toString(count))
                        .appendQueryParameter(APPID,"aba3459bccace16f31d19275081e973d")
                        .build();

                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null){
                    return null;
                }
                StringBuffer buffer = new StringBuffer();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                if((line = bufferedReader.readLine()) != null){
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0){
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Huma Error" + forecastJsonStr);
            }
            catch(IOException e){

            }
            finally{
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(bufferedReader != null){
                    try{
                        bufferedReader.close();
                    }
                    catch(final IOException e){

                    }
                }
            }

            try{
                return parseData(forecastJsonStr,count);
            }catch(JSONException e){

            }
            return null;
        }
    }
}

