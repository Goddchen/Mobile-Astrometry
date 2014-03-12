package de.goddchen.android.astrometry.activities;

import android.app.Activity;
import android.os.Bundle;

import de.goddchen.android.astrometry.fragments.JobListFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, JobListFragment.newInstance())
                .commit();
    }

}
