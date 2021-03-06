package com.receiptbook.receiptbook;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import android.nfc.*;
import android.widget.TextView;
import android.widget.Toast;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static String lastUUID = "";
    public static String BASE_URL = "http://receiptbook.dreeemteam.co.uk";
    public static final String TAG = "RB";
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("lastUUID", lastUUID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        lastUUID = savedInstanceState.getString("lastUUID", "");
    }


    @Override
    public void onResume() {
        super.onResume();


        Context context = getApplicationContext();
        Intent intent = getIntent();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        lastUUID = sharedPref.getString("lastUUID", "");

        String str = "Nothing";
        int id = -1;
        String vendor = "";
        double price = ScanFragment.NOT_SET;
        boolean detectedNew = false;

        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {

            CharSequence text = "Please enable NFC!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            openHome("Please Enable NFC", ScanFragment.FAILED);

            return;
        }


        // adapter exists and is enabled.

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            //Message recieved
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {


                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    NdefRecord[] records = msgs[i].getRecords();

                    byte[] payload = records[i].getPayload();


                    byte status = payload[0];
                    int enc = status & 0x80; // Bit mask 7th bit 1
                    String encString = null;
                    if (enc == 0) {
                        encString = "UTF-8";
                    } else {
                        encString = "UTF-16";
                    }

                    int ianaLength = status & 0x3F; // Bit mask bit 5..0

                    try {
                        String content = new String(payload, ianaLength + 1, payload.length - 1 - ianaLength, encString);
                        str = content;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }


                try {

                    JSONObject json = new JSONObject(str);

                    id = Integer.parseInt(json.optString("id"));
                    str = json.optString("uuid");
                    vendor = json.optString("vendor");
                    price = Double.parseDouble(json.optString("price"));

                    detectedNew = !lastUUID.equals(str);
                    if (detectedNew) {
                        link(str, vendor, price);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    CharSequence text = "Receipt not valid!";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();


                    openHome("Invalid Receipt", ScanFragment.FAILED);
                    return;
                }


            }

            if (detectedNew) {
                openHome(vendor, ScanFragment.LOADING);
            } else {
                openHome(vendor, price);
            }

        } else {
            //Nothing detected
            openHome();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    public void link(String str, String vendor, double price) {

        final String ven = vendor;
        final double pri = price;
        final String uuid = str;


        if (lastUUID.equals(str)) {
            return;
        }


        queue = Volley.newRequestQueue(this);
        String url = BASE_URL + "/link?userid=1&uuid=" + str;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Only update if successful
                        lastUUID = uuid;

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("lastUUID", lastUUID);
                        editor.commit();

                        openHome(ven, pri);

                       /* Context context = getApplicationContext();
                        CharSequence text = "Scanned Successfully!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();*/
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Context context = getApplicationContext();
                        String errMsg = VolleyErrorHelper.getMessage(error, context);

                        openHome(ven, ScanFragment.FAILED);

                        CharSequence text = "Error: " + errMsg;
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        //Show current page as selected
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(menu.getItem(i).getItemId() == id);
        }

        if (id == R.id.nav_home) {
            openHome();
        } else if (id == R.id.nav_search) {
            setTitle("Search");
            openWeb("/search");

        } else if (id == R.id.nav_budget) {
            setTitle("Budget");
            openWeb("/budget");

        } else if (id == R.id.nav_projection) {
            setTitle("Projection");
            openWeb("/projection");

        } else if (id == R.id.nav_awards) {
            setTitle("Awards");
            openWeb("/awards");
        } else if (id == R.id.nav_vouchers) {
            setTitle("Vouchers");
            openWeb("/vouchers");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setTitle(String title) {

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle(title);
    }

    private void openHome(String vendor, double price) {
        setTitle("Scan");

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment currentWindow = (Fragment)
                fm.findFragmentById(R.id.fragment_container);

        if (currentWindow != null) {
            ft.remove(currentWindow);
        }

        //See if the window we are requesting has already been created
        Fragment newWindow = ScanFragment.getInstance(vendor, price);

        ft.replace(R.id.fragment_container, newWindow);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void openHome() {
        openHome("", ScanFragment.NOT_SET);
    }


    private void openWeb(String url) {
        url = BASE_URL + url;

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment currentWindow = fm.findFragmentById(R.id.fragment_container);

        if (currentWindow != null) {
            ft.remove(currentWindow);
        }

        //See if the window we are requesting has already been created 
        WebAppFragment newWindow = (WebAppFragment)
                fm.findFragmentByTag(url);

        if (newWindow == null) {
            newWindow = WebAppFragment.getInstance(url);
        }

        ft.replace(R.id.fragment_container, newWindow, url);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
}
