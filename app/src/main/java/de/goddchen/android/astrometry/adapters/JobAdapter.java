package de.goddchen.android.astrometry.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import de.goddchen.android.astrometry.data.Job;

/**
 * Created by Goddchen on 12.03.14.
 */
public class JobAdapter extends ArrayAdapter<Job> {
    public JobAdapter(Context context, List<Job> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }
}
