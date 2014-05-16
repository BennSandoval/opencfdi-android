package com.cfdimovil.app.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class Impuestos extends Activity {

    private String fechaCotizacion;
    private Spinner ivaTrasladado;
    protected TextView ieps;
    private TextView ivaRetenido;
    private TextView isr;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private DatabaseManager database;
    private Comprobante comprobante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_impuestos);

        database = new DatabaseManager(getApplicationContext());
        Button guardar = (Button) findViewById(R.id.guardar);

        ivaTrasladado = (Spinner) findViewById(R.id.iva_trasladado);
        ieps = (TextView) findViewById(R.id.ieps);
        ivaRetenido = (TextView) findViewById(R.id.iva_retenido);
        isr = (TextView) findViewById(R.id.isr);

        ivaTrasladado = (Spinner) findViewById(R.id.iva_trasladado);
        ArrayAdapter<String> impuestoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.iva_array));
        impuestoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ivaTrasladado.setAdapter(impuestoAdapter);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            fechaCotizacion =(String) bundle.get("fechaCotizacion");

            database.openToRead();
            comprobante = (Comprobante) database.getCotizacionCampo("fecha", fechaCotizacion);
            database.close();

            int ivaPosition = impuestoAdapter.getPosition(String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(0).getTasa()));
            ivaTrasladado.setSelection(ivaPosition);

            if(!String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(1).getTasa()).equals("0")){
                ieps.setText(String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(1).getTasa()));
            }

            if(!String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(0).getImporte()).equals("0")) {
                ivaRetenido.setText(String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(0).getImporte()));
            }

            if(!String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(1).getImporte()).equals("0")) {
                isr.setText(String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(1).getImporte()));
            }

        } else {
            finish();
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ivaTrasladadoSelected = ivaTrasladado.getItemAtPosition(ivaTrasladado.getSelectedItemPosition()).toString();
                comprobante.getImpuestos().getTraslados().getTraslado().get(0).setTasa(new BigDecimal(ivaTrasladadoSelected));

                BigDecimal importe = comprobante.getSubTotal().multiply((comprobante.getImpuestos().getTraslados().getTraslado().get(0).getTasa().divide(new BigDecimal(100))));
                comprobante.getImpuestos().getTraslados().getTraslado().get(0).setImporte(importe);
                if (ieps.getText().length() > 0) {
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setTasa(new BigDecimal(ieps.getText().toString()));
                    importe = comprobante.getSubTotal().multiply(
                            (comprobante.getImpuestos().getTraslados().getTraslado().get(1).getTasa().divide(new BigDecimal(100))));
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImporte(importe);
                } else {
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setTasa(new BigDecimal(0));
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImporte(new BigDecimal(0));
                }
                if (ivaRetenido.getText().length() > 0) {
                    BigDecimal value = new BigDecimal(ivaRetenido.getText().toString());
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImporte(value);
                } else {
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImporte(new BigDecimal(0));
                }
                if (isr.getText().length() > 0) {
                    BigDecimal value = new BigDecimal(isr.getText().toString());
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImporte(value);
                } else {
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImporte(new BigDecimal(0));
                }

                String estructura = gson.toJson(comprobante);
                database.openToWrite();
                database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
                database.close();
                finish();

            }
        });


    }
}
