package com.cfdimovil.app.views.proceso;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.views.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.util.Date;

/**
 * Created by ruben_sandoval on 4/27/14.
 */
public class DetalleBorrador extends ActionBarActivity {

    private String fechaCotizacion="";

    private Spinner tipoComprobante;
    private AutoCompleteTextView metodoDePago;
    private AutoCompleteTextView formaDePago;
    private AutoCompleteTextView lugarExpedicion;
    private AutoCompleteTextView moneda;
    private EditText numCtaPago;
    private EditText tipoCambio;

    private ArrayAdapter<String> tipoComprobanteArrayAdapter;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private DatabaseManager database;
    private Comprobante comprobante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_borrador);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new DatabaseManager(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            fechaCotizacion = (String) bundle.get("fechaCotizacion");
            database.openToRead();
            comprobante =(Comprobante)database.getCotizacionCampo("fecha",fechaCotizacion);
            database.close();
        }

        tipoComprobante = (Spinner) findViewById(R.id.tipoDeComprobante);
        tipoComprobanteArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.tipoDeComprobante_array));
        tipoComprobanteArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoComprobante.setAdapter(tipoComprobanteArrayAdapter);

        metodoDePago = (AutoCompleteTextView) findViewById(R.id.metodoDePago);
        ArrayAdapter<String> metodoDePagoArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.metodoDePago_array));
        metodoDePago.setAdapter(metodoDePagoArrayAdapter);

        formaDePago = (AutoCompleteTextView) findViewById(R.id.formaDePago);
        ArrayAdapter<String> formaDePagoArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.formaDePago_array));
        formaDePago.setAdapter(formaDePagoArrayAdapter);

        lugarExpedicion = (AutoCompleteTextView) findViewById(R.id.lugarExpedicion);
        ArrayAdapter<String> estadosArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.estados_array));
        lugarExpedicion.setAdapter(estadosArrayAdapter);

        moneda = (AutoCompleteTextView) findViewById(R.id.moneda);
        ArrayAdapter<String> monedaArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.moneda_array));
        moneda.setAdapter(monedaArrayAdapter);

        numCtaPago = (EditText) findViewById(R.id.numCtaPago);
        tipoCambio = (EditText) findViewById(R.id.tipoCambio);

        Button guardar = (Button) findViewById(R.id.guardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tipoComprobanteSeleccionado = tipoComprobante.getItemAtPosition(tipoComprobante.getSelectedItemPosition()).toString();

                comprobante.setTipoDeComprobante(tipoComprobanteSeleccionado);

                if (metodoDePago.getText().toString().length() > 0)
                    comprobante.setMetodoDePago(metodoDePago.getText().toString());
                else
                    comprobante.setMetodoDePago(null);
                if (formaDePago.getText().toString().length() > 0)
                    comprobante.setFormaDePago(formaDePago.getText().toString());
                else
                    comprobante.setFormaDePago(null);
                if (lugarExpedicion.getText().toString().length() > 0)
                    comprobante.setLugarExpedicion(lugarExpedicion.getText().toString());
                else
                    comprobante.setLugarExpedicion(null);
                if (moneda.getText().toString().length() > 0)
                    comprobante.setMoneda(moneda.getText().toString());
                else
                    comprobante.setMoneda(null);
                if (numCtaPago.getText().toString().length() > 0)
                    comprobante.setNumCtaPago(numCtaPago.getText().toString());
                else
                    comprobante.setNumCtaPago(null);
                if (tipoCambio.getText().toString().length() > 0)
                    comprobante.setTipoCambio(tipoCambio.getText().toString());
                else
                    comprobante.setTipoCambio(null);

                if (comprobante.getLugarExpedicion() == null || comprobante.getLugarExpedicion().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Lugar de expedicion no valido.", Toast.LENGTH_LONG).show();
                } else if (comprobante.getFormaDePago() == null || comprobante.getFormaDePago().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Forma de pago vacia.", Toast.LENGTH_LONG).show();
                } else if (comprobante.getMetodoDePago() == null || comprobante.getMetodoDePago().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Metodo de pago vacio.", Toast.LENGTH_LONG).show();
                } else {
                    String estructura = gson.toJson(comprobante);
                    database.openToRead();
                    database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
                    database.close();
                    finish();
                }

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
        comprobante =(Comprobante)database.getCotizacionCampo("fecha",fechaCotizacion);
        database.close();

        int tipoComprobantePosition = tipoComprobanteArrayAdapter.getPosition(String.valueOf(comprobante.getTipoDeComprobante()));
        tipoComprobante.setSelection(tipoComprobantePosition);

        if(comprobante.getMetodoDePago()!=null&&comprobante.getMetodoDePago().length()>0) {
            metodoDePago.setText(comprobante.getMetodoDePago());
        }
        if(comprobante.getFormaDePago()!=null&&comprobante.getFormaDePago().length()>0) {
            formaDePago.setText(comprobante.getFormaDePago());
        }
        if(comprobante.getLugarExpedicion()!=null&&comprobante.getLugarExpedicion().length()>0) {
            lugarExpedicion.setText(comprobante.getLugarExpedicion());
        }
        if(comprobante.getMoneda()!=null&&comprobante.getMoneda().length()>0) {
            moneda.setText(comprobante.getMoneda());
        }
        if(comprobante.getNumCtaPago()!=null&&comprobante.getNumCtaPago().length()>0) {
            numCtaPago.setText(comprobante.getNumCtaPago());
        }
        if(comprobante.getTipoCambio()!=null&&comprobante.getTipoCambio().length()>0) {
            tipoCambio.setText(comprobante.getTipoCambio());
        }
    }

}
