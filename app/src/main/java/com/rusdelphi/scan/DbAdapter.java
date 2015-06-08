package com.rusdelphi.scan;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DbAdapter {
    private static final String DB_NAME = "news.db";
    private static final String DB_TABLE_NAME = "news";
    private static final int DB_VERSION = 1;
    private static final String DB_VERSION_TAG = "DB_VERSION";
    private static final String TAG = DbAdapter.class.getSimpleName();
    // private static final String IMAGE_PATH = "images";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_DATE = "date";

    /*
     * private static final String DB_CREATE = "create table " + DB_TABLE_NAME +
     * " (" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NOTE +
     * " text not null);";
     */
    private static String DB_PATH = null;
    private static SQLiteDatabase mDb = null;
    private final Context mContext;
    private DBHelper mDbHelper;

    static void copyDBifNeeded(Context c) throws IOException {
        boolean unpackDB = false;
        File dbFile = new File(getDBPath(c));
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        if (!dbFile.exists()) {
            Log.d(TAG, "DB doesn't exist");
            dbFile.getParentFile().mkdirs();
            unpackDB = true;
        } else if (pref.getInt(DB_VERSION_TAG, 0) < DB_VERSION) {
            Log.d(TAG, "Forcing updating DB");
            unpackDB = true;
        }

        if (unpackDB) {
            unpackDB(c);
        }

    }

    static void unpackDB(Context c) throws IOException {
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        Log.d(TAG, "Unpacking DB from assets");// to " +
        // dbFile.getAbsolutePath());

        InputStream is = c.getAssets().open(DB_NAME);
        int size;
        byte[] buffer = new byte[2048];

        // c.getApplicationInfo().dataDir.toString() + "/"
        // + DB_NAME;

        FileOutputStream fout = new FileOutputStream(getDBPath(c), false);
        BufferedOutputStream bufferOut = new BufferedOutputStream(fout,
                buffer.length);
        while ((size = is.read(buffer, 0, buffer.length)) != -1) {
            bufferOut.write(buffer, 0, size);
        }
        bufferOut.flush();
        bufferOut.close();
        fout.close();
        is.close();

        mDb = SQLiteDatabase.openDatabase(getDBPath(c), null,
                SQLiteDatabase.OPEN_READWRITE);// OPEN_READONLY

        Log.d(TAG, "Upgrading complete!");

        pref.edit().putInt(DB_VERSION_TAG, DB_VERSION).commit();
        // Mark that everything has been done correctly

    }

    ;

    public static String getDBPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath() + "/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/db/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        return path + DB_NAME;
    }

    static String getDataPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath() + "/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        return path;
    }

    static String getImagesPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath() + "/images/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/images/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        File imagesDirectory = new File(path);
        // have the object build the directory structure, if needed.
        imagesDirectory.mkdirs();
        return path;
    }

    static String getMyImagesPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath()
                    + "/my_images/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/my_images/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        File imagesDirectory = new File(path);
        // have the object build the directory structure, if needed.
        imagesDirectory.mkdirs();
        return path;
    }

    public DbAdapter(Context context) {
        mContext = context;
        DB_PATH = getDBPath(context);// context.getDatabasePath(DB_NAME).getPath();
        mDbHelper = new DBHelper(mContext);

    }


    public DbAdapter open() throws SQLException {
        if (mDb == null || !mDb.isOpen()) {
            try {
                copyDBifNeeded(mContext);
                //Log.d(TAG, "Opening DB");
                if (mDb == null || !mDb.isOpen())
                    mDb = SQLiteDatabase.openDatabase(getDBPath(mContext), null,
                            SQLiteDatabase.OPEN_READWRITE);// OPEN_READONLY
            } catch (IOException e) {
                //Log.e(TAG, "bad DB", e);
                mDb = null;
            }
        }

        return this;
    }

    public void close() {
        mDb.close();
        mDb = null;
    }

    public void insertNews(String text, String date, String image) {
        if (mDb == null) {
            return;
        } else {

            long ldate = Long.valueOf(date);
            DateFormat df;
            df = new SimpleDateFormat("dd:MM:yyyy");
            date = df.format(new Date(ldate * 1000));

            ContentValues newValues = new ContentValues();
            // ������� �������� ��� ������ ������.
            newValues.put("text", text);
            newValues.put("date", date);
            newValues.put("image", image);

            mDb.insert(DB_TABLE_NAME, null, newValues);
        }
    }

    public Cursor getAllNews() {
        if (mDb == null)
            return null;
        else
            return mDb.rawQuery("select * from news", null);

    }

    private class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = DB_NAME;
        private static final int DATABASE_VERSION = DB_VERSION;
        private static final String SP_KEY_DB_VER = "db_ver";
        private final Context mContext;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            initialize();
        }

        /**
         * Initializes database. Creates database if doesn't exist.
         */
        private void initialize() {
            if (databaseExists()) {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mContext);
                int dbVersion = prefs.getInt(SP_KEY_DB_VER, 1);
                if (DATABASE_VERSION != dbVersion) {
                    File dbFile = mContext.getDatabasePath(DATABASE_NAME);
                    if (!dbFile.delete()) {
                        Log.w(TAG, "Unable to update database");
                    }
                }
            }
            if (!databaseExists()) {
                createDatabase();
            }
        }

        /**
         * Returns true if database file exists, false otherwise.
         */
        private boolean databaseExists() {
            File dbFile = mContext.getDatabasePath(DATABASE_NAME);
            return dbFile.exists();
        }

        /**
         * Creates database by copying it from assets directory.
         */
        private void createDatabase() {
            String parentPath = mContext.getDatabasePath(DATABASE_NAME).getParent();
            String path = mContext.getDatabasePath(DATABASE_NAME).getPath();

            File file = new File(parentPath);
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.w(TAG, "Unable to create database directory");
                    return;
                }
            }

            InputStream is = null;
            OutputStream os = null;
            try {
                is = mContext.getAssets().open(DATABASE_NAME);
                os = new FileOutputStream(path);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(SP_KEY_DB_VER, DATABASE_VERSION);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
