package com.opencfdimovil.api.models;

import com.opencfdimovil.sat.cfdi.v32.schema.Comprobante;

/**
 * Created with IntelliJ IDEA.
 * User: ruben_sandoval
 */
public class PeticionTimbrado {

    private Comprobante comprobante;

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }
}
