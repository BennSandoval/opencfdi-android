package com.cfdimovil.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cfdimovil.app.R;
import com.cfdimovil.app.adapter.ObjectAdapter;
import com.cfdimovil.app.managers.DatabaseManager;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.cfdimovil.app.views.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ruben_sandoval on 2/8/14.
 */
public class Receptores extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_COTIZACION = "fechaCotizacion";
    private List<Comprobante.Receptor> receptores = new ArrayList<Comprobante.Receptor>();

    private ListView lista;
    private ObjectAdapter receptorAdapter;
    private DatabaseManager database;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).serializeNulls().create();

    public static Receptores newInstance(int sectionNumber, String fechaCotizacion) {
        Receptores fragment = new Receptores();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_COTIZACION, fechaCotizacion);
        fragment.setArguments(args);
        return fragment;
    }

    public static Receptores newInstance(int sectionNumber) {
        Receptores fragment = new Receptores();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Receptores() {
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        database = new DatabaseManager(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista, container, false);

        lista = (ListView) rootView.findViewById(R.id.lista);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String fechaCotizacion =(String)getArguments().get(ARG_COTIZACION);
                if(fechaCotizacion!=null && fechaCotizacion.length()>0) {
                    database.openToWrite();
                    Comprobante comprobante = (Comprobante) database.getCotizacionCampo("fecha", fechaCotizacion);
                    comprobante.setReceptor(receptores.get(position));
                    String estructura = gson.toJson(comprobante);

                    database.updateFactura(fechaCotizacion, comprobante.getReceptor().getRfc(), "", estructura);
                    database.close();
                    getActivity().finish();

                } else {
                    Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Receptor.class);
                    intent.putExtra("rfc", receptores.get(position).getRfc());
                    getActivity().startActivity(intent);
                }

            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        String fechaCotizacion=(String)getArguments().get(ARG_COTIZACION);
        if(fechaCotizacion==null || fechaCotizacion.length()==0){
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private void updateView(){

        database.openToRead();
        receptores = database.getReceptores();
        database.close();

        receptorAdapter = new ObjectAdapter(getActivity().getApplicationContext(),R.layout.item_card_receptor, receptores);
        lista.setAdapter(receptorAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_receptores, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {

            Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Receptor.class);
            getActivity().startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
