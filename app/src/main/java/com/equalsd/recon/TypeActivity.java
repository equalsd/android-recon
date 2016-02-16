package com.equalsd.recon;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by generic on 8/11/15.
 */
public class TypeActivity extends AppCompatActivity {

    String username;
    String password;
    String trackerType;
    String address;
    String date;
    String name;
    String current;
    String actionable;
    String tracker;

    ArrayList<String> tokenNames = new ArrayList<String>();
    ArrayList<String> tokenPrices = new ArrayList<String>();
    ArrayList<String> tokens = new ArrayList<String>();
    ArrayList<String> type;
    ArrayList<String> verifiedType;
    ArrayList<sites> tokenDisplay = new ArrayList<sites>();
    ArrayList<String> tokenOriginals = new ArrayList<String>();
    ArrayList<String> typeDepth = new ArrayList<>();
    ArrayList<String> depthDisplay = new ArrayList<>();

    ArrayAdapter adapter;
    ListView listview;
    TextView tv;


    final static String URL_Token = "http://ada-veracity.com/api/get-token-description.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
        listview = (ListView) findViewById(R.id.main_listview);

        tv = (TextView) findViewById(R.id.test);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Log.d("recon start New:", extras.toString());

            username = extras.getString("username");
            password = extras.getString("password");
            trackerType = extras.getString("trackerType");
            tokenNames = extras.getStringArrayList("tokenNames");
            tokenPrices = extras.getStringArrayList("tokenPrices");
            type = extras.getStringArrayList("type");
            verifiedType = extras.getStringArrayList("verifiedType");
            tokens = extras.getStringArrayList("tokens");
            address = extras.getString("address");
            name = extras.getString("name");
            date = extras.getString("date");
            actionable = extras.getString("actionable");

            /*            Bundle dataBundle = new Bundle();
            dataBundle.putString("username", username);
            dataBundle.putString("password", password);
            dataBundle.putString("address", text_address);
            dataBundle.putString("date", text_name);
            dataBundle.putString("name", text_date);
            //dataBundle.putString("state", site[0].toString());
            dataBundle.putStringArrayList("type", type);
            dataBundle.putStringArrayList("category", newCategory);
            dataBundle.putStringArrayList("tokens", tokens);*/
        }

        if (actionable.matches("upgrade")) {
            tokenOriginals = extras.getStringArrayList("tokenOriginals");
            tracker = extras.getString("tracker");
            //Log.d("recon orig tokens", tokenOriginals.toString());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0042aa")));
            //actionBar.hide();
        }

        Log.d("recon", type.toString());
        setUp();
    }

    private void setUp() {
        tokenDisplay.clear();

        ListIterator listIterator = verifiedType.listIterator();
        if (listIterator.hasNext()) {
            current = listIterator.next().toString();
            listIterator.remove();
            Log.d("recon", current + " " + verifiedType.toString());

            tv.setText(current + ": Select Appropriate Tier");


            if (current.equals("Parking") || current.equals("Restroom")) {
                getType();
            } else {
                setUp();
            }
        } else {
            //go to checkout; purchaseactivity.
            //checkoutTokens();

            Bundle dataBundle = new Bundle();
            dataBundle.putString("username", username);
            dataBundle.putString("password", password);
            dataBundle.putString("trackerType", trackerType);
            dataBundle.putStringArrayList("verifiedType", verifiedType);
            dataBundle.putStringArrayList("tokenNames", tokenNames);
            dataBundle.putStringArrayList("tokenPrices", tokenPrices);
            dataBundle.putStringArrayList("tokens", tokens);
            dataBundle.putStringArrayList("typeDepth", typeDepth);
            if (actionable.matches("upgrade")) {
                dataBundle.putStringArrayList("tokenOriginals", tokenOriginals);
                dataBundle.putString("tracker", tracker);
            }
            dataBundle.putString("actionable", actionable);
            dataBundle.putStringArrayList("type", type);
            dataBundle.putString("address", address);
            dataBundle.putString("date", name);
            dataBundle.putString("name", date);

            Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.PurchaseActivity.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
            finish();
        }

        adapter = new siteAdapter(this, tokenDisplay);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("recon", tokenDisplay.get(position).siteType);
                tokens.add(tokenDisplay.get(position).tracking);
                tokenNames.add(tokenDisplay.get(position).location);
                tokenPrices.add(tokenDisplay.get(position).siteType);
                typeDepth.add(tokenDisplay.get(position).depth);
                setUp();
            }
        });
    }

    private void getType() {
        AsyncHttpClient client = new AsyncHttpClient();
        final String[] status = new String[1];

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
            jsonParams.put("type", current);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), URL_Token, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // called when response HTTP status is "200 OK"
                        Log.d("recon", jsonObject.toString());
                        try {
                            status[0] = jsonObject.getString("status");
                            Log.d("recon site: ", status[0].toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (status[0].equals("Success")) {
                            JSONObject jObj = null;
                            try {
                                jObj = jsonObject.getJSONObject("result");
                                /*Log.d("recon", jObj.toString());
                                Iterator<String> keys = jObj.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    JSONObject innerObj = jObj.getJSONObject(key);
                                    Log.d("recon result", innerObj.getString("ID").toString());
                                    tokenDisplay.add(new sites(innerObj.getString("Name").toString(), innerObj.getString("Description").toString(), innerObj.getString("ID").toString(), ""));
                                }*/

                                /*if (current.equals("Parking") && !actionable.matches("upgrade")) {
                                    tokenDisplay.add(new sites("Parking Tier 0", "No parking present", "com.adaveracity.casp.parking_tier_0", "0"));
                                }

                                if (current.equals("Restroom") && !actionable.matches("upgrade")) {
                                    tokenDisplay.add(new sites("Restroom Tier 0", "No restrooms to inspect", "com.adaveracity.casp.restroom_tier_0", "0"));
                                }*/

                                for (Integer i = 0; i < jObj.length(); i++) {
                                    String index = "index" + i.toString();
                                    JSONObject innerObj = jObj.getJSONObject(index);
                                    //Log.d("recon", innerObj.getString("ID").toString() + " " + jsonObject.length());
                                    if (actionable.matches("upgrade")) {
                                        try {
                                            String ID = innerObj.getString("ID");
                                            Integer price = tokenCheck(ID);
                                            if (price == -1) {
                                                tokenDisplay.add(new sites(innerObj.getString("Name"), innerObj.getString("Description"), innerObj.getString("ID"), innerObj.getString("Price"), innerObj.getString("Depth")));
                                            } else {
                                                if (price != -2) {
                                                    tokenDisplay.add(new sites(innerObj.getString("Name"), innerObj.getString("Description"), innerObj.getString("ID"), price.toString(), innerObj.getString("Depth")));
                                                }
                                            }
                                            /*String[] newLevels = ID.split("\\.");
                                            String[] newTiers = newLevels[3].split("_");
                                            for (String item : tokenOriginals) {
                                                String[] itemLevels = item.split("\\.");
                                                if (itemLevels[3].contains(newTiers[0])) {
                                                    String[] itemTiers = itemLevels[3].split("_");
                                                    if (Integer.parseInt(itemTiers[2]) <= Integer.parseInt(newTiers[2])) {
                                                        Integer price = (Integer.parseInt(itemTiers[2]) - Integer.parseInt(newTiers[2])) * 25;
                                                        tokenDisplay.add(new sites(innerObj.getString("Name"), innerObj.getString("Description"), innerObj.getString("ID"), price.toString()));
                                                    }
                                                }
                                            }*/
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        tokenDisplay.add(new sites(innerObj.getString("Name"), innerObj.getString("Description"), innerObj.getString("ID"), innerObj.getString("Price"), innerObj.getString("Depth")));
                                    }
                                }

                                if (tokenDisplay.isEmpty()) {
                                    setUp();
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //Toast.makeText(getApplicationContext(), status[0].toString(), Toast.LENGTH_LONG).show();
                        }

                        //Toast.makeText(getApplicationContext(), site[0].toString(), Toast.LENGTH_LONG).show();
                        /*Bundle dataBundle = new Bundle();
                        dataBundle.putString("username", username);
                        dataBundle.putString("password", password);
                        dataBundle.putString("trackerType", trackerType);
                        dataBundle.putStringArrayList("tokens", tokens);
                        dataBundle.putString("actionable", "newSite");
                        dataBundle.putString("address", address);
                        dataBundle.putString("date", name);
                        dataBundle.putString("name", date);

                        Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.TypeActivity.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);*/

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

    private Integer tokenCheck(String webToken) {
        String[] newLevels = webToken.split("\\.");
        String[] newTiers = newLevels[3].split("_");
        for (String item : tokenOriginals) {
            String[] itemLevels = item.split("\\.");
            if (itemLevels[3].contains(newTiers[0])) {
                String[] itemTiers = itemLevels[3].split("_");
                if (Integer.parseInt(itemTiers[2]) <= Integer.parseInt(newTiers[2])) {
                    Integer price = (Integer.parseInt(newTiers[2]) - Integer.parseInt(itemTiers[2])) * 25;
                    if (price < 0) {price = 0;}

                    return price;

                } else {
                    return -2;
                }
            }
        }

        return -1;
    }

}
