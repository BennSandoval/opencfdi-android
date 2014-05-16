package com.opencfdimovil.api.models;

/**
 * Created by ruben_sandoval on 5/2/14.
 */
public class AppengineErrorResponse{

    private AppengineError error;

    public AppengineError getError() {
        return error;
    }

    public void setError(AppengineError error) {
        this.error = error;
    }

}
