package com.opencfdimovil.api.models;

/**
 * Created with IntelliJ IDEA.
 * User: ruben_sandoval
 */
public class RespuestaCancela {

    private boolean cancelado;
    private String UUID;

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
