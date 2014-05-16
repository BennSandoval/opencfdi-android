package com.cfdimovil.app.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import com.cfdimovil.app.R;
import com.opencfdimovil.api.models.RespuestaComprobante;
import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import clr.android.pdfwriter.PDFWriter;
import clr.android.pdfwriter.PaperSize;
import clr.android.pdfwriter.StandardFonts;
import clr.android.pdfwriter.Transformation;

/**
 * Created by ruben_sandoval on 5/2/14.
 */
public class PDFUtils {

    public static boolean outputToFile(Activity activity, String path, String fileName, String pdfContent, String encoding) {
        String archivo = path + fileName;

        File directory = new File(path);
        File file = new File(archivo);
        if(!directory.exists()) {
            directory.mkdirs();
        }

        try {
            if(!file.exists()){
                file.createNewFile();
            }
            try {
                FileOutputStream pdfFile = new FileOutputStream(file);
                pdfFile.write(pdfContent.getBytes(encoding));
                pdfFile.close();
                return true;
            } catch(FileNotFoundException e) {
                Toast.makeText(activity.getApplicationContext(), "FileNotFoundException " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch(IOException e) {
            Toast.makeText(activity.getApplicationContext(),"IOException "+e.getMessage()+" "+archivo,Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static String generarCotizacionPDF(Activity activity, Comprobante comprobante, String vence) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String pictureData = settings.getString(activity.getString(R.string.image_preference), "");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fecha = sdf.format(comprobante.getFecha());

        PDFWriter mPDFWriter = new PDFWriter(PaperSize.A4_WIDTH, PaperSize.A4_HEIGHT);

        int headeLeft=250;
        if(pictureData!=null && pictureData.length()>0) {
            byte[] b = Base64.decode(pictureData, Base64.DEFAULT);
            Bitmap logo = BitmapFactory.decodeByteArray(b, 0, b.length);
            mPDFWriter.addImage(30, 30, 210, 100, logo);
        } else {
            headeLeft=30;
        }

        /*
        * Datos Emisor
        */
        int topImpuestos=30;
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.setColor(98, 99, 102);
        int altoNombre = mPDFWriter.addTextMultiline(headeLeft, topImpuestos, 9, 30, comprobante.getEmisor().getNombre());
        topImpuestos+=altoNombre-10;
        //mPDFWriter.addText(headeLeft, topImpuestos, 8, comprobante.getEmisor().getNombre());
        if(comprobante.getEmisor().getDomicilioFiscal()!=null) {
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
            if(comprobante.getEmisor().getDomicilioFiscal().getCalle()!=null && comprobante.getEmisor().getDomicilioFiscal().getCalle().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getEmisor().getDomicilioFiscal().getCalle() + " #" + comprobante.getEmisor().getDomicilioFiscal().getNoExterior());
                topImpuestos+=10;
            }
            if(comprobante.getEmisor().getDomicilioFiscal().getColonia()!=null && comprobante.getEmisor().getDomicilioFiscal().getColonia().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, "COL. " + comprobante.getEmisor().getDomicilioFiscal().getColonia() + " | C.P. " + comprobante.getEmisor().getDomicilioFiscal().getCodigoPostal());
                topImpuestos+=10;
            }
            if(comprobante.getEmisor().getDomicilioFiscal().getEstado()!=null && comprobante.getEmisor().getDomicilioFiscal().getEstado().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getEmisor().getDomicilioFiscal().getEstado() + ", " + comprobante.getEmisor().getDomicilioFiscal().getEstado() + ", " + comprobante.getEmisor().getDomicilioFiscal().getPais());
                topImpuestos+=10;
            }
        }
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, "R.F.C. " + comprobante.getEmisor().getRfc());
        topImpuestos+=10;
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, "FECHA "+fecha);

        /*
        *Folio
        */
        if(comprobante.getTipoDeComprobante()!=null && comprobante.getTipoDeComprobante().length()>0) {
            mPDFWriter.addImage(250 + 175, 30, 145, 30, getRectBitmap(145, 45, 0xFFEEF1D7));
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
            mPDFWriter.setColor(156, 179, 39);
            mPDFWriter.addText(250 + 220, 37, 13, comprobante.getTipoDeComprobante().toUpperCase());
        }

        if(comprobante.getFolio()!=null && comprobante.getFolio().length()>0){
            mPDFWriter.setColor(238, 241, 215);
            mPDFWriter.addRectangle(250 + 176, 60, 143, 30);
            mPDFWriter.setColor(238,49,36);
            mPDFWriter.addText(250+200, 60, 36, comprobante.getFolio());
        }
        /*
        * Datos Receptor
        */
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        mPDFWriter.setColor(98, 99, 102);
        mPDFWriter.addText(30, 145, 12, "DIRIGIDO A");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(30, 158 , 9, comprobante.getReceptor().getNombre());


        mPDFWriter.addText(250, 120 , 32, "MX$"+comprobante.getTotal().setScale(2, RoundingMode.CEILING));
        if(vence.length()>0) {
            mPDFWriter.addText(252, 155, 9, "VALIDO HASTA " + vence.toUpperCase());
        }

        /*
        * Conceptos
        */
        topImpuestos=180;

        mPDFWriter.addImage(30, topImpuestos, 540, 18, getRectBitmap(540, 18, 0xFF000000));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.addText(40, topImpuestos+3, 9, "CANTIDAD");
        mPDFWriter.addText(100, topImpuestos+3, 9, "UNIDAD");
        mPDFWriter.addText(180, topImpuestos+3, 9, "CONCEPTO");
        mPDFWriter.addText(500, topImpuestos+3, 9, "IMPORTE");
        mPDFWriter.setColor(0,0,0);
        int inicio = topImpuestos+20;
        int sumatoria = 17;
        int index = 0;
        for(Comprobante.Conceptos.Concepto concepto : comprobante.getConceptos().getConcepto()){
            int posicionY = inicio+(sumatoria*index);
            mPDFWriter.addText(40, posicionY , 9, String.valueOf(concepto.getCantidad().setScale(2, RoundingMode.CEILING)));
            mPDFWriter.addText(100, posicionY , 9, concepto.getUnidad());
            mPDFWriter.addText(180, posicionY , 9, concepto.getDescripcion());
            mPDFWriter.addText(500, posicionY , 9, "$"+String.valueOf(concepto.getImporte().setScale(2, RoundingMode.CEILING)));
            index++;
        }

        topImpuestos=700;
        int leftImpuestos=30+120+237;

        /*
        * Total, impuestos...
        */

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFFEEF1D7));
        mPDFWriter.setColor(156,179,39);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "SUBTOTAL");
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getSubTotal().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFFDCDDDE));
        mPDFWriter.setColor(148,150,153);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IVA "+comprobante.getImpuestos().getTraslados().getTraslado().get(0).getTasa()+"%");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(0).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFFDCDDDE));
        mPDFWriter.setColor(148,150,153);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IEPS "+comprobante.getImpuestos().getTraslados().getTraslado().get(1).getTasa()+"%");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(1).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFF949699));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IVA RETENIDO");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(0).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFF949699));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "ISR RETENIDO");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(1).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        mPDFWriter.addImage(30, topImpuestos, 450, 18, getRectBitmap(450,18,0xFF000000));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "TOTAL");
        if(vence.length()>0) {
            mPDFWriter.addText(100, topImpuestos + 3, 9, "VALIDO HASTA "+vence.toUpperCase());
        }
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 11, "$"+String.valueOf(comprobante.getTotal().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=20;

        /*
        * Footer
        */

        mPDFWriter.setColor(148,150,153);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);

        int pageCount = mPDFWriter.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            mPDFWriter.setCurrentPage(i);
            mPDFWriter.addText(10, 920, 8, Integer.toString(i + 1) + " / " + Integer.toString(pageCount)+"  https://www.cfdimovil.com.mx/");
        }

        String s = mPDFWriter.asString();
        return s;

    }

    public static String generaFacturaPDF(Activity activity, RespuestaComprobante respuesta) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String pictureData = settings.getString(activity.getString(R.string.image_preference), "");


        Comprobante comprobante = respuesta.getComprobanteTimbrado();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String fecha = sdf.format(comprobante.getFecha());

        PDFWriter mPDFWriter = new PDFWriter(PaperSize.A4_WIDTH, PaperSize.A4_HEIGHT);

        /*
        CANCELADA
        */
        if(!respuesta.getValid()){
            mPDFWriter.setColor(221,99,99);
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);

            mPDFWriter.addTextCorrect(130, 200, 50, "CANCELADA", Transformation.DEGREES_45_ROTATION);

            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
            mPDFWriter.setColor(0,0,0);
        }

        int headeLeft=250;
        if(pictureData!=null && pictureData.length()>0) {
            byte[] b = Base64.decode(pictureData, Base64.DEFAULT);
            Bitmap logo = BitmapFactory.decodeByteArray(b, 0, b.length);
            mPDFWriter.addImage(30, 30, 210, 100, logo);
        } else {
            headeLeft=30;
        }

        /*
        * Datos Emisor
        */
        int topImpuestos=30;
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.setColor(98, 99, 102);
        int altoNombre = mPDFWriter.addTextMultiline(headeLeft, topImpuestos, 8, 30, comprobante.getEmisor().getNombre());
        topImpuestos+=altoNombre-10;
        //mPDFWriter.addText(headeLeft, topImpuestos, 8, comprobante.getEmisor().getNombre());
        if(comprobante.getEmisor().getDomicilioFiscal()!=null) {
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
            if(comprobante.getEmisor().getDomicilioFiscal().getCalle()!=null && comprobante.getEmisor().getDomicilioFiscal().getCalle().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getEmisor().getDomicilioFiscal().getCalle() + " #" + comprobante.getEmisor().getDomicilioFiscal().getNoExterior());
                topImpuestos+=10;
            }
            if(comprobante.getEmisor().getDomicilioFiscal().getColonia()!=null && comprobante.getEmisor().getDomicilioFiscal().getColonia().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, "COL. " + comprobante.getEmisor().getDomicilioFiscal().getColonia() + " | C.P. " + comprobante.getEmisor().getDomicilioFiscal().getCodigoPostal());
                topImpuestos+=10;
            }
            if(comprobante.getEmisor().getDomicilioFiscal().getEstado()!=null && comprobante.getEmisor().getDomicilioFiscal().getEstado().length()>0) {
                mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getEmisor().getDomicilioFiscal().getEstado() + ", " + comprobante.getEmisor().getDomicilioFiscal().getEstado() + ", " + comprobante.getEmisor().getDomicilioFiscal().getPais());
                topImpuestos+=10;
            }
        }
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, "R.F.C. " + comprobante.getEmisor().getRfc());
        topImpuestos+=10;
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.setColor(0, 0, 0);
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getMetodoDePago());
        topImpuestos+=10;
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getFormaDePago());
        topImpuestos+=10;
        mPDFWriter.addText(headeLeft, topImpuestos+10, 8, comprobante.getComplemento().getTimbreFiscalDigital().getUUID());

        /*
        *Folio
        */
        if(comprobante.getTipoDeComprobante()!=null && comprobante.getTipoDeComprobante().length()>0) {
            mPDFWriter.addImage(250 + 175, 30, 145, 30, getRectBitmap(145, 45, 0xFFEEF1D7));
            mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
            mPDFWriter.setColor(156, 179, 39);
            mPDFWriter.addText(250 + 220, 37, 13, comprobante.getTipoDeComprobante().toUpperCase());
        }

        if(comprobante.getFolio()!=null && comprobante.getFolio().length()>0){
            mPDFWriter.setColor(238, 241, 215);
            mPDFWriter.addRectangle(250 + 176, 60, 143, 30);
            mPDFWriter.setColor(238,49,36);
            mPDFWriter.addText(250+200, 60, 36, comprobante.getFolio());
        }
        /*
        * Datos Receptor
        */
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        mPDFWriter.addImage(30, 145, 540, 18, getRectBitmap(540, 18, 0xFFEEF1D7));
        mPDFWriter.setColor(156,179,39);
        mPDFWriter.addText(40, 148, 9, "NOMBRE");
        mPDFWriter.addText(310, 148, 9, "R.F.C.");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(40, 165 , 9, comprobante.getReceptor().getNombre());
        mPDFWriter.addText(310, 165 , 9, comprobante.getReceptor().getRfc());

        mPDFWriter.addImage(30, 180, 540, 18, getRectBitmap(540, 18, 0xFFDCDDDE));
        mPDFWriter.setColor(148, 150, 153);
        mPDFWriter.addText(40, 183, 9, "DIRECCION");
        mPDFWriter.addText(310, 183, 9, "CIUDAD");
        mPDFWriter.addText(470, 183, 9, "CODIGO POSTAL");
        mPDFWriter.setColor(0, 0, 0);

        if(comprobante.getReceptor().getDomicilio()!=null) {
            mPDFWriter.addText(40, 200, 9, comprobante.getReceptor().getDomicilio().getCalle() + " " + comprobante.getReceptor().getDomicilio().getNoExterior() + " " + comprobante.getReceptor().getDomicilio().getColonia());
            mPDFWriter.addText(310, 200, 9, comprobante.getReceptor().getDomicilio().getMunicipio() + ", " + comprobante.getReceptor().getDomicilio().getEstado());
            mPDFWriter.addText(470, 200, 9, comprobante.getReceptor().getDomicilio().getCodigoPostal());
        }

        mPDFWriter.addImage(30, 215, 540, 18, getRectBitmap(540,18,0xFFEEF1D7));
        mPDFWriter.setColor(156,179,39);
        mPDFWriter.addText(40, 218, 9, "FECHA");
        mPDFWriter.addText(310, 218, 9, "LUGAR DE EXPEDICION");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(40, 235 , 9, fecha);
        mPDFWriter.addText(310, 235 , 9, comprobante.getLugarExpedicion());

        /*
        * Conceptos
        */
        mPDFWriter.addImage(30, 250, 540, 18, getRectBitmap(540, 18, 0xFF949699));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.addText(40, 253, 9, "CANTIDAD");
        mPDFWriter.addText(100, 253, 9, "UNIDAD");
        mPDFWriter.addText(180, 253, 9, "CONCEPTO");
        mPDFWriter.addText(500, 253, 9, "IMPORTE");
        mPDFWriter.setColor(0,0,0);
        int inicio = 270;
        int sumatoria = 17;
        int index = 0;
        for(Comprobante.Conceptos.Concepto concepto : comprobante.getConceptos().getConcepto()){
            int posicionY = inicio+(sumatoria*index);
            mPDFWriter.addText(40, posicionY , 9, String.valueOf(concepto.getCantidad().setScale(2, RoundingMode.CEILING)));
            mPDFWriter.addText(100, posicionY , 9, concepto.getUnidad());
            mPDFWriter.addText(180, posicionY , 9, concepto.getDescripcion());
            mPDFWriter.addText(500, posicionY , 9, "$"+String.valueOf(concepto.getImporte().setScale(2, RoundingMode.CEILING)));
            index++;
        }

        topImpuestos=615;
        int leftImpuestos=30;
        /*
        * QR Code
        */
        InputStream qrCode  = new ByteArrayInputStream(respuesta.getCodeQrBmpByte());
        Bitmap qrCodeImg = BitmapFactory.decodeStream(qrCode);
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 120, 120, qrCodeImg);
        mPDFWriter.setColor(148,150,153);
        leftImpuestos+=120;

        /*
        * Cantidad con letra
        */
        leftImpuestos+=2;
        mPDFWriter.setColor(148,150,153);
        mPDFWriter.addRectangle(leftImpuestos, topImpuestos, 235, 50);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+8, topImpuestos+3, 9, "Cantidad con letra");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        String letra =NumeroALetra.convertir(String.valueOf(comprobante.getTotal().setScale(2, RoundingMode.CEILING)),true);
        int altoLetra = mPDFWriter.addTextMultiline(leftImpuestos+8, topImpuestos+18, 9, 40, letra);
        mPDFWriter.setColor(148,150,153);
        int altoSello = mPDFWriter.addTextMultiline(leftImpuestos, topImpuestos+55, 9, 42, "Cadena orginal: "+respuesta.getCadenaOriginal());
        leftImpuestos+=237;

        /*
        * Total, impuestos...
        */

        leftImpuestos+=2;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFFEEF1D7));
        mPDFWriter.setColor(156,179,39);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "SUBTOTAL");
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getSubTotal().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        topImpuestos+=2;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFFDCDDDE));
        mPDFWriter.setColor(148,150,153);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IVA "+comprobante.getImpuestos().getTraslados().getTraslado().get(0).getTasa()+"%");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(0).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        topImpuestos+=0;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFFDCDDDE));
        mPDFWriter.setColor(148,150,153);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IEPS "+comprobante.getImpuestos().getTraslados().getTraslado().get(1).getTasa()+"%");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getTraslados().getTraslado().get(1).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        topImpuestos+=2;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFF949699));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "IVA RETENIDO");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(0).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        topImpuestos+=0;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFF949699));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "ISR RETENIDO");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 10, "$"+String.valueOf(comprobante.getImpuestos().getRetenciones().getRetencion().get(1).getImporte().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=18;

        topImpuestos+=2;
        mPDFWriter.addImage(leftImpuestos, topImpuestos, 90, 18, getRectBitmap(90,18,0xFF000000));
        mPDFWriter.setColor(255,255,255);
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA_BOLD);
        mPDFWriter.addText(leftImpuestos+5, topImpuestos+3, 9, "TOTAL");
        mPDFWriter.setColor(0,0,0);
        mPDFWriter.addText(leftImpuestos+100, topImpuestos+3 , 11, "$"+String.valueOf(comprobante.getTotal().setScale(2, RoundingMode.CEILING)));
        topImpuestos+=20;

        /*
        * Footer
        */

        mPDFWriter.setColor(148,150,153);
        topImpuestos+=5;
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.HELVETICA);
        int altoSelloCFDI = mPDFWriter.addTextMultiline(30, topImpuestos, 9, 100, "Sello CFD: "+comprobante.getComplemento().getTimbreFiscalDigital().getSelloCFD());
        topImpuestos+=2;
        int altoCadena = mPDFWriter.addTextMultiline(30, topImpuestos+altoSelloCFDI, 9, 100, "Sello SAT: "+comprobante.getComplemento().getTimbreFiscalDigital().getSelloSAT());


        int pageCount = mPDFWriter.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            mPDFWriter.setCurrentPage(i);
            mPDFWriter.addText(10, 920, 8, Integer.toString(i + 1) + " / " + Integer.toString(pageCount)+"  https://www.cfdimovil.com.mx/");
        }

        String s = mPDFWriter.asString();
        return s;
    }

    public static Bitmap getRectBitmap(int width ,int height, int color) {

        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRect(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        return output ;
    }
}
