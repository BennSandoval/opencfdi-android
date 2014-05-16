package com.cfdimovil.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.cfdimovil.app.R;
import com.cfdimovil.app.views.MainActivity;

/**
 * Created by ruben_sandoval on 5/3/14.
 */
public class Notification {

    public static void generate(Context context,String title, String message, String UUID){

        Bitmap iconBitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        long[] vibrate = {0,100,200,300};

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("UUID",UUID);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);//Cancel create new aCtivity

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentIntent(intent)
                        .setSmallIcon(icon)
                        .setLargeIcon(iconBitmap)
                        .setWhen(when)
                        .setAutoCancel(true)
                        .setLights(0xff00ff00, 300, 1000)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setTicker(context.getString(R.string.app_name))
                        .setVibrate(vibrate);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        android.app.Notification n = mBuilder.build();

        nm.notify(0, n);
    }


}
