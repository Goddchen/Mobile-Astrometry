package de.goddchen.android.astrometry.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;

/**
 * Created by Goddchen on 12.03.14.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
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
            EditText apikeyEditText = (EditText) getView().findViewById(R.id.apikey);
            if (apikeyEditText.length() == 0) {
                //TODO show error
            } else {
                String apikey = apikeyEditText.getText().toString();
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(Application.Preferences.PREF_APIKEY, apikey)
                        .commit();
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, JobListFragment.newInstance(), "job-list")
                        .commit();
            }
        }
    }
}
