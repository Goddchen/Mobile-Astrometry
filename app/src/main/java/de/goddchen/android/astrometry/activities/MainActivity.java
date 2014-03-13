package de.goddchen.android.astrometry.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.fragments.JobListFragment;
import de.goddchen.android.astrometry.fragments.LoginFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String session = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Application.Preferences.PREF_SESSION, null);
        if (session == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, LoginFragment.newInstance(), "login")
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, JobListFragment.newInstance(), "job-list")
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
