package com.equalsd.recon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by generic on 7/2/15.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<elements> imageArray;
    private LayoutInflater Inflater;

    public ImageAdapter(Context context, ArrayList<elements> imageArray) {
        this.context = context;
        this.imageArray = imageArray;
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView = (View) convertView;

        Log.d("recon path: ", position + ": " + imageArray.get(position).picture);

        if (convertView == null) {
            gridView = Inflater.inflate(R.layout.item_grid, parent, false);
            //gridView = Inflater.inflate(R.layout.item_grid, null);

            TextView textView = (TextView) gridView.findViewById(R.id.grid_item_label);
            textView.setText(imageArray.get(position).notes.toString());

            ImageView imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
            if (!imageArray.get(position).picture.contains("content")) {
                Ion.with(imageView).load(imageArray.get(position).picture.toString());
            } else {
                String[] uriArray = imageArray.get(position).picture.split("/");
                Long originalID = Long.valueOf(uriArray[uriArray.length - 1]).longValue();
                //String[] uriSingle = new String[1];
                //uriSingle[0] = uriArray[uriArray.length - 1];
                //Log.d("recon path: ", imageArray.get(position).picture + " " + originalID.toString());

                    /*Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, uriSingle, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String fullPath = cursor.getString(0);
                        Log.d("recon path: ", fullPath);
                        cursor.close();
                    }*/
                Bitmap bitMap = null;
                bitMap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), originalID, MediaStore.Images.Thumbnails.MINI_KIND,
                        (BitmapFactory.Options) null);
                imageView.setImageBitmap(bitMap);
            }

        } /*else {
            gridView = (View) convertView;
        }*/

        return gridView;
    }

    /*public static Bitmap Rotate(Bitmap _input, float _angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(_angle);
        return Bitmap.createBitmap(_input, 0, 0, _input.getWidth(), _input.getHeight(), matrix, true);
    }*/

    @Override
    public int getCount() {
        return (imageArray != null) ? imageArray.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (imageArray != null) ? imageArray.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {return true;}

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
