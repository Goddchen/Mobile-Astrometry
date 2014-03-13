package de.goddchen.android.astrometry.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.sql.SQLException;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.api.AstrometryNetClient;
import de.goddchen.android.astrometry.data.Job;
import de.goddchen.android.astrometry.events.JobsUpdatedEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Goddchen on 12.03.14.
 */
public class AddJobFragment extends Fragment implements View.OnClickListener {

    private Uri mImageUri;

    public static AddJobFragment newInstance() {
        AddJobFragment fragment = new AddJobFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_job, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.submit).setOnClickListener(this);
        view.findViewById(R.id.select).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            try {
                LoadingDialogFragment.newInstance().show(getFragmentManager());
                AstrometryNetClient.with(getActivity())
                        .upload(mImageUri,
                                new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        if (e != null) {
                                            LoadingDialogFragment.safeDismiss(getFragmentManager());
                                            Log.e(Application.Constants.LOG_TAG,
                                                    "Error uploading photo", e);
                                            Toast.makeText(getActivity(),
                                                    getString(R.string.toast_error_try_again),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            //Get submission id to get job id...
                                            //Sample response:
                                            // {"status":"success","subid":199628,
                                            // "hash":"37dc61e283f169ef0c8058413d4af2c7c25bab62"}
                                            long submissionID = result.get("subid").getAsLong();
                                            getJobForSubmission(submissionID);
                                        }
                                    }
                                }
                        );
            } catch (Exception e) {
                LoadingDialogFragment.safeDismiss(getFragmentManager());
                Log.e(Application.Constants.LOG_TAG, "Error uploading photo", e);
                Toast.makeText(getActivity(),
                        getString(R.string.toast_error_try_again),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.select) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 0);
        }
    }

    private void getJobForSubmission(final long submissionID) {
        try {
            AstrometryNetClient.with(getActivity())
                    .request("submissions/" + submissionID, null,
                            new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (e != null) {
                                        Log.e(Application.Constants.LOG_TAG,
                                                "Error getting job infos", e);
                                        Toast.makeText(getActivity(),
                                                getString(R.string.toast_error_try_again),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Sample response:
                                        // {"processing_started": "2014-03-12 11:51:57
                                        // .907107",
                                        // "job_calibrations": [[246083, 168575]],
                                        // "jobs": [246083],
                                        // "processing_finished": "2014-03-12 11:51:58
                                        // .304619",
                                        // "user": 2870, "user_images": [232377]}
                                        Log.d(Application.Constants.LOG_TAG,
                                                "Response: " + result.toString());
                                        JsonArray jobs = result.get("jobs").getAsJsonArray();
                                        if (jobs.size() == 0) {
                                            Log.d(Application.Constants.LOG_TAG,
                                                    "Job has not started to process, waiting...");
                                            try {
                                                Thread.sleep(3000);
                                            } catch (InterruptedException e1) {
                                                Log.w(Application.Constants.LOG_TAG,
                                                        "Error sleeping");
                                            }
                                            getJobForSubmission(submissionID);
                                        } else {
                                            LoadingDialogFragment.safeDismiss(getFragmentManager());
                                            Job job = new Job();
                                            job.id = result.get("jobs").getAsJsonArray().get
                                                    (0).getAsLong();
                                            try {
                                                Application.EVENT_DAO.create(job);
                                                EventBus.getDefault().post(new
                                                        JobsUpdatedEvent());
                                                getFragmentManager().popBackStack();
                                            } catch (SQLException e1) {
                                                Log.e(Application.Constants.LOG_TAG,
                                                        "Error creating job", e1);
                                            }
                                        }
                                    }
                                }
                            }
                    );
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    getString(R.string.toast_error_try_again),
                    Toast.LENGTH_SHORT).show();
            LoadingDialogFragment.safeDismiss(getFragmentManager());
            Log.e(Application.Constants.LOG_TAG, "Error getting job infos", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            mImageUri = data.getData();
            ImageView imageView = (ImageView) getView().findViewById(R.id.image);
            imageView.setImageURI(mImageUri);
            imageView.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.submit).setVisibility(View.VISIBLE);
        }
    }
}
