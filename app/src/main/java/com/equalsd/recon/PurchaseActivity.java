package com.equalsd.recon;

import android.content.Intent;
import android.database.Cursor;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PurchaseActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;

    Boolean purchased;

    String username;
    String password;
    String trackerType;
    String address;
    String date;
    String name;
    String actionable;
    String tracker;
    Integer sum = 0;

    ArrayList<String> tokenNames;
    ArrayList<String> tokens = new ArrayList<String>();
    ArrayList<String> tokenPrices = new ArrayList<String>();
    ArrayList<String> type;
    ArrayList<String> tokenOriginals = new ArrayList<String>();
    ArrayList<String> typeDepth;
    String token;
    String tokenSet;
    ArrayList<sites> tokenDisplay = new ArrayList<sites>();

    ArrayAdapter adapter;
    ListView listview;
    TextView tv;
    //Button details;

    private Menu menu;
    final static String URL_New = "http://ada-veracity.com/api/new-site-json.php";
    final static String URL_Update = "http://ada-veracity.com/api/update-site-json.php";
    final static String split = "QuPx3noFFQXsmInJKr1Jmu8WwRJluLnfmyxjr59g7/hq2+yLBx4Wvna4AzEbvPqtPTHcSlwLkb3lKYuD7JA4IfIQiT8IcEDU+wYfDXHWYwNwx8noUItaSF0EvsOBf2tAi0t7X2ofjq+FGZimFgKpY45BiAHYiosmJUwIDAQAB";
    final static String front = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoB7OAGjig6uJglbW9XCeWYjje5G2ZDEM7gmsW6hCRy+z/fA2j9cMkymmqzbCrAfOXqr35DPuZhssA";

    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bp = new BillingProcessor(this, front + "2G7IdeZmOpXZ2vzCVZxhZDynvAPyBegHAjQB+DQfXL7B03SsiFusd56fEICnd70zLcw7cVMP0Odd/2ovTZkGnSibGhyWqV4x/uW9PH" + split, this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Log.d("recon start New:", extras.toString());
            actionable = extras.getString("actionable");
            username = extras.getString("username");
            password = extras.getString("password");

            if (!actionable.matches("subscribe")) {
                trackerType = extras.getString("trackerType");
                tokenNames = extras.getStringArrayList("tokenNames");
                tokenPrices = extras.getStringArrayList("tokenPrices");
                tokens = extras.getStringArrayList("tokens");
                type = extras.getStringArrayList("type");
                address = extras.getString("address");
                name = extras.getString("name");
                date = extras.getString("date");
                typeDepth = extras.getStringArrayList("typeDepth");
            }
        }

        if (actionable.matches("upgrade")) {
            tokenOriginals = extras.getStringArrayList("tokenOriginals");
            tracker = extras.getString("tracker");
            Log.d("recon tracker check", tracker.toString());
        }

        mydb = new DBHelper(this);

        //details.performClick();
        if (!actionable.matches("subscribe")) {
            setContentView(R.layout.activity_purchase);

            listview = (ListView) findViewById(R.id.main_listview);
            tv = (TextView) findViewById(R.id.test);
            //details = (Button) findViewById(R.id.purchase);

            Log.d("recon type check", type.toString());
            checkoutTokens();

            adapter = new siteAdapter(this, tokenDisplay);
            listview.setAdapter(adapter);
        } else {
            Log.d("recon", "check subscription status, display message");
            setContentView(R.layout.activity_subscribe);
            tv = (TextView) findViewById(R.id.status);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0042aa")));
            //actionBar.hide();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //checkoutTokens();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //add MenuItem(s) to ActionBar using Java code
        String title = "";
        if (sum < 1) {
            title = "Update Site";
        } else {
            title = "Purchase";
        }
        MenuItem menuItem_Purchase = menu.add(0, 1, Menu.NONE, title);
        MenuItemCompat.setShowAsAction(menuItem_Purchase,
                MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                //Log.d("recon", savedSite.toString());
        return true;
    }

    /*private void setOptionTitle(int id, String title)
    {
        MenuItem item = menu.findItem(id);
        item.setTitle(title);
    }*/

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
            Buy();
        }

        return false;
    }

    public void checkoutTokens() {

        Log.d("Recon", tokenNames.toString());
        for(Integer i = 0; i < tokenNames.size(); i++) {
            tokenDisplay.add(new sites(tokenNames.get(i), "", "", "", ""));
            sum = sum + Integer.parseInt(tokenPrices.get(i));
        }

        if (sum <= 50 && !actionable.matches("upgrade")) {
            sum = 49;
            token = "com.adaveracity.casp.report_50";
        } else if (sum == 105) {
            sum = 109;
            token = "com.adaveracity.casp.report_110";
        } else if (sum == 135) {
            sum = 139;
            token = "com.adaveracity.casp.report_160";
        } else if (sum == 165) {
            sum = 169;
            token = "com.adaveracity.casp.report_170";
        } else if (sum > 199) {
            sum = 199;
            token = "com.adaveracity.casp.report_200";
        } else {
            token = "com.adaveracity.casp.report_" + sum.toString();
            sum = sum - 1;
        }


        if (sum < 1) {
            sum = 0;
            tv.setText("Total: $" + sum.toString() + " No upgrade selected.");
            //details.setText("Update site");
            //setOptionTitle(1, "Update site");
        } else {
            tv.setText("Total: $" + sum.toString() + ".99");
        }

        if (actionable.matches("upgrade")) {
            tokenSet = reconcileTokens(tokens, tokenOriginals);
        } else {
            tokenSet = TextUtils.join("|", tokens);
        }

        //adapter.notifyDataSetChanged();
    }

    // IBillingHandler implementation

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        Log.d("recon", "ready to purchase");
        if (actionable.matches("subscribe")) {
            //TransactionDetails purchasedItem = bp.getSubscriptionTransactionDetails("com.adaveracity.casp.sub_50");
            SkuDetails purchasedItem = bp.getSubscriptionListingDetails("com.adaveracity.casp.sub_50");
            Log.d("recon", purchasedItem.toString());
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        Log.d("recon", productId + " " + details.toString());
        Log.d("recon", "this has been purchased");
        bp.consumePurchase(token);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         */
        //Log.d("recon", "error: " + error.toString());
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    //public void Buy(View v) {
    public void Buy() {
        if (sum != 0) {
            //purchased = bp.purchase(this, token);
            Log.d("recon", username);
            //if (username.matches("testmin")) {
            //   putNew();
            //} else {
                purchased = bp.purchase(this, token);
            //}
        } else {
            updateSite();
        }
        //updateSite();
    }

    public void proceed(View v) {
        bp.subscribe(this, "com.adaveracity.casp.sub_50");
    }

    public void Details(View v) {
        /*ArrayList<String> preparedTokens = new ArrayList<String>();
        for(String item : tokens) {
            if (item.equals("com.adaveracity.casp.parking_tier_0")) {
                tokenDisplay.add(new sites("Parking Tier 0", "$0.00", "", ""));
            } else if (item.equals("com.adaveracity.casp.restroom_tier_0")) {
                tokenDisplay.add(new sites("Restroom Tier 0", "$0.00", "", ""));
            } else {
                preparedTokens.add(item);
            }
        }
        java.util.List<SkuDetails> listed = bp.getPurchaseListingDetails(preparedTokens);
        //sku = bp.getPurchaseListingDetails("com.adaveracity.casp.parking_tier_1");

        Log.d("recon", preparedTokens.toString() + " " + tokens.toString());
        for(SkuDetails tempSku : listed) {
            Log.d("recon", tempSku.priceText + " " + tempSku.description);
            tokenDisplay.add(new sites(tempSku.title, tempSku.priceText, "", ""));
            sum = sum + tempSku.priceValue;
        }

        adapter.notifyDataSetChanged();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        //Log.d("recon", data.toString());
        Log.d("recon", "" + resultCode);
        if (resultCode == -1) { //-1 purchased, 0 cancelled
            Log.d("recon", "product purchased");
            //bp.consumePurchase(token);
            if (actionable.matches("subscribe")) {
                //subscription
            } else {
                //purchase token
                if (actionable.matches("upgrade")) {
                    updateSite();
                } else {
                    putNew();
                }
            }
        } else {
            Log.d("recon", "product cancelled");
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }

    private void putNew() {
        AsyncHttpClient client = new AsyncHttpClient();
        final String[] site = new String[1];

        Integer unixStamp = (int) (System.currentTimeMillis() / 1000L);

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
            jsonParams.put("name", name);
            jsonParams.put("description", address);
            jsonParams.put("date", date);
            jsonParams.put("type", trackerType);
            jsonParams.put("tokenSet", tokenSet);
            jsonParams.put("unixStamp", unixStamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), URL_New, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // called when response HTTP status is "200 OK"
                        Log.d("recon", jsonObject.toString());
                        try {
                            String loadSite = "";
                            String[] track;

                            site[0] = jsonObject.getString("site");
                            Log.d("recon site: ", site[0].toString());

                            if (!site[0].matches("stop")) {
                                track = site[0].split("-");
                                //save in DB for login, continue
                                /*ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
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
                                pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                                pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                                mydb.insertData("elements", pairs);
                                Log.d("recon", "saved in db for saved sites okay");

                                pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                ??pairs.add(new AbstractMap.SimpleEntry("location", address));
                                pairs.add(new AbstractMap.SimpleEntry("picture", "parking"));
                                ??pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                                ??pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                                pairs.add(new AbstractMap.SimpleEntry("notes", typeDepth.get(0)));
                                mydb.insertData("elements", pairs);

                                pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                ??pairs.add(new AbstractMap.SimpleEntry("location", address));
                                pairs.add(new AbstractMap.SimpleEntry("picture", "restroom"));
                                ??pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                                ??pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                                pairs.add(new AbstractMap.SimpleEntry("notes", typeDepth.get(1)));
                                mydb.insertData("elements", pairs);*/
                                loadSite = site[0].toString();
                            } else {
                                loadSite = tracker;
                            }

                            //Toast.makeText(getApplicationContext(), site[0].toString(), Toast.LENGTH_LONG).show();
                            Bundle dataBundle = new Bundle();
                            dataBundle.putString("username", username);
                            dataBundle.putString("password", password);
                            dataBundle.putString("tracking", loadSite);
                            dataBundle.putString("tracker", loadSite);
                            dataBundle.putString("trackerType", trackerType);
                            ArrayList<String> newCategory = new ArrayList<String>();
                            dataBundle.putString("state", loadSite);
                            dataBundle.putStringArrayList("type", type);
                            dataBundle.putString("actionable", "loadWebelements");
                            dataBundle.putStringArrayList("category", newCategory);

                            Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                            intent.putExtras(dataBundle);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error, check connectivity", Toast.LENGTH_LONG).show();
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

        //Log.d("recon", "results: " + values.toString());
    }

    private void updateSite() {
        AsyncHttpClient client = new AsyncHttpClient();
        final String[] site = new String[1];

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("username", username);
            jsonParams.put("password", password);
            jsonParams.put("name", name);
            jsonParams.put("description", address);
            jsonParams.put("date", date);
            jsonParams.put("type", trackerType);
            jsonParams.put("site", tracker);
            jsonParams.put("tokenSet", tokenSet);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString(), null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(getApplicationContext(), URL_Update, entity, "application/json",
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // called when response HTTP status is "200 OK"
                        Log.d("recon", jsonObject.toString());
                        try {
                            site[0] = jsonObject.getString("site");
                            Log.d("recon site: ", site[0].toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //save in DB for login, continue
                        ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        pairs.add(new AbstractMap.SimpleEntry("site", tracker));
                        pairs.add(new AbstractMap.SimpleEntry("type", trackerType));
                        mydb.updateContact("login", 1, pairs);
                        Log.d("recon", "saved in login okay");

                        //save in DB for saved sites
                        String track[] = tracker.split("-");
                        Cursor res = mydb.getNames(track[1], username);
                        String id = "non";
                        if (res.getCount() > 0) {
                            res.moveToFirst();
                            Log.d("recon", "in names...");
                            while (!res.isAfterLast()) {
                                //Log.d("recon continue this: ", res.getString(res.getColumnIndex("notes")));
                                Log.d("recon tracking: ", res.getString(res.getColumnIndex("tracking")));
                                if (res.getString(res.getColumnIndex("tracking")).matches(track[1])) {
                                    id = res.getString(res.getColumnIndex("id"));
                                    Log.d("recon id: ", id);
                                }
                                res.moveToNext();
                            }
                        }

                        if (id.matches("non")) {
                            /*pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

                            //arrayOfElements.add(new elements(issue.getString("location"), issue.getString("picture"), issue.getString("notes"), issue.getString("category"), 0));

                            pairs.add(new AbstractMap.SimpleEntry("location", address));
                            pairs.add(new AbstractMap.SimpleEntry("picture", "name"));
                            pairs.add(new AbstractMap.SimpleEntry("notes", name));
                            pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                            pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                            mydb.insertData("elements", pairs);
                            Log.d("recon", "saved in db for saved sites okay");*/
                            Log.d("recon", "error");
                        } else {
                            pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

                            pairs.add(new AbstractMap.SimpleEntry("location", address));
                            pairs.add(new AbstractMap.SimpleEntry("notes", name));
                            pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                            pairs.add(new AbstractMap.SimpleEntry("category", trackerType));
                            mydb.updateContact("elements", Integer.parseInt(id), pairs);
                            Log.d("recon", "updated db for saved sites okay");
                        }

                        pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        pairs.add(new AbstractMap.SimpleEntry("notes", typeDepth.get(0)));
                        mydb.updateLimit("parking", pairs);

                        pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        pairs.add(new AbstractMap.SimpleEntry("notes", typeDepth.get(1)));
                        mydb.updateLimit("restroom", pairs);

                        //Toast.makeText(getApplicationContext(), site[0].toString(), Toast.LENGTH_LONG).show();
                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("username", username);
                        dataBundle.putString("password", password);
                        dataBundle.putString("tracking", tracker);
                        dataBundle.putString("tracker", tracker);
                        dataBundle.putString("trackerType", trackerType);
                        ArrayList<String> newCategory = new ArrayList<String>();
                        dataBundle.putString("state", tracker);
                        dataBundle.putStringArrayList("type", type);
                        dataBundle.putString("actionable", "newSite");
                        dataBundle.putStringArrayList("category", newCategory);

                        Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                        intent.putExtras(dataBundle);
                        startActivity(intent);

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

    private String reconcileTokens(ArrayList<String> tokens, ArrayList<String> originalTokens) {
        Map<String, String> finalTokens = new HashMap<String, String>();
        ArrayList<String> finalTokenSet = new ArrayList<String>();
        Boolean in = false;
        for (String token : tokens) {
            String[] newLevels = token.split("\\.");
            String[] newTiers = newLevels[3].split("_");
            in = false;
            for (String original: originalTokens) {
                String[] orgLevels = original.split("\\.");
                if (orgLevels[3].contains(newTiers[0])) {
                    in = true;
                    String[] orgTiers = orgLevels[3].split("_");
                    if (Integer.parseInt(orgTiers[2]) > Integer.parseInt(newTiers[2])) {
                        if (finalTokens.get(newTiers[0]) == null) {
                            finalTokens.put(newTiers[0], orgTiers[2]);
                        } else {
                            if (Integer.parseInt(finalTokens.get(newTiers[0])) < Integer.parseInt(orgTiers[2])) {
                                finalTokens.put(newTiers[0], orgTiers[2]);
                            }
                        }
                    } else {
                        if (finalTokens.get(newTiers[0]) == null) {
                            finalTokens.put(newTiers[0], newTiers[2]);
                        } else {
                            if (Integer.parseInt(finalTokens.get(newTiers[0])) < Integer.parseInt(newTiers[2])) {
                                finalTokens.put(newTiers[0], newTiers[2]);
                            }
                        }
                    }
                }
            }

            if (!in) {
                if (finalTokens.get(newTiers[0]) == null) {
                    finalTokens.put(newTiers[0], newTiers[2]);
                } else {
                    if (Integer.parseInt(finalTokens.get(newTiers[0])) < Integer.parseInt(newTiers[2])) {
                        finalTokens.put(newTiers[0], newTiers[2]);
                    }
                }
            }
        }

        for(Map.Entry<String, String> entry : finalTokens.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d("recon final tokens", key + ": " + value);
            finalTokenSet.add("com.adaveracity.casp." + key + "_tier_" + value);
        }

        return TextUtils.join("|", tokens);
    }
}