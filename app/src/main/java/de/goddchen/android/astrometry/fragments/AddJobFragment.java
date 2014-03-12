package de.goddchen.android.astrometry.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.api.AstrometryNetClient;
import de.goddchen.android.astrometry.events.JobsUpdatedEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Goddchen on 12.03.14.
 */
public class AddJobFragment extends Fragment implements View.OnClickListener {

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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            try {
                AstrometryNetClient.with(getActivity())
                        .upload(Uri.fromFile(getActivity().getFileStreamPath("upload")),
                                new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        if (e != null) {
                                            Log.e(Application.Constants.LOG_TAG,
                                                    "Error uploading photo", e);
                                        } else {
                                            //TODO persist job
                                            EventBus.getDefault().post(new JobsUpdatedEvent());
                                        }
                                    }
                                }
                        );
            } catch (Exception e) {
                Log.e(Application.Constants.LOG_TAG, "Error uploading photo", e);
            }
        }
    }
}
