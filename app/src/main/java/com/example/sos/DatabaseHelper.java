package com.example.sos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CONTACT.db";
    private static final int DATABASE_VERSION = 3; // Incremented Version
    private static final String TABLE_NAME = "contact_table";
    private static final String ID = "ID";
    private static final String NAME_COLUMN = "NAME";
    private static final String MOBILE_COLUMN = "MOBILE";
    private static final String KEYWORD_COLUMN = "KEYWORD";

    // Helpline Table
    public static final String TABLE_HELPLINE = "helpline_table";
    public static final String COL_HELP_ID = "ID";
    public static final String COL_HELP_NAME = "NAME";
    public static final String COL_HELP_NUMBER = "NUMBER";
    public static final String COL_HELP_KEYWORD = "KEYWORD";
    public static final String COL_HELP_MESSAGE = "MESSAGE";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NAME_COLUMN + " TEXT," +
                MOBILE_COLUMN + " TEXT," +
                KEYWORD_COLUMN + " TEXT" + ")");

        // Create Helpline Table
        db.execSQL("create table " + TABLE_HELPLINE +
                "(" + COL_HELP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_HELP_NAME + " TEXT," +
                COL_HELP_NUMBER + " TEXT," +
                COL_HELP_KEYWORD + " TEXT," +
                COL_HELP_MESSAGE + " TEXT" + ")");

        // Default Helplines
        insertDefaultHelplines(db);
    }

    private void insertDefaultHelplines(SQLiteDatabase db) {
        String[][] defaults = {
                { "Police", "100", "Police" },
                { "Ambulance", "108", "Ambulance" },
                { "Fire", "101", "Fire" },
                { "Disaster Management", "104", "Disaster" },
                { "Gas Leak", "1906", "Gas" }
        };

        for (String[] item : defaults) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_HELP_NAME, item[0]);
            contentValues.put(COL_HELP_NUMBER, item[1]);
            contentValues.put(COL_HELP_KEYWORD, item[2]);
            contentValues.put(COL_HELP_MESSAGE, ""); // Default empty message
            db.insert(TABLE_HELPLINE, null, contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEYWORD_COLUMN + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create Helpline Table for existing users
            db.execSQL("create table " + TABLE_HELPLINE +
                    "(" + COL_HELP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_HELP_NAME + " TEXT," +
                    COL_HELP_NUMBER + " TEXT," +
                    COL_HELP_KEYWORD + " TEXT," +
                    COL_HELP_MESSAGE + " TEXT" + ")");
            insertDefaultHelplines(db);
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_HELPLINE + " ADD COLUMN " + COL_HELP_MESSAGE + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- Helpline Methods ---
    public ArrayList<HelplineModel> fetchHelplineData() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HelplineModel> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HELPLINE, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String keyword = cursor.getString(3);
                String message = "";
                if (cursor.getColumnCount() > 4) {
                    message = cursor.getString(4);
                }
                list.add(new HelplineModel(id, name, number, keyword, message));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateHelpline(String id, String name, String number, String keyword, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HELP_NAME, name);
        contentValues.put(COL_HELP_NUMBER, number);
        contentValues.put(COL_HELP_KEYWORD, keyword);
        contentValues.put(COL_HELP_MESSAGE, message);
        return db.update(TABLE_HELPLINE, contentValues, "ID = ?", new String[] { id }) > 0;
    }

    public boolean insertHelpline(String name, String number, String keyword, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_HELP_NAME, name);
        contentValues.put(COL_HELP_NUMBER, number);
        contentValues.put(COL_HELP_KEYWORD, keyword);
        contentValues.put(COL_HELP_MESSAGE, message);
        return db.insert(TABLE_HELPLINE, null, contentValues) != -1;
    }

    public boolean insertDataFunc(String name, String mob, String keyword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(NAME_COLUMN, name);
        contentValues.put(MOBILE_COLUMN, mob);
        contentValues.put(KEYWORD_COLUMN, keyword);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public int count() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public ArrayList<ContactModel> fetchData() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(" select * from " + TABLE_NAME, null);
        ArrayList<ContactModel> dataArrayList = new ArrayList<>();
        while (result.moveToNext()) {
            ContactModel model = new ContactModel();
            model.id = result.getString(0);
            model.name = result.getString(1);
            model.number = result.getString(2);
            // Handle possibility of missing column in older DBs if upgrade failed,
            // effectively handled by index check
            if (result.getColumnCount() > 3) {
                model.keyword = result.getString(3);
            } else {
                model.keyword = "";
            }
            dataArrayList.add(model);
        }
        result.close();
        return dataArrayList;
    }

    public boolean updateData(String id, String name, String mob, String keyword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ID, id);
        contentValues.put(NAME_COLUMN, name);
        contentValues.put(MOBILE_COLUMN, mob);
        contentValues.put(KEYWORD_COLUMN, keyword);

        int result = db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { id });
        return result != -1;
    }

    public boolean deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, " ID = ?", new String[] { id });
        return result != -1;
    }
}
