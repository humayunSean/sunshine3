package com.example.humayun.sunshine2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.container,new PlaceHolderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_setting){
            Intent intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceHolderFragment extends Fragment{

        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String mForecastStr;
        private static final String LOG_TAG = PlaceHolderFragment.class.getSimpleName();

        public PlaceHolderFragment(){
            setHasOptionsMenu(true);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rtview = inflater.inflate(R.layout.fragment_detail,container,false);


            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                ((TextView) rtview.findViewById(R.id.detail_fragment_text)).setText(mForecastStr);
            }
            return rtview;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment,menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        private Intent createShareForecastIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_TEXT,mForecastStr + FORECAST_SHARE_HASHTAG);
            shareIntent.setType("text/plain");
            return shareIntent;
        }
    }
}
