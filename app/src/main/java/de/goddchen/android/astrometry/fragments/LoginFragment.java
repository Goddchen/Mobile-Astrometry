package de.goddchen.android.astrometry.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import org.json.JSONException;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;
import de.goddchen.android.astrometry.api.AstrometryNetClient;

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
        String storedApikey = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(Application.Preferences.PREF_APIKEY, null);
        if (storedApikey != null) {
            ((EditText) view.findViewById(R.id.apikey)).setText(storedApikey);
        }
        TextView hintTextView = (TextView) view.findViewById(R.id.login_hint);
        hintTextView.setText(Html.fromHtml(getString(R.string.hint_apikey)));
        hintTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            EditText apikeyEditText = (EditText) getView().findViewById(R.id.apikey);
            if (apikeyEditText.length() == 0) {
                Toast.makeText(getActivity(), getString(R.string.toast_enter_apikey),
                        Toast.LENGTH_SHORT).show();
            } else {
                String apikey = apikeyEditText.getText().toString();
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(Application.Preferences.PREF_APIKEY, apikey)
                        .commit();
                LoadingDialogFragment.newInstance().show(getFragmentManager());
                try {
                    AstrometryNetClient.with(getActivity()).login(apikey,
                            new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    LoadingDialogFragment.safeDismiss(getFragmentManager());
                                    if (e != null
                                            || !result.has("status")
                                            || !result.has("session")
                                            || !"success".equals(result.get("status")
                                            .getAsString())) {
                                        Log.e(Application.Constants.LOG_TAG,
                                                "Error logging in", e);
                                        Toast.makeText(getActivity(),
                                                getString(R.string.toast_error_try_again),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        String session = result.get("session").getAsString();
                                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                .edit().putString(Application.Preferences
                                                        .PREF_SESSION,
                                                session
                                        )
                                                .commit();
                                        getFragmentManager().beginTransaction()
                                                .replace(R.id.fragment,
                                                        JobListFragment.newInstance(), "job-list")
                                                .commit();
                                    }
                                }
                            }
                    );
                } catch (JSONException e) {
                    LoadingDialogFragment.safeDismiss(getFragmentManager());
                    Log.e(Application.Constants.LOG_TAG, "Error logging in", e);
                    Toast.makeText(getActivity(), getString(R.string.toast_error_try_again),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
