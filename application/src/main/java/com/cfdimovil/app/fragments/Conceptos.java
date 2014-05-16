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
import com.cfdimovil.app.views.MainActivity;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruben_sandoval on 2/11/14.
 */
public class Conceptos extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_COTIZACION = "fechaCotizacion";
    private List<Comprobante.Conceptos.Concepto> conceptos =new ArrayList<Comprobante.Conceptos.Concepto>();

    private ListView lista;
    private ObjectAdapter conceptoAdapter;
    private DatabaseManager database;

    public static Conceptos newInstance(int sectionNumber, String fechaCotizacion) {
        Conceptos fragment = new Conceptos();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_COTIZACION, fechaCotizacion);
        fragment.setArguments(args);
        return fragment;
    }

    public static Conceptos newInstance(int sectionNumber) {
        Conceptos fragment = new Conceptos();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Conceptos() {
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

                    Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Concepto.class);
                    intent.putExtra("fechaCotizacion", fechaCotizacion);
                    intent.putExtra("noIdentificacion", conceptos.get(position).getNoIdentificacion());
                    startActivity(intent);
                    getActivity().finish();

                } else {
                    Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Concepto.class);
                    intent.putExtra("noIdentificacion", conceptos.get(position).getNoIdentificacion());
                    getActivity().startActivity(intent);
                }
            }
        });

        database = new DatabaseManager(getActivity().getApplicationContext());

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
        conceptos = database.getConceptos();
        database.close();

        conceptoAdapter = new ObjectAdapter(getActivity().getApplicationContext(),R.layout.item_card_concepto, conceptos);
        lista.setAdapter(conceptoAdapter);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_concepto, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {

            Intent intent = new Intent(getActivity(), com.cfdimovil.app.views.Concepto.class);
            String fechaCotizacion =(String)getArguments().get(ARG_COTIZACION);
            if(fechaCotizacion!=null && fechaCotizacion.length()>0) {
                intent.putExtra("fechaCotizacion", fechaCotizacion);
            }
            getActivity().startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
