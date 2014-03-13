package de.goddchen.android.astrometry.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import de.goddchen.android.astrometry.Application;

/**
 * Created by Goddchen on 12.03.14.
 */
public class LoadingDialogFragment extends DialogFragment {

    public static LoadingDialogFragment newInstance() {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        return fragment;
    }

    public static void safeDismiss(FragmentManager fragmentManager) {
        try {
            ((LoadingDialogFragment) fragmentManager.findFragmentByTag("dialog-loading")).dismiss();
        } catch (Exception e) {
            Log.w(Application.Constants.LOG_TAG, "Error dismissing dialog");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "dialog-loading");
    }
}
