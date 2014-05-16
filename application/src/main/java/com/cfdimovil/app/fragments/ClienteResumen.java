package com.cfdimovil.app.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.utils.Blur;
import com.cfdimovil.app.utils.RoundedImageView;
import com.cfdimovil.app.views.ErroresAppengine;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.opencfdimovil.api.interfaces.OnTaskCompleted;
import com.opencfdimovil.api.interfaces.UsuariosV1;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.utils.OAuthCall;
import com.cfdimovil.app.views.MainActivity;
import com.cfdimovil.app.views.proceso.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.models.AppengineErrorResponse;
import com.opencfdimovil.api.models.PeticionUsuario;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by ruben_sandoval on 2/19/14.
 */
public class ClienteResumen extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int PICK_CHOOSE_ACCOUNT=100;
    private static final int PICK_ACCOUNT_ACCEPT_OAUTH=200;
    private static final int RESULT_LOAD_IMAGE = 600;

    private TextView asignados;
    private TextView disponibles;
    private ImageView build;
    private RoundedImageView imagen;

    private UsuariosV1 service;
    private AccountManager accountManager;
    private SharedPreferences settings;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private DatabaseManager database;
    private RespuestaEstatusCuenta usuario;
    private ProgressDialog progressDialog;

    public static ClienteResumen newInstance(int sectionNumber) {
        ClienteResumen fragment = new ClienteResumen();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ClienteResumen() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        progressDialog = new ProgressDialog(getActivity(),R.style.alert);
        progressDialog.setTitle(getActivity().getString(R.string.app_name));
        progressDialog.setMessage(getActivity().getString(R.string.registrando));
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
            new OAuthCall(getActivity(), responseOAuth).execute(null, null, null);
        } else {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_resumen, container, false);


        database = new DatabaseManager(getActivity());
        database.openToRead();
        usuario = (RespuestaEstatusCuenta)database.getUsuario();
        Comprobante.Emisor emisor = (Comprobante.Emisor) database.getEmisor();
        int clientesCount = database.getReceptoresCount();
        int articulosCount = database.getConceptosCount();
        int cotizacionesCount = database.getCotizacionesCount();
        int facturasCount = database.getFacturasCount();
        database.close();

        accountManager = AccountManager.get(getActivity().getApplicationContext());
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String account=settings.getString(getActivity().getString(R.string.account_preference), "");

        Log.i(getActivity().getString(R.string.app_name),"Cuenta: "+account);
        Button inicio = (Button) rootView.findViewById(R.id.inicio);

        if(account!=null && account.length()>0){
            inicio.setVisibility(View.GONE);
        } else {
            inicio.setVisibility(View.VISIBLE);
            inicio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
                    startActivityForResult(googlePicker, PICK_CHOOSE_ACCOUNT);

                }
            });
        }

        TextView clientes = (TextView) rootView.findViewById(R.id.clientes);
        clientes.setText(clientesCount+" "+getActivity().getString(R.string.section_clientes));
        TextView articulos = (TextView) rootView.findViewById(R.id.articulos);
        articulos.setText(articulosCount+" "+getActivity().getString(R.string.section_articulos));
        TextView cotizaciones = (TextView) rootView.findViewById(R.id.cotizaciones);
        cotizaciones.setText(cotizacionesCount+" "+getActivity().getString(R.string.section_cotizaciones));
        TextView facturas = (TextView) rootView.findViewById(R.id.facturas);
        facturas.setText(facturasCount+" "+getActivity().getString(R.string.section_facturas));


        asignados = (TextView) rootView.findViewById(R.id.asignados);
        disponibles = (TextView) rootView.findViewById(R.id.disponibles);
        TextView razon = (TextView) rootView.findViewById(R.id.razon);
        razon.setText(emisor.getNombre());
        imagen = (RoundedImageView) rootView.findViewById(R.id.profile);
        build = (ImageView) rootView.findViewById(R.id.build);


        asignados.setText(String.valueOf(usuario.getTimbresAsignados().intValue()));
        disponibles.setText(String.valueOf(usuario.getTimbresDisponibles().intValue()));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap background;
                final String pictureData = settings.getString(getString(R.string.image_preference), "");
                if(pictureData!=null && pictureData.length()>0) {

                    byte[] b = Base64.decode(pictureData, Base64.DEFAULT);
                    background = BitmapFactory.decodeByteArray(b, 0, b.length);
                    imagen.setImageBitmap(background);

                } else {
                    background =  Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.build), 200, 120, false);
                }
                build.setImageBitmap(Blur.fastblur(background, 5));
            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String pictureData = settings.getString(getString(R.string.image_preference), "");

                if (pictureData == null || pictureData.length() == 0) {

                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);

                } else {

                    new Thread() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(getString(R.string.image_preference), "");
                            editor.commit();
                            final Bitmap background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.build), 200, 120, false);
                            final Bitmap blurBitmap = Blur.fastblur(background, 5);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imagen.setImageDrawable(getResources().getDrawable(R.drawable.cfdi_image));
                                    build.setImageBitmap(blurBitmap);
                                }
                            });
                        }
                    }.start();

                }
            }
        });

        Button facturar = (Button) rootView.findViewById(R.id.facturar);
        facturar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ResumenBorrador.class);
                getActivity().startActivity(intent);

            }
        });

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(120000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(120000, TimeUnit.MILLISECONDS);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://"+ BuildConfig.APPENGINE+".appspot.com/_ah/api")
                .setClient(new OkClient(client))
                .build();

        service = restAdapter.create(UsuariosV1.class);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_resumen, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_actualizar) {

            if(usuario.getRfc()!=null){
                update();
            } else {
                Toast.makeText(getActivity(),"Ingresa tu RFC y razon social en el campo de datos personales.",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == getActivity().RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Bitmap background;
                if(picturePath!=null && picturePath.length()>0) {
                    background = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picturePath), 200, 120, false);
                    imagen.setImageBitmap(background);
                    build.setImageBitmap(Blur.fastblur(background, 5));

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    background.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();

                    String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(getString(R.string.image_preference), encodedImage);
                    editor.commit();

                }
                }
            });


        }
        if (requestCode == PICK_ACCOUNT_ACCEPT_OAUTH && resultCode == getActivity().RESULT_OK) {

            Log.i(getString(R.string.app_name), "PICK_ACCOUNT_ACCEPT_OAUTH");
            PeticionUsuario usuario = new PeticionUsuario();
            usuario.setRfc(BuildConfig.RFC);
            usuario.setRazon(BuildConfig.RAZON);

            String registrationId = settings.getString("Google Cloud Messaging", "");
            usuario.setGcmKey(registrationId);
            register(usuario);
        }

        if (requestCode == PICK_CHOOSE_ACCOUNT && resultCode == getActivity().RESULT_OK) {
            Log.i(getString(R.string.app_name), "PICK_CHOOSE_ACCOUNT");

            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            Account cuenta=null;
            Account[] accounts = accountManager.getAccountsByType("com.google");
            for (Account a: accounts) {
                if (a.name.equals(accountName)) {
                    cuenta=a;
                }
            }

            if(cuenta != null){

                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getString(R.string.account_preference), cuenta.name);
                editor.commit();

                PeticionUsuario usuario = new PeticionUsuario();
                usuario.setRfc(BuildConfig.RFC);
                usuario.setRazon(BuildConfig.RFC);

                String registrationId = settings.getString("Google Cloud Messaging", "");
                usuario.setGcmKey(registrationId);
                usuario.setGcmKey(registrationId);

                register(usuario);

            } else {

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.error_email)
                        .setCancelable(false)
                        .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null,
                                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
                                startActivityForResult(googlePicker, PICK_CHOOSE_ACCOUNT);
                            }

                        })
                        .show();

            }
        }
    }

    private void register(final PeticionUsuario usuario) {

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(getString(R.string.rfc_preference), usuario.getRfc());
        editor.putString(getString(R.string.razon_preference), usuario.getRazon());
        editor.commit();

        progressDialog.setTitle(getActivity().getString(R.string.app_name));
        progressDialog.setMessage(getActivity().getString(R.string.registrando));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        OnTaskCompleted responseOAuth = new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(final String oauth) {

                String estructura = gson.toJson(usuario);
                Log.i(getString(R.string.app_name), "POST: " + estructura);

                service.registrar(usuario, "Bearer " + oauth, new Callback<RespuestaEstatusCuenta>() {

                    @Override
                    public void success(RespuestaEstatusCuenta usuario, Response response) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        saveResponse(usuario);
                    }

                    @Override
                    public void failure(RetrofitError cause) {
                        if (progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                        if (cause.getCause() instanceof SSLHandshakeException) {

                        } else if (cause.isNetworkError() || cause.getResponse()==null) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
                        } else {
                            int httpCode = cause.getResponse().getStatus();
                            Log.e(getActivity().getString(R.string.app_name), "HTTP Code: " + httpCode);

                            if (httpCode == 404) {

                                register(usuario);
                                Log.e(getActivity().getString(R.string.app_name), "Intentando de nuevo");

                            } else if (httpCode == 301 || httpCode == 302) {

                            } else if (httpCode == 401){
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_app_identidad), Toast.LENGTH_SHORT).show();
                                GoogleAuthUtil.invalidateToken(getActivity(),oauth);
                            } else {
                                Response response = cause.getResponse();
                                if (response != null) {

                                    AppengineErrorResponse errores = (AppengineErrorResponse) cause.getBodyAs(AppengineErrorResponse.class);
                                    Log.e(getActivity().getString(R.string.app_name), "Errores: " + gson.toJson(errores));
                                    Intent intent = new Intent(getActivity(), ErroresAppengine.class);
                                    intent.putExtra("AppengineErrores", gson.toJson(errores));
                                    intent.putExtra("Titulo", "Error registrando");
                                    intent.putExtra("Mensaje", "Cuando trato de ingresar al sistema me arroja el siguiente error:\n\n");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                    startActivity(intent);

                                } else {
                                    Log.e(getActivity().getString(R.string.app_name), "Error no conocido.");
                                }
                            }
                        }
                    }

                });

            }

            @Override
            public void onTaskCompletedFail(String mensaje) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
            }
        };

        if (isOnline()) {
            new OAuthCall(getActivity(), responseOAuth).execute(null, null, null);
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(getActivity(), getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
        }

    }

    private void update() {

        progressDialog.setTitle(getActivity().getString(R.string.app_name));
        progressDialog.setMessage(getActivity().getString(R.string.actualizando));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        OnTaskCompleted responseOAuth = new OnTaskCompleted() {

            @Override
            public void onTaskCompleted(final String oauth) {

                SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String registrationId = preferencias.getString("Google Cloud Messaging", "");

                Map<String, String> options = new ArrayMap<String, String>();
                Log.i(getActivity().getString(R.string.app_name), "GET: gcmKey=" + registrationId);
                options.put("gcmKey",registrationId);

                service.estatus(options,"Bearer " + oauth, new Callback<RespuestaEstatusCuenta>() {

                    @Override
                    public void success(RespuestaEstatusCuenta usuario, Response response) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        saveResponse(usuario);
                    }

                    @Override
                    public void failure(RetrofitError cause) {
                        if (progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                        if (cause.getCause() instanceof SSLHandshakeException) {

                        } else if (cause.isNetworkError() || cause.getResponse()==null) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
                        } else {
                            int httpCode = cause.getResponse().getStatus();

                            Log.e(getActivity().getString(R.string.app_name), "HTTP Code: " + httpCode);

                            if (httpCode == 404) {

                                update();
                                Log.e(getActivity().getString(R.string.app_name), "Intentando de nuevo");

                            } else if (httpCode == 301 || httpCode == 302) {

                            } else if (httpCode == 401){
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_app_identidad), Toast.LENGTH_SHORT).show();
                                GoogleAuthUtil.invalidateToken(getActivity(), oauth);
                            } else {
                                Response response = cause.getResponse();
                                if (response != null) {

                                    AppengineErrorResponse errores = (AppengineErrorResponse) cause.getBodyAs(AppengineErrorResponse.class);
                                    Log.e(getActivity().getString(R.string.app_name), "Errores: " + gson.toJson(errores));
                                    Intent intent = new Intent(getActivity(), ErroresAppengine.class);
                                    intent.putExtra("AppengineErrores", gson.toJson(errores));
                                    intent.putExtra("Titulo", "Error obteniendo el cliente");
                                    intent.putExtra("Mensaje", "Cuando trato de actualizar mis datos el sistema muestra el siguiente error:\n\n");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                    startActivity(intent);

                                } else {
                                    Log.e(getActivity().getString(R.string.app_name), "Error no conocido.");
                                }
                            }
                        }
                    }

                });

            }

            @Override
            public void onTaskCompletedFail(String mensaje) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_cuenta), Toast.LENGTH_SHORT).show();
            }
        };

        if (isOnline()) {
            new OAuthCall(getActivity(), responseOAuth).execute(null, null, null);
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(getActivity(), getActivity().getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();

    }

    private void saveResponse(RespuestaEstatusCuenta usuarioRespuesta){

        database.openToRead();
        usuario = (RespuestaEstatusCuenta)database.getUsuario();
        database.close();

        usuario.setRfc(BuildConfig.RFC);
        usuario.setRazon(BuildConfig.RAZON);

        usuario.setTimbresAsignados(usuarioRespuesta.getTimbresAsignados());
        usuario.setTimbresDisponibles(usuarioRespuesta.getTimbresDisponibles());
        usuario.setCodigo(usuarioRespuesta.getCodigo());

        asignados.setText(String.valueOf(usuario.getTimbresAsignados().intValue()));
        disponibles.setText(String.valueOf(usuario.getTimbresDisponibles().intValue()));

        String estructura = gson.toJson(usuario);

        database.openToRead();
        database.updateUsuario(estructura);
        database.close();

        Toast.makeText(getActivity(), getActivity().getString(R.string.actualizacion_folios_success), Toast.LENGTH_LONG).show();
    }

}