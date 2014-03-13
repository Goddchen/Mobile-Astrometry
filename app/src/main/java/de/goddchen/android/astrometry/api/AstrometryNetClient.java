package de.goddchen.android.astrometry.api;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import de.goddchen.android.astrometry.Application;
import de.goddchen.android.astrometry.R;

/**
 * Created by Goddchen on 12.03.14.
 */
public class AstrometryNetClient {

    private Context mContext;

    private String mSession;

    private AstrometryNetClient() {
    }

    public static AstrometryNetClient with(Context context) {
        AstrometryNetClient client = new AstrometryNetClient();
        client.mContext = context;
        client.mSession = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Application.Preferences.PREF_SESSION, null);
        return client;
    }

    public void request(final String service, final Map<String, String> args,
                        final FutureCallback<JsonObject> callback) throws Exception {
        //ensure that we have a session
        if (mSession == null) {
            JSONObject data = new JSONObject();
            data.put("apikey", PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString(Application.Preferences.PREF_APIKEY, null));
            Ion.with(mContext, mContext.getString(R.string.astrometry_server_base_url) +
                    "login")
                    .setBodyParameter("request-json", data.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null
                                    || !result.has("status")
                                    || !result.has("session")
                                    || !"success".equals(result.get("status")
                                    .getAsString())) {
                                Log.e(Application.Constants.LOG_TAG, "Error during request", e);
                            } else {
                                String session = result.get("session").getAsString();
                                PreferenceManager.getDefaultSharedPreferences(mContext)
                                        .edit().putString(Application.Preferences.PREF_SESSION,
                                        session)
                                        .commit();
                                mSession = session;
                                try {
                                    request(service, args, callback);
                                } catch (Exception e1) {
                                    Log.e(Application.Constants.LOG_TAG,
                                            "Error during request", e1);
                                }
                            }
                        }
                    });
        } else {
            JSONObject data = newSessionJson();
            if (args != null) {
                for (Map.Entry<String, String> entry : args.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
            }
            Ion.with(mContext, mContext.getString(R.string.astrometry_server_base_url) + service)
                    .setBodyParameter("request-json", data.toString())
                    .asJsonObject()
                    .setCallback(callback);
        }
    }

    public void upload(Uri uri, FutureCallback<JsonObject> callback) throws Exception {
        if ("file".equals(uri.getScheme())) {
            Ion.with(mContext, mContext.getString(R.string.astrometry_server_base_url) + "upload/")
                    .setMultipartParameter("request-json", newSessionJson().toString())
                    .setMultipartFile("file", new File(uri.getPath()))
                    .asJsonObject()
                    .setCallback(callback);
        } else if ("content".equals(uri.getScheme())) {
            File file = mContext.getFileStreamPath("upload");
            IOUtils.copy(mContext.getContentResolver().openInputStream(uri),
                    new FileOutputStream(file));
            Ion.with(mContext, mContext.getString(R.string.astrometry_server_base_url) + "upload/")
                    .setMultipartParameter("request-json", newSessionJson().toString())
                    .setMultipartFile("file", file)
                    .asJsonObject()
                    .setCallback(callback);
        } else {
            throw new Exception("Unsupported uri: " + uri.toString());
        }
    }

    public void login(String apikey, FutureCallback<JsonObject> callback) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("apikey", apikey);
        Ion.with(mContext, mContext.getString(R.string.astrometry_server_base_url) +
                "login")
                .setBodyParameter("request-json", data.toString())
                .asJsonObject()
                .setCallback(callback);
    }

    private JSONObject newSessionJson() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("session", mSession);
        return data;
    }

}
