package com.cfdimovil.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cfdimovil.app.R;
import com.opencfdimovil.api.interfaces.OnTaskCompleted;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;

/**
 * Created by ruben_sandoval on 2/5/14.
 */

public class OAuthCall extends AsyncTask<Void, Integer, String> {

    private Activity activity;
    private Context context;
    private OnTaskCompleted listener;
    private String account;
    private String scope;

    private static final int PICK_ACCOUNT_ACCEPT_OAUTH=200;
    private static final int PICK_ERROR=300;

    public OAuthCall(Activity activity, OnTaskCompleted listener){

        this.listener=listener;
        this.activity=activity;
        this.context=activity.getApplicationContext();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        account=settings.getString(context.getString(R.string.account_preference), "");
        scope=context.getString(R.string.scope_constant);
    }

    public OAuthCall(Context context, OnTaskCompleted listener){

        this.listener=listener;
        this.context=context;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        account=settings.getString(context.getString(R.string.account_preference), "");
        scope=context.getString(R.string.scope_constant);
    }

    @Override
    protected void onPreExecute() {
    }

    protected String doInBackground(Void... urls) {
        String token = null;

        if(account!=null && account.length()>0){
            try {
                token = GoogleAuthUtil.getToken(context, account, scope);
                //token = GoogleAuthUtil.getToken(context, account, scope);
                Log.i(context.getString(R.string.app_name), "Token success");
                if(context!=null){
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(context.getString(R.string.permission_preference), true);
                    editor.commit();
                }
                return token;
            } catch (GooglePlayServicesAvailabilityException playEx) {

                if(activity!=null){
                    Log.e(context.getString(R.string.app_name), "GooglePlayServicesAvailabilityException "+playEx.toString());
                    Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                            playEx.getConnectionStatusCode(),
                            activity,
                            PICK_ERROR);
                    alert.show();
                }
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(context.getString(R.string.permission_preference), false);
                editor.commit();

            } catch (UserRecoverableAuthException userRecoverableException) {

                if(activity!=null){
                    Log.e(context.getString(R.string.app_name), "userRecoverableException "+userRecoverableException.toString());
                    activity.startActivityForResult(
                            userRecoverableException.getIntent(),
                            PICK_ACCOUNT_ACCEPT_OAUTH);
                }
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(context.getString(R.string.permission_preference), false);
                editor.commit();

            }catch (GoogleAuthException fatalException) {

                Log.e(context.getString(R.string.app_name), "GoogleAuthException "+fatalException.toString());
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(context.getString(R.string. permission_preference), false);
                editor.commit();

                if(activity!=null){
                    activity.finish();
                }

            }catch (IOException transientEx) {

                Log.e(context.getString(R.string.app_name), "IOException "+transientEx.toString());
            }
        }
        return token;
    }

    protected void onPostExecute(String token) {
        if((activity!=null || context!=null) && token!= null){
            listener.onTaskCompleted(token);
        } else if((activity!=null || context!=null) && token== null) {
            listener.onTaskCompletedFail("Usuario no encontrado");
        }
    }
}