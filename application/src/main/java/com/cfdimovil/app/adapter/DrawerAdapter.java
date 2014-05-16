package com.cfdimovil.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cfdimovil.app.R;
import com.cfdimovil.app.utils.Blur;
import com.cfdimovil.app.utils.RoundedImageView;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.util.List;

/**
 * Created by ruben_sandoval on 4/23/14.
 */
public class DrawerAdapter <T extends Object> extends BaseAdapter {

    private int itemTemplateId;
    private int itemHeader;

    private Activity activity;
    private Context context;
    private SharedPreferences settings;

    private List<String> sections;
    private RespuestaEstatusCuenta usuarioDatabase;
    private Comprobante.Emisor emisor;

    public DrawerAdapter (Activity activity,Context context,int itemHeader ,int itemTemplateId,List<String> sections,RespuestaEstatusCuenta usuarioDatabase, Comprobante.Emisor emisor) {

        this.activity=activity;
        this.context = context;
        this.sections=sections;
        this.itemTemplateId = itemTemplateId;
        this.itemHeader = itemHeader;
        this.usuarioDatabase = usuarioDatabase;
        this.emisor=emisor;

        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount() {
        return sections.size();
    }

    @Override
    public String getItem(int position) {
        return sections.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        if(position==0){

            view = LayoutInflater.from(context).inflate(itemHeader, null);
            TextView razon = (TextView)view.findViewById(R.id.razon);
            TextView asignados = (TextView)view.findViewById(R.id.folios_asignados);
            TextView disponibles = (TextView)view.findViewById(R.id.folios_disponibles);
            final ImageView build = (ImageView) view.findViewById(R.id.build);
            final RoundedImageView imagen = (RoundedImageView) view.findViewById(R.id.profile);

            final String pictureData = settings.getString(context.getString(R.string.image_preference), "");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap background;
                    if (pictureData != null && pictureData.length() > 0) {
                        byte[] b = Base64.decode(pictureData, Base64.DEFAULT);
                        background = BitmapFactory.decodeByteArray(b, 0, b.length);
                        imagen.setImageBitmap(background);
                    } else {
                        background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.build), 200, 120, false);
                    }
                    build.setImageBitmap(Blur.fastblur(background, 5));
                }
            });
            razon.setText(emisor.getNombre());
            asignados.setText(String.valueOf(usuarioDatabase.getTimbresAsignados().intValue()));
            disponibles.setText(String.valueOf(usuarioDatabase.getTimbresDisponibles().intValue()));


        } else {

            view = LayoutInflater.from(context).inflate(itemTemplateId, null);
            String item= sections.get(position);
            final String  account=settings.getString(context.getString(R.string.account_preference), "");
            TextView section = (TextView)view.findViewById(R.id.section);
            if(section!=null && item !=null){
                section.setText(item);
            }

        }

        return view;
    }

}
