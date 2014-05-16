package com.cfdimovil.app.views.proceso;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import com.cfdimovil.app.R;
import com.cfdimovil.app.views.MainActivity;

/**
 * Created by ruben_sandoval on 4/27/14.
 */
public class Conceptos extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_fragment);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();


        if(bundle!=null){
            String fechaCotizacion = (String) bundle.get("fechaCotizacion");
            Log.i(getString(R.string.app_name), fechaCotizacion);
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.cfdimovil.app.fragments.Conceptos conceptos = com.cfdimovil.app.fragments.Conceptos.newInstance(3, fechaCotizacion);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, conceptos,getString(R.string.section_clientes))
                    .commit();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            com.cfdimovil.app.fragments.Conceptos conceptos = com.cfdimovil.app.fragments.Conceptos.newInstance(3);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, conceptos,getString(R.string.section_clientes))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //super. onBackPressed();

                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
