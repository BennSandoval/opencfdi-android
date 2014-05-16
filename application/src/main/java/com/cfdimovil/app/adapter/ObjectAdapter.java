package com.cfdimovil.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cfdimovil.app.R;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ruben_sandoval on 2/6/14.
 */
public class ObjectAdapter<T extends Object> extends BaseAdapter {

    private int itemTemplateId;

    private List<Class<T>> objects;
    private Context context;

    private ArrayList<Integer> fieldsIds = new ArrayList<Integer>();
    private ArrayList<String> fieldsView = new ArrayList<String>();

    public ObjectAdapter (Context context, int itemTemplateId,List<Class<T>> objects) {
        this.context = context;
        this.objects=objects;
        this.itemTemplateId = itemTemplateId;

        analizeChilds(LayoutInflater.from(context).inflate(itemTemplateId, null));

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(itemTemplateId, null);
        } else {
            view = convertView;
        }

        for(int index=0; index<fieldsView.size(); index++){
            Object item= objects.get(position);
            //int id = 0;
            try{
                if (fieldsView.get(index).contains(".")) {
                    String[] fields = fieldsView.get(index).split("\\.");
                    for (String field : fields) {
                        String method = "get" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1);
                        Method getter = item.getClass().getMethod(method);
                        //if(indexField == (fields.length-1)){
                        //    id = view.getResources().getIdentifier(fieldsView.get(index), "id", context.getPackageName());
                        //}
                        item = getter.invoke(item);
                    }
                } else {
                    String method="get" +String.valueOf(fieldsView.get(index).charAt(0)).toUpperCase() + fieldsView.get(index).substring(1);
                    Method getter = item.getClass().getMethod(method);
                    //id = view.getResources().getIdentifier(fieldsView.get(index), "id", context.getPackageName());
                    item = getter.invoke(item);
                }

                if(fieldsIds.get(index)!=0 && item!=null){
                    if(item instanceof Date){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        item = sdf.format(item);
                    }

                    View viewChild = view.findViewById(fieldsIds.get(index));
                    if (viewChild instanceof TextView) {
                        if(item instanceof Boolean){
                            if((Boolean)item ){
                                ((TextView)viewChild).setText("ACTIVA");
                                ((TextView)viewChild).setTextColor(context.getResources().getColor(R.color.green_card_inactive));
                            } else {
                                ((TextView)viewChild).setText("CANCELADA");
                                ((TextView)viewChild).setTextColor(context.getResources().getColor(R.color.red_card_inactive));
                            }
                        } else {
                            ((TextView)viewChild).setText(item.toString());
                        }
                    } else if (viewChild instanceof ImageView) {
                        int drawable = view.getResources().getIdentifier(item.toString(), "drawable", context.getPackageName());
                        ((ImageView)viewChild).setImageResource(drawable);
                    }
                }

            }catch(Exception ex) {
                //Log.e("CFDIMovil", ex.getMessage());
            }
        }

        return view;
    }

    void analizeChilds(View view){

        if(!(view instanceof ViewGroup)){
            if(view.getId()!=-1){
                fieldsIds.add(view.getId());
                String id = view.getResources().getResourceEntryName(view.getId());
                fieldsView.add(id);
            }
        } else {
            for(int i=0; i<((ViewGroup)view).getChildCount(); ++i) {
                View nextChild = ((ViewGroup)view).getChildAt(i);
                analizeChilds(nextChild);
            }
        }

    }

}
