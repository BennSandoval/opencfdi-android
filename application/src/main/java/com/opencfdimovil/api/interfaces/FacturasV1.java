package com.opencfdimovil.api.interfaces;

import com.opencfdimovil.api.models.PeticionTimbrado;
import com.opencfdimovil.api.models.PeticionTransforma;
import com.opencfdimovil.api.models.RespuestaCancela;
import com.opencfdimovil.api.models.RespuestaComprobante;
import com.opencfdimovil.api.models.RespuestaTransforma;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by ruben_sandoval on 5/1/14.
 */
public interface FacturasV1 {

    @POST("/facturas/v1/timbrar")
    void timbrar(
            @Body PeticionTimbrado user,
            @Header("Authorization") String auth,
            Callback<RespuestaComprobante> callback
    );

    @DELETE("/facturas/v1/cancela/{UUID}")
    void cancela(
            @Path("UUID") String UUID,
            @Header("Authorization") String auth,
            Callback<RespuestaCancela> callback
    );

    @PUT("/facturas/v1/transforma")
    void transforma(
            @Body PeticionTransforma user,
            @Header("Authorization") String auth,
            Callback<RespuestaTransforma> callback
    );

    //@GET("/facturas/v1/obtiene/{id}")
    //@Path("id") int id,
    @GET("/facturas/v1/obtiene")
    void obtiene(
            @QueryMap Map<String, String> opciones,
            @Header("Authorization") String auth,
            Callback<RespuestaComprobante> callback
    );

}
