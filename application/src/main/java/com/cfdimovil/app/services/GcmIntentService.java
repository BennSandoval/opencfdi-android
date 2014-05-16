package com.cfdimovil.app.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cfdimovil.app.R;
import com.cfdimovil.app.views.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Date;

/**
 * Created by ruben_sandoval on 5/4/14.
 */
public class GcmIntentService extends IntentService {

    private NotificationManager mNotificationManager;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
        String messageType = googleCloudMessaging.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!(extras != null && extras.isEmpty())) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e(this.getString(R.string.app_name), "Erro al enviar: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.e(this.getString(R.string.app_name), "Mensaje borrado en el servidor: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(this.getString(R.string.app_name), "Recibido: " + extras.getString("Mensaje"));
                procesaNotificacion(extras.getString("Mensaje"));
            }
        }
    }

    private void procesaNotificacion(String msg) {

        int NOTIFICATION_ID= (int) (new Date().getTime()/1000);

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = {0,100,200,300};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg).
                        setVibrate(vibrate)
                        .setSound(soundUri);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }
}
