package com.cfdimovil.app.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.opencfdimovil.sat.cfdi.v32.schema.ObjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ruben_sandoval on 4/24/14.
 */
public class Regimen extends Activity {

    private int id;
    private boolean update=false;

    private AutoCompleteTextView regimen;
    private Spinner regimenes;
    private List<String> regimenEmisor = new ArrayList<String>();
    private String[] array;
    private ArrayAdapter<String> regimenesArrayAdapter;
    private TextView title;

    private Button eliminar;
    private Button guardar;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();
    private Comprobante.Emisor emisor;
    private DatabaseManager database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_regimen);

        database = new DatabaseManager(getApplicationContext());
        database.openToRead();
        emisor = (Comprobante.Emisor)database.getEmisor();
        database.close();

        title = (TextView) findViewById(R.id.title);
        regimenes = (Spinner) findViewById(R.id.regimenes);

        title.setText(emisor.getRegimenFiscal().size()+" Regimenes");
        regimenEmisor.clear();
        regimenEmisor.add("Ingresa nuevo regimen");
        for (Comprobante.Emisor.RegimenFiscal regimenUnitario : emisor.getRegimenFiscal()) {
            regimenEmisor.add(regimenUnitario.getRegimen());
        }
        array = regimenEmisor.toArray(new String[regimenEmisor.size()]);
        regimenesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,array);
        regimenesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regimenes.setAdapter(regimenesArrayAdapter);
        regimenes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!=0){
                    id = position-1;
                    regimen.setText(emisor.getRegimenFiscal().get(id).getRegimen());

                    LinearLayout divider = (LinearLayout) findViewById(R.id.container);
                    divider.setVisibility(View.VISIBLE);
                    guardar.setText("Modificar");
                    update=true;
                } else {
                    update=false;
                    guardar.setText("Guardar");
                    regimen.setText("");

                    LinearLayout container = (LinearLayout) findViewById(R.id.container);
                    container.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        regimen = (AutoCompleteTextView) findViewById(R.id.regimen);
        ArrayAdapter<String> regimenArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drop_down, getResources().getStringArray(R.array.regimen_array));
        regimen.setAdapter(regimenArrayAdapter);

        eliminar = (Button) findViewById(R.id.eliminar);
        guardar = (Button) findViewById(R.id.guardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(regimen.getText().toString().length()>0){
                    if(update) {
                        emisor.getRegimenFiscal().get(id).setRegimen(regimen.getText().toString());
                    } else {
                        ObjectFactory of = new ObjectFactory();
                        Comprobante.Emisor.RegimenFiscal nuevo = of.createComprobanteEmisorRegimenFiscal();
                        nuevo.setRegimen(regimen.getText().toString().toUpperCase());
                        emisor.getRegimenFiscal().add(nuevo);
                    }

                    String estructura = gson.toJson(emisor);

                    database.openToWrite();
                    database.updateEmisor(estructura);
                    database.close();
                    setRegimenes(false);

                    if(update){
                        Toast.makeText(getApplicationContext(), "Regimen actualizado.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Regimen guardado.",Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(update) {
                    emisor.getRegimenFiscal().remove(id);

                    String estructura = gson.toJson(emisor);

                    database.openToRead();
                    database.updateEmisor(estructura);
                    database.close();
                }
                setRegimenes(true);
            }
        });

    }

    private void setRegimenes(boolean delete) {

        if(delete || !update){
            title.setText(emisor.getRegimenFiscal().size()+" Regimenes");
            regimenEmisor.clear();
            regimenEmisor.add("Ingresa nuevo regimen");
            for (Comprobante.Emisor.RegimenFiscal regimenUnitario : emisor.getRegimenFiscal()) {
                regimenEmisor.add(regimenUnitario.getRegimen());
            }
            array = regimenEmisor.toArray(new String[regimenEmisor.size()]);
            regimenesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,array);
            regimenesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            regimenes.setAdapter(regimenesArrayAdapter);

            LinearLayout container = (LinearLayout) findViewById(R.id.container);
            container.setVisibility(View.GONE);
        } else {
            array[id+1]=emisor.getRegimenFiscal().get(id).getRegimen();
        }

        regimenesArrayAdapter.notifyDataSetChanged();
    }
}
