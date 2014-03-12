package de.goddchen.android.astrometry;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import de.goddchen.android.astrometry.data.Job;

/**
 * Created by Goddchen on 12.03.14.
 */
public class Application extends android.app.Application {

    public static Dao<Job, String> EVENT_DAO;

    public static class Constants {
        public static final String LOG_TAG = "Mobile Astrometry";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initOrmLite();
    }

    private void initOrmLite() {
        OrmLiteSqliteOpenHelper sqliteOpenHelper =
                new OrmLiteSqliteOpenHelper(this, "jobs", null, 1) {
                    @Override
                    public void onCreate(SQLiteDatabase database,
                                         ConnectionSource connectionSource) {
                        try {
                            TableUtils.createTable(connectionSource, Job.class);
                        } catch (SQLException e) {
                            Log.e(Constants.LOG_TAG, "Error creating tables", e);
                        }
                    }

                    @Override
                    public void onUpgrade(SQLiteDatabase database,
                                          ConnectionSource connectionSource,
                                          int oldVersion, int newVersion) {

                    }
                };
        ConnectionSource connectionSource = new AndroidConnectionSource(sqliteOpenHelper);
        try {
            EVENT_DAO = DaoManager.createDao(connectionSource, Job.class);
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG, "Error creating DAO", e);
        }
    }
}
