package com.cfdimovil.app.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cfdimovil.app.BuildConfig;
import com.cfdimovil.app.R;
import com.cfdimovil.app.utils.ByteArrayToBase64TypeAdapter;
import com.cfdimovil.app.utils.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencfdimovil.api.models.RespuestaComprobante;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;
import com.opencfdimovil.sat.cfdi.v32.schema.ObjectFactory;
import com.opencfdimovil.sat.cfdi.v32.schema.TUbicacionFiscal;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by ruben_sandoval on 1/24/14.
 */
public class DatabaseManager <T extends Object> {

    private int version = 3;
    private String appName = "CFDIMovil";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter()).create();

    public DatabaseManager(Context c){
        context = c;
    }

    public DatabaseManager openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, appName, null, version);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public DatabaseManager openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, appName, null, version);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        sqLiteHelper.close();
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context, String name,SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE usuario (_id integer primary key autoincrement, estructura TEXT)");
            db.execSQL("CREATE TABLE emisor (_id integer primary key autoincrement, estructura TEXT)");
            db.execSQL("CREATE TABLE receptor (_id integer primary key autoincrement, rfc TEXT, nombre TEXT, estructura TEXT)");
            db.execSQL("CREATE TABLE concepto (_id integer primary key autoincrement, noIdentificacion TEXT, descripcion TEXT, valorUnitario TEXT, estructura TEXT)");
            db.execSQL("CREATE TABLE factura (_id integer primary key autoincrement, fecha TEXT, rfc TEXT, UUID TEXT, total TEXT, estructura TEXT)");

            db.execSQL("INSERT INTO usuario (estructura) values ('')");
            db.execSQL("INSERT INTO emisor (estructura) values ('')");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        }
    }

    /*
    usuario
    */

    public void updateUsuario(String estructura){
        //Log.v(context.getString(R.string.app_name), "Update usuario: "+estructura);
        String query="UPDATE usuario SET estructura='"+estructura+"'";
        sqLiteDatabase.execSQL(query);
    }

    public T getUsuario (){

        String query="SELECT * FROM usuario";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        RespuestaEstatusCuenta usuario = new RespuestaEstatusCuenta();

        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                //Log.v(context.getString(R.string.app_name), "Get usuario: "+estructura);
                usuario = gson.fromJson(estructura, RespuestaEstatusCuenta.class);
            } while(cursor.moveToNext());
        }
        cursor.close();
        if(usuario==null){
            usuario = new RespuestaEstatusCuenta();
            usuario.setTimbresAsignados(0.0);
            usuario.setTimbresDisponibles(0.0);
            usuario.setRfc(BuildConfig.RFC);
            usuario.setRazon(BuildConfig.RAZON);
        }
        return (T)usuario;
    }

    /*
    emisor
    */

    public void updateEmisor(String estructura){
        String query="UPDATE emisor SET estructura='"+estructura+"'";
        sqLiteDatabase.execSQL(query);
    }

    public T getEmisor (){
        String query="SELECT * FROM emisor";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Comprobante.Emisor emisor = null;

        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                Log.v(context.getString(R.string.app_name), estructura);
                emisor = gson.fromJson(estructura, Comprobante.Emisor.class);
                if(emisor==null){

                    RespuestaEstatusCuenta usuario= (RespuestaEstatusCuenta)getUsuario();
                    ObjectFactory factory = new ObjectFactory();
                    emisor = factory.createComprobanteEmisor();
                    emisor.setRfc(usuario.getRfc());
                    emisor.setNombre(usuario.getRazon());

                    TUbicacionFiscal domicilio = factory.createTUbicacionFiscal();
                    emisor.setDomicilioFiscal(domicilio);

                } else {
                    if(emisor.getRfc().length()==0){
                        emisor.setRfc(BuildConfig.RFC);
                    }
                    if(emisor.getNombre().length()==0){
                        emisor.setNombre(BuildConfig.RAZON);
                    }
                    if(emisor!=null && emisor.getDomicilioFiscal() == null) {
                        ObjectFactory factory = new ObjectFactory();
                        TUbicacionFiscal domicilio = factory.createTUbicacionFiscal();
                        emisor.setDomicilioFiscal(domicilio);
                    }
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        String estructura = gson.toJson(emisor);
        Log.i(context.getString(R.string.app_name),estructura);

        return (T)emisor;
    }

    /*
    receptor
    */
    public void saveReceptor(String rfc,String nombre, String estructura){
        String query="INSERT INTO receptor (rfc, nombre, estructura) values ('"+rfc+"','"+nombre+"','"+estructura+"')";
        sqLiteDatabase.execSQL(query);
    }

    public void updateReceptor(String rfcOld, String rfc,String nombre, String estructura){
        String query="UPDATE receptor SET rfc='"+rfc+"', nombre='"+nombre+"', estructura='"+estructura+"' WHERE rfc='"+rfcOld+"'";
        sqLiteDatabase.execSQL(query);
    }

    public void deleteReceptor(String rfc){
        String query="DELETE FROM receptor WHERE rfc='"+rfc+"'";
        sqLiteDatabase.execSQL(query);
    }

    public Cursor getReceptor(String rfc, String nombre){
        String query="SELECT * FROM receptor WHERE rfc LIKE '%"+rfc+"%' OR nombre LIKE '%"+nombre+"%'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public int getReceptoresCount(){
        int result=0;
        String query="SELECT _id FROM receptor";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        result=cursor.getCount();
        cursor.close();
        return result;
    }

    public T getReceptorCampo (String campo, String valor){
        Log.i(context.getString(R.string.app_name),"getReceptorCampo("+campo+","+valor+")");
        String query="SELECT estructura FROM receptor WHERE "+campo+" = '"+valor+"'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Comprobante.Receptor receptor = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                receptor = gson.fromJson(estructura, Comprobante.Receptor.class);
                if(receptor.getNombre()==null){
                    receptor.setNombre("");
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        if(receptor==null){
            ObjectFactory of = new ObjectFactory();
            receptor = of.createComprobanteReceptor();
            receptor.setRfc("");
            receptor.setNombre("");
            receptor.setDomicilio(of.createTUbicacion());
        }
        String estructura = gson.toJson(receptor);
        Log.i(context.getString(R.string.app_name),estructura);

        return (T)receptor;
    }

    public Cursor getReceptoresCursor(){
        String query="SELECT * FROM receptor";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public Cursor searchReceptor(String campo, String value){
        String query="SELECT * FROM receptor WHERE "+campo+" LIKE '%"+value+"%'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public List<Comprobante.Receptor> getReceptores(){
        String query="SELECT estructura FROM receptor ORDER BY _id DESC";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<Comprobante.Receptor> receptores = new ArrayList<Comprobante.Receptor>();
        Comprobante.Receptor receptor = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                receptor = gson.fromJson(estructura, Comprobante.Receptor.class);
                receptores.add(receptor);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return receptores;
    }

    /*
    concepto
    */
    public void saveConcepto(String noIdentificacion,String descripcion, BigDecimal valorUnitario, String estructura){
        String query="INSERT INTO concepto (noIdentificacion, descripcion, valorUnitario, estructura) values ('"+noIdentificacion+"','"+descripcion+"','"+valorUnitario+"','"+estructura+"')";
        sqLiteDatabase.execSQL(query);
    }

    public void updateConcepto(String noIdentificacion,String descripcion, BigDecimal valorUnitario, String estructura){
        String query="UPDATE concepto SET noIdentificacion='"+noIdentificacion+"', descripcion='"+descripcion+"', valorUnitario='"+valorUnitario+"', estructura='"+estructura+"' WHERE noIdentificacion='"+noIdentificacion+"'";
        sqLiteDatabase.execSQL(query);
    }

    public void deleteConcepto(String noIdentificacion){
        String query="DELETE FROM concepto WHERE noIdentificacion='"+noIdentificacion+"'";
        sqLiteDatabase.execSQL(query);
    }

    public Cursor getConcepto(String noIdentificacion,String descripcion){
        String query="SELECT * FROM concepto WHERE noIdentificacion LIKE '%"+noIdentificacion+"%' OR descripcion LIKE '%"+descripcion+"%'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public int getConceptosCount(){
        int result=0;
        String query="SELECT _id FROM concepto";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        result=cursor.getCount();
        cursor.close();
        return result;
    }

    public List<Comprobante.Conceptos.Concepto> getConceptos(){
        String query="SELECT estructura FROM concepto ORDER BY _id DESC";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<Comprobante.Conceptos.Concepto> conceptos = new ArrayList<Comprobante.Conceptos.Concepto>();
        Comprobante.Conceptos.Concepto concepto = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                concepto = gson.fromJson(estructura, Comprobante.Conceptos.Concepto.class);
                conceptos.add(concepto);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return conceptos;
    }

    public Cursor getConceptoCursor(){
        String query="SELECT * FROM concepto";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public T getConceptoCampo(String campo, String valor){
        Log.i(context.getString(R.string.app_name),"getConceptoCampo("+campo+","+valor+")");
        String query="SELECT estructura FROM concepto WHERE "+campo+" = '"+valor+"'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Comprobante.Conceptos.Concepto concepto = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                concepto = gson.fromJson(estructura, Comprobante.Conceptos.Concepto.class);
            } while(cursor.moveToNext());
        }
        cursor.close();

        String estructura = gson.toJson(concepto);
        Log.i(context.getString(R.string.app_name),estructura);

        return (T)concepto;
    }

    public Cursor searchConcepto(String campo, String value){
        String query="SELECT * FROM concepto WHERE "+campo+" LIKE '%"+value+"%'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    /*
   factura  fecha TEXT, rfc TEXT, UUID TEXT, total TEXT, estructura TEXT
   */
    public void saveFactura(String fecha, String rfc, String UUID, String estructura){
        Log.i(context.getString(R.string.app_name),"saveFactura "+estructura);
        String query="INSERT INTO factura (fecha, rfc, UUID, estructura) values ('"+fecha+"','"+rfc+"','"+UUID+"','"+estructura+"')";
        sqLiteDatabase.execSQL(query);
    }

    public void updateFactura(String fecha, String rfc, String UUID, String estructura){
        Log.i(context.getString(R.string.app_name),"updateFactura "+estructura);
        String query="UPDATE factura SET rfc='"+rfc+"', UUID='"+UUID+"', estructura='"+estructura+"' WHERE fecha='"+fecha+"'";
        sqLiteDatabase.execSQL(query);
    }

    public void deleteFactura(String fecha){
        String query="DELETE FROM factura WHERE fecha='"+fecha+"'";
        sqLiteDatabase.execSQL(query);
    }

    public int getFacturasCount(){
        int result=0;
        String query="SELECT estructura FROM factura WHERE UUID!=''";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        result=cursor.getCount();
        cursor.close();
        return result;
    }

    public List<RespuestaComprobante> getFacturas(){
        String query="SELECT estructura FROM factura WHERE UUID!='' ORDER BY _id DESC";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<RespuestaComprobante> comprobantes = new ArrayList<RespuestaComprobante>();
        RespuestaComprobante comprobante = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                comprobante = gson.fromJson(estructura, RespuestaComprobante.class);
                Log.i(context.getString(R.string.app_name),"getFacturas "+estructura);
                comprobantes.add(comprobante);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return comprobantes;
    }

    public Cursor getFacturaCursor(){
        String query="SELECT * FROM factura";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        return cursor;
    }

    public T getFacturaCampo(String campo, String valor){
        Log.i(context.getString(R.string.app_name),"getFacturaCampo("+campo+","+valor+")");
        String query="SELECT estructura FROM factura WHERE "+campo+" = '"+valor+"' AND UUID!=''";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        RespuestaComprobante comprobante = null;
        if (cursor.moveToFirst()){
            do {
                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));
                comprobante = gson.fromJson(estructura, RespuestaComprobante.class);
            } while(cursor.moveToNext());
        }
        cursor.close();

        String estructura = gson.toJson(comprobante);
        Log.i(context.getString(R.string.app_name),estructura);
        return (T)comprobante;
    }

    public int getCotizacionesCount(){
        int result=0;
        String query="SELECT estructura FROM factura WHERE UUID==''";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        result=cursor.getCount();
        cursor.close();
        return result;
    }

    public List<Comprobante> getCotizaciones(){
        String query="SELECT estructura FROM factura WHERE UUID=='' ORDER BY _id DESC";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        List<Comprobante> comprobantes = new ArrayList<Comprobante>();
        Comprobante comprobante = null;
        if (cursor.moveToFirst()){
            do {

                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));

                comprobante = gson.fromJson(estructura, Comprobante.class);
                if(comprobante.getConceptos()==null){
                    ObjectFactory factory = new ObjectFactory();
                    comprobante.setConceptos(factory.createComprobanteConceptos());
                }

                if(comprobante.getReceptor()!=null) {
                    comprobante.setReceptor((Comprobante.Receptor) getReceptorCampo("rfc", comprobante.getReceptor().getRfc()));
                } else {
                    comprobante.setReceptor((Comprobante.Receptor) getReceptorCampo("rfc", ""));
                }

                if((comprobante.getReceptor().getRfc()==null || comprobante.getReceptor().getRfc().length()==0) && (comprobante.getConceptos()==null || comprobante.getConceptos().getConcepto().size()==0)){
                    Date fechaFactura=comprobante.getFecha();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    deleteFactura(sdf.format(fechaFactura));
                } else {
                    comprobantes.add(comprobante);
                }
            } while(cursor.moveToNext());
        }
        cursor.close();

        return comprobantes;
    }

    public T getCotizacionCampo(String campo, String valor){
        Log.i(context.getString(R.string.app_name),"getCotizacionCampo("+campo+","+valor+")");
        String query="SELECT estructura FROM factura WHERE "+campo+" = '"+valor+"' AND UUID==''";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        Comprobante comprobante = null;
        if (cursor.moveToFirst()){
            do {

                String estructura  = cursor.getString(cursor.getColumnIndex("estructura"));

                comprobante = gson.fromJson(estructura, Comprobante.class);
                if(comprobante.getConceptos()==null){
                    ObjectFactory factory = new ObjectFactory();
                    comprobante.setConceptos(factory.createComprobanteConceptos());
                }
                if(comprobante.getReceptor()!=null) {
                    comprobante.setReceptor((Comprobante.Receptor) getReceptorCampo("rfc", comprobante.getReceptor().getRfc()));
                } else {
                    comprobante.setReceptor((Comprobante.Receptor) getReceptorCampo("rfc", ""));
                }

                ObjectFactory factory = new ObjectFactory();
                if(comprobante.getImpuestos()==null){

                    comprobante.setImpuestos(factory.createComprobanteImpuestos());
                    comprobante.getImpuestos().setRetenciones(factory.createComprobanteImpuestosRetenciones());
                    comprobante.getImpuestos().setTraslados(factory.createComprobanteImpuestosTraslados());

                    comprobante.getImpuestos().getTraslados().getTraslado().add(factory.createComprobanteImpuestosTrasladosTraslado());
                    comprobante.getImpuestos().getTraslados().getTraslado().get(0).setImpuesto("IVA");
                    comprobante.getImpuestos().getTraslados().getTraslado().get(0).setTasa(new BigDecimal(0));
                    comprobante.getImpuestos().getTraslados().getTraslado().get(0).setImporte(new BigDecimal(0));

                    comprobante.getImpuestos().getTraslados().getTraslado().add(factory.createComprobanteImpuestosTrasladosTraslado());
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImpuesto("IEPS");
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setTasa(new BigDecimal(0));
                    comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImporte(new BigDecimal(0));

                    comprobante.getImpuestos().getRetenciones().getRetencion().add(factory.createComprobanteImpuestosRetencionesRetencion());
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImpuesto("IVA");
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImporte(new BigDecimal(0));

                    comprobante.getImpuestos().getRetenciones().getRetencion().add(factory.createComprobanteImpuestosRetencionesRetencion());
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImpuesto("ISR");
                    comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImporte(new BigDecimal(0));

                    comprobante.getImpuestos().setTotalImpuestosRetenidos(new BigDecimal(0));
                    comprobante.getImpuestos().setTotalImpuestosTrasladados(new BigDecimal(0));

                }

            } while(cursor.moveToNext());
        }
        cursor.close();

        if(comprobante==null){

            ObjectFactory factory = new ObjectFactory();
            comprobante = factory.createComprobante();
            comprobante.setFecha(new Date());
            comprobante.setTipoDeComprobante("");
            comprobante.setFormaDePago("");
            comprobante.setMetodoDePago("");
            comprobante.setNumCtaPago("");
            comprobante.setMoneda("");
            comprobante.setTipoCambio("");

            Comprobante.Receptor receptor = factory.createComprobanteReceptor();
            receptor.setRfc("");
            receptor.setNombre("");

            comprobante.setReceptor(receptor);
            comprobante.setConceptos(factory.createComprobanteConceptos());

            comprobante.setImpuestos(factory.createComprobanteImpuestos());
            comprobante.getImpuestos().setRetenciones(factory.createComprobanteImpuestosRetenciones());
            comprobante.getImpuestos().setTraslados(factory.createComprobanteImpuestosTraslados());

            comprobante.getImpuestos().getTraslados().getTraslado().add(factory.createComprobanteImpuestosTrasladosTraslado());
            comprobante.getImpuestos().getTraslados().getTraslado().get(0).setImpuesto("IVA");
            comprobante.getImpuestos().getTraslados().getTraslado().get(0).setTasa(new BigDecimal(0));
            comprobante.getImpuestos().getTraslados().getTraslado().get(0).setImporte(new BigDecimal(0));

            comprobante.getImpuestos().getTraslados().getTraslado().add(factory.createComprobanteImpuestosTrasladosTraslado());
            comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImpuesto("IEPS");
            comprobante.getImpuestos().getTraslados().getTraslado().get(1).setTasa(new BigDecimal(0));
            comprobante.getImpuestos().getTraslados().getTraslado().get(1).setImporte(new BigDecimal(0));

            comprobante.getImpuestos().getRetenciones().getRetencion().add(factory.createComprobanteImpuestosRetencionesRetencion());
            comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImpuesto("IVA");
            comprobante.getImpuestos().getRetenciones().getRetencion().get(0).setImporte(new BigDecimal(0));

            comprobante.getImpuestos().getRetenciones().getRetencion().add(factory.createComprobanteImpuestosRetencionesRetencion());
            comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImpuesto("ISR");
            comprobante.getImpuestos().getRetenciones().getRetencion().get(1).setImporte(new BigDecimal(0));

            comprobante.getImpuestos().setTotalImpuestosRetenidos(new BigDecimal(0));
            comprobante.getImpuestos().setTotalImpuestosTrasladados(new BigDecimal(0));

            comprobante.setSubTotal(new BigDecimal(0));
        }

        String estructura = gson.toJson(comprobante);
        Log.i(context.getString(R.string.app_name),estructura);

        return (T)comprobante;
    }

}
