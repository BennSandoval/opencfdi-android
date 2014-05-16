package com.cfdimovil.app.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.utils.OAuthCall;
import com.cfdimovil.app.views.ErroresAppengine;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.interfaces.FacturasV1;
import com.opencfdimovil.api.interfaces.OnTaskCompleted;
import com.opencfdimovil.api.models.AppengineErrorResponse;
import com.opencfdimovil.api.models.PeticionTimbrado;
import com.opencfdimovil.api.models.RespuestaComprobante;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.squareup.okhttp.OkHttpClient;

import java.text.SimpleDateFormat;
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
 * Created by ruben_sandoval on 5/4/14.
 */
public class ProcesoFacturar extends WakefulBroadcastReceiver {

    private int intentos=0;
    private int NOTIFICATION_ID;
    private boolean finalizado=false;

    private Context context;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").
            registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).
            create();

    private DatabaseManager database;
    private Comprobante comprobante;
    private RespuestaEstatusCuenta usuarioDatabase;
    private  Comprobante.Emisor emisor;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        String fechaCotizacion = intent.getStringExtra("fechaCotizacion");

        database = new DatabaseManager(context);
        database.openToRead();
        comprobante = (Comprobante) database.getCotizacionCampo("fecha",fechaCotizacion);
        usuarioDatabase = (RespuestaEstatusCuenta) database.getUsuario();
        emisor = (Comprobante.Emisor) database.getEmisor();
        database.close();

        NOTIFICATION_ID= (int) (comprobante.getFecha().getTime()/1000);
        Log.i(context.getString(R.string.app_name), "Iniciando el proceso de factuarcion de la cotizacion " + fechaCotizacion);
        startOauth();
    }

    private void startOauth() {

        final OnTaskCompleted responseOAuth = new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(final String oauth) {

                final PeticionTimbrado peticion = new PeticionTimbrado();
                comprobante.setVersion("3.2");
                comprobante.setEmisor(emisor);
                comprobante.setFecha(new Date());
                peticion.setComprobante(comprobante);

                String estructura = gson.toJson(peticion.getComprobante());
                Log.i(context.getString(R.string.app_name), "Facturar POST: " + estructura);

                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                retrofitCall(peticion,oauth);
                            }
                        }
                ).start();

            }

            @Override
            public void onTaskCompletedFail(String mensaje) {
                Toast.makeText(context, context.getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
            }
        };

        if (isOnline()) {

            new OAuthCall(context, responseOAuth).execute(null, null, null);

        } else {
            Toast.makeText(context, context.getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();

    }

    private void retrofitCall(PeticionTimbrado peticion, final String oauth) {

        finalizado = false;
        intentos++;

        final NotificationManager mNotifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final NotificationCompat.Builder mBuilderFirst = new NotificationCompat.Builder(context);
        mBuilderFirst.setContentTitle(context.getString(R.string.app_name))
                .setContentText("Facturando")
                .setAutoCancel(false)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_notification);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilderFirst.build());

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.app_name))
                .setContentText("Facturando")
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_notification);

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(120000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(120000, TimeUnit.MILLISECONDS);
        client.setResponseCache(null);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://"+ BuildConfig.APPENGINE+".appspot.com/_ah/api")
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(gson))
                .build();

        FacturasV1 service = restAdapter.create(FacturasV1.class);
        service.timbrar(peticion, "Bearer " + oauth, new Callback<RespuestaComprobante>() {

            @Override
            public void success(RespuestaComprobante comprobanteTimbrado, Response response) {

                finalizado = true;
                String estructura = gson.toJson(comprobanteTimbrado);

                String UUID = comprobanteTimbrado.getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String fechaFactura = sdf.format(comprobanteTimbrado.getComprobanteTimbrado().getFecha());

                String estructuraComprobante = gson.toJson(comprobanteTimbrado.getComprobante());
                Log.i(context.getString(R.string.app_name), "Fecha "+fechaFactura+" RespuestaComprobante: " + estructuraComprobante);

                database.openToWrite();
                database.saveFactura(fechaFactura,
                        comprobanteTimbrado.getComprobanteTimbrado().getReceptor().getRfc(),
                        UUID,
                        estructura);
                database.close();

                /*
                Intent notificationIntent = new Intent(context, AbrirFactura.class);
                notificationIntent.putExtra("UUID",UUID);
                notificationIntent.putExtra("fecha",fechaFactura);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                long[] vibrate = {0,100,200,300};
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                mBuilder.setContentText("Factura completada: " + UUID)
                        .setProgress(0, 0, false)
                        .setContentIntent(intent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Factura completada: " + UUID))
                        .setTicker(context.getString(R.string.app_name))
                        .setVibrate(vibrate)
                        .setSound(soundUri);

                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                */
                mNotifyManager.cancel(NOTIFICATION_ID);
            }

            @Override
            public void failure(RetrofitError cause) {

                finalizado = true;
                if (cause.getCause() instanceof SSLHandshakeException) {
                    Toast.makeText(context, context.getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
                } else if (cause.isNetworkError() || cause.getResponse()==null) {

                    mBuilder.setContentText(context.getString(R.string.error_conexion))
                            .setAutoCancel(true)
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                    Toast.makeText(context, context.getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();

                } else {

                    int httpCode = cause.getResponse().getStatus();
                    Log.e(context.getString(R.string.app_name), "HTTP Code: " + httpCode);

                    if (httpCode == 404) {

                        startOauth();
                        Log.e(context.getString(R.string.app_name), "Intentando de nuevo");

                    } else if (httpCode == 301 || httpCode == 302) {
                        mBuilder.setContentText(context.getString(R.string.error_desconocido))
                                .setAutoCancel(true)
                                .setProgress(0, 0, false);
                        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                    } else if (httpCode == 401){

                        mBuilder.setContentText(context.getString(R.string.error_email_identidad))
                                .setAutoCancel(true)
                                .setProgress(0, 0, false);
                        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                        Toast.makeText(context, context.getString(R.string.error_app_identidad), Toast.LENGTH_SHORT).show();
                        GoogleAuthUtil.invalidateToken(context, oauth);

                    } else {
                        Response response = cause.getResponse();
                        if (response != null) {

                            AppengineErrorResponse errores = (AppengineErrorResponse) cause.getBodyAs(AppengineErrorResponse.class);
                            Intent notificationIntent = new Intent(context, ErroresAppengine.class);
                            notificationIntent.putExtra("AppengineErrores",gson.toJson(errores));
                            notificationIntent.putExtra("Titulo","Error facturando");
                            notificationIntent.putExtra("Mensaje","Cuando trato de realizar mi factura, no es generada por el siguiente error:\n\n");
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            long[] vibrate = {0,100,200,300};
                            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                            mBuilder.setContentText("Error facturando la cotizacion")
                                    .setProgress(0, 0, false)
                                    .setContentIntent(intent)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText("Error facturando la cotizacion"))
                                    .setTicker(context.getString(R.string.app_name))
                                    .setVibrate(vibrate)
                                    .setSound(soundUri);

                            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                        } else {

                            if(intentos<3){
                                startOauth();
                                mBuilder.setProgress(100, 0, false);
                            } else {
                                mBuilder.setContentText(context.getString(R.string.error_desconocido))
                                        .setProgress(0, 0, false);
                            }
                            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                            Log.e(context.getString(R.string.app_name), "Error no conocido.");
                        }
                    }
                }
            }

        });

        int incr;
        for (incr = 0; incr <= 90; incr+=10) {
            if(!finalizado) {
                mBuilder.setProgress(100, incr, false);
                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }

    }
}