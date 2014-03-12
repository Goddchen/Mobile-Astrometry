package de.goddchen.android.astrometry.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.List;

import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.data.Job;

/**
 * Created by Goddchen on 12.03.14.
 */
public class JobAdapter extends ArrayAdapter<Job> {
    public JobAdapter(Context context, List<Job> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Job job = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_job, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.infos))
                .setText(job.toString());
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        Ion.with(imageView)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .animateIn(android.R.anim.fade_in)
                .load(getContext().getString(R.string.astrometry_annotated_image_url, job.id));
        return convertView;
    }
}
