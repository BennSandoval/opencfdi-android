package com.cfdimovil.app.receivers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.cfdimovil.app.services.GcmIntentService;

/**
 * Created by ruben_sandoval on 5/4/14.
 */
public class Notificacion extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

    }
}
