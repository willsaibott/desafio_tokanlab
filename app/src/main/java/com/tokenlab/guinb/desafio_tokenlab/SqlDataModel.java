package com.tokenlab.guinb.desafio_tokenlab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

/**
 * Created by guinb on 11/16/2016.
 *
 * Sql Data Model
 *
 * This Class makes the back-end reading from a database the reviews
 */

public class SqlDataModel extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DB_DESAFIO_TOKENLAB";
    public static final String TABLE_NAME = "tb_review";
    public static final String COLUMN_GAME = "game";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_NAME = "userName";
    public static final String COLUMN_PHOTO_URL = "photoUrl";
    public static final String COLUMN_COMMENTARY = "commentary";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_RATE = "rate";

    // Sql Table Create query Statements
    private static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME + "(" + COLUMN_ID + " INTEGER," + COLUMN_GAME + " TEXT," + COLUMN_USER_NAME
            + " TEXT," + COLUMN_PHOTO_URL + " TEXT," + COLUMN_COMMENTARY + " TEXT," + COLUMN_DATE + " TEXT," + COLUMN_RATE + " INTEGER, PRIMARY KEY("+ COLUMN_ID + ", " + COLUMN_GAME+ "))";

    public SqlDataModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // create new tables
        onCreate(db);
    }

    // Sql Insertion query
    public long insertIntoTable(Reviews review, String game, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // storing the values into the content value
        values.put(COLUMN_ID, id);
        values.put(COLUMN_GAME, game);
        values.put(COLUMN_USER_NAME, review.getUserName());
        values.put(COLUMN_PHOTO_URL, review.getUserPhotoUrl());
        values.put(COLUMN_COMMENTARY, review.getComment());
        values.put(COLUMN_DATE, review.getDate());
        values.put(COLUMN_RATE, review.getRate());

        // insert row
        return db.insert(TABLE_NAME, null, values);
    }

    /* Select * from  review_tb, where game = 'chosen_game';
        This function executes a reading sql query by game name and returns the read data
     */
    public ArrayList<Reviews> getAllReviews(String game) {
        ArrayList<Reviews> reviews = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + COLUMN_GAME + " = '" + game + "';";

        Log.e("SQL query", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Reviews review = new Reviews();
                review.setUserName(c.getString(c.getColumnIndex(COLUMN_USER_NAME)));
                review.setComment(c.getString(c.getColumnIndex(COLUMN_COMMENTARY)));
                review.setDate(c.getString(c.getColumnIndex(COLUMN_DATE)));
                review.setRate(c.getInt(c.getColumnIndex(COLUMN_RATE)));
                review.setUserPhotoUrl(c.getString(c.getColumnIndex(COLUMN_PHOTO_URL)));
                // adding to reviewList
                reviews.add(review);
            } while (c.moveToNext());
        }
        return reviews;
    }

    // close Database Function
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }


}


