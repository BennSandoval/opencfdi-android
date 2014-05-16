package com.cfdimovil.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.views.proceso.ResumenBorrador;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.opencfdimovil.sat.cfdi.v32.schema.ObjectFactory;

import java.util.Date;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class Receptor extends ActionBarActivity {

    private String rfc;
    private String fechaCotizacion;
    private boolean cotizacion=false;
    private boolean update=false;

    private TextView rfcField;
    private TextView nombreField;
    private TextView domicilioCalleField;
    private TextView domicilioNoExteriorField;
    private TextView domicilioNoInteriorField;
    private TextView domicilioCodigoPostalField;
    private TextView domicilioColoniaField;
    private TextView domicilioLocalidadField;
    private TextView domicilioMunicipioField;
    private TextView domicilioReferenciaField;
    private TextView domicilioPaisField;
    private AutoCompleteTextView domicilioEstadoField;

    private Button eliminar;
    private Button guardar;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();
    private Comprobante comprobante;
    private Comprobante.Receptor receptor;
    private DatabaseManager database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptor);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rfcField = (TextView) findViewById(R.id.rfc);
        nombreField = (TextView) findViewById(R.id.nombre);
        domicilioCalleField = (TextView) findViewById(R.id.domicilio_calle);
        domicilioNoExteriorField = (TextView) findViewById(R.id.domicilio_noExterior);
        domicilioNoInteriorField = (TextView) findViewById(R.id.domicilio_noInterior);
        domicilioCodigoPostalField = (TextView) findViewById(R.id.domicilio_codigoPostal);
        domicilioColoniaField = (TextView) findViewById(R.id.domicilio_colonia);
        domicilioLocalidadField = (TextView) findViewById(R.id.domicilio_localidad);
        domicilioMunicipioField = (TextView) findViewById(R.id.domicilio_municipio);
        domicilioReferenciaField = (TextView) findViewById(R.id.domicilio_referencia);
        domicilioEstadoField = (AutoCompleteTextView) findViewById(R.id.domicilio_estado);
        domicilioPaisField = (TextView) findViewById(R.id.domicilio_pais);

        eliminar = (Button) findViewById(R.id.eliminar);
        guardar = (Button) findViewById(R.id.guardar);

        ArrayAdapter<String> estadosArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.estados_array));
        domicilioEstadoField.setAdapter(estadosArrayAdapter);

        database = new DatabaseManager(getApplicationContext());
        Bundle bundle = getIntent().getExtras();

        if(bundle!=null){

            rfc =(String) bundle.get("rfc");

            database.openToRead();
            receptor = (Comprobante.Receptor)database.getReceptorCampo("rfc",rfc);
            database.close();

            rfcField.setText(receptor.getRfc());
            nombreField.setText(receptor.getNombre());

            if(receptor.getDomicilio()!=null) {
                if (receptor.getDomicilio().getCalle() != null)
                    domicilioCalleField.setText(receptor.getDomicilio().getCalle());
                if (receptor.getDomicilio().getNoExterior() != null)
                    domicilioNoExteriorField.setText(String.valueOf(receptor.getDomicilio().getNoExterior()));
                if (receptor.getDomicilio().getNoInterior() != null)
                    domicilioNoInteriorField.setText(String.valueOf(receptor.getDomicilio().getNoInterior()));
                if (receptor.getDomicilio().getCodigoPostal() != null)
                    domicilioCodigoPostalField.setText(String.valueOf(receptor.getDomicilio().getCodigoPostal()));
                if (receptor.getDomicilio().getColonia() != null)
                    domicilioColoniaField.setText(String.valueOf(receptor.getDomicilio().getColonia()));
                if (receptor.getDomicilio().getLocalidad() != null)
                    domicilioLocalidadField.setText(String.valueOf(receptor.getDomicilio().getLocalidad()));
                if (receptor.getDomicilio().getMunicipio() != null)
                    domicilioMunicipioField.setText(String.valueOf(receptor.getDomicilio().getMunicipio()));
                if (receptor.getDomicilio().getReferencia() != null)
                    domicilioReferenciaField.setText(String.valueOf(receptor.getDomicilio().getReferencia()));
                if (receptor.getDomicilio().getEstado() != null)
                    domicilioEstadoField.setText(String.valueOf(receptor.getDomicilio().getEstado()));
                if (receptor.getDomicilio().getPais() != null)
                    domicilioPaisField.setText(String.valueOf(receptor.getDomicilio().getPais()));
            }

            LinearLayout divider = (LinearLayout) findViewById(R.id.divider);
            divider.setVisibility(View.VISIBLE);
            eliminar.setText(getString(R.string.eliminar));
            eliminar.setVisibility(View.VISIBLE);
            eliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                database.openToWrite();
                if(cotizacion) {
                    comprobante.setReceptor(new Comprobante.Receptor());
                    String estructura = gson.toJson(comprobante);

                    if(update){
                        database.updateFactura(fechaCotizacion,receptor.getRfc(),"",estructura);
                    }else{
                        database.saveFactura(fechaCotizacion,receptor.getRfc(),"",estructura);
                    }
                } else {
                    database.deleteReceptor(rfc);
                }
                database.close();
                finish();
                }
            });

            update=true;

            fechaCotizacion =(String) bundle.get("fechaCotizacion");
            if(fechaCotizacion!=null && fechaCotizacion.length()>0) {
                database.openToRead();
                comprobante = (Comprobante) database.getCotizacionCampo("fecha", fechaCotizacion);
                database.close();
                cotizacion=true;
            }


        } else {

            ObjectFactory of = new ObjectFactory();
            receptor = of.createComprobanteReceptor();
            receptor.setDomicilio(of.createTUbicacion());

        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rfcField.getText().length()>0){
                    save();
                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.error_rfc),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!cotizacion) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_receptor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:

                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                return true;

            case R.id.action_facturar:

                if(rfcField.getText().length()>0){

                    save();
                    Intent intent = new Intent(this, ResumenBorrador.class);
                    intent.putExtra("rfc",receptor.getRfc());
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),getString(R.string.error_rfc),Toast.LENGTH_SHORT).show();
                }

                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void save() {

        receptor.setRfc(rfcField.getText().toString());
        receptor.setNombre(nombreField.getText().toString());

        if(domicilioCalleField.getText().toString().length()>0)
            receptor.getDomicilio().setCalle(domicilioCalleField.getText().toString());
        else
            receptor.getDomicilio().setCalle(null);
        if(domicilioNoExteriorField.getText().toString().length()>0)
            receptor.getDomicilio().setNoExterior(domicilioNoExteriorField.getText().toString());
        else
            receptor.getDomicilio().setNoExterior(null);
        if(domicilioNoInteriorField.getText().toString().length()>0)
            receptor.getDomicilio().setNoInterior(domicilioNoInteriorField.getText().toString());
        else
            receptor.getDomicilio().setNoInterior(null);
        if(domicilioCodigoPostalField.getText().toString().length()>0)
            receptor.getDomicilio().setCodigoPostal(domicilioCodigoPostalField.getText().toString());
        else
            receptor.getDomicilio().setCodigoPostal(null);
        if(domicilioColoniaField.getText().toString().length()>0)
            receptor.getDomicilio().setColonia(domicilioColoniaField.getText().toString());
        else
            receptor.getDomicilio().setColonia(null);
        if(domicilioLocalidadField.getText().toString().length()>0)
            receptor.getDomicilio().setLocalidad(domicilioLocalidadField.getText().toString());
        else
            receptor.getDomicilio().setLocalidad(null);
        if(domicilioMunicipioField.getText().toString().length()>0)
            receptor.getDomicilio().setMunicipio(domicilioMunicipioField.getText().toString());
        else
            receptor.getDomicilio().setMunicipio(null);
        if(domicilioReferenciaField.getText().toString().length()>0)
            receptor.getDomicilio().setReferencia(domicilioReferenciaField.getText().toString());
        else
            receptor.getDomicilio().setReferencia(null);
        if(domicilioEstadoField.getText().toString().length()>0)
            receptor.getDomicilio().setEstado(domicilioEstadoField.getText().toString());
        else
            receptor.getDomicilio().setEstado(null);
        if(domicilioPaisField.getText().toString().length()>0)
            receptor.getDomicilio().setPais(domicilioPaisField.getText().toString());
        else
            receptor.getDomicilio().setPais(null);

        String estructura = gson.toJson(receptor);

        database.openToWrite();
        if(update){

            database.updateReceptor(rfc,
                    receptor.getRfc(),
                    receptor.getNombre(),
                    estructura);
        } else {

            database.saveReceptor(receptor.getRfc(),
                    receptor.getNombre(),
                    estructura);

        }
        if(cotizacion) {
            comprobante.setReceptor(receptor);
            estructura = gson.toJson(comprobante);
            database.updateFactura(fechaCotizacion,receptor.getRfc(),"",estructura);
        }
        database.close();
        finish();
    }
}
