package com.example.pyotr.hackathon_v1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextUsername, editTextPassword;
    private Button btnLogin;
    private TextView textViewLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

       if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, Activitate.class));
            return;
        }

        editTextUsername = (EditText) findViewById(R.id.txtEmail);
        editTextPassword = (EditText) findViewById(R.id.txtPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        textViewLogin = (TextView) findViewById(R.id.register);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        btnLogin.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);

    }

    private String uriBuilder(String name) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("mojostudio.go.ro")
                .appendPath("googlers-api")
                .appendPath("api")
                .appendPath("person")
                .appendQueryParameter("name", name);

        String myUrl = builder.build().toString();
        return myUrl;
    }

    /*private String uriBuilder(String email, String pass) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("mojostudio.go.ro")
                .appendPath("googlers-api/api/person")
                .appendQueryParameter("email", email)
                .appendQueryParameter("password", pass);
        String myUrl = builder.build().toString();
        return myUrl;
    }*/


    /*private void userLogin(){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        progressDialog.show();



        String url = uriBuilder(username);
        Log.d("URL_CREAT", url);
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        //daca avem un raspuns atunci se efectueaza logarea
                        //
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        finish();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());

                        Toast.makeText(getApplicationContext(), "Date introduse gresit", Toast.LENGTH_LONG).show();
                    }
                }
        );

// add it to the RequestQueue
        RequestHandler.getInstance(this).addToRequestQueue(getRequest);
    }*/

    private void userLogin()
    {
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        progressDialog.setMessage("Login user...");
        progressDialog.show();

        String url = "http://mojostudio.go.ro/googlers-api/api/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                if (response.toString().equals("900")) {
                    Toast.makeText(getApplicationContext(), "Date introduse gresit", Toast.LENGTH_LONG).show();

                }
                else
                {

                    Log.i("Login", "Succesful:" + response.toString());
                    Intent i = new Intent(LoginActivity.this, Activitate.class);
                    String strName = response.toString();

                   SharedPrefManager.getInstance(getApplicationContext()).userLogin(Integer.parseInt(strName),username,password); //salvam userul

                    i.putExtra("id", strName);
                    startActivity(i);
                    finish();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                       // Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Date introduse gresit", Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", username);
                params.put("password", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s","USERNAME","PASSWORD");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;
            }
        };


        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onClick(View view) {
        if(view == btnLogin){
            userLogin();
        }
        if(view==textViewLogin)
        {
            startActivity(new Intent(this, MainActivity.class));

        }
    }
}
