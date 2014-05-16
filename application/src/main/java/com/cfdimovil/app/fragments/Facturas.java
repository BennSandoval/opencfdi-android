package com.cfdimovil.app.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.cfdimovil.app.views.AbrirFactura;
import com.cfdimovil.app.views.MainActivity;
import com.cfdimovil.app.views.proceso.ResumenBorrador;
import com.opencfdimovil.api.models.RespuestaComprobante;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruben_sandoval on 2/11/14.
 */
public class Facturas extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private List<RespuestaComprobante> facturas =new ArrayList<RespuestaComprobante>();

    private ListView lista;
    private ObjectAdapter conceptoAdapter;
    private DatabaseManager database;
    private ProgressDialog progressDialog;

    public static Facturas newInstance(int sectionNumber) {
        Facturas fragment = new Facturas();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Facturas() {
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        conceptoAdapter = new ObjectAdapter(getActivity().getApplicationContext(), R.layout.item_card_factura, facturas);
        lista.setAdapter(conceptoAdapter);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        database = new DatabaseManager(getActivity().getApplicationContext());
        progressDialog = new ProgressDialog(getActivity(),R.style.alert);
        progressDialog.setTitle(getActivity().getString(R.string.app_name));
        progressDialog.setMessage(getActivity().getString(R.string.procesado_facturas));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista, container, false);

        lista = (ListView) rootView.findViewById(R.id.lista);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            Intent intent = new Intent(getActivity(), AbrirFactura.class);
            intent.putExtra("UUID",facturas.get(position).getComprobanteTimbrado().getComplemento().getTimbreFiscalDigital().getUUID());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            intent.putExtra("fecha", sdf.format(facturas.get(position).getComprobanteTimbrado().getFecha()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void updateView(){

        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                database.openToRead();
                facturas = database.getFacturas();
                database.close();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conceptoAdapter = new ObjectAdapter(getActivity().getApplicationContext(),R.layout.item_card_factura, facturas);
                        lista.setAdapter(conceptoAdapter);
                        if (progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                    }
                });
            }
        }.start();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_factura, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_nuevo) {

            Intent intent = new Intent(getActivity(), ResumenBorrador.class);
            getActivity().startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
