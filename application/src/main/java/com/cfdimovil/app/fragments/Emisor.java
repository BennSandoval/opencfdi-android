package com.cfdimovil.app.fragments;

/**
 * Created by ruben_sandoval on 2/3/14.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.views.MainActivity;
import com.cfdimovil.app.R;
import com.cfdimovil.app.managers.DatabaseManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class Emisor <T extends Object> extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private DatabaseManager database;
    private View rootView;
    private Comprobante.Emisor emisor;
    private List<String> regimenEmisor = new ArrayList<String>();

    private TextView rfcField;
    private TextView nombreField;
    private TextView domicilioCalleField;
    private TextView domicilioNoExteriorField;
    private TextView domicilioNoInteriorField;
    private TextView domicilioCodigoPostalField;
    private TextView domicilioColoniaField;
    private TextView domicilioLocalidadField;
    private TextView domicilioMunicipioField;
    private TextView domicilioReferenciaField;
    private AutoCompleteTextView domicilioEstadoField;
    private TextView domicilioPaisField;

    private Button agregar;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static Emisor newInstance(int sectionNumber) {
        Emisor fragment = new Emisor();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }

    public Emisor() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_emisor, container, false);

        agregar = (Button) rootView.findViewById(R.id.agregarRegimen);
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Regimen.class);
                getActivity().startActivity(intent);
            }
        });

        rfcField = (TextView) rootView.findViewById(R.id.rfc);
        nombreField = (TextView) rootView.findViewById(R.id.nombre);
        domicilioCalleField = (TextView) rootView.findViewById(R.id.domicilio_calle);
        domicilioNoExteriorField = (TextView) rootView.findViewById(R.id.domicilio_noExterior);
        domicilioNoInteriorField = (TextView) rootView.findViewById(R.id.domicilio_noInterior);
        domicilioCodigoPostalField = (TextView) rootView.findViewById(R.id.domicilio_codigoPostal);
        domicilioColoniaField = (TextView) rootView.findViewById(R.id.domicilio_colonia);
        domicilioLocalidadField = (TextView) rootView.findViewById(R.id.domicilio_localidad);
        domicilioMunicipioField = (TextView) rootView.findViewById(R.id.domicilio_municipio);
        domicilioReferenciaField = (TextView) rootView.findViewById(R.id.domicilio_referencia);
        domicilioEstadoField = (AutoCompleteTextView) rootView.findViewById(R.id.domicilio_estado);
        domicilioPaisField = (TextView) rootView.findViewById(R.id.domicilio_pais);

        ArrayAdapter<String> estadosArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_drop_down, getResources().getStringArray(R.array.estados_array));
        domicilioEstadoField.setAdapter(estadosArrayAdapter);

        database = new DatabaseManager(getActivity());
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
        inflater.inflate(R.menu.menu_guardar, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_guardar) {
            guardarFunction();
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.guardado), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void guardarFunction() {

        emisor.setRfc(rfcField.getText().toString());
        emisor.setNombre(nombreField.getText().toString());

        if(domicilioCalleField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setCalle(domicilioCalleField.getText().toString());
        else
            emisor.getDomicilioFiscal().setCalle(null);
        if(domicilioNoExteriorField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setNoExterior(domicilioNoExteriorField.getText().toString());
        else
            emisor.getDomicilioFiscal().setNoExterior(null);
        if(domicilioNoInteriorField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setNoInterior(domicilioNoInteriorField.getText().toString());
        else
            emisor.getDomicilioFiscal().setNoInterior(null);
        if(domicilioCodigoPostalField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setCodigoPostal(domicilioCodigoPostalField.getText().toString());
        else
            emisor.getDomicilioFiscal().setCodigoPostal(null);
        if(domicilioColoniaField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setColonia(domicilioColoniaField.getText().toString());
        else
            emisor.getDomicilioFiscal().setColonia(null);
        if(domicilioLocalidadField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setLocalidad(domicilioLocalidadField.getText().toString());
        else
            emisor.getDomicilioFiscal().setLocalidad(null);
        if(domicilioMunicipioField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setMunicipio(domicilioMunicipioField.getText().toString());
        else
            emisor.getDomicilioFiscal().setMunicipio(null);
        if(domicilioReferenciaField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setReferencia(domicilioReferenciaField.getText().toString());
        else
            emisor.getDomicilioFiscal().setReferencia(null);
        if(domicilioEstadoField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setEstado(domicilioEstadoField.getText().toString());
        else
            emisor.getDomicilioFiscal().setEstado(null);
        if(domicilioPaisField.getText().toString().length()>0)
            emisor.getDomicilioFiscal().setPais(domicilioPaisField.getText().toString());
        else
            emisor.getDomicilioFiscal().setPais(null);

        String estructura = gson.toJson(emisor);
        Log.i("CFDIMovil",estructura);

        database.openToWrite();
        database.updateEmisor(estructura);
        database.close();
    }


    private void updateView(){

        database.openToRead();
        emisor = (Comprobante.Emisor)database.getEmisor();
        database.close();

        rfcField.setText(emisor.getRfc());
        nombreField.setText(emisor.getNombre());
        if(emisor.getRegimenFiscal().size()>0){
            agregar.setText("EDITAR MIS "+emisor.getRegimenFiscal().size()+" REGIMENES");
        } else {
            agregar.setText(getActivity().getString(R.string.agregar_regimen));
        }

        if(emisor.getDomicilioFiscal()!=null) {
            if (emisor.getDomicilioFiscal().getCalle() != null)
                domicilioCalleField.setText(emisor.getDomicilioFiscal().getCalle());
            if (emisor.getDomicilioFiscal().getNoExterior() != null)
                domicilioNoExteriorField.setText(String.valueOf(emisor.getDomicilioFiscal().getNoExterior()));
            if (emisor.getDomicilioFiscal().getNoInterior() != null)
                domicilioNoInteriorField.setText(String.valueOf(emisor.getDomicilioFiscal().getNoInterior()));
            if (emisor.getDomicilioFiscal().getCodigoPostal() != null)
                domicilioCodigoPostalField.setText(String.valueOf(emisor.getDomicilioFiscal().getCodigoPostal()));
            if (emisor.getDomicilioFiscal().getColonia() != null)
                domicilioColoniaField.setText(String.valueOf(emisor.getDomicilioFiscal().getColonia()));
            if (emisor.getDomicilioFiscal().getLocalidad() != null)
                domicilioLocalidadField.setText(String.valueOf(emisor.getDomicilioFiscal().getLocalidad()));
            if (emisor.getDomicilioFiscal().getMunicipio() != null)
                domicilioMunicipioField.setText(String.valueOf(emisor.getDomicilioFiscal().getMunicipio()));
            if (emisor.getDomicilioFiscal().getReferencia() != null)
                domicilioReferenciaField.setText(String.valueOf(emisor.getDomicilioFiscal().getReferencia()));
            if (emisor.getDomicilioFiscal().getEstado() != null)
                domicilioEstadoField.setText(String.valueOf(emisor.getDomicilioFiscal().getEstado()));
            if (emisor.getDomicilioFiscal().getPais() != null)
                domicilioPaisField.setText(String.valueOf(emisor.getDomicilioFiscal().getPais()));
        }
    }
}