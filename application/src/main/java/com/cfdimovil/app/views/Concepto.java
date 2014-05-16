package com.cfdimovil.app.views;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.opencfdimovil.sat.cfdi.v32.schema.ObjectFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class Concepto extends Activity {

    private boolean update=false;
    private String fechaCotizacion;
    private String noIdentificacion;
    private Boolean cotizacion=false;
    private int indiceEnComprobante=-1;

    private TextView importe;
    private EditText descripcionField;
    private EditText cantidad;
    private EditText valorField;
    private AutoCompleteTextView unidadField;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private Comprobante.Conceptos.Concepto articulo;
    private Comprobante comprobante;
    private DatabaseManager database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_concepto);

        descripcionField = (EditText) findViewById(R.id.descripcion);
        unidadField = (AutoCompleteTextView) findViewById(R.id.unidad);
        valorField = (EditText) findViewById(R.id.valorUnitario);
        valorField.setText("");

        Button eliminar = (Button) findViewById(R.id.eliminar);
        Button guardar = (Button) findViewById(R.id.guardar);

        LinearLayout layout_detalle = (LinearLayout) findViewById(R.id.layout_detalle);
        LinearLayout layout_detalle_leyenda = (LinearLayout) findViewById(R.id.layout_detalle_leyenda);
        cantidad = (EditText) findViewById(R.id.cantidad);
        importe = (TextView) findViewById(R.id.importe);

        database = new DatabaseManager(getApplicationContext());
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            noIdentificacion =(String) bundle.get("noIdentificacion");
            fechaCotizacion =(String) bundle.get("fechaCotizacion");


            if(noIdentificacion!=null) {

                database.openToRead();
                articulo = (Comprobante.Conceptos.Concepto) database.getConceptoCampo("noIdentificacion", noIdentificacion);
                database.close();

                LinearLayout divider = (LinearLayout) findViewById(R.id.divider);
                divider.setVisibility(View.VISIBLE);
                eliminar.setVisibility(View.VISIBLE);
                eliminar.setText(getString(R.string.eliminar));

                update = true;

            } else {

                ObjectFactory of = new ObjectFactory();
                articulo = of.createComprobanteConceptosConcepto();
                articulo.setImporte(new BigDecimal(0));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                noIdentificacion = sdf.format(new Date());
                articulo.setNoIdentificacion(noIdentificacion);

            }

            if(fechaCotizacion!=null && fechaCotizacion.length()>0) {

                database.openToRead();
                comprobante = (Comprobante) database.getCotizacionCampo("fecha", fechaCotizacion);
                database.close();

                for(int indice=0;indice<comprobante.getConceptos().getConcepto().size();indice++){
                    if(comprobante.getConceptos().getConcepto().get(indice).getNoIdentificacion().equals(noIdentificacion)){
                        indiceEnComprobante=indice;
                    }
                }

                if(indiceEnComprobante<0) {
                    comprobante.getConceptos().getConcepto().add(articulo);
                    indiceEnComprobante = comprobante.getConceptos().getConcepto().size()-1;
                }

                articulo = comprobante.getConceptos().getConcepto().get(indiceEnComprobante);
                if(articulo.getImporte()==null){
                    articulo.setImporte(new BigDecimal(0));
                }

                if(update) {

                    layout_detalle.setVisibility(View.VISIBLE);
                    layout_detalle_leyenda.setVisibility(View.VISIBLE);
                    Button menos = (Button) findViewById(R.id.menos);
                    menos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                            cantidadValue -= 1;
                            if (cantidadValue < 0) {
                                cantidadValue = 0;
                            }
                            cantidad.setText(String.valueOf(cantidadValue));

                            float valorUnitarioValue = 0;
                            if (valorField.getText().length() > 0) {
                                valorUnitarioValue = Float.parseFloat(valorField.getText().toString());
                            }
                            importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                        }
                    });

                    Button mas = (Button) findViewById(R.id.mas);
                    mas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                            cantidadValue += 1;
                            cantidad.setText(String.valueOf(cantidadValue));

                            float valorUnitarioValue = 0;
                            if (valorField.getText().toString().length() > 0) {
                                valorUnitarioValue = Float.parseFloat(valorField.getText().toString());
                            }
                            importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                        }
                    });

                    valorField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                            if (valorField.getText().length() >0 && cantidad.getText().length() > 0) {

                                float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                                float valorUnitarioValue = Float.parseFloat(valorField.getText().toString());

                                importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                            } else {
                                importe.setText("0.00");
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                            if (valorField.getText().length() >0 && cantidad.getText().length() > 0) {

                                float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                                float valorUnitarioValue = Float.parseFloat(valorField.getText().toString());

                                importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                            } else {
                                importe.setText("0.00");
                            }
                        }
                    });


                    cantidad.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                            if (valorField.getText().length() >0 && cantidad.getText().length() > 0) {

                                float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                                float valorUnitarioValue = Float.parseFloat(valorField.getText().toString());

                                importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                            } else {
                                importe.setText("0.00");
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                            if (valorField.getText().length() >0 && cantidad.getText().length() > 0) {

                                float cantidadValue = Float.parseFloat(cantidad.getText().toString());
                                float valorUnitarioValue = Float.parseFloat(valorField.getText().toString());

                                importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
                            } else {
                                importe.setText("0.00");
                            }
                        }
                    });

                    if (articulo.getCantidad() != null) {
                        cantidad.setText(String.valueOf(articulo.getCantidad()));
                    }

                    if (articulo.getImporte() != null) {
                        importe.setText(String.valueOf(articulo.getImporte()));
                    }

                    if (articulo.getValorUnitario() != null && !String.valueOf(articulo.getValorUnitario()).equals("0.00")) {
                        valorField.setText(String.valueOf(articulo.getValorUnitario()));
                    }

                    float cantidadValue = 0;
                    if (cantidad.getText().toString().length() > 0) {
                        cantidadValue = Float.parseFloat(cantidad.getText().toString());
                    }
                    float valorUnitarioValue = 0;
                    if (valorField.getText().toString().length() > 0) {
                        valorUnitarioValue = Float.parseFloat(valorField.getText().toString());
                    }
                    importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));

                    cotizacion = true;
                }
            }

        } else {

            ObjectFactory of = new ObjectFactory();
            articulo = of.createComprobanteConceptosConcepto();
            articulo.setImporte(new BigDecimal(0));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            noIdentificacion = sdf.format(new Date());
            articulo.setNoIdentificacion(noIdentificacion);

        }

        descripcionField.setText(articulo.getDescripcion());
        unidadField.setText(articulo.getUnidad());
        if(articulo.getValorUnitario()!=null && !String.valueOf(articulo.getValorUnitario()).equals("0.00")){
            valorField.setText(String.valueOf(articulo.getValorUnitario()));
        }

        AutoCompleteTextView unidad = (AutoCompleteTextView)findViewById(R.id.unidad);
        ArrayAdapter<String> unidadArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.unidades_array));
        unidad.setAdapter(unidadArrayAdapter);
        unidad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {

            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database.openToWrite();
                if (cotizacion) {

                    if (indiceEnComprobante >= 0) {
                        comprobante.getConceptos().getConcepto().remove(indiceEnComprobante);
                    }

                    BigDecimal subTotal = new BigDecimal(0);
                    for (Comprobante.Conceptos.Concepto concepto : comprobante.getConceptos().getConcepto()) {
                        subTotal = subTotal.add(concepto.getImporte());
                    }
                    comprobante.setSubTotal(subTotal);

                    String estructura = gson.toJson(comprobante);

                    if (update) {
                        database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
                    } else {
                        database.saveFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
                    }

                } else {
                    database.deleteConcepto(articulo.getNoIdentificacion());
                }
                database.close();
                finish();
            }
        });

    }

    private void save() {

        articulo.setNoIdentificacion(noIdentificacion);
        articulo.setDescripcion(descripcionField.getText().toString());
        articulo.setUnidad(unidadField.getText().toString());
        if(valorField.getText().toString().length()>0) {
            articulo.setValorUnitario(new BigDecimal(valorField.getText().toString()));
        }

        database.openToWrite();
        if(!cotizacion){
            String estructura = gson.toJson(articulo);
            if(update){
                database.updateConcepto(noIdentificacion,
                        articulo.getDescripcion(),
                        articulo.getValorUnitario(),
                        estructura);

            } else {
                database.saveConcepto(articulo.getNoIdentificacion(),
                        articulo.getDescripcion(),
                        articulo.getValorUnitario(),
                        estructura);
            }
        } else {

            if(cantidad.getText().toString().length()>0) {
                articulo.setCantidad(new BigDecimal(cantidad.getText().toString()));
            } else {
                articulo.setCantidad(new BigDecimal("0"));
            }

            if(valorField.getText().toString().length()>0) {
                articulo.setValorUnitario(new BigDecimal(valorField.getText().toString()));
            } else {
                articulo.setValorUnitario(new BigDecimal("0"));
            }

            float cantidadValue =0;
            if(cantidad.getText().toString().length()>0) {
                cantidadValue = Float.parseFloat(cantidad.getText().toString());
            }
            float valorUnitarioValue = 0;
            if(valorField.getText().toString().length()>0) {
                valorUnitarioValue = Float.parseFloat(valorField.getText().toString());
            }

            importe.setText(String.valueOf(valorUnitarioValue * cantidadValue));
            articulo.setImporte(new BigDecimal(importe.getText().toString()));

            if(indiceEnComprobante>=0){
                comprobante.getConceptos().getConcepto().remove(indiceEnComprobante);
                comprobante.getConceptos().getConcepto().add(indiceEnComprobante,articulo);
            }

            BigDecimal subTotal=new BigDecimal(0);
            for(Comprobante.Conceptos.Concepto concepto:comprobante.getConceptos().getConcepto()){
                subTotal=subTotal.add(concepto.getImporte());
            }
            comprobante.setSubTotal(subTotal);

            if(update) {
                String estructura = gson.toJson(comprobante);
                database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
            }
        }
        database.close();
        finish();
    }
}
