package com.equalsd.recon;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;


public class MainActivity extends Activity {

    ArrayList<String> category = new ArrayList<String>();

    EditText ed_username;
    EditText ed_password;
    TextView tv_warning;
    private DBHelper login;
    Boolean added = false;
    String tracker = "";
    String trackerType = "";
    String old_username = "";
    String old_password = "";
    Button loginButton;

    private static final String QUERY_URL = "http://ada-veracity.com/api/verify-login-json.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ed_username = (EditText) findViewById(R.id.ed_username);
        ed_password = (EditText) findViewById(R.id.ed_password);
        tv_warning = (TextView) findViewById(R.id.tv_warning);
        loginButton = (Button) findViewById(R.id.loginButton);
        tv_warning.setVisibility(View.GONE);

        login = new DBHelper(this);
        Cursor rs = login.getData("login", 1);

        if (rs != null && rs.getCount() > 0) {
            rs.moveToFirst();

            ed_username.setText(rs.getString(rs.getColumnIndex("username")), TextView.BufferType.EDITABLE);
            old_username = rs.getString(rs.getColumnIndex("username"));
            old_password = rs.getString(rs.getColumnIndex("password"));
            ed_password.setText(rs.getString(rs.getColumnIndex("password")), TextView.BufferType.EDITABLE);
            tracker = rs.getString(rs.getColumnIndex("site"));
            trackerType = rs.getString(rs.getColumnIndex("type"));
            Log.d("recon tracker: ", tracker.toString());
            added = true;
            loginButton.setText("Login");
        }

        ed_username.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0 || ed_password.getText().length() != 0) {
                    loginButton.setText("Login");
                } else {
                    loginButton.setText("New User");
                }
            }
        });

        ed_password.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 || ed_username.getText().length() != 0) {
                    loginButton.setText("Login");
                } else {
                    loginButton.setText("New User");
                }
            }
        });
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void Login(View view) {
        if (loginButton.getText().equals("Login")) {
            if (old_password.matches(ed_password.getText().toString()) && old_username.matches(ed_username.getText().toString())) {
                Log.d("recon", "no weblogin");
                Bundle dataBundle = new Bundle();
                dataBundle.putString("username", ed_username.getText().toString());
                dataBundle.putString("password", ed_password.getText().toString());
                dataBundle.putString("state", "default");
                dataBundle.putString("actionable", "site");
                dataBundle.putString("tracker", tracker);
                dataBundle.putString("trackerType", trackerType);
                dataBundle.putStringArrayList("category", category);
                //dataBundle.putString("credit", credit[0]);

                Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                intent.putExtras(dataBundle);

                startActivity(intent);
            } else {
                Log.d("recon", "going online for weblogin");
                queryLogin();
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.UserActivity.class);
            startActivity(intent);
        }
    }

    public void queryLogin() {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        final String[] status = new String[1];
        try {
            jsonParams.put("username", ed_username.getText().toString());
            jsonParams.put("password", ed_password.getText().toString());
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

                                if (!old_username.matches(ed_username.getText().toString())) {
                                    Log.d("Recon", "clear sites, different username");
                                    //login.allDelete();
                                    tracker = "";
                                    trackerType = "";
                                }

                                ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                pairs.add(new AbstractMap.SimpleEntry("username", ed_username.getText().toString().toLowerCase()));
                                pairs.add(new AbstractMap.SimpleEntry("password", ed_password.getText().toString()));
                                pairs.add(new AbstractMap.SimpleEntry("site", tracker));
                                pairs.add(new AbstractMap.SimpleEntry("type", trackerType));
                                //pairs.add(new AbstractMap.SimpleEntry("credit", credit[0]));

                                if (!added) {
                                    login.insertData("login", pairs);
                                } else {
                                    login.updateContact("login", 1, pairs);
                                }
                                Bundle dataBundle = new Bundle();
                                dataBundle.putString("username", ed_username.getText().toString().toLowerCase());
                                dataBundle.putString("password", ed_password.getText().toString());
                                dataBundle.putString("state", "default");
                                dataBundle.putString("actionable", "site");
                                dataBundle.putString("tracker", tracker);
                                dataBundle.putString("trackerType", trackerType);
                                dataBundle.putStringArrayList("category", category);
                                //dataBundle.putString("credit", credit[0]);

                                Intent intent = new Intent(getApplicationContext(), com.equalsd.recon.List.class);
                                intent.putExtras(dataBundle);

                                startActivity(intent);
                            } else {
                                tv_warning.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }
}