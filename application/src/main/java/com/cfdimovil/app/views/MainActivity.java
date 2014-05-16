package com.cfdimovil.app.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.fragments.NavigationDrawerFragment;
import com.cfdimovil.app.fragments.Borradores;
import com.cfdimovil.app.fragments.Conceptos;
import com.cfdimovil.app.fragments.Emisor;
import com.cfdimovil.app.fragments.ClienteResumen;
import com.cfdimovil.app.fragments.Facturas;
import com.cfdimovil.app.fragments.Receptores;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CharSequence mTitle;
    private String keyGoogleCloudMessaging;
    private int GCMIntent=0;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SharedPreferences settings;
    private GoogleCloudMessaging googleCloudMessaging;

    private static final int PICK_SERVICES_RESOLUTION_REQUEST = 300;

    @Override
    protected void onResume(){
        super.onResume();
        mNavigationDrawerFragment.update();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        GoogleCloudMessage();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        position++;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.i(getString(R.string.app_name), "onNavigationDrawerItemSelected: "+position);
        switch (position) {
            case 1:
                ClienteResumen clienteResumen =  ClienteResumen.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, clienteResumen,getString(R.string.app_name))
                        .commit();
                break;
            case 2:
                Emisor emisor = Emisor.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, emisor,getString(R.string.section_datos_facturacion))
                        .commit();
                break;
            case 3:
                Conceptos conceptos = Conceptos.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, conceptos,getString(R.string.section_articulos))
                        .commit();
                break;
            case 4:
                Receptores receptores = Receptores.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, receptores,getString(R.string.section_clientes))
                        .commit();
                break;
            case 5:
                Borradores borradores = Borradores.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, borradores,getString(R.string.section_cotizaciones))
                        .commit();
                break;
            case 6:
                Facturas facturas = Facturas.newInstance(position);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, facturas,getString(R.string.section_facturas))
                        .commit();
                //Facturas
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        menu.clear();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.i(getString(R.string.app_name), "requestCode: "+requestCode+" resultCode "+resultCode);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(getString(R.string.app_name));

        if (fragment != null){
            ((ClienteResumen)fragment).onActivityResult(requestCode, resultCode, data);
        } else {
            Log.e(getString(R.string.app_name), "Error, no podemos acceder al fragment");
        }
    }

    public void onSectionAttached(int number) {
        Log.i(getString(R.string.app_name), "onSectionAttached: " + number);
        switch (number) {
            case 1:
                mTitle = getString(R.string.app_name);
                break;
            case 2:
                mTitle = getString(R.string.section_datos_facturacion);
                break;
            case 3:
                mTitle = getString(R.string.section_articulos);
                break;
            case 4:
                mTitle = getString(R.string.section_clientes);
                break;
            case 5:
                mTitle = getString(R.string.section_cotizaciones);
                break;
            case 6:
                mTitle = getString(R.string.section_facturas);
                break;
            default:
                mTitle = getString(R.string.app_name);
                break;
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void GoogleCloudMessage(){
        if (checkPlayServices()) {
            googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            keyGoogleCloudMessaging = getKeyLocal();
            Log.i(getString(R.string.app_name), "Google Cloud Messaging Local: "+keyGoogleCloudMessaging);

            if (keyGoogleCloudMessaging.equals("") && isOnline()) {
                registraEnBackground();
            }
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void registraEnBackground() {

        AsyncTask tak = new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    if (googleCloudMessaging == null) {
                        googleCloudMessaging = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    Log.i(getString(R.string.app_name), "Google Cloud Messaging SENDER: "+BuildConfig.GOOGLE_CLOUD_ID);
                    keyGoogleCloudMessaging = googleCloudMessaging.register(BuildConfig.GOOGLE_CLOUD_ID);
                    Log.i(getString(R.string.app_name), "Google Cloud Messaging: "+keyGoogleCloudMessaging);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("Google Cloud Messaging",keyGoogleCloudMessaging);
                    editor.commit();

                } catch (IOException ex) {
                    if(isOnline()){
                        if(GCMIntent<10){
                            registraEnBackground();
                            GCMIntent++;
                        }
                    }
                    Log.e(getString(R.string.app_name), "Google Cloud Messaging Error :" + ex.getMessage());
                }
                return null;
            }
        };

        tak.execute();
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PICK_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(getString(R.string.app_name), "Dispositivo no soportado");
                finish();
            }
            return false;
        }
        return true;

    }

    private String getKeyLocal() {

        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String registrationId = preferencias.getString("Google Cloud Messaging", "");
        if (registrationId.equals("")) {
            Log.i(getString(R.string.app_name), "Aun no se ha registrado.");
            return "";
        }
        return registrationId;

    }
}
