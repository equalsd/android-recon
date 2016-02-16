package com.equalsd.recon;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by generic on 8/25/15.
 */
public class UserActivity extends AppCompatActivity {

    ArrayList<String> category = new ArrayList<String>();

    EditText login_name;
    EditText password;
    EditText password2;
    EditText company_name;
    EditText company_address;
    EditText user_name;
    EditText casp;
    EditText phone;
    EditText email;
    EditText question;
    EditText answer;

    private Boolean check;
    String textWrap = "Loading agreement...";
    TextView clickWrapText;
    private String clickWrapTitle;
    private Dialog clickWrap;
    private Button accept;
    private Button refuse;
    private DBHelper login;
    final String QUERY_URL = "http://ada-veracity.com/api/new-user-json.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        login_name = (EditText) findViewById(R.id.login_name);
        password = (EditText) findViewById(R.id.password);
        password2 = (EditText) findViewById(R.id.password_verify);
        company_name = (EditText) findViewById(R.id.company_name);
        company_address = (EditText) findViewById(R.id.company_address);
        user_name = (EditText) findViewById(R.id.login_name);
        casp = (EditText) findViewById(R.id.casp);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        question = (EditText) findViewById(R.id.question);
        answer = (EditText) findViewById(R.id.answer);

        login = new DBHelper(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void submit(View v) {
        Boolean errorCheck = false;
        check = true;
        if (!password.getText().toString().equals(password2.getText().toString())) {
            password2.setError("Must match password");
            errorCheck = true;
        }

        if (password.getText().length() == 0) {
            password.setError("Password cannot be blank");
            errorCheck = true;
        }

        if (email.getText().length() == 0 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Email is not valid");
            errorCheck = true;
        }

        if (company_name.getText().length() == 0) {
            company_name.setError("Company Name cannot be blank");
            errorCheck = true;
        }

        if (login_name.getText().length() == 0) {
            login_name.setError("Account Name cannot be blank");
            errorCheck = true;
        }

        if (company_address.getText().length() == 0) {
            company_address.setError("Company Address cannot be blank");
            errorCheck = true;
        }

        if (question.getText().length() == 0) {
            question.setError("Password Recovery Question cannot be blank.");
            errorCheck = true;
        }

        if (answer.getText().length() == 0) {
            answer.setError("Password Recovery Answer cannot be blank");
            errorCheck = true;
        }

        if (!errorCheck) {
            querySubmit();
        } else {
            Toast.makeText(getApplicationContext(), "There are errors in the submission form.", Toast.LENGTH_LONG).show();
        }
    }

    protected void querySubmit() {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        final String[] status = new String[1];
        try {
            jsonParams.put("username", login_name.getText().toString());
            jsonParams.put("password", password.getText().toString());
            jsonParams.put("email", email.getText().toString());
            jsonParams.put("contact_name", user_name.getText().toString());
            jsonParams.put("contact_phone", phone.getText().toString());
            jsonParams.put("company_address", company_address.getText().toString());
            jsonParams.put("company_name", company_name.getText().toString());
            jsonParams.put("question", question.getText().toString());
            jsonParams.put("answer", answer.getText().toString());
            jsonParams.put("check", check.toString());
            jsonParams.put("casp", casp.getText().toString());
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
                            if (status[0].equals("login")) {
                                login.allDelete();

                                //credit[0] = jsonObject.get("credit").toString();
                                //Log.d("recon", "credit " + credit[0]);
                                ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                                pairs.add(new AbstractMap.SimpleEntry("username", login_name.getText().toString()));
                                pairs.add(new AbstractMap.SimpleEntry("password", password.getText().toString()));
                                pairs.add(new AbstractMap.SimpleEntry("site", ""));
                                pairs.add(new AbstractMap.SimpleEntry("type", ""));
                                //pairs.add(new AbstractMap.SimpleEntry("credit", credit[0]));
                                login.updateContact("login", 1, pairs);
                                Bundle dataBundle = new Bundle();
                                dataBundle.putString("username", login_name.getText().toString());
                                dataBundle.putString("password", password.getText().toString());
                                dataBundle.putString("state", "default");
                                dataBundle.putString("actionable", "site");
                                dataBundle.putString("tracker", "");
                                dataBundle.putString("trackerType", "");
                                dataBundle.putStringArrayList("category", category);
                                //dataBundle.putString("credit", credit[0]);

                                Intent intent = new Intent(getApplicationContext(),com.equalsd.recon.List.class);
                                intent.putExtras(dataBundle);

                                startActivity(intent);
                            } else if (status[0].equals("ok")) {
                                Toast.makeText(getApplicationContext(), "Working...", Toast.LENGTH_LONG).show();
                                new RequestTask().execute("http://ada-veracity.com/signup-wrapContent.php?content=wrap");
                                clickWrapTitle = "License Agreement";
                                //alertDialogClick();
                            } else {
                                Toast.makeText(getApplicationContext(), status[0], Toast.LENGTH_LONG).show();
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

    private void alertDialogClick() {
        clickWrap = new Dialog(UserActivity.this);
        clickWrap.setContentView(R.layout.clickwrap_dialogue);

        clickWrap.setTitle(clickWrapTitle);
        clickWrapText = (TextView) clickWrap.findViewById(R.id.clickText);
        clickWrapText.setText(Html.fromHtml(textWrap));
        refuse = (Button) clickWrap.findViewById(R.id.refuse);
        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickWrap.dismiss();
            }
        });

        accept = (Button) clickWrap.findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload();
                //v.setEnabled(false);
                if (clickWrapTitle.matches("License Agreement")) {
                    clickWrapTitle = "Private Policy";
                    new RequestTask().execute("http://ada-veracity.com/signup-wrapContent.php?content=private");
                    clickWrap.dismiss();
                    Toast.makeText(getApplicationContext(), "Working...", Toast.LENGTH_LONG).show();
                } else {
                    check = false;
                    querySubmit();
                }
                //Toast.makeText(getApplicationContext(), "UPLOAD", Toast.LENGTH_LONG).show();
            }
        });

        clickWrap.show();
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        // username, password, message, mobile
        protected String doInBackground(String... url) {
            // constants
            int timeoutSocket = 5000;
            int timeoutConnection = 5000;

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient client = new DefaultHttpClient(httpParameters);

            HttpGet httpget = new HttpGet(url[0]);

            try {
                HttpResponse getResponse = client.execute(httpget);
                final int statusCode = getResponse.getStatusLine().getStatusCode();

                if(statusCode != HttpStatus.SC_OK) {
                    Log.d("Recon", "Download Error: " + statusCode + "| for URL: " + url);
                    return null;
                }

                String line = "";
                StringBuilder total = new StringBuilder();

                HttpEntity getResponseEntity = getResponse.getEntity();

                BufferedReader reader = new BufferedReader(new InputStreamReader(getResponseEntity.getContent()));

                while((line = reader.readLine()) != null) {
                    total.append(line);
                }

                line = total.toString();
                return line;
            } catch (Exception e) {
                Log.d("Recon", "Download Exception : " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // do something with result
            //textWrap = result.replace("\\\n", System.getProperty("line.separator"));
            textWrap = result;
            alertDialogClick();
        }
    }
}
