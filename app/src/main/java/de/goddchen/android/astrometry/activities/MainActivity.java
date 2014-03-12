package de.goddchen.android.astrometry.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

import de.goddchen.android.astrometry.Application;
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

}
