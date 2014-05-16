package com.cfdimovil.app.utils;

/**
 * Created by ruben_sandoval on 5/1/14.
 */

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//2014-04-11T04:04:12
public class DateDeserializer implements JsonSerializer<Date>, JsonDeserializer<Date>{

    @Override
    public Date deserialize(JsonElement jsonElement, Type typeOF,
                            JsonDeserializationContext context) throws JsonParseException {

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(jsonElement.getAsString());
        } catch (ParseException e) {
            e.printStackTrace();
        }


        try {
            Date fechaPago=new Date(Long.valueOf(jsonElement.getAsString())*1000);
            return fechaPago;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()+ "\". Supported formats: yyyy-MM-dd'T'HH:mm:ss");
    }

    @Override
    public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return new JsonPrimitive(sdf.format(date));
    }
}