package com.opencfdimovil.api.models;

import java.util.List;

/**
 * Created by ruben_sandoval on 5/2/14.
 */
public class AppengineError {

    private List<AppengineErrors> errors;
    private Double code;
    private String message;

    public List<AppengineErrors> getErrors() {
        return errors;
    }

    public void setErrors(List<AppengineErrors> errors) {
        this.errors = errors;
    }

    public Double getCode() {
        return code;
    }

    public void setCode(Double code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
