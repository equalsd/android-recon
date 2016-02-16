package com.equalsd.recon;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * Created by generic on 6/30/15.
 */
public class elements implements Adapter {
    public String location;
    public String picture;
    public String notes;
    public String category;
    public Integer uniqueID;

        public elements(String location, String picture, String notes, String category, int uniqueID) {
            this.location = location;
            this.picture = picture;
            this.notes = notes;
            this.category = category;
            this.uniqueID = uniqueID;
        }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
