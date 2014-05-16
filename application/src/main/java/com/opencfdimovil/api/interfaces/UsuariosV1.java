package com.opencfdimovil.api.interfaces;

import com.opencfdimovil.api.models.PeticionUsuario;
import com.opencfdimovil.api.models.RespuestaEstatusCuenta;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.QueryMap;

/**
 * Created by ruben_sandoval on 1/31/14.
 */
public interface UsuariosV1 {

    @POST("/usuarios/v1/registrar")
    void registrar(
            @Body PeticionUsuario user,
            @Header("Authorization") String auth,
            Callback<RespuestaEstatusCuenta> callback
    );

    @POST("/usuarios/v1/proxy")
    void proxy(
            @Body String gcmKey,
            @Body String email,
            @Header("Authorization") String auth,
            Callback<RespuestaEstatusCuenta> callback
    );

    @GET("/usuarios/v1/estatus")
    void estatus(
            @QueryMap Map<String, String> options,
            @Header("Authorization") String auth,
            Callback<RespuestaEstatusCuenta> callback
    );
}
