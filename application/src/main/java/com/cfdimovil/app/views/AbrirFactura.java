package com.cfdimovil.app.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.utils.OAuthCall;
import com.cfdimovil.app.utils.PDFUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.interfaces.FacturasV1;
import com.opencfdimovil.api.interfaces.OnTaskCompleted;
import com.opencfdimovil.api.models.AppengineErrorResponse;
import com.opencfdimovil.api.models.RespuestaCancela;
import com.opencfdimovil.api.models.RespuestaComprobante;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class AbrirFactura extends Activity {

    private String UUID;
    private String path;

    private Activity mActivity;
    private ProgressDialog progressDialog;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private DatabaseManager database;
    private RespuestaComprobante factura;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_abrir_factura);

        Button pdf = (Button) findViewById(R.id.pdf);
        Button enviar = (Button) findViewById(R.id.enviar);
        Button cancelar = (Button) findViewById(R.id.cancelar);
        mActivity=this;

        database = new DatabaseManager(getApplicationContext());


        progressDialog = new ProgressDialog(this,R.style.alert);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage(getString(R.string.procesado_factura));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        OnTaskCompleted responseOAuth = new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String oauth) {
            }

            @Override
            public void onTaskCompletedFail(String mensaje) {

            }
        };
        if (isOnline()) {
            new OAuthCall(this, responseOAuth).execute(null, null, null);
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            UUID = bundle.getString("UUID");
            database.openToRead();
            factura = (RespuestaComprobante) database.getFacturaCampo("UUID",UUID);
            database.close();

            TextView rfc = (TextView) findViewById(R.id.rfc);
            rfc.setText(factura.getComprobanteTimbrado().getReceptor().getNombre());

            TextView monto = (TextView) findViewById(R.id.monto);
            monto.setText("$"+String.valueOf(factura.getComprobanteTimbrado().getTotal()));

            TextView uuid = (TextView) findViewById(R.id.uuid);
            uuid.setText(UUID);

            if(factura.getValid()){
                ((TextView)findViewById(R.id.valid)).setText("ACTIVA");
                ((TextView)findViewById(R.id.valid)).setTextColor(this.getResources().getColor(R.color.green_card_inactive));
            } else {
                ((TextView)findViewById(R.id.valid)).setText("CANCELADA");
                ((TextView)findViewById(R.id.valid)).setTextColor(this.getResources().getColor(R.color.red_card_inactive));

                ((LinearLayout)findViewById(R.id.container)).setVisibility(View.GONE);
            }

            path = Environment.getExternalStorageDirectory()+ "/CFDIMovil/"+factura.getComprobanteTimbrado().getReceptor().getRfc()+"/facturas/";


            progressDialog.show();
            new Thread() {
                @Override
                public void run() {
                    String fileName = factura.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID()+".xml";
                     PDFUtils.outputToFile(mActivity, path, fileName, factura.getComprobanteXMLTimbrado(), "UTF-8");

                    String pdfcontent = PDFUtils.generaFacturaPDF(mActivity, factura);
                    fileName = factura.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID()+".pdf";
                    PDFUtils.outputToFile(mActivity, path, fileName, pdfcontent, "ISO-8859-1");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog.isShowing()) {
                                progressDialog.cancel();
                            }
                        }
                    });
                }
            }.start();


        }

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String fileName = factura.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID() + ".pdf";
                File templateCotizacion = new File(path.concat(fileName));
                Uri pathUri = Uri.fromFile(templateCotizacion);
                Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(pathUri, "application/pdf");
                pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(Intent.createChooser(pdfIntent, "PDF"));

            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String xmlFileName = factura.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID() + ".xml";
                File xmlFile = new File(path.concat(xmlFileName));
                Uri pathXml = Uri.parse("file://" + xmlFile);

                String pdfFileName = factura.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID() + ".pdf";
                File pdfFile = new File(path.concat(pdfFileName));
                Uri pathPdf = Uri.parse("file://" + pdfFile);

                ArrayList<Uri> uriList = new ArrayList<Uri>();
                uriList.add(pathXml);
                uriList.add(pathPdf);


                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("text/plain");
                //emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,  new String[]{""} );
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Factura: " + UUID);
                //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Factura: "+UUID);

                startActivity(emailIntent);

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startOauth();

            }
        });
    }

    private void startOauth() {

        final OnTaskCompleted responseOAuth = new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(final String oauth) {
                progressDialog.show();
                retrofitCall(oauth);
            }

            @Override
            public void onTaskCompletedFail(String mensaje) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
            }
        };

        if (isOnline()) {

            new OAuthCall(mActivity, responseOAuth).execute(null, null, null);

        } else {
            Toast.makeText(mActivity, getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();

    }

    private void retrofitCall( final String oauth) {

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(120000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(120000, TimeUnit.MILLISECONDS);
        client.setResponseCache(null);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://"+ BuildConfig.APPENGINE+".appspot.com/_ah/api")
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(gson))
                .build();

        Log.i(mActivity.getString(R.string.app_name), "CANCELA: " + UUID);
        FacturasV1 service = restAdapter.create(FacturasV1.class);
        service.cancela(UUID, "Bearer " + oauth, new Callback<RespuestaCancela>() {

            @Override
            public void success(RespuestaCancela cancelacion, Response response) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                factura.setValid(false);

                String estructura = gson.toJson(factura);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String fechaFactura = sdf.format(factura.getComprobanteTimbrado().getFecha());

                factura.setValid(cancelacion.isCancelado());
                String respuestaCancela = gson.toJson(factura);
                Log.i(mActivity.getString(R.string.app_name), "RespuestaCancela: " + respuestaCancela);
                database.openToWrite();
                database.updateFactura(fechaFactura,
                        factura.getComprobanteTimbrado().getReceptor().getRfc(),
                        UUID,
                        estructura);
                database.close();
            }

            @Override
            public void failure(RetrofitError cause) {

                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                if (cause.getCause() instanceof SSLHandshakeException) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
                } else if (cause.isNetworkError() || cause.getResponse()==null) {
                    Toast.makeText(mActivity, mActivity.getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
                } else {
                    int httpCode = cause.getResponse().getStatus();
                    Log.e(mActivity.getString(R.string.app_name), "HTTP Code: " + httpCode);

                    if (httpCode == 404) {

                        startOauth();
                        Log.e(mActivity.getString(R.string.app_name), "Intentando de nuevo");

                    } else if (httpCode == 301 || httpCode == 302) {

                    } else if (httpCode == 401){
                        Toast.makeText(mActivity, mActivity.getString(R.string.error_app_identidad), Toast.LENGTH_SHORT).show();
                        GoogleAuthUtil.invalidateToken(mActivity, oauth);
                    } else {
                        Response response = cause.getResponse();
                        if (response != null) {

                            AppengineErrorResponse errores = (AppengineErrorResponse) cause.getBodyAs(AppengineErrorResponse.class);
                            Log.e(mActivity.getString(R.string.app_name), "Errores: " + gson.toJson(errores));
                            Intent intent = new Intent(mActivity, ErroresAppengine.class);
                            intent.putExtra("AppengineErrores", gson.toJson(errores));
                            intent.putExtra("Titulo", "Error cancelando");
                            intent.putExtra("Mensaje", "Cuando trato de cancelar mi factura, no es cancelada por el siguiente error:\n\n");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            startActivity(intent);

                        } else {
                            Log.e(mActivity.getString(R.string.app_name), "Error no conocido.");
                        }
                    }
                }
            }

        });

    }

}
