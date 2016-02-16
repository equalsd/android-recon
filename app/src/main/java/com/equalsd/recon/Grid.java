package com.equalsd.recon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.koushikdutta.ion.Ion;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by generic on 7/2/15.
 */
public class Grid extends AppCompatActivity {

    static int TAKE_PICTURE_REQUEST_B = 100;

    GridView gridView;
    static String state;
    static String username;
    static String actionable;
    static String current = "";
    private DBHelper mydb;
    TextView title;
    static int TAKE_PICTURE = 1;
    //Button button;

    static ArrayList<String> category;
    ImageAdapter adapter;
    ArrayList<elements> arrayOfElements = new ArrayList<elements>();

    Runnable run = new Runnable() {
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        //RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        //button = (Button) findViewById(R.id.button);
        title = (TextView) findViewById(R.id.title);
        //title.setWidth(layout.getWidth() - button.getWidth());

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        mydb = new DBHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d("recon start Grid:", extras.toString());

            //username = extras.getString("username");
            state = extras.getString("state");
            category = extras.getStringArrayList("category");
            actionable = extras.getString("actionable");
        }

        if (!category.isEmpty()) {
            current = category.get(category.size() - 1);
            title.setText(TextUtils.join(" > ", category));
        }

        /*String[] track = state.split("-");
        arrayOfElements = mydb.getCategoryImages("elements", track[1], current);
        if (arrayOfElements.isEmpty()) {
            title.setText(TextUtils.join(" > ", category) + ": No images present");
        } else {
            for (int i = 0; i < arrayOfElements.size(); i++) {
                //check to see if local.
                if (!arrayOfElements.get(i).picture.contains("content")) {
                    arrayOfElements.get(i).picture = "http://precisreports.com/clients/" + state + "/" + arrayOfElements.get(i).picture + ".jpg";
                }
            }*/

        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Alpha);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
               Toast.makeText(getApplicationContext(), ((TextView) v).getText(), Toast.LENGTH_LONG).show();
           }
        });*/
        //}

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0042aa")));
            //actionBar.hide();
        }

        if (actionable.matches("camera")) {
            actionable = "not";
            startActivity(new Intent(getApplicationContext(), CameraActivity.class));
        }

        Log.d("recon actionable", actionable);

        //actionBar.setTitle("Inspection Program");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //add MenuItem(s) to ActionBar using Java code
        MenuItem menuItem_Purchase = menu.add(0, 1, Menu.NONE, "Take Photo");
        MenuItemCompat.setShowAsAction(menuItem_Purchase,
                MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        //Log.d("recon", savedSite.toString());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == 1) {
            takePhoto();
        }

        return false;
    }

    private void alertDialog(final Integer position) {
        final Dialog builder = new Dialog(this);

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.detail_dialogue, null);
        builder.setContentView(view);

        final EditText input = (EditText) builder.findViewById(R.id.note);
        builder.setTitle("Edit notes for this image.");
        //input.setText(position + ": " + arrayOfElements.get(position).notes.toString() + arrayOfElements.get(position).picture.toString());
        input.setText(arrayOfElements.get(position).notes.toString());

        ImageView image = (ImageView) builder.findViewById(R.id.image);
        if (!arrayOfElements.get(position).picture.contains("content")) {
            Ion.with(image).load(arrayOfElements.get(position).picture.toString());
            Log.d("recon path: ", arrayOfElements.get(position).picture);
        } else {
            String[] uriArray = arrayOfElements.get(position).picture.split("/");
            Long originalID = Long.valueOf(uriArray[uriArray.length - 1]).longValue();
            //String[] uriSingle = new String[1];
            //uriSingle[0] = uriArray[uriArray.length - 1];
            Log.d("recon path: ", arrayOfElements.get(position).picture + " " + originalID.toString());

                /*Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, uriSingle, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String fullPath = cursor.getString(0);
                    Log.d("recon path: ", fullPath);
                    cursor.close();
                }*/
            Bitmap bitMap = null;
            bitMap = MediaStore.Images.Thumbnails.getThumbnail(this.getContentResolver(), originalID, MediaStore.Images.Thumbnails.MINI_KIND,
                    (BitmapFactory.Options) null);
            image.setImageBitmap(bitMap);
        }

        Log.d("recon", arrayOfElements.get(position).uniqueID.toString());

        /*input.setOnKeyListener(new DialogInterface.OnKeyListener(view) {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            Toast.makeText(getApplicationContext(), "Okay", Toast.LENGTH_LONG).show();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });*/

        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            //Toast.makeText(getApplicationContext(), "Okay", Toast.LENGTH_LONG).show();

                            //save...
                            String newNote = input.getText().toString();
                            arrayOfElements.get(position).notes = newNote;

                            //insert into DB
                            ArrayList<AbstractMap.SimpleEntry<String, String>> newInsert = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                            newInsert.add(new AbstractMap.SimpleEntry("notes", DatabaseUtils.sqlEscapeString(newNote)));
                            mydb.updateContact("elements", arrayOfElements.get(position).uniqueID, newInsert);

                            //update adapter
                            //adapter.notifyDataSetChanged();
                            gridView.setAdapter(new ImageAdapter(getApplicationContext(), arrayOfElements));
                            builder.dismiss();

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        //input.(keyListener);
        /*
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(notes);
        builder.setView(input);*/

        // Set up the buttons
        Button positive = (Button) builder.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save...
                String newNote = input.getText().toString();
                arrayOfElements.get(position).notes = newNote;

                //insert into DB
                ArrayList<AbstractMap.SimpleEntry<String, String>> newInsert = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                newInsert.add(new AbstractMap.SimpleEntry("notes", newNote));
                mydb.updateContact("elements", arrayOfElements.get(position).uniqueID, newInsert);

                //update adapter
                //adapter.notifyDataSetChanged();
                gridView.setAdapter(new ImageAdapter(getApplicationContext(), arrayOfElements));
                builder.dismiss();
            }
        });

        Button delete = (Button) builder.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mydb.deleteContact(arrayOfElements.get(position).uniqueID, "elements");
                int pos = position;
                arrayOfElements.remove(pos);
                //String[] track = state.split("-");
                //arrayOfElements = mydb.getCategoryImages("elements", track[1], current);
                gridView.setAdapter(new ImageAdapter(getApplicationContext(), arrayOfElements));
                builder.dismiss();
                //Toast.makeText(getApplicationContext(), position + " " + arrayOfElements.get(position).uniqueID.toString(), Toast.LENGTH_LONG).show();
*/

                /*AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d("recon", "yes");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d("recon", "no");
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
                builder.dismiss();
                confirmDialog(position);
            }
        });

        Button negative = (Button) builder.findViewById(R.id.negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }

        });

        builder.show();
    }

    private void confirmDialog(final Integer position) {
        new AlertDialog.Builder(Grid.this)
                .setTitle("Warning: ")
                .setMessage("Are you sure you want to delete this image?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("recon", "yes");
                        mydb.deleteContact(arrayOfElements.get(position).uniqueID, "elements");
                        int pos = position;
                        arrayOfElements.remove(pos);
                        //String[] track = state.split("-");
                        //arrayOfElements = mydb.getCategoryImages("elements", track[1], current);
                        gridView.setAdapter(new ImageAdapter(getApplicationContext(), arrayOfElements));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("recon", "no");
                        alertDialog(position);
                    }
                }).show();

        /*AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("Are you sure you want to delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("recon", "yes");
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("recon", "no");
                        dialog.cancel();
                        alertDialog(position);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("recon resume", "resumed");
        String[] track = state.split("-");
        String position = TextUtils.join("|", category);
        arrayOfElements = mydb.getCategoryImages("elements", track[1], current, position);
        Integer[] IDs = {};

        for (int i = 0; i < arrayOfElements.size(); i++) {
            //check to see if local.
            if (!arrayOfElements.get(i).picture.contains("content")) {
                arrayOfElements.get(i).picture = "http://ada-veracity.com/clients/" + state + "/" + arrayOfElements.get(i).picture + ".jpg";
            }

            Log.d("recon pre-image: ", String.valueOf(i));
        }

        adapter = new ImageAdapter(this, arrayOfElements);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                alertDialog(position);
            }
        });
    }

    //public void takePhoto(View view) {
    public void takePhoto() {
        //Toast.makeText(getApplicationContext(), "DetailView", Toast.LENGTH_LONG).show();
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, TAKE_PICTURE);

        //startActivityForResult(new Intent(getApplicationContext(), CameraActivity.class), TAKE_PICTURE_REQUEST_B);
        startActivity(new Intent(getApplicationContext(), CameraActivity.class));
        //Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.CameraActivity.class);
        //startActivityForResult(intent, TAKE_PICTURE_REQUEST_B);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Grid Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.equalsd.recon/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Grid Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.equalsd.recon/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Toast.makeText(getApplicationContext(), resultCode + " " + requestCode, Toast.LENGTH_LONG).show();

        if (requestCode == TAKE_PICTURE && resultCode== RESULT_OK && intent != null){
            // get bundle
            Bundle extras = intent.getExtras();

            Toast.makeText(this, "Image saved to:\n" +
                    intent.getData(), Toast.LENGTH_LONG).show();

            // get bitmap
            /*Uri imageUri = intent.getData();
            Bitmap bitMap = null;
            try {
                bitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

    //bitMap = (Bitmap) extras.get("data");
    //ivThumbnailPhoto.setImageBitmap(bitMap);
            /*String parent = "";

            String[] track = state.split("-");
            String catString = TextUtils.join("|", category);
            Log.d("recon save image: ", intent.getData().toString());
            //String[] uriArray = intent.getData().toString().split("/");
            //String originalID = uriArray[uriArray.length - 1];

            ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
            pairs.add(new AbstractMap.SimpleEntry("location", current));
            pairs.add(new AbstractMap.SimpleEntry("picture", intent.getData().toString()));
            pairs.add(new AbstractMap.SimpleEntry("notes", ""));
            pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
            pairs.add(new AbstractMap.SimpleEntry("category", catString));
            mydb.insertData("elements", pairs);

            gridView.setAdapter(new ImageAdapter(getApplicationContext(), arrayOfElements));

        }

        //intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, TAKE_PICTURE);
    }*/

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Enter your code here

        Toast.makeText(getApplicationContext(), "t", Toast.LENGTH_LONG).show();

    }*/

}
