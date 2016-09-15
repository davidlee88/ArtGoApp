package com.ArtGo.ArtGoApp.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * DBHelperLocations is created to handle location database operation that used within application.
 * Created using SQLiteOpenHelper.
 */
public class DBHelperLocations extends SQLiteOpenHelper {

    // Path of database when app installed on device
    private static String DB_PATH = Utils.ARG_DATABASE_PATH;

    // Create table name and fields
    private final static String TABLE_CATEGORIES = "tbl_categories";
    private final static String CATEGORY_ID = "category_id";
    private final static String CATEGORY_NAME = "category_name";

    // Create database name and version
    private final static String DB_NAME = "db_locations";
    public final static int DB_VERSION = 1;
    public static SQLiteDatabase db;

    private final Context context;


    public DBHelperLocations(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // Method to create database
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        SQLiteDatabase db_Read;

        // If database exist delete database and copy the new one
        if(dbExist){
            deleteDataBase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }else{
            db_Read = this.getReadableDatabase();
            db_Read.close();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    // Method to delete database
    private void deleteDataBase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        dbFile.delete();
    }

    // Method to check database on path
    private boolean checkDataBase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    // Method to copy database from app to db path
    private void copyDataBase() throws IOException{

        InputStream myInput = context.getAssets().open(DB_NAME);

        String outFileName = DB_PATH + DB_NAME;

        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    // Method to open database and read it
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    // Close database after it is used
    @Override
    public void close() {
        if(db.isOpen()){
            db.close();
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    // Method to get all categories data from database
    public ArrayList<ArrayList<Object>> getAllCategoriesData(){
        ArrayList<ArrayList<Object>>  dataArrays= new ArrayList<>();

        Cursor cursor;

        try{
            cursor = db.query(
                    TABLE_CATEGORIES,
                    new String[]{CATEGORY_ID, CATEGORY_NAME},
                    null, null, null, null, null);
            cursor.moveToFirst();

            if (!cursor.isAfterLast()){
                do{
                    ArrayList<Object> dataList = new ArrayList<>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        }catch (SQLException e){
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        return dataArrays;
    }

    // Method to get locations data from database based on category id
    public ArrayList<ArrayList<Object>> getLocationsByCategory(String id){
        ArrayList<ArrayList<Object>>  dataArrays= new ArrayList<>();

        Cursor cursor;

        String query;

        if (id.equals("0")){
            query = "SELECT location_id, location_name, " +
                    "location_address, location_image, " +
                    "location_latitude, location_longitude, " +
                    "c.category_marker " +
                    "FROM tbl_categories c, tbl_locations l " +
                    "WHERE l.category_id = c.category_id";
        } else {
            query = "SELECT location_id, location_name, " +
                    "location_address, location_image, " +
                    "location_latitude, location_longitude, " +
                    "c.category_marker " +
                    "FROM tbl_categories c, tbl_locations l " +
                    "WHERE l.category_id "+" = "+id+" AND l.category_id = c.category_id";
        }

        try{

            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            if (!cursor.isAfterLast()){
                do{
                    ArrayList<Object> dataList = new ArrayList<>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(cursor.getString(3));
                    dataList.add(cursor.getString(4));
                    dataList.add(cursor.getString(5));
                    dataList.add(cursor.getString(6));

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        }catch (SQLException e){
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        return dataArrays;
    }


    // Method to get locations data from database base on location id
    public ArrayList<Object> getLocationDetailById(String id){

        ArrayList<Object> rowArray = new ArrayList<Object>();
        Cursor cursor;

        try{
            String query = "SELECT location_name, " +
                    "location_address, location_description, location_image, " +
                    "location_latitude, location_longitude, location_description, " +
                    "location_phone, location_website, c.category_marker, c.category_name " +
                    "FROM tbl_categories c, tbl_locations l " +
                    "WHERE l.location_id "+" = "+id+" AND l.category_id = c.category_id";

            cursor = db.rawQuery(query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()){
                do{
                    rowArray.add(cursor.getString(0));
                    rowArray.add(cursor.getString(1));
                    rowArray.add(cursor.getString(2));
                    rowArray.add(cursor.getString(3));
                    rowArray.add(cursor.getString(4));
                    rowArray.add(cursor.getString(5));
                    rowArray.add(cursor.getString(6));
                    rowArray.add(cursor.getString(7));
                    rowArray.add(cursor.getString(8));
                    rowArray.add(cursor.getString(9));
                    rowArray.add(cursor.getString(10));
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
        catch (SQLException e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }

        return rowArray;
    }

}
