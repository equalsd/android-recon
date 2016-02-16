package com.equalsd.recon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by generic on 6/24/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MobileInspection.db";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("create table login " + "(id integer primary key, username text not null, password text not null, site text not null, type text not null)");
        db.execSQL("create table elements " + "(id integer primary key, location text not null, notes text not null, picture text not null, category text not null, tracking text not null, user text not null)");
    }

    public void onClear(String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + table);
    }

    public void onDelete(String tracking) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from elements where tracking like " + tracking);
    }

    public void allDelete() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from elements");
        Log.d("recon", "clearing all elements");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS login");
        db.execSQL("DROP TABLE IF EXISTS elements");
        onCreate(db);

    }

    /*public void onDelete(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + table);
    }*/

    public boolean insertData(String table, ArrayList<AbstractMap.SimpleEntry<String, String>> pairs)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sanitized = "";
        ContentValues contentValues = new ContentValues();
        for (Integer i = 0; i < pairs.size(); i++) {
            //if (pairs.get(i).getKey() == "uniqueID") {
            //    contentValues.put("id", Integer.parseInt(pairs.get(i).getValue()));
            //} else {
                sanitized = pairs.get(i).getValue().replaceAll("'", "\'");
                contentValues.put(pairs.get(i).getKey(), sanitized);
            //}
        }
        Log.d("recon", contentValues.toString());
        Long string = db.insert(table, null, contentValues);
        Log.d("recon", "inserted: " + string.toString());
        return true;
    }

    public Cursor getData(String table, int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + table + " where id=" + id + "", null);
        return res;
    }

    public Cursor getNames(String tracking, String user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        if (tracking.matches("")) {
            res = db.rawQuery("select * from elements where picture='name' and user='" + user + "'", null);
        } else {
            res = db.rawQuery("select * from elements where picture='name' and tracking='" + tracking + "' and user='" + user + "'", null);
        }
        return res;
    }

    public String getTypes(String tracking){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from elements where picture='sitetype' and tracking='" + tracking + "'", null);
        String type = "";
        if (res.getCount() > 0) {
            res.moveToFirst();
            while (!res.isAfterLast()) {
                type = res.getString(res.getColumnIndex("notes"));
                res.moveToNext();
            }
        }
        return type;
    }


    public int numberOfRows(String table){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, table);
        return numRows;
    }

    public boolean updateContact (String table, Integer id, ArrayList<AbstractMap.SimpleEntry<String, String>> pairs)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Integer i = 0; i < pairs.size(); i++) {
            contentValues.put(pairs.get(i).getKey(), pairs.get(i).getValue());
            Log.d("recon, update: ", pairs.get(i).getKey() + " " + pairs.get(i).getValue() + " @ " + id);
        }
        Integer returned = db.update(table, contentValues, "id = ? ", new String[]{Integer.toString(id)});
        Log.d("recon, update", returned.toString());
        return true;
    }

    public Integer deleteContact (Integer id, String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<elements> getAll(String tracking) {
        ArrayList<elements> array_list = new ArrayList<elements>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from elements where tracking like \"" + tracking + "\"";

        Cursor res = null;
        res = db.rawQuery(query, null);
        if (res.getCount() > 0 && res != null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("id"))));
                res.moveToNext();
            }
        }

        return array_list;
    }

    public ArrayList<elements> categoryExists(String tracking, String category, String location) {
        ArrayList<elements> array_list = new ArrayList<elements>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from elements where tracking like \"" + tracking + "\" and category like \"" + category + "\" and location like \"" + location + "\"";

        Cursor res = null;
        res = db.rawQuery(query, null);
        Log.d("recon Count: ", String.valueOf(res.getCount()) + " for: " + tracking + ", " + category);
        if (res.getCount() > 0 && res != null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("id"))));
                res.moveToNext();
            }
        }

        return array_list;
    }

    public ArrayList<elements> getCategory(String table, String tracking, String category, String parent) {
        ArrayList<elements> array_list = new ArrayList<elements>();

        SQLiteDatabase db = this.getReadableDatabase();
        String sanitizedCategory = category.replaceAll("'", "\'");
        String query = "select * from " + table + " where tracking = \"" + tracking + "\" and category = \"" + sanitizedCategory + "\" and picture = \"locationed\""; // and location not like \"" + parent + "\"";
        //query = "select * from " + table + " where tracking = \"" + tracking + "\" and picture = \"location\""; // and location not like \"" + parent + "\"";
        /*if (category.equals("")) {
            query = "select * from " + table + " where tracking like \"" + tracking + "\" and picture like \"location\"";
        }*/

        Cursor res = null;
        res = db.rawQuery(query, null);
        Log.d("recon query", query);
        Log.d("recon Count: ", String.valueOf(res.getCount()) + " for: " + tracking + ", " + sanitizedCategory);
        if (res.getCount() > 0 && res != null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                Log.d("recon", res.getString(res.getColumnIndex("picture")) + ", " + res.getString(res.getColumnIndex("location")));
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("id"))));
                res.moveToNext();
            }
        }
        /*if (res != null && res.getCount() > 0) {
            res.moveToFirst();
            do {
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("uniqueID"))));
            } while (res.moveToNext());
        }*/

        return array_list;
    }

    //get Limiter by type
    public ArrayList<elements> getLimits(String table, String category) {
        ArrayList<elements> array_list = new ArrayList<elements>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from elements where picture like \"" + category + "\" and tracking like \"" + table + "\"";
        Cursor res =  db.rawQuery(query, null);
        Log.d("recon get limit: ", category + " for " + table);
        if (res.getCount() > 0 && res != null) {
            res.moveToFirst();
            array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("id"))));
        }

        return array_list;
    }

    public boolean updateLimit(String type, ArrayList<AbstractMap.SimpleEntry<String, String>> pairs)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (Integer i = 0; i < pairs.size(); i++) {
            contentValues.put(pairs.get(i).getKey(), pairs.get(i).getValue());
            Log.d("recon, update: ", pairs.get(i).getKey() + " " + pairs.get(i).getValue() + " @ " + type);
        }
        Integer returned = db.update("elements", contentValues, "picture = ? ", new String[] { type } );
        Log.d("recon, update", returned.toString());
        return true;
    }

    public ArrayList<elements> getCategoryImages(String table, String tracking, String parent, String category) {
        ArrayList<elements> array_list = new ArrayList<elements>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + table + " where tracking like \"" + tracking + "\" and location like \"" + parent + "\" and category like \"" + category + "\" and picture not like \"locationed\"";

        Cursor res = null;
        res = db.rawQuery(query, null);
        Log.d("recon Count Images: ", String.valueOf(res.getCount()) + " for: " + tracking + ", " + parent);
        if (res.getCount() > 0 && res != null) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("id"))));
                res.moveToNext();
            }
        }
        /*if (res != null && res.getCount() > 0) {
            res.moveToFirst();
            do {
                array_list.add(new elements(res.getString(res.getColumnIndex("location")), res.getString(res.getColumnIndex("picture")), res.getString(res.getColumnIndex("notes")), res.getString(res.getColumnIndex("category")), res.getInt(res.getColumnIndex("uniqueID"))));
            } while (res.moveToNext());
        }*/

        return array_list;
    }

    public void ti() {
        SQLiteDatabase db = this.getReadableDatabase();

       /* Cursor ti = db.rawQuery("PRAGMA table_info(elements)", null);
        if (ti.moveToFirst()) {
            do {
                Log.d("recon col: ", ti.getString(1));
            } while (ti.moveToNext());
        }*/

        Cursor ti = db.rawQuery("select * from elements", null);
        ti.moveToFirst();
        while (ti.isAfterLast() == false) {
            Log.d("recon", ti.getString(ti.getColumnIndex("category")));
            ti.moveToNext();
        }
    }
    /*public ArrayList<String> getAllContacts(String table)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + table, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }*/
}
