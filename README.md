[OpenCFDIMovil Android](https://www.cfdimovil.com.mx/)
================================

Esta aplicacion consume los endpoints generados por el [proyecto openCFDI](https://github.com/BennSandoval/opencfdi-api)

Utilizando los servicios de Google Cloud Endpoints, App Engine, Google Cloud Message
y Android se porta la funcionalidad de facturacion desde cualquier origen utilizando JSON.

API de Facturacion expuesta de ejemplo (Asegurada con OAuth2):
[OpenCFDI](https://apis-explorer.appspot.com/apis-explorer/?base=https://opencfdimovil.appspot.com/_ah/api#p/)

Cliente de ejemplo para consumir los enpoints:
[Google Play](https://play.google.com/store/apps/details?id=com.cfdimovil.app.open)

Este cliente implementa el consumo de los servicios de prueba de [Ecodex](http://www.ecodex.com.mx/)

Gracias a Eduardo Serrano por su apoyo en la implementacion (eserrano@ecodex.com.mx)


## Products
- [App Engine]
- [Google Cloud Message]
- [Android]

## Language
- [Java][2]

## APIs
- [Google Cloud Endpoints][3]
- [Google App Engine Maven plugin][6]

## Setup Instructions

Genera tus keyfiles y realiza el setup de la aplicacion en la consola de la applicacion en APP engine,
necesitas darle permiso a esta aplicacion para consumir tu servicio.

 keytool -genkey -v -keystore opencfdimovil.keystore -alias opencfdimovil -keyalg RSA -keysize 2048 -validity 10000
 keytool -list -v -keystore "opencfdimovil.keystore"
