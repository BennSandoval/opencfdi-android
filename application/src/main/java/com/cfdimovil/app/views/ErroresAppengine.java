package com.cfdimovil.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cfdimovil.app.R;
import com.cfdimovil.app.adapter.ObjectAdapter;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.models.AppengineErrorResponse;

import java.util.Date;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class ErroresAppengine extends ActionBarActivity {

    private String titulo;
    private String mensaje;

    private ListView lista;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private AppengineErrorResponse errores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_lista);

        errores = null;
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            String estructura = (String) bundle.get("AppengineErrores");
            errores = gson.fromJson(estructura, AppengineErrorResponse.class);
            mensaje = (String) bundle.get("Mensaje");
            titulo = (String) bundle.get("Titulo");
            if(titulo!=null && titulo.length()>0){
                setTitle(titulo);
            }
        }
        Toast.makeText(this, getString(R.string.ayuda), Toast.LENGTH_LONG).show();
        lista = (ListView) findViewById(R.id.lista);
        ObjectAdapter conceptoAdapter = new ObjectAdapter(this,R.layout.item_card_error_appengine, errores.getError().getErrors());
        lista.setAdapter(conceptoAdapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"ruben.sandoval@cfdimovil.com.mx"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, titulo);
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mensaje+errores.getError().getErrors().get(position).getMessage());
                startActivity(emailIntent);

            }

        });


    }
}
