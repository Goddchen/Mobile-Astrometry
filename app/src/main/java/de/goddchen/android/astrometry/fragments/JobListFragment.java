package de.goddchen.android.astrometry.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.adapters.JobAdapter;
import de.goddchen.android.astrometry.api.AstrometryNetClient;
import de.goddchen.android.astrometry.data.Job;
import de.goddchen.android.astrometry.events.JobsUpdatedEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Goddchen on 12.03.14.
 */
public class JobListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private List<Job> mJobs;

    public static JobListFragment newInstance() {
        JobListFragment fragment = new JobListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(JobsUpdatedEvent event) {
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void updateUI() {
        try {
            mJobs = Application.EVENT_DAO.queryForAll();
            setListAdapter(new JobAdapter(getActivity(), mJobs));
            ((ArrayAdapter<Job>) getListAdapter()).sort(new Comparator<Job>() {
                @Override
                public int compare(Job lhs, Job rhs) {
                    return -(lhs.id.compareTo(rhs.id));
                }
            });
            //TODO properly update adapter
            for (final Job job : mJobs) {
                if (!"success".equals(job.status)) {
                    //Try to update job
                    try {
                        AstrometryNetClient.with(getActivity())
                                .request("jobs/" + job.id + "/info", null,
                                        new FutureCallback<JsonObject>() {

                                            @Override
                                            public void onCompleted(Exception e,
                                                                    JsonObject result) {
                                                if (e != null) {
                                                    Log.e(Application.Constants.LOG_TAG,
                                                            "Error getting job infos", e);
                                                } else {
                                                    try {
                                                        updateJobInfo(job.id, result);
                                                    } catch (SQLException e1) {
                                                        Log.e(Application.Constants.LOG_TAG,
                                                                "Error updating job", e1);
                                                    }
                                                }
                                            }
                                        }
                                );
                    } catch (Exception e) {
                        Log.e(Application.Constants.LOG_TAG, "Error getting job infos", e);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(Application.Constants.LOG_TAG, "Error getting jobs", e);
        }
    }

    private void updateJobInfo(Long id, JsonObject json) throws SQLException {
        Job job = new Job();
        job.id = id;
        job.status = json.get("status").getAsString();
        JsonArray machineTags = json.get("machine_tags").getAsJsonArray();
        job.machine_tags = new String[machineTags.size()];
        for (int i = 0; i < machineTags.size(); i++) {
            job.machine_tags[i] = machineTags.get(i).getAsString();
        }
        JsonArray objects = json.get("objects_in_field").getAsJsonArray();
        job.objects_in_field = new String[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            job.objects_in_field[i] = objects.get(i).getAsString();
        }
        job.original_filename = json.get("original_filename").getAsString();
        JsonArray tags = json.get("tags").getAsJsonArray();
        job.tags = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            job.tags[i] = tags.get(i).getAsString();
        }
        Application.EVENT_DAO.createOrUpdate(job);
        EventBus.getDefault().post(new JobsUpdatedEvent());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            AstrometryNetClient.with(getActivity())
                    .request("myjobs/", null, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                Log.e(Application.Constants.LOG_TAG, "Error getting jobs", e);
                            } else {
                                JsonArray jobs = result.get("jobs").getAsJsonArray();
                                for (int i = 0; i < jobs.size(); i++) {
                                    Job job = new Job();
                                    job.id = jobs.get(i).getAsLong();
                                    try {
                                        Application.EVENT_DAO.createIfNotExists(job);
                                    } catch (SQLException e1) {
                                        Log.e(Application.Constants.LOG_TAG,
                                                "Error creating job", e1);
                                    }
                                }
                                EventBus.getDefault().post(new JobsUpdatedEvent());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(Application.Constants.LOG_TAG, "Error getting jobs", e);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_joblist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_job) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, AddJobFragment.newInstance(), "add-job")
                    .addToBackStack("add-job")
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Job job = (Job) getListAdapter().getItem(position);
        String imageUrl = getString(R.string.astrometry_annotated_image_url, job.id);
        LoadingDialogFragment.newInstance().show(getFragmentManager(), "dialog-loading");
        Ion.with(getActivity(), imageUrl)
                .write(getActivity().getFileStreamPath("download"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        LoadingDialogFragment.safeDismiss(getFragmentManager(),
                                "dialog-loading");
                        if (e != null) {
                            Log.e(Application.Constants.LOG_TAG, "Error downloading image", e);
                        } else {
                            result.setReadable(true, false);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(result), "image/*");
                            startActivity(Intent.createChooser(intent, ""));
                        }
                    }
                });
    }
}
