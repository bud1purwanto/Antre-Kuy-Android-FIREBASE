package com.bud1purwanto.smartqueue.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by root on 5/9/18.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "smart_queue";
    private static final String TABLE_USER = "user";

    private static final String KEY_NIK         = "nik";
    private static final String KEY_NAMA        = "nama";
    private static final String KEY_TEMPAT      = "tempat_lahir";
    private static final String KEY_TANGGAL     = "tanggal_lahir";
    private static final String KEY_ALAMAT      = "alamat";
    private static final String KEY_AGAMA       = "agama";
    private static final String KEY_STATUS      = "status";
    private static final String KEY_PEKERJAAN   = "pekerjaan";
    private static final String KEY_EMAIL       = "email";
    private static final String KEY_FOTO        = "foto";
    private static final String KEY_ANTRE       = "antre";
    private static final String KEY_UID         = "uid";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_NIK + " INTEGER PRIMARY KEY," + KEY_NAMA + " TEXT,"
                + KEY_TEMPAT + " TEXT," + KEY_TANGGAL + " TEXT,"
                + KEY_ALAMAT + " TEXT," + KEY_AGAMA + " TEXT," + KEY_STATUS + " TEXT,"
                + KEY_PEKERJAAN + " TEXT," + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_FOTO + " TEXT," + KEY_ANTRE + " TEXT," + KEY_UID + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String nik, String nama, String tempat_lahir, String tanggal_lahir, String alamat, String agama, String status,
                        String pekerjaan, String email, String foto, String antre, String uid) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NIK, nik);
        values.put(KEY_NAMA, nama);
        values.put(KEY_TEMPAT, tempat_lahir);
        values.put(KEY_TANGGAL, tanggal_lahir);
        values.put(KEY_ALAMAT, alamat);
        values.put(KEY_AGAMA, agama);
        values.put(KEY_STATUS, status);
        values.put(KEY_PEKERJAAN, pekerjaan);
        values.put(KEY_EMAIL, email);
        values.put(KEY_FOTO, foto);
        values.put(KEY_ANTRE, antre);
        values.put(KEY_UID, uid);

        long id = db.insert(TABLE_USER, null, values);
        db.close();

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void updateUser(String nik, String foto) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FOTO, foto);


        long id = db.update(TABLE_USER,  values, KEY_NIK + "='" + nik+"'", null);
        db.close();

        Log.d(TAG, "New user updated into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(KEY_NIK, cursor.getString(0));
            user.put(KEY_NAMA, cursor.getString(1));
            user.put(KEY_TEMPAT, cursor.getString(2));
            user.put(KEY_TANGGAL, cursor.getString(3));
            user.put(KEY_ALAMAT, cursor.getString(4));
            user.put(KEY_AGAMA, cursor.getString(5));
            user.put(KEY_STATUS, cursor.getString(6));
            user.put(KEY_PEKERJAAN, cursor.getString(7));
            user.put(KEY_EMAIL, cursor.getString(8));
            user.put(KEY_FOTO, cursor.getString(9));
            user.put(KEY_ANTRE, cursor.getString(10));
            user.put(KEY_UID, cursor.getString(11));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re create database Delete all tables and create them again
     * */

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}