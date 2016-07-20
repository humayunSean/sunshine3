package com.example.humayun.sunshine2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_setting){
            Intent intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String Location = sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));

        Uri geoLocation =Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",Location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if(intent.resolveActivity(getPackageManager())!= null){
            startActivity(intent);
        }else{
            Log.v(LOG_TAG,"no app installed to handle this request:");
        }
    }

}

