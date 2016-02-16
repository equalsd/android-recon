package com.equalsd.recon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;

import static java.util.Arrays.asList;

/**
 * Created by generic on 6/27/15.
 */
public class List extends AppCompatActivity {

    TextView title;
    static final String URL_SITE = "http://ada-veracity.com/api/get-sites-type-json.php";
    static final String URL_ELEMENT = "http://ada-veracity.com/api/get-elements-json.php";
    static final String URL_UPLOAD = "http://ada-veracity.com/api/put-json-elements.php";
    private static final String QUERY_URL = "http://ada-veracity.com/api/verify-login-json.php";
    static String state;
    String actionable;
    String tracker;
    String trackerType;
    String credit;
    static String password;
    static String username;
    static String old_username;
    static String old_password;
    Integer i;

    ArrayList<String> typeArray;
    static ArrayList<String> category;
    ArrayList<sites> arrayOfSites = new ArrayList<com.equalsd.recon.sites>();
    ArrayList<elements> arrayOfElements = new ArrayList<com.equalsd.recon.elements>();
    ArrayList<String> elementDisplay = new ArrayList<String>();
    ArrayList<elements> arrayOfPictures = new ArrayList<elements>();
    ArrayList<AbstractMap.SimpleEntry<String, String>> arrayOfNames = new ArrayList<>();
    ArrayList<String> savedSite = new ArrayList<String>();
    ArrayList<String> elementCount = new ArrayList<>();

    ArrayAdapter adapter;
    ListView listview;
    //Button context;

    private DBHelper mydb;
    private Dialog builderUpload;
    private ProgressBar progressUpload;
    private TextView progressText;
    private Button dialogUpload;
    private Button dialogCancel;
    private String limiterType;
    private String limits;
    private String titleBar = "Inspection Program";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listview = (ListView) findViewById(R.id.main_listview);
        title = (TextView) findViewById(R.id.test);
        //context = (Button) findViewById(R.id.context_option);

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        Log.d("recon", "starting intent");
        if (extras != null) {
            //int Value = extras.getInt("id");
            //test.setText(String.valueOf(Value));

            username = extras.getString("username");
            password = extras.getString("password");
            state = extras.getString("state");
            actionable = extras.getString("actionable");
            category = extras.getStringArrayList("category");
            typeArray = extras.getStringArrayList("type");
            tracker = extras.getString("tracker");
            trackerType = extras.getString("trackerType");
            //credit = extras.getString("credit");

            if (tracker == null) {
                Log.d("recon tracker: ", "null");
            } else {
                Log.d("recon tracker: ", tracker);
            }

            if (!tracker.contains("-")) {
                if (!state.contains("-")) {
                    //context.setEnabled(false);
                } else {

                    Log.d("recon", "look");

                    tracker = state;
                    trackerType = TextUtils.join("|", typeArray);
                    ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                    pairs.add(new AbstractMap.SimpleEntry("site", state));
                    pairs.add(new AbstractMap.SimpleEntry("type", trackerType));
                    mydb.updateContact("login", 1, pairs);
                }
            } else {
                //Button continuer = (Button) findViewById(R.id.context_option);
                /*String[] track = tracker.split("-");
                Cursor res = mydb.getNames(track[1]);
                if (res.getCount() > 0) {
                    res.moveToFirst();
                    while (!res.isAfterLast()) {
                        Log.d("recon continue this: ", res.getString(res.getColumnIndex("notes")));
                        //context.setText("Continue "  + res.getString(res.getColumnIndex("notes")));
                        res.moveToNext();
                    }
                }*/
            }

            if (state.contains("-")) {
                String[] track = state.split("-");
                Cursor res = mydb.getNames(track[1], username);
                if (res.getCount() > 0) {
                    res.moveToFirst();
                    while (!res.isAfterLast()) {
                        Log.d("recon continue this: ", res.getString(res.getColumnIndex("notes")));
                        titleBar = res.getString(res.getColumnIndex("notes"));
                        res.moveToNext();
                    }
                }
            } else {
                Cursor Names = null;
                Names = mydb.getNames("", username);
                Log.d("recon Names", "Count: " + String.valueOf(Names.getCount()));
                if (Names.getCount() > 0) {
                    Names.moveToFirst();
                    while (!Names.isAfterLast()) {
                        savedSite.add(Names.getString(Names.getColumnIndex("tracking")));
                        arrayOfNames.add(new AbstractMap.SimpleEntry<String, String>(Names.getString(Names.getColumnIndex("notes")), Names.getString(Names.getColumnIndex("tracking"))));
                        Log.d("recon names: ", Names.getString(Names.getColumnIndex("notes")) + " " + Names.getString(Names.getColumnIndex("tracking")));
                        Names.moveToNext();
                    }
                }
            }

            limiterType = getLimiter();
            if (limiterType != "neither") {
                String[] track = tracker.split("-");
                ArrayList<elements> temp_list = mydb.getLimits(track[1], limiterType);
                //limits = mydb.getLimits(track[1], limiterType);
                if (!temp_list.isEmpty()) {
                    limits = temp_list.get(0).notes.replaceAll("\'", "");
                } else {
                    limits = "0";
                }
                Log.d("recon", limits + " for " + limiterType);
            } else {
                Log.d("recon", "no limit...");
            }

            old_username = username;
            old_password = password;

            populate();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0042aa")));
            //actionBar.hide();
        }

        actionBar.setTitle(titleBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //add MenuItem(s) to ActionBar using Java code
        //MenuItem menuItem_Purchase = menu.add(0, 1, Menu.NONE, "Purchase Credits (" + credit +  ")");
        //MenuItemCompat.setShowAsAction(menuItem_Purchase,
                //MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        //add MenuItem(s) to ActionBar using Java code
        Log.d("recon empty?", tracker + " " + state);
        if (tracker.contains("-")) {
            if (state.contains("-")) {
                MenuItem menuItem_Info = menu.add(0, 1, Menu.NONE, "Upload");
                MenuItemCompat.setShowAsAction(menuItem_Info,
                        MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            } else {
                /*MenuItem menuItem_Info = menu.add(0, 1, Menu.NONE, "Locally Saved   :");
                MenuItemCompat.setShowAsAction(menuItem_Info,
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);


                Cursor Names = null;
                Names = mydb.getNames("");
                int i = 1;
                Log.d("recon Names", "Count: " + String.valueOf(Names.getCount()));
                if (Names.getCount() > 0) {
                    Names.moveToFirst();
                    while (!Names.isAfterLast()) {
                        i++;
                        savedSite.add(Names.getString(Names.getColumnIndex("tracking")));
                        arrayOfNames.add(new AbstractMap.SimpleEntry<String, String>(Names.getString(Names.getColumnIndex("notes")), Names.getString(Names.getColumnIndex("tracking"))));
                        Log.d("recon names: ", Names.getString(Names.getColumnIndex("notes")) + " " + Names.getString(Names.getColumnIndex("tracking")));
                        MenuItem menuItem_Info1 = menu.add(0, i, Menu.NONE, Names.getString(Names.getColumnIndex("notes")));
                        //menuItem_Info.setIcon(android.R.drawable.ic_menu_info_details);
                        MenuItemCompat.setShowAsAction(menuItem_Info1,
                                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                        Log.d("recon Names", Names.toString());
                        Names.moveToNext();
                    }
                }*/
                MenuItem menuItem_Info = menu.add(0, 1, Menu.NONE, "Continue");
                MenuItemCompat.setShowAsAction(menuItem_Info,
                        MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        } else {
            /*MenuItem menuItem_Info = menu.add(0, 1, Menu.NONE, "Upload");
            MenuItemCompat.setShowAsAction(menuItem_Info,
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);*/
        }

        //Log.d("recon", savedSite.toString());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        /*if (item.getItemId() == 1) {
            //Toast.makeText(getApplicationContext(), "Buy", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.PurchaseActivity.class);
            //intent.putExtras(dataBundle);
            startActivity(intent);
        } else */
        if (tracker.contains("-")) {
            if (state.contains("-")) {
                //check username and password first...
                checkLogin();
            } else {
                /*if (item.getItemId() == 1) {
                    //do nothing
                } else {
                    int selected = item.getItemId() - 2;
                    String value = arrayOfNames.get(selected).getValue();
                    String track = username + "-" + value;
                    String trackingType = mydb.getTypes(value);
                    continueSite(track, trackingType);
                }*/
                String[] split = tracker.split("-");
                String trackingType = mydb.getTypes(split[1]);
                continueSite(tracker, trackerType);
            }
        } else {
            //check username and password first...
            checkLogin();
        }

        return false;
    }

    private void checkLogin() {
        //check login
        verifyLogin();
    }

    public void verifyLogin() {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        final String[] status = new String[1];
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), QUERY_URL, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        // called when response HTTP status is "200 OK"
                        try {
                            status[0] = jsonObject.get("status").toString();
                            Log.d("recon", status[0]);
                            if (status[0].equals("success")) {
                                //credit[0] = jsonObject.get("credit").toString();
                                //Log.d("recon", "credit " + credit[0]);

                                if (!old_username.matches(username) || !old_password.matches(password)) {
                                    Log.d("Recon", "clear sites, different username");
                                    mydb.allDelete();

                                    ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                    pairs.add(new AbstractMap.SimpleEntry("username", username));
                                    pairs.add(new AbstractMap.SimpleEntry("password", password));
                                    //pairs.add(new AbstractMap.SimpleEntry("site", tracker));
                                    //pairs.add(new AbstractMap.SimpleEntry("type", trackerType));

                                    mydb.updateContact("login", 1, pairs);
                                }

                                alertDialogUpload();
                            } else {
                                alertCheckLogin("login");
                            }
                        } catch (JSONException e) {
                            Log.d("recon", String.valueOf(e));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("recon", "error " + throwable);
                        Toast.makeText(getApplicationContext(), "Error:  " + statusCode + " Verify your Internet Connection is stable or working.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });
    }

    private ArrayList<sites> arrangeDisplay(ArrayList<String> elementToDisplay) {
        ArrayList<sites> arrayOfItems = new ArrayList<sites>();

        for (Integer i = 0; i < elementToDisplay.size(); i++) {
            if (limiterType == "neither") {
                arrayOfItems.add(new sites(elementToDisplay.get(i), "", "", "", ""));
            } else {
                arrayOfItems.add(new sites(elementToDisplay.get(i), elementCount.get(i), "", "", ""));
            }
        }

        return arrayOfItems;
    }

    private void populate() {
        Log.d("recon", "populating adapter");
        // Construct the data source
        getAdapterCategory(state);
        // Create the adapter to convert the array to views
        /*if (state.contains("-")) {
            //adapter = new elementAdapter(this, arrayOfElements);
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, elementDisplay);
        } else {
            adapter = new siteAdapter(this, arrayOfSites);
        }*/
        if (state.contains("-")) {
            arrayOfSites = arrangeDisplay(elementDisplay);
        }
        adapter = new siteAdapter(this, arrayOfSites);
        // Attach the adapter to a ListView
        listview = (ListView) findViewById(R.id.main_listview);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle dataBundle = new Bundle();
                dataBundle.putString("username", username);
                dataBundle.putString("password", password);
                //dataBundle.putString("credit", credit);

                Intent intent = null;
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (!actionable.equals("site")) {
                    dataBundle.putString("tracker", tracker);
                    dataBundle.putString("trackerType", trackerType);
                    sites rawValue = (sites) parent.getItemAtPosition(position);
                    String value = rawValue.location;

                    Log.d("recon", "singleton: " + value);
                    if (value.equals("Images") && position == 0) {
                        //Toast.makeText(getApplicationContext(), "View Images Intent", Toast.LENGTH_LONG).show();
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.Grid.class);
                        dataBundle.putString("state", state);
                        dataBundle.putStringArrayList("category", category);
                        dataBundle.putStringArrayList("type", typeArray);
                        dataBundle.putString("actionable", "camera");

                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    } else if ((value.equals("Add Restroom") || value.equals("Add Parking Area") || value.equals("Add Parking Space") || value.equals("Add Sub-Location")) && position == 1) {
                        //Toast.makeText(getApplicationContext(), "Add Sub-Location", Toast.LENGTH_LONG).show();
                        //check parking...
                        //get type.  get limit.  get location.
                        Log.d("recon", "adding location.... ");
                        Boolean goAndAdd = true;
                        if (limiterType != "neither") {
                            //count spaces.
                            if (!countLimit()) {
                                goAndAdd = false;
                                Log.d("recon", "do not add, past limit");
                            }
                        }

                        if (goAndAdd) {
                            Log.d("recon", "go and add");
                            alertDialogLocation(value);
                        } else {
                            //warning dialogue
                            new AlertDialog.Builder(com.equalsd.recon.List.this)
                                    .setTitle("Cannot add")
                                    .setMessage("The limit of the number of sub-locations has been reached.  Modify the site specifications in the root menu to add more restroom or parking locations.")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {

                                        }
                                    }).show();
                        }
                    } else if (value.equals("Modify Site Constraints") && position == 0) {
                        dataBundle.putString("tracker", tracker);
                        dataBundle.putString("trackerType", trackerType);
                        dataBundle.putString("actionable", "upgrade");
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.NewActivity.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    } else {
                        //go to next Location...
                        ArrayList<String> newCategory = category;
                        newCategory.add(value);

                        Log.d("recon", "cat: " + category.toString());

                        dataBundle.putString("state", state);
                        dataBundle.putStringArrayList("type", typeArray);
                        dataBundle.putString("actionable", actionable);
                        dataBundle.putStringArrayList("category", newCategory);

                        intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                } else {
                    //site menu...
                    sites value = (sites) parent.getItemAtPosition(position);

                    dataBundle.putString("tracker", value.tracking);
                    dataBundle.putString("trackerType", value.siteType);
                    dataBundle.putString("state", value.tracking);
                    dataBundle.putStringArrayList("category", category);

                    //intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);

                    if (value.tracking.contains("-")) {
                        //loading site...
                        Log.d("recon", "starting site " + value.tracking);
                        Log.d("recon state", state);

                        //Toast.makeText(getApplicationContext(), "load elements", Toast.LENGTH_LONG).show();
                        typeArray = new ArrayList<String>(asList(value.siteType.split("\\|")));
                        dataBundle.putStringArrayList("type", typeArray);
                        ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        pairs.add(new AbstractMap.SimpleEntry("site", value.tracking));
                        pairs.add(new AbstractMap.SimpleEntry("type", value.siteType));
                        mydb.updateContact("login", 1, pairs);

                        //save as continue
                        //startActivity(intent);

                        if (state.equals("Saved Sites")) {
                            dataBundle.putString("actionable", "loadDBelements");
                            intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                            intent.putExtras(dataBundle);

                            final Intent finalIntent = intent;
                            String[] tracked = value.tracking.split("-");

                            startActivity(finalIntent);
                        } else {
                            dataBundle.putString("actionable", "loadWebelements");
                            intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                            intent.putExtras(dataBundle);

                            final Intent finalIntent = intent;
                            String[] tracked = value.tracking.split("-");

                            if (savedSite.contains(tracked[1])) {
                                new AlertDialog.Builder(com.equalsd.recon.List.this)
                                        .setTitle("Warning: ")
                                        .setMessage("This site is already on this device.  Reloading from the server will erase any unique data on this device for this site.  Do you really want to reload from Server?\n\n (To continue without reloading, go to 'Saved Sites' and select from there.)")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                startActivity(finalIntent);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null).show();
                            } else {
                                startActivity(finalIntent);
                            }
                        }

                    } else if (value.tracking.equals("Add New Site")) {
                        //adding site...
                        //Toast.makeText(getApplicationContext(), "add new site", Toast.LENGTH_LONG).show();
                        dataBundle.putString("actionable", actionable);
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.NewActivity.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    } else if (value.tracking.equals("Subscribe")) {
                        //checking subscription status
                        dataBundle.putString("actionable", "subscribe");
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.PurchaseActivity.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    } else if (value.tracking.equals("Saved Sites")) {
                        //load saved sites
                        dataBundle.putString("actionable", actionable);
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    } else {

                        dataBundle.putString("actionable", actionable);
                        intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private Integer numberOf() {
        String[] track = tracker.split("-");
        String position = TextUtils.join("|", category);
        //ArrayList<elements> arrayToCheck = mydb.getAll(track[1]);
        ArrayList<elements> arrayToCheck = mydb.getCategory("elements", track[1], position, "");
        Integer count = 0;
        //Integer size = 0;
        String[] parts = null;

        for (Integer i = 0; i < arrayToCheck.size(); i++) {
            if (arrayToCheck.get(i).category.contains("|")) {
                parts = arrayToCheck.get(i).category.split("\\|");
                if (parts.length == 2) {
                    if (limiterType.matches("parking")) {
                        if (parts[0].matches("Parking") && !parts[1].matches("Passenger Loading Zones")) {
                            Log.d("recon", arrayToCheck.get(i).category + " " + parts.length);
                            count++;
                        }
                    } else {
                        if (parts[0].matches("Interior Path of Travel") && parts[1].matches("Restrooms")) {
                            Log.d("recon", arrayToCheck.get(i).category + " " + parts.length);
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    private Boolean countLimit() {
        Integer count = numberOf();

        Log.d("recon", "echo present: " + count + " vs limit: " + limits + " for " + limiterType);
        if (count < Integer.parseInt(limits)) {
           return true;
        }

        return false;
    }

    private void getAdapterCategory(String state) {
        //sites.add(new sites("Bank", "Bank"));
        //context.setText("Continue");
        //context.setOnClickListener(continuer);

        switch (state) {
            case "default":
                title.setText("Select Site");
                //arrayOfSites.add(new sites("Subscribe", "", "Subscribe", ""));
                arrayOfSites.add(new sites("Add New Site", "", "Add New Site", "", ""));
                arrayOfSites.add(new sites("Saved Sites", "", "Saved Sites", "", ""));
                arrayOfSites.add(new sites("Bank", "", "Bank", "", ""));
                arrayOfSites.add(new sites("Gas Station", "", "Gas Station", "", ""));
                arrayOfSites.add(new sites("Health Care Facility", "", "Health", "", ""));
                arrayOfSites.add(new sites("Hotel/Motel", "", "Hotel", "", ""));
                arrayOfSites.add(new sites("Retail/Office", "", "Office", "", ""));
                arrayOfSites.add(new sites("Restaurant", "", "Restaurant", "", ""));
                arrayOfSites.add(new sites("Strip Mall", "", "Strip Mall", "", ""));
                arrayOfSites.add(new sites("Miscellaneous, no set type", "", "Misc", "", ""));
                break;
            case "Saved Sites":
                Cursor Names = null;
                Names = mydb.getNames("", username);
                Log.d("recon Names", "Count: " + String.valueOf(Names.getCount()));
                if (Names.getCount() > 0) {
                    Names.moveToFirst();
                    while (!Names.isAfterLast()) {
                        //savedSite.add(Names.getString(Names.getColumnIndex("tracking")));
                        //arrayOfNames.add(new AbstractMap.SimpleEntry<String, String>(Names.getString(Names.getColumnIndex("notes")), Names.getString(Names.getColumnIndex("tracking"))));
                        Log.d("recon names: ", Names.getString(Names.getColumnIndex("notes")) + " " + Names.getString(Names.getColumnIndex("tracking")));
                        arrayOfSites.add(new sites(Names.getString(Names.getColumnIndex("notes")), Names.getString(Names.getColumnIndex("location")), username + "-" + Names.getString(Names.getColumnIndex("tracking")), "", ""));
                                Log.d("recon Names", Names.toString());
                        Names.moveToNext();
                    }
                } else {
                    title.setText("No saved sites");
                }
                break;
            default:
                if (state.contains("-")) {
                    //context.setText("Upload");
                    //context.setOnClickListener(uploader);
                    //title.setText("Select Location");
                    String position = "";
                    String parent = "";
                    Log.d("recon", "contains -" + actionable);

                    Log.d("recon", "category: " + category.toString() + category.size());
                    if (!category.isEmpty()) {
                        parent = category.get(category.size() -1);
                        position = TextUtils.join("|", category);
                        title.setText(TextUtils.join(" > ", category));
                    } else {
                        title.setText("Select Location");
                    }

                    Log.d("recon", "position: " + position + ", parent: " + parent);
                    elementDisplay = categorizer(position);

                    if (actionable.equals("loadWebelements")) {
                        //get from server
                        Log.d("recon", "getting from website elements");
                        actionable = "loaded";
                        String[] track = state.split("-");
                        mydb.onDelete(track[1]);
                        query(state, "elements");
                    }

                    if (actionable.equals("loadDBelements")) {
                        //Toast.makeText(getApplicationContext(), "loadDBelements", Toast.LENGTH_LONG).show();
                        actionable = "loaded";
                        arrayOfElements = null;
                    }

                    if (!category.isEmpty()) {
                        String[] track = state.split("-");
                        arrayOfElements = mydb.getCategory("elements", track[1], position, parent);
                        //Log.d("recon ArrayOfElements", "position: "  + position + ", column: " + arrayOfElements.toString());
                        for (Integer i = 0; i < elementDisplay.size(); i++) {
                            elementCount.add("");
                        }
                        if (arrayOfElements != null) {
                            for (int i = 0; i < arrayOfElements.size(); i++) {
                                elementDisplay.add(arrayOfElements.get(i).location);
                                elementCount.add(arrayOfElements.get(i).notes);
                                Log.d("recon ArrayOfElements", arrayOfElements.get(i).location + " " + arrayOfElements.get(i).notes);
                            }
                        }
                    }

                    Log.d("recon", "nowhat:" + elementDisplay.toString());
                } else {
                    title.setText("Select " + state);
                    query(state, "site");
                }
        }
    }

    private void query(final String state, final String type) {
        AsyncHttpClient client = new AsyncHttpClient();
        String QUERY_URL = "";

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", List.username);
            jsonParams.put("password", List.password);
            if (type.equals("site")) {
                jsonParams.put("type", state);
                QUERY_URL = URL_SITE;
            } else {
                jsonParams.put("site", state);
                QUERY_URL = URL_ELEMENT;
                //mydb.onClear("elements");
                //Log.d("recon", "cleared: getting " + state);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), QUERY_URL, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // called when response HTTP status is "200 OK"
                        Log.d("recon", jsonObject.toString());
                        try {
                            String status = jsonObject.getString("status");
                            switch (status) {
                                case "Bad Login":
                                    alertCheckLogin(type);
                                    return;
                                case "no file":
                                    Toast.makeText(getApplicationContext(), "Request corrupted.  Check your internet connection.", Toast.LENGTH_LONG).show();
                                    return;
                                case "NA":
                                case "Nothing Found":
                                    Toast.makeText(getApplicationContext(), "No Sites in this Category.", Toast.LENGTH_LONG).show();
                                    title.setText("No " + state + " sites found");
                                    return;
                            }
                            if (jsonObject.getString("result").equals("Nothing Found")) {
                                title.setText("No " + state + " sites found");
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Iterator<String> iterator = jsonObject.keys();
                        ArrayList<AbstractMap.SimpleEntry<String, String>> pairs;
                        String[] track = state.split("-");

                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            try {
                                JSONObject issue = jsonObject.getJSONObject(key);

                                Log.d("recon issue.toString", issue.toString());
                                if (type.equals("site")) {
                                    arrayOfSites.add(new sites(issue.getString("info"), issue.getString("description"), issue.getString("tracking"), issue.getString("type"), ""));
                                    adapter.notifyDataSetChanged();
                                } else {
                                    //if (issue.getString("category").equals();
                                    pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                    //arrayOfElements.add(new elements(issue.getString("location"), issue.getString("picture"), issue.getString("notes"), issue.getString("category"), 0));

                                    String picture = "";
                                    if (issue.getString("picture").matches("location")) {
                                        picture = "locationed";
                                    } else {
                                        picture = issue.getString("picture");
                                    }
                                    pairs.add(new AbstractMap.SimpleEntry("location", issue.getString("location")));
                                    pairs.add(new AbstractMap.SimpleEntry("picture", picture));
                                    //if (picture.matches("locationed")) {
                                        pairs.add(new AbstractMap.SimpleEntry("notes", issue.getString("notes")));
                                    //} else {
                                    //    pairs.add(new AbstractMap.SimpleEntry("notes", DatabaseUtils.sqlEscapeString(issue.getString("notes"))));
                                    //}
                                    pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                                    pairs.add(new AbstractMap.SimpleEntry("category", issue.getString("category")));
                                    pairs.add(new AbstractMap.SimpleEntry("user", username));
                                    mydb.insertData("elements", pairs);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        /*if (!type.equals("site")) {
                            //ArrayList<elements> test = mydb.getCategory("elements", track[1], "");
                            //Log.d("recon-", test.toString());
                            mydb.ti();
                        }*/
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("recon", "error " + throwable);

                        Toast.makeText(getApplicationContext(), "Error:  " + statusCode + " Verify your Internet Connection is stable or working.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });

        //Log.d("recon", "results: " + values.toString());
    }

    private ArrayList<String> categorizer(String parent) {
        Log.d("recon", "parse: " + typeArray.toString());
        ArrayList<String> categories = new ArrayList<String>();
        if (!parent.equals("")) {
            categories.add("Images");
            //if (!parent.equals("General Location")) {
            categories.add("Add Sub-Location");
            //}
        } else {
            categories.add("Modify Site Constraints");
        }

        Log.d("recon", "categorizing " + parent);
        switch (parent) {
            case "Parking":
                //categories.add("Parking Lots");
                categories.set(1, "Add Parking Area");
                categories.add("Passenger Loading Zones");
                break;
            case "Primary Function Areas":
                if (typeArray.contains("Bank")) {
                    categories.add("Lobby");
                    categories.add("Offices");
                }
                if (typeArray.contains("Gas Station")) {
                    categories.add("Lobby");
                    categories.add("Fuel Pumps");
                }
                if (typeArray.contains("Hotel")) {
                    categories.add("Lobby");
                    categories.add("Accessible Guest Rooms");
                    categories.add("Non-Accessible Guest Rooms");
                }
                if (typeArray.contains("Restaurant")) {
                    categories.add("Lobby");
                    categories.add("Dining Areas");
                    categories.add("Bars");
                }
                break;
            case "Interior Path of Travel":
                categories.add("Path");
                categories.add("Telephones and Drinking Fountains");
                categories.add("Restrooms");

                if (typeArray.contains("Hotel")) {
                    categories.add("Laundry Room");
                    categories.add("Gym");
                    categories.add("Locker Rooms");
                }
                break;
            case "Interior Path of Travel|Restrooms" :
                categories.set(1, "Add Restroom");
                break;
            case "":
                /*self.sub = false
                self.elementCategories.removeAtIndex(0)
                self.elementCategories.removeAtIndex(0)*/
                categories.add("General Location");
                categories.add("Parking");
                categories.add("Exterior Path of Travel");
                categories.add("Egress");
                categories.add("Primary Function Areas");
                categories.add("Interior Path of Travel");
                break;
            default:
                if (category.size() == 2) {
                    if (category.get(0).matches("Parking") && !category.get(1).matches("Passenger Loading Zones")) {
                        categories.set(1, "Add Parking Space");
                    }
                }
        }

        return categories;
    }

    private void alertCheckLogin(final String typed) {
        final Dialog builder = new Dialog(this);

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.detail_login, null);
        builder.setContentView(view);

        final EditText inputUsername = (EditText) builder.findViewById(R.id.u);
        final EditText inputPassword = (EditText) builder.findViewById(R.id.p);
        builder.setTitle("Verify Login.");
        //input.setText(position + ": " + arrayOfElements.get(position).notes.toString() + arrayOfElements.get(position).picture.toString());
        inputUsername.setText(username);
        inputPassword.setText(password);

        // Set up the buttons
        Button positive = (Button) builder.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = inputUsername.getText().toString();
                password = inputPassword.getText().toString();

                builder.dismiss();
                if (typed.matches("login")) {
                    verifyLogin();
                } else {
                    query(state, typed);
                }
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

    //alert for upload menu
    private void alertDialogUpload() {
        builderUpload = new Dialog(List.this);
        builderUpload.setContentView(R.layout.upload_dialogue);

        builderUpload.setTitle("Upload Menu");
        progressText = (TextView) builderUpload.findViewById(R.id.progressText);
        progressText.setText("");
        progressUpload = (ProgressBar) builderUpload.findViewById(R.id.progressBar1);
        dialogCancel = (Button) builderUpload.findViewById(R.id.upload_cancel);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builderUpload.dismiss();
            }
        });

        dialogUpload = (Button) builderUpload.findViewById(R.id.upload_start);
        dialogUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
                v.setEnabled(false);
                dialogCancel.setEnabled(false);
                progressText.setText("Please wait...");
                //Toast.makeText(getApplicationContext(), "UPLOAD", Toast.LENGTH_LONG).show();
            }
        });

        builderUpload.show();
    }

    //alert for input of new sub-location
    private void alertDialogLocation(String location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (location.matches("Add Parking Area")) {
            builder.setMessage("Add Parking Lot, Parking Level, Parking Structure or an identifier for a cluser of parking spaces.");
        }
        builder.setTitle(location);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            String position = "";
                            String[] track = state.split("-");
                            //String parent = "";
                            String countOf = "";

                            if (!category.isEmpty()) {
                                position = TextUtils.join("|", category);
                                //parent = category.get(category.size() - 1);
                            }

                            //Log.d("recon", "category: " + position + ", location: " + input.getText().toString());
                            arrayOfElements = mydb.categoryExists(track[1], position, input.getText().toString());

                            if (!arrayOfElements.isEmpty()) {
                                Log.d("recon", arrayOfElements.toString());
                                Toast.makeText(getApplicationContext(), "Sub location already exists", Toast.LENGTH_LONG).show();
                            } else {
                                Log.d("recon", arrayOfElements.toString());
                                String sublocation = input.getText().toString();
                                //Toast.makeText(getApplicationContext(), sublocatoin, Toast.LENGTH_LONG).show();
                                //check for pipe
                                if (sublocation.indexOf("|") != -1 || sublocation.indexOf("\\") != -1 || sublocation.indexOf("/") != -1 || sublocation.indexOf("<") != -1 || sublocation.indexOf(">") != -1) {
                                    Toast.makeText(getApplicationContext(), "|, \\, /, > and < are an invalid characters", Toast.LENGTH_LONG).show();
                                } else if (sublocation.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Location must have a name", Toast.LENGTH_LONG).show();
                                } else {

                                    //insert into DB
                                    if (limiterType != "neither") {
                                        Integer countOfLocations = numberOf() + 1;
                                        countOf = limiterType + "#" + countOfLocations;
                                    }

                                    ArrayList<AbstractMap.SimpleEntry<String, String>> newInsert = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                    newInsert.add(new AbstractMap.SimpleEntry("location", sublocation));
                                    newInsert.add(new AbstractMap.SimpleEntry("picture", "locationed"));
                                    newInsert.add(new AbstractMap.SimpleEntry("notes", countOf));
                                    newInsert.add(new AbstractMap.SimpleEntry("category", position));
                                    newInsert.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                                    newInsert.add((new AbstractMap.SimpleEntry("user", username)));
                                    mydb.insertData("elements", newInsert);

                                    //open intent for new sublocation
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString("username", username);
                                    dataBundle.putString("password", password);
                                    dataBundle.putString("credit", credit);
                                    dataBundle.putString("state", state);
                                    dataBundle.putStringArrayList("type", typeArray);
                                    dataBundle.putString("tracker", tracker);
                                    dataBundle.putString("trackerType", trackerType);
                                    dataBundle.putString("actionable", actionable);
                                    category.add(sublocation);
                                    dataBundle.putStringArrayList("category", category);

                                    Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                                    intent.putExtras(dataBundle);
                                    startActivity(intent);
                                }
                            }

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String position = "";
                String[] track = state.split("-");
                //String parent = "";
                String countOf = "";

                if (!category.isEmpty()) {
                    position = TextUtils.join("|", category);
                    //parent = category.get(category.size() - 1);
                }

                //Log.d("recon", "category: " + position + ", location: " + input.getText().toString());
                arrayOfElements = mydb.categoryExists(track[1], position, input.getText().toString());

                if (!arrayOfElements.isEmpty()) {
                    Log.d("recon", arrayOfElements.toString());
                    Toast.makeText(getApplicationContext(), "Sub location already exists", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("recon", arrayOfElements.toString());
                    String sublocation = input.getText().toString();
                    //Toast.makeText(getApplicationContext(), sublocatoin, Toast.LENGTH_LONG).show();
                    //check for pipe
                    if (sublocation.indexOf("|") != -1 || sublocation.indexOf("\\") != -1 || sublocation.indexOf("/") != -1 || sublocation.indexOf("<") != -1 || sublocation.indexOf(">") != -1) {
                        Toast.makeText(getApplicationContext(), "|, \\, /, > and < are an invalid characters", Toast.LENGTH_LONG).show();
                    } else if (sublocation.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Location must have a name", Toast.LENGTH_LONG).show();
                    } else {

                        //insert into DB
                        if (limiterType != "neither") {
                            Integer countOfLocations = numberOf() + 1;
                            countOf = limiterType + "#" + countOfLocations;
                        }

                        ArrayList<AbstractMap.SimpleEntry<String, String>> newInsert = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        newInsert.add(new AbstractMap.SimpleEntry("location", sublocation));
                        newInsert.add(new AbstractMap.SimpleEntry("picture", "locationed"));
                        newInsert.add(new AbstractMap.SimpleEntry("notes", countOf));
                        newInsert.add(new AbstractMap.SimpleEntry("category", position));
                        newInsert.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                        newInsert.add(new AbstractMap.SimpleEntry("user", username));
                        mydb.insertData("elements", newInsert);

                        //open intent for new sublocation
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("username", username);
                        dataBundle.putString("password", password);
                        dataBundle.putString("credit", credit);
                        dataBundle.putString("state", state);
                        dataBundle.putStringArrayList("type", typeArray);
                        dataBundle.putString("tracker", tracker);
                        dataBundle.putString("trackerType", trackerType);
                        dataBundle.putString("actionable", actionable);
                        category.add(sublocation);
                        dataBundle.putStringArrayList("category", category);

                        Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        Log.d("recon", "back button pressed");

        Bundle dataBundle = new Bundle();
        if (state.contains("-")) {
            dataBundle.putString("tracker", state);
            String typeArrayNew =  TextUtils.join("|", typeArray);
            dataBundle.putString("trackerType", typeArrayNew);

            ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
            //arrayOfElements.add(new elements(issue.getString("location"), issue.getString("picture"), issue.getString("notes"), issue.getString("category"), 0));

            //String[] track = state.split("-");
            pairs.add(new AbstractMap.SimpleEntry("site", state));
            pairs.add(new AbstractMap.SimpleEntry("type", typeArrayNew));
            mydb.updateContact("login", 1, pairs);
        } else {
            dataBundle.putString("tracker", tracker);
            dataBundle.putString("trackerType", trackerType);
        }
        Intent intent = null;

        if (!category.isEmpty()) {
            Log.d("recon", "still in elements");
            category.remove(category.size() - 1);
            dataBundle.putString("actionable", actionable);
            dataBundle.putString("state", state);

            intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        } else if (state.equals("default")) {
            Log.d("recon", "going back to main: " + state);
            intent = new Intent(getApplicationContext(), com.equalsd.recon.MainActivity.class);
            startActivity(intent);
        } else {
            Log.d("recon", "selecting sites");
            dataBundle.putString("actionable", "site");
            dataBundle.putString("state", "default");

            intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
        }

        dataBundle.putString("username", username);
        dataBundle.putString("password", password);
        dataBundle.putString("credit", credit);
        dataBundle.putStringArrayList("type", typeArray);
        dataBundle.putStringArrayList("category", category);

        intent.putExtras(dataBundle);
        startActivity(intent);
    }

    private View.OnClickListener continuer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           Toast.makeText(getApplicationContext(), "CONTINUE " + tracker, Toast.LENGTH_LONG).show();
            continueSite(tracker, trackerType);
        }

    };

    private void continueSite(String continueSite, String continueSiteType) {

        Bundle dataBundle = new Bundle();
        dataBundle.putString("username", username);
        dataBundle.putString("password", password);
        dataBundle.putString("credit", credit);
        dataBundle.putString("tracker", continueSite);

        Intent intent = null;

        dataBundle.putString("state", continueSite);
        dataBundle.putStringArrayList("category", new ArrayList<String>());

        //loading site...
        Log.d("recon", "starting site " + continueSite);

        typeArray = new ArrayList<String>(asList(continueSiteType.split("\\|")));
        dataBundle.putStringArrayList("type", typeArray);
        dataBundle.putString("actionable", "loadDBelements");

        //save as continue
        ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        pairs.add(new AbstractMap.SimpleEntry("site", continueSite));
        pairs.add(new AbstractMap.SimpleEntry("type", continueSiteType));
        mydb.updateContact("login", 1, pairs);


        intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
        intent.putExtras(dataBundle);
        startActivity(intent);
    }

    /*private View.OnClickListener uploader = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialogUpload();
        }

    };*/

    private void upload() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String[] track = state.split("-");
        ArrayList<elements> arrayToUpload = mydb.getAll(track[1]);

        arrayOfPictures.clear();
        AsyncHttpClient client = new AsyncHttpClient();

        JSONArray jsonParams = new JSONArray();
        //walk arrayToUpload, insert into JSONOBJECT
        JSONArray ja = new JSONArray();
        ja.put(username);
        ja.put(password);
        ja.put("skip");
        ja.put(state);
        jsonParams.put(ja);

        java.util.Date date = new java.util.Date();
        Integer timeStamp = (int) (long) date.getTime();

        for (Integer i = 0; i < arrayToUpload.size(); i++) {
            if (!arrayToUpload.get(i).picture.equals("name")) {
                ja = new JSONArray();
                //location
                ja.put(arrayToUpload.get(i).location);
                //notes
                ja.put(arrayToUpload.get(i).notes);
                //switch
                if (arrayToUpload.get(i).picture.equals("locationed")) {
                    ja.put("location");
                    ja.put("blank");
                } else if (!arrayToUpload.get(i).picture.contains("content") || arrayToUpload.get(i).uniqueID < 0) {
                    ja.put("leave be");
                    ja.put(arrayToUpload.get(i).picture);
                    Log.d("recon", "leave be " + arrayToUpload.get(i).picture + " " + arrayToUpload.get(i).uniqueID.toString());

                } else {
                    ja.put("change");
                    Log.d("recon", "change " + arrayToUpload.get(i).picture + " " + arrayToUpload.get(i).uniqueID.toString() + " " + timeStamp.toString());
                    ja.put(timeStamp);
                    arrayOfPictures.add(new elements(arrayToUpload.get(i).uniqueID.toString(), arrayToUpload.get(i).picture, "picture_" + i, i.toString(), timeStamp));
                }
                //ID
                timeStamp++;
                //category
                ja.put(arrayToUpload.get(i).category.toString());
                jsonParams.put(ja);
            }
        }

        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
            Log.d("recon json", jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), URL_UPLOAD, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        // called when response HTTP status is "200 OK"
                        try {
                            String status = jsonObject.get("status").toString();
                            Log.d("recon upload reponse", status);
                            //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Log.d("recon", String.valueOf(e));
                            Log.d("recon", jsonObject.toString());
                        }

                        if (!arrayOfPictures.isEmpty()) {
                            i = arrayOfPictures.size();
                            progressText.setText("Uploading 1 of " + i);
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //Your code goes here
                                        uploadPicture(0);
                                    } catch (Exception e) {
                                        Log.e("test background", e.getMessage());
                                    }
                                }

                            });
                            thread.start();
                        } else {
                            progressText.setText("Upload successful");
                            progressUpload.setProgress(100);
                            Toast.makeText(getApplicationContext(), "Upload successful.", Toast.LENGTH_LONG).show();
                            dialogUpload.setEnabled(true);
                            dialogCancel.setEnabled(true);
                            dialogCancel.setText("Close");
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("recon", "error " + throwable);

                        Toast.makeText(getApplicationContext(), "Error:  " + statusCode + " Verify your Internet Connection is stable or working.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });
        //Toast.makeText(getApplicationContext(), "upload", Toast.LENGTH_LONG).show();
    }

    private void uploadPicture(final Integer position) {
        if (position < i) {
            Integer progress = position + 1;
            progressText.setText("Uploading " +  progress + " of " + i);
            Uri uri = Uri.parse(arrayOfPictures.get(position).picture.toString());
            String localFile = getRealPathFromUri(getApplicationContext(), uri);
            final File fileToUpload = new File(localFile);
            Log.d("recon", fileToUpload.toString() + ", key:" + arrayOfPictures.get(position).uniqueID.toString());
            Ion.with(getApplicationContext())
                    .load("http://ada-veracity.com/api/put-picture.php")
                    .progressBar(progressUpload)
                    .setMultipartParameter("key", arrayOfPictures.get(position).uniqueID.toString())
                    .setMultipartParameter("title", arrayOfPictures.get(position).notes.toString())
                    .setMultipartParameter("orientation", "webset")
                    .setMultipartParameter("site", state)
                    .setMultipartFile("file", "image/jpeg", fileToUpload)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (e != null) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("recon upload error: ", e.getMessage());
                                return;
                            }
                            //Toast.makeText(getApplicationContext(), "File upload complete: " + result, Toast.LENGTH_LONG).show();
                            Log.d("recon upload : ", result);
                            //String[] split = result.split("=");

                            //update new name into database...
                            ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                            pairs.add(new AbstractMap.SimpleEntry("picture", result));
                            mydb.updateContact("elements", Integer.parseInt(arrayOfPictures.get(position).location), pairs);

                            Integer newPosition = position + 1;
                            Float progressDouble = (float) newPosition / i * 100;
                            Log.d("recon progress", newPosition + " of " + i + " for progress: " + String.valueOf(progressDouble));
                            Integer progressInt = Math.round(progressDouble);
                            progressUpload.setProgress(progressInt);
                            uploadPicture(newPosition);
                            dialogCancel.setText("Close");
                        }
                    });
        } else {
            progressText.setText("Upload successful");
            progressUpload.setProgress(100);
            Toast.makeText(getApplicationContext(), "Upload successful.", Toast.LENGTH_LONG).show();
            dialogUpload.setEnabled(true);
            dialogCancel.setEnabled(true);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getLimiter() {
        if (category.size() == 2) {
            if (category.get(0).matches("Parking") && !category.get(1).matches("Passenger Loading Zones")) {
                return "parking";
            } else if (category.get(0).matches("Interior Path of Travel") && category.get(1).matches("Restrooms")) {
                return "restroom";
            } else {
                return "neither";
            }
        }

        return "neither";
    }
}
