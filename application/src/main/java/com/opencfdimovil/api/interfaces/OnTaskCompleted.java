package com.opencfdimovil.api.interfaces;

/**
 * Created by ruben_sandoval on 2/5/14.
 */
public interface OnTaskCompleted{
    void onTaskCompleted(String oauth);
    void onTaskCompletedFail(String mensaje);
}