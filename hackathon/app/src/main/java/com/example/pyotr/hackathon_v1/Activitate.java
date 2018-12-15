package com.example.pyotr.hackathon_v1;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.Manifest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class Activitate extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    String userId="";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    // FIXME: 5/16/17
    private static final long UPDATE_INTERVAL = 10 * 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    // FIXME: 5/14/17
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

private TextView textView,textView2,textView4;
private Button btnDir;
private ImageView imageView;
private LinearLayout listView;
public List<String> myloc;
    private String city;

    @Override
protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitate);


      //  textView=(TextView)findViewById(R.id.textView);
        textView2= (TextView) findViewById(R.id.textView2);
        textView4= (TextView) findViewById(R.id.textView4);
        listView=(LinearLayout) findViewById(R.id.listView) ;


        btnDir=(Button) findViewById(R.id.btnDir);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
                String j =(String) b.get("id");
                  //textView.setText("Welcome:"+j);
                  userId=j;
        }







    if (!checkPermissions()) {
        requestPermissions();
    }

    buildGoogleApiClient();

        //start location update
    btnDir.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //latlng a  userului + latitudinea de pe server cu un get






            String url = "http://mojostudio.go.ro/googlers-api/api/needhelp";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    //trimitem catre maps Direction

                    if (response.toString().equals("900")) {
                        Toast.makeText(getApplicationContext(), "You are not in danger!", Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        Log.i("Help", "Succesful:" + response.toString());

                        //prelucrare json
                        String string = response.toString();
                        string = string.replace("[","");
                        string = string.replace("]","");
                        String[] parts = string.split(",");
                        String userlat = parts[0];
                        String userlng = parts[1];
                        String safelat = parts[2];
                        String safelng = parts[3];

                        Log.i("HelpLat:",userlat+" "+ userlng +" "+safelat+" "+safelng);





                        String a="https://www.google.com/maps/dir/"+userlat+",+"+userlng+"/'"+safelat+","+safelng+"'";
                         Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(a));
                        startActivity(intent);

                    }







                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                        //trimitem catre pagina de login



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                           // Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    params.put("id",userId);

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


            RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);



        }
    }
    );





}


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);



    }


    @Override
    protected void onResume() {
        super.onResume();
        updateButtonsState(LocationRequestHelper.getRequesting(this));
        //mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));

        //faceum un string tokenizer si luam lat si lng

      //requestLocationUpdates();
        String str=LocationResultHelper.getSavedLocationResult(this);
        Log.i("LocatieString",   str);

        List<String> linii=new ArrayList<>();
        linii= getLines(LocationResultHelper.getSavedLocationResult(this));
        if(linii.size()>0) {
            //trimitem post
            List<String> date = new ArrayList<>();
            date = getTokens(linii.get(0));
            Log.i("yes", "linie0:" + linii.get(0) + "lat" + date.get(0) + " lng " + date.get(1));
            final String lat = date.get(0);
            final String lng = date.get(1);



            //update


            String url = "http://mojostudio.go.ro/googlers-api/api/person/" + userId;

            final List<String> finalDate = date;
            StringRequest stringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    double lt = Double.parseDouble(lat);
                    double lg = Double.parseDouble(lng);
                    try {

                        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List < Address > addresses = geo.getFromLocation(lt, lg, 1);
                        if (addresses.isEmpty()) {
                            //addres.setText("Waiting for Location");
                            Log.i("locatie","Waiting for Location");
                        } else {
                            if (addresses.size() > 0) {
                                city=addresses.get(0).getAdminArea();
                                Log.i("locatie",addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
                                //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                  /*  try {
                        params.put("location", URLEncoder.encode(String.valueOf(getCity()),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/
                    params.put("location",city);

                    params.put("latitude", lat);
                    params.put("longitude",lng);









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




            date.clear();
        }
        linii.clear();



    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();


    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    /**
     * Builds {@link GoogleApiClient}, enabling automatic lifecycle management using
     * {@link GoogleApiClient.Builder#enableAutoManage(android.support.v4.app.FragmentActivity,
     * int, GoogleApiClient.OnConnectionFailedListener)}. I.e., GoogleApiClient connects in
     * {@link AppCompatActivity#onStart}, or if onStart() has already happened, it connects
     * immediately, and disconnects automatically in {@link AppCompatActivity#onStop}.
     */
    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");

        requestLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.in_case:
            //add the function to perform here
            //ascundem textele
            listView.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);
            textView4.setText("• Evacuate only as recommended by authorities to stay clear of lava, mud flows, and flying rocks and debris.\n" +
                    "\n" +
                    "• Avoid river areas and low-lying regions.\n" +
                    "\n" +
                    "• Before you leave the house, change into long-sleeved shirts and long pants and use goggles or eyeglasses, not contacts. Wear an emergency mask or hold a damp cloth over your face.\n" +
                    "\n" +
                    "• If you are not evacuating, close windows and doors and block chimneys and other vents, to prevent ash from coming into the house.\n" +
                    "\n" +
                    "• Be aware that ash may put excess weight on your roof and need to be swept away. Wear protection during cleanups.\n" +
                    "\n" +
                    "• Ash can damage engines and metal parts, so avoid driving. If you must drive, stay below 35 miles (56 kilometers) an hour.");
            return(true);
        case R.id.menu_Logout:
            //add the function to perform here
            SharedPrefManager.getInstance(getApplicationContext()).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return(true);

    }
        return(super.onOptionsItemSelected(item));
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
        final String text = "Connection suspended";
        Log.w(TAG, text + ": Error code: " + i);
        showSnackbar("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        final String text = "Exception while connecting to Google Play services";
        Log.w(TAG, text + ": " + connectionResult.getErrorMessage());
        showSnackbar(text);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.user_activity);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.user_activity),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(Activitate.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(Activitate.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted. Kick off the process of building and connecting
                // GoogleApiClient.
                buildGoogleApiClient();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.user_activity),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }
    public List<String> getTokens(String str) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        while (tokenizer.hasMoreElements()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    public List<String> getLines(String str) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, "\n");
        while (tokenizer.hasMoreElements()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
          //  mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));


            Log.i("shared","1");



            //faceum un string tokenizer si luam lat si lng


            String str=LocationResultHelper.getSavedLocationResult(this);
            Log.i("LocatieString1",   str);

            List<String> linii=new ArrayList<>();
            linii= getLines(LocationResultHelper.getSavedLocationResult(this));
            if(linii.size()>0)
            {
                //trimitem post
                List<String>  date=new ArrayList<>();
                date=getTokens(linii.get(0));
                Log.i("yes1",  "linie0:"+linii.get(0)+ "lat"+date.get(0)+" lng "+date.get(1));
                final String lat=date.get(0);
                final String lng=date.get(1);




                //update
                String url = "http://mojostudio.go.ro/googlers-api/api/person/"+userId;
                final List<String> finalDate = date;
                StringRequest stringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            //Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();

                            //trimitem catre pagina de login



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                               // Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();

                       // params.put("name", "name_test");
                        // params.put("email", email);
                        //params.put("password", password);
                        // params.put("phone",phoneNumber);
                        //params.put("address",address);
                        //params.put("help","0");


                        double lt = Double.parseDouble(lat);
                        double lg = Double.parseDouble(lng);
                        try {

                            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List < Address > addresses = geo.getFromLocation(lt, lg, 1);
                            if (addresses.isEmpty()) {
                                //addres.setText("Waiting for Location");
                                Log.i("locatie","Waiting for Location");
                            } else {
                                if (addresses.size() > 0) {
                                    city=addresses.get(0).getAdminArea();
                                    Log.i("locatie",addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
                                    //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace(); // getFromLocation() may sometimes fail
                        }
                  /*  try {
                        params.put("location", URLEncoder.encode(String.valueOf(getCity()),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/
                       params.put("location",city);
                        params.put("latitude", lat);
                        params.put("longitude",lng);







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




                date.clear();
            }
            linii.clear();

        } else if (s.equals(LocationRequestHelper.KEY_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(LocationRequestHelper.getRequesting(this));
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            LocationRequestHelper.setRequesting(this, true);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        Log.i(TAG, "Removing location updates");
        LocationRequestHelper.setRequesting(this, false);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                getPendingIntent());
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void updateButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
           // mRequestUpdatesButton.setEnabled(false);
           // mRemoveUpdatesButton.setEnabled(true);
        } else {
           // mRequestUpdatesButton.setEnabled(true);
           // mRemoveUpdatesButton.setEnabled(false);
        }
    }



}
