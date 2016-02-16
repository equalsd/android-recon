package com.equalsd.recon;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by generic on 7/14/15.
 */
public class NewActivity extends AppCompatActivity {

    Switch switch_bank;
    Switch switch_gas;
    Switch switch_hotel;
    Switch switch_mall;
    Switch switch_restaurant;
    Switch switch_office;
    Switch switch_health;

    Boolean bool_bank;
    Boolean bool_gas;
    Boolean bool_hotel;
    Boolean bool_restaurant;
    Boolean bool_mall;
    Boolean bool_office;
    Boolean bool_health;

    EditText site_name;
    EditText site_address;
    EditText site_date;

    String username;
    String password;
    String actionable;
    String tracker;
    ArrayList<String> type = new ArrayList<String>();
    ArrayList<String> tokens = new ArrayList<String>();
    ArrayList<String> tokenNames = new ArrayList<String>();
    ArrayList<String> tokenPrices = new ArrayList<String>();
    ArrayList<String> tokenOriginals = new ArrayList<String>();

    String reportType = "";
    static final String URL_GET = "http://ada-veracity.com/api/get-site-type-json.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        switch_bank = (Switch) findViewById(R.id.switch_bank);
        switch_gas = (Switch) findViewById(R.id.switch_gas);
        switch_hotel = (Switch) findViewById(R.id.switch_hotel);
        switch_mall = (Switch) findViewById(R.id.switch_mall);
        switch_restaurant = (Switch) findViewById(R.id.switch_restaurant);
        switch_office = (Switch) findViewById(R.id.switch_office);
        switch_health = (Switch) findViewById(R.id.switch_health);

        site_name = (EditText) findViewById(R.id.site_name);
        site_address = (EditText) findViewById(R.id.site_address);
        site_date = (EditText) findViewById(R.id.site_date);

        bool_bank = false;
        bool_restaurant = false;
        bool_gas = false;
        bool_hotel = false;
        bool_mall = false;
        bool_office = false;
        bool_health = false;

        switch_bank.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_bank = isChecked;
            }
        });
        switch_gas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_gas = isChecked;
            }
        });
        switch_hotel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_hotel = isChecked;
            }
        });
        switch_mall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_mall = isChecked;
            }
        });
        switch_restaurant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_restaurant = isChecked;
            }
        });

        switch_office.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_office = isChecked;
            }
        });

        switch_health.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bool_health = isChecked;
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d("recon start New:", extras.toString());

            username = extras.getString("username");
            password = extras.getString("password");
            actionable = extras.getString("actionable");
        }

        if (actionable.matches("upgrade")) {
            tracker = extras.getString("tracker");
            //find site on server & set conditions
            findTokens();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0042aa")));
            //actionBar.hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //add MenuItem(s) to ActionBar using Java code
        MenuItem menuItem_Purchase = menu.add(0, 1, Menu.NONE, "Next");
        MenuItemCompat.setShowAsAction(menuItem_Purchase,
                MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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
        if (item.getItemId() == 1) {
            Submit();
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        type.clear();
        tokenNames.clear();
        tokenPrices.clear();
        tokens.clear();
    }


    //public void Submit(View view) {
    public void Submit() {
        Log.d("recon", "pressed");
        String text_name = site_name.getText().toString();
        String text_address = site_address.getText().toString();
        String text_date = site_date.getText().toString();

        if (text_name.matches("") || text_address.matches("") || text_date.matches("")) {
            Toast.makeText(getApplicationContext(), "Name, address and inspection date fields must be filled in.", Toast.LENGTH_LONG).show();
        } else {
            if (bool_mall) {
                type.add("Strip Mall");
                tokenNames.add("Mall");
                if (!actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }

            if (bool_bank) {
                type.add("Bank");
                tokenNames.add("Bank");
                if (!tokens.contains("com.adaveracity.casp.undefined_tier_1") && !actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }
            if (bool_gas) {
                type.add("Gas Station");
                tokenNames.add("Gas Station");
                if (!actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }
            if (bool_hotel) {
                type.add("Hotel");
                tokenNames.add("Hotel");
                if (!tokens.contains("com.adaveracity.casp.undefined_tier_1") && !actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }
            if (bool_health) {
                type.add("Health");
                tokenNames.add("Health Care Facility");
                if (!tokens.contains("com.adaveracity.casp.undefined_tier_1") && !actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }
            if (bool_restaurant) {
                type.add("Restaurant");
                tokenNames.add("Restaurant");
                if (!tokens.contains("com.adaveracity.casp.undefined_tier_1") && !actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }
            /*if (bool_pool) {
                tokens.add("com.adaveracity.casp.pool");
                tokenNames.add("Pool");
                tokenPrices.add("30.00");
            }
            if (bool_shower) {
                tokens.add("com.adaveracity.shower_doublet");
                tokenNames.add("Showers");
                tokenPrices.add("20.00");
            }
            if (bool_office) {type.add("Office");}*/

            if (bool_office) {
                type.add("Office");
                tokenNames.add("Retail/Office");
                if (!tokens.contains("com.adaveracity.casp.undefined_tier_1") && !actionable.matches("update") && !tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("0");
                } else {
                    tokenPrices.add("0");
                }
                tokens.add("com.adaveracity.casp.undefined_tier_1");
            }

            if (type.isEmpty()) {
                tokenNames.add("Undefined Site");
                tokens.add("com.adaveracity.casp.undefined_tier_1");
                if (!actionable.matches("update") && tokenOriginals.contains("com.adaveracity.casp.undefined_tier_1")) {
                    tokenPrices.add("25");
                } else {
                    tokenPrices.add("0");
                }
            }

            ArrayList<String> verifiedType = type;
            reportType = TextUtils.join("|", type);
            type.add("Parking");
            type.add("Restroom");


            Log.d("recon", reportType + text_address + text_date + text_name);
            //putNew(reportType, text_name, text_address, text_date);
            Bundle dataBundle = new Bundle();
            dataBundle.putString("username", username);
            dataBundle.putString("password", password);
            dataBundle.putString("address", text_address);
            dataBundle.putString("date", text_name);
            dataBundle.putString("name", text_date);
            dataBundle.putString("trackerType", reportType);
            dataBundle.putString("tracker", tracker);
            dataBundle.putString("actionable", actionable);
            dataBundle.putStringArrayList("tokenOriginals", tokenOriginals);
            dataBundle.putStringArrayList("verifiedType", verifiedType);
            dataBundle.putStringArrayList("type", type);
            dataBundle.putStringArrayList("tokenNames", tokenNames);
            dataBundle.putStringArrayList("tokenPrices", tokenPrices);
            dataBundle.putStringArrayList("tokens", tokens);

            Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.TypeActivity.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
        }
    }

    private void findTokens() {
        AsyncHttpClient client = new AsyncHttpClient();
        final String[] site = new String[5];

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
            jsonParams.put("site", tracker);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), URL_GET, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // called when response HTTP status is "200 OK"
                        Log.d("recon", "site found");
                        Log.d("recon", jsonObject.toString());
                        try {
                            site[0] = jsonObject.getString("name");
                            site[1] = jsonObject.getString("address");
                            site[2] = jsonObject.getString("date");
                            site[4] = jsonObject.getString("tokens");
                            site[3] = jsonObject.getString("type");
                            site_address.setText(site[1]);
                            site_date.setText(site[2]);
                            site_name.setText(site[0]);
                            if (site[3].contains("|")) {
                                String[] split = site[3].split("\\|");
                                for (String item : split) {
                                    switch (item) {
                                        case "Office" :
                                            switch_office.setChecked(true);
                                            break;
                                        case "Bank" :
                                            switch_bank.setChecked(true);
                                            break;
                                        case "Strip Mall" :
                                        case "Mall" :
                                            switch_mall.setChecked(true);
                                            break;
                                        case "Hotel" :
                                            switch_hotel.setChecked(true);
                                            Log.d("recon", "hotel ok");
                                            break;
                                        case "Gas Station" :
                                            switch_gas.setChecked(true);
                                            break;
                                        case "Restaurant" :
                                            switch_restaurant.setChecked(true);
                                            break;
                                        default :
                                    }
                                }
                            } else {
                                switch (site[3]) {
                                    case "Office" :
                                        switch_office.setChecked(true);
                                        break;
                                    case "Bank" :
                                        switch_bank.setChecked(true);
                                        break;
                                    case "Strip Mall" :
                                    case "Mall" :
                                        switch_mall.setChecked(true);
                                        break;
                                    case "Hotel" :
                                        switch_hotel.setChecked(true);
                                        break;
                                    case "Gas Station" :
                                        switch_gas.setChecked(true);
                                        break;
                                    case "Restaurant" :
                                        switch_restaurant.setChecked(true);
                                        break;
                                    default :
                                }
                            }

                            if (site[4].contains("|")) {
                                String[] tokenSet = site[4].split("\\|");
                                for (String item : tokenSet) {
                                    tokenOriginals.add(item);
                                }
                            } else {
                                tokenOriginals.add(site[4]);
                            }
                            //Log.d("recon site: ", site[0].toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /*//save in DB for login, continue
                        ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        pairs.add(new AbstractMap.SimpleEntry("site", site[0].toString()));
                        pairs.add(new AbstractMap.SimpleEntry("type", trackerType));
                        mydb.updateContact("login", 1, pairs);
                        Log.d("recon", "saved in login okay");

                        //save in DB for saved sites
                        pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        //arrayOfElements.add(new elements(issue.getString("location"), issue.getString("picture"), issue.getString("notes"), issue.getString("category"), 0));

                        pairs.add(new AbstractMap.SimpleEntry("location", address));
                        pairs.add(new AbstractMap.SimpleEntry("picture", "name"));
                        pairs.add(new AbstractMap.SimpleEntry("notes", name));
                        pairs.add(new AbstractMap.SimpleEntry("tracking", site[0].toString()));
                        pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                        mydb.insertData("elements", pairs);
                        Log.d("recon", "saved in db for saved sites okay");

                        //Toast.makeText(getApplicationContext(), site[0].toString(), Toast.LENGTH_LONG).show();
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("username", username);
                        dataBundle.putString("password", password);
                        dataBundle.putString("tracking", site[0].toString());
                        dataBundle.putString("tracker", site[0].toString());
                        dataBundle.putString("trackerType", trackerType);
                        ArrayList<String> newCategory = new ArrayList<String>();
                        dataBundle.putString("state", site[0].toString());
                        dataBundle.putStringArrayList("type", type);
                        dataBundle.putString("actionable", "newSite");
                        dataBundle.putStringArrayList("category", newCategory);

                        Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);*/

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("recon", "error " + statusCode + " " + throwable);

                        Toast.makeText(getApplicationContext(), "Error:  " + statusCode + " Verify your Internet Connection is stable or working.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });

        //Log.d("recon", "results: " + values.toString());
    }
}