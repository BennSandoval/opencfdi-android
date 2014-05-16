package com.cfdimovil.app.views.proceso;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cfdimovil.app.R;
import com.cfdimovil.app.adapter.ObjectAdapter;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.views.MainActivity;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

/**
 * Created by ruben_sandoval on 4/27/14.
 */
public class ResumenConceptos extends ActionBarActivity {

    private String fecha="";

    private DatabaseManager database;
    private Comprobante comprobante;

    private ListView lista;
    private TextView subtotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_conceptos);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new DatabaseManager(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            fecha = (String) bundle.get("fechaCotizacion");
            database.openToRead();
            comprobante =(Comprobante)database.getCotizacionCampo("fecha",fecha);
            database.close();
        }

        subtotal = (TextView) findViewById(R.id.subtotal);
        lista = (ListView) findViewById(R.id.lista);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent intent = new Intent(getApplicationContext(), com.cfdimovil.app.views.Concepto.class);
                intent.putExtra("fechaCotizacion", fecha);
                intent.putExtra("noIdentificacion", comprobante.getConceptos().getConcepto().get(position).getNoIdentificacion());
                startActivity(intent);
            }
        });

        Button agregar = (Button) findViewById(R.id.agregar);
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), com.cfdimovil.app.views.proceso.Conceptos.class);
                intent.putExtra("fechaCotizacion", fecha);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
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

    private void updateView(){

        database.openToRead();
        comprobante =(Comprobante)database.getCotizacionCampo("fecha",fecha);
        database.close();

        if(comprobante.getSubTotal()!=null){
            subtotal.setText("$"+comprobante.getSubTotal());
        }

        ObjectAdapter conceptoAdapter = new ObjectAdapter(this, R.layout.item_card_concepto_cotizacion, comprobante.getConceptos().getConcepto());
        lista.setAdapter(conceptoAdapter);

    }
}
