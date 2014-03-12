package de.goddchen.android.astrometry.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.fragments.JobListFragment;
import de.goddchen.android.astrometry.fragments.LoginFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        String apikey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Application.Preferences.PREF_APIKEY, null);
        if (apikey == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, LoginFragment.newInstance(), "login")
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, JobListFragment.newInstance(), "job-list")
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            FlurryAgent.onStartSession(this, getString(R.string.id_flurry));
        } catch (Exception e) {
            Log.w(Application.Constants.LOG_TAG, "Error starting Flurry", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            FlurryAgent.onEndSession(this);
        } catch (Exception e) {
            Log.w(Application.Constants.LOG_TAG, "Error stopping Flurry", e);
        }
    }
}
