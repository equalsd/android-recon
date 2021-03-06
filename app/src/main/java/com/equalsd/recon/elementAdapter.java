package com.equalsd.recon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by generic on 6/30/15.
 */
public class elementAdapter extends ArrayAdapter<elements> {

    public elementAdapter(Context context, ArrayList<elements> elements) {
        super(context, 0, elements);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        elements element = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.site_title);
        TextView tvHome = (TextView) convertView.findViewById(R.id.site_sub);
        // Populate the data into the template view using the data object
        tvName.setText(element.location);
        //tvHome.setText(element.address);
        // Return the completed view to render on screen

        return convertView;
    }
}

