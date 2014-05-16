package com.cfdimovil.app.views.proceso;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.utils.PDFUtils;
import com.opencfdimovil.api.interfaces.OnTaskCompleted;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.utils.OAuthCall;
import com.cfdimovil.app.views.Impuestos;
import com.cfdimovil.app.views.MainActivity;
import com.cfdimovil.app.views.Receptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;


import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ruben_sandoval on 4/27/14.
 */
public class ResumenBorrador extends ActionBarActivity {

    private String fechaCotizacion="";
    private String password = "";

    private TextView tipoDeComprobante;
    private TextView formaDePago;
    private TextView metodoDePago;
    private TextView lugarDeExpedicion;
    private TextView numCtaPago;
    private TextView moneda;
    private TextView tipoCambio;
    private TextView nombre;
    private TextView subtotal;
    private TextView conceptosTitulo;
    private TextView total;
    private TextView retenidos;
    private TextView trasladados;

    private ActionBarActivity mActivity;
    private ProgressDialog progressDialog;

    private DatabaseManager database;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();
    private Comprobante comprobante;
    private RespuestaEstatusCuenta usuarioDatabase;
    private Comprobante.Emisor emisor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrador);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new DatabaseManager(this);
        mActivity = this;
        progressDialog = new ProgressDialog(this,R.style.alert);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage(getString(R.string.procesado_cotizacion));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);


        Date fechaFactura=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fechaCotizacion = sdf.format(fechaFactura);

        database.openToRead();
        comprobante =(Comprobante)database.getCotizacionCampo("fecha",fechaCotizacion);
        usuarioDatabase = (RespuestaEstatusCuenta)database.getUsuario();
        emisor = (Comprobante.Emisor) database.getEmisor();
        database.close();

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {

            if((String) bundle.get("fechaCotizacion")!=null){
                fechaCotizacion = (String) bundle.get("fechaCotizacion");
                database.openToRead();
                comprobante =(Comprobante)database.getCotizacionCampo("fecha",fechaCotizacion);
                database.close();
            }

            if((String) bundle.get("rfc")!=null){

                String rfc = (String)bundle.get("rfc");

                database.openToRead();
                comprobante.setReceptor((Comprobante.Receptor)database.getReceptorCampo("rfc",rfc));
                database.close();
                String estructura = gson.toJson(comprobante);

                database.openToWrite();
                database.saveFactura(fechaCotizacion,"","",estructura);
                database.close();
            }

        } else {

            String estructura = gson.toJson(comprobante);

            database.openToWrite();
            database.saveFactura(fechaCotizacion,"","",estructura);
            database.close();

        }

        Button datos = (Button) findViewById(R.id.datos);
        Button cliente = (Button) findViewById(R.id.cliente);
        Button conceptos = (Button) findViewById(R.id.conceptos);
        Button impuestos = (Button) findViewById(R.id.impuestos);
        Button pdf = (Button) findViewById(R.id.pdf);
        Button facturar = (Button) findViewById(R.id.facturar);

        tipoDeComprobante = (TextView) findViewById(R.id.tipoDeComprobante);
        formaDePago = (TextView) findViewById(R.id.formaDePago);
        metodoDePago = (TextView) findViewById(R.id.metodoDePago);

        lugarDeExpedicion = (TextView) findViewById(R.id.lugarDeExpedicion);
        numCtaPago = (TextView) findViewById(R.id.numCtaPago);
        moneda = (TextView) findViewById(R.id.moneda);
        tipoCambio = (TextView) findViewById(R.id.tipoCambio);

        conceptosTitulo = (TextView) findViewById(R.id.titulo_conceptos);
        nombre = (TextView) findViewById(R.id.nombre);
        subtotal = (TextView) findViewById(R.id.subtotal);
        total = (TextView) findViewById(R.id.total);
        Button eliminar = (Button) findViewById(R.id.eliminar);

        retenidos = (TextView) findViewById(R.id.retenidos);
        trasladados = (TextView) findViewById(R.id.trasladados);

        datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), DetalleBorrador.class);
                intent.putExtra("fechaCotizacion", fechaCotizacion);
                startActivity(intent);

            }
        });

        impuestos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), Impuestos.class);
                intent.putExtra("fechaCotizacion", fechaCotizacion);
                startActivity(intent);

            }
        });

        cliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comprobante.getReceptor().getRfc() == null || comprobante.getReceptor().getRfc().length() == 0) {

                    Intent intent = new Intent(getApplicationContext(), Receptores.class);
                    intent.putExtra("fechaCotizacion", fechaCotizacion);
                    startActivity(intent);

                } else {

                    Intent intent = new Intent(getApplicationContext(), Receptor.class);
                    intent.putExtra("rfc", comprobante.getReceptor().getRfc());
                    intent.putExtra("fechaCotizacion", fechaCotizacion);
                    startActivity(intent);

                }
            }
        });

        conceptos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ResumenConceptos.class);
                intent.putExtra("fechaCotizacion", fechaCotizacion);
                startActivity(intent);

            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.openToWrite();
                database.deleteFactura(fechaCotizacion);
                database.close();
                finish();
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                LinearLayout layout = new LinearLayout(mActivity);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(params);
                layout.setPadding(10, 0, 10, 0);

                final EditText inputVence = new EditText(mActivity);
                inputVence.setHint("Enero 2015");
                inputVence.setLayoutParams(params);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputVence.getWindowToken(), 0);
                inputVence.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                layout.addView(inputVence);

                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.vence));
                alert.setView(layout);

                alert.setPositiveButton(getString(R.string.generar_pdf), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogLabel, int whichButton) {
                        final String vence = inputVence.getText().toString();
                        progressDialog.show();
                        new Thread() {
                            @Override
                            public void run() {

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                String fecha = sdf.format(new Date());

                                String pdfcontent = PDFUtils.generarCotizacionPDF(mActivity, comprobante, vence);
                                final String path = Environment.getExternalStorageDirectory() + "/CFDIMovil/" + comprobante.getReceptor().getRfc() + "/cotizaciones/";
                                final String fileName = fecha + "_" + comprobante.getTotal() + ".pdf";
                                final boolean pdfCreado = PDFUtils.outputToFile(mActivity, path, fileName, pdfcontent, "ISO-8859-1");

                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog.isShowing()) {
                                            progressDialog.cancel();
                                        }
                                        if (pdfCreado) {
                                            File templateCotizacion = new File(path.concat(fileName));
                                            Uri pathUri = Uri.fromFile(templateCotizacion);
                                            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                                            pdfIntent.setDataAndType(pathUri, "application/pdf");
                                            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(pdfIntent);
                                        }
                                    }
                                });
                            }
                        }.start();
                    }
                });

                alert.show();

            }
        });

        facturar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
                String account = settings.getString(mActivity.getString(R.string.account_preference), "");
                if (account != null && account.length() > 0) {
                    if (comprobante.getEmisor().getRegimenFiscal().size() == 0) {
                        Intent intent = new Intent(mActivity, com.cfdimovil.app.views.Regimen.class);
                        mActivity.startActivity(intent);
                    } else if (comprobante.getReceptor().getRfc() == null || comprobante.getReceptor().getRfc().length() == 0) {
                        Toast.makeText(mActivity, "Selecciona un cliente.", Toast.LENGTH_LONG).show();
                    } else if (comprobante.getConceptos().getConcepto().size() == 0) {
                        Toast.makeText(mActivity, "Selecciona un articulo.", Toast.LENGTH_LONG).show();
                    } else if (comprobante.getTipoDeComprobante() == null || comprobante.getTipoDeComprobante().length() == 0) {
                        Toast.makeText(mActivity, "Tipo de comprobante invalido.", Toast.LENGTH_LONG).show();
                    } else {
                        factura();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
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
        emisor = (Comprobante.Emisor) database.getEmisor();
        comprobante.setEmisor(emisor);
        database.close();

        if(comprobante.getReceptor().getNombre().length()>0) {
            nombre.setText(comprobante.getReceptor().getNombre());
        } else {
            nombre.setText(comprobante.getReceptor().getRfc());
        }
        conceptosTitulo.setText(String.valueOf(comprobante.getConceptos().getConcepto().size())+" "+getString(R.string.conceptos));

        tipoDeComprobante.setText(comprobante.getTipoDeComprobante().toUpperCase());
        formaDePago.setText(comprobante.getFormaDePago());
        metodoDePago.setText(comprobante.getMetodoDePago());
        lugarDeExpedicion.setText(comprobante.getLugarExpedicion());
        numCtaPago.setText(comprobante.getNumCtaPago());
        moneda.setText(comprobante.getMoneda());
        tipoCambio.setText(comprobante.getTipoCambio());

        calculaTotales();
        if(comprobante.getSubTotal()!=null) {
            subtotal.setText("$"+String.valueOf(comprobante.getSubTotal()));
        }

        if(comprobante.getImpuestos()!=null){

            if(comprobante.getImpuestos().getTotalImpuestosTrasladados()!=null){
                trasladados.setText("$"+String.valueOf(comprobante.getImpuestos().getTotalImpuestosTrasladados()));
            }

            if(comprobante.getImpuestos().getTotalImpuestosRetenidos()!=null){
                retenidos.setText("-$"+String.valueOf(comprobante.getImpuestos().getTotalImpuestosRetenidos()));
            }

        }
        total.setText("$"+String.valueOf(comprobante.getTotal()));

    }

    private void calculaTotales() {
        if(comprobante.getImpuestos()!=null){
            BigDecimal totalImpuestosTrasladados = new BigDecimal(0);
            for(Comprobante.Impuestos.Traslados.Traslado traslado:comprobante.getImpuestos().getTraslados().getTraslado()){
                BigDecimal importe= comprobante.getSubTotal().multiply(
                        (traslado.getTasa().divide(new BigDecimal(100))));
                traslado.setImporte(importe);
                totalImpuestosTrasladados = totalImpuestosTrasladados.add(traslado.getImporte());
            }
            comprobante.getImpuestos().setTotalImpuestosTrasladados(totalImpuestosTrasladados);

            BigDecimal totalImpuestosRetenidos = new BigDecimal(0);
            for(Comprobante.Impuestos.Retenciones.Retencion retencion:comprobante.getImpuestos().getRetenciones().getRetencion()){
                totalImpuestosRetenidos = totalImpuestosRetenidos.add(retencion.getImporte());
            }
            comprobante.getImpuestos().setTotalImpuestosRetenidos(totalImpuestosRetenidos);
        }
        comprobante.setTotal(comprobante.getSubTotal().add(comprobante.getImpuestos().getTotalImpuestosTrasladados()).subtract(comprobante.getImpuestos().getTotalImpuestosRetenidos()));

        String estructura = gson.toJson(comprobante);

        database.openToWrite();
        database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
        database.close();
    }

    private void factura() {

        final OnTaskCompleted responseOAuth = new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(final String oauth) {

                String action= BuildConfig.ACCION_FACTURAR;
                Log.i(getString(R.string.app_name), "ACTION_FACTURACION: " + action);
                Intent intent = new Intent(action);
                intent.putExtra("password", password);
                intent.putExtra("fechaCotizacion", fechaCotizacion);
                getApplicationContext().sendBroadcast(intent);
                finish();

            }

            @Override
            public void onTaskCompletedFail(String mensaje) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
            }
        };

        if (isOnline()) {

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            LinearLayout layout = new LinearLayout(this);
            ViewGroup.LayoutParams params =  new ViewGroup.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(params);
            layout.setPadding(10,0,10,0);

            final EditText inputPass = new EditText(this);
            inputPass.setLayoutParams(params);
            inputPass.setHint("Contraseña de tus certificados");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(inputPass.getWindowToken(), 0);
            inputPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

            layout.addView(inputPass);

            alert.setTitle(getString(R.string.app_name));
            alert.setMessage(getString(R.string.password));
            alert.setView(layout);

            alert.setPositiveButton(getString(R.string.generar_factura), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogLabel, int whichButton) {
                    password = inputPass.getText().toString();
                    if(password.length()>0){
                        new OAuthCall(mActivity,responseOAuth).execute(null, null, null);
                    } else {
                        factura();
                    }
                }
            });

            alert.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogLabel, int whichButton) {

                }
            });

            alert.show();

        } else {
            Toast.makeText(this, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();

    }
}
