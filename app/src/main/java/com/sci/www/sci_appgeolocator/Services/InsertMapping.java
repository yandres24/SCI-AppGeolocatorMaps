package com.sci.www.sci_appgeolocator.Services;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.sci.www.sci_appgeolocator.Repository.Urls;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class InsertMapping extends Service {
    public static String IdVisita = "";
    public static String IdUsuario = "";
    public static String Imei = "";
    public static String Longitud = "";
    public static String Latitud = "";
    public static String Velocidad = "";
    public static String Altitud = "";
    public static String Rumbo = "";
    public static String Fecha = "";
    public static String Hora = "";
    public static String EstadoGps = "";
    public boolean resultPost = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getSimpleName(), "Creating service");
    }

    public boolean ParametersInyection(String sIdVisita, String sIdUsuario, String sImei, String sLongitud,
                                       String sLatitud, String sVelocidad, String sAltitud, String sRumbo,
                                       String sFecha, String sHora, String sEstadoGps)
    {
        try{
            IdVisita = sIdVisita;
            IdUsuario = sIdUsuario;
            Imei = sImei;
            Longitud = sLongitud;
            Latitud = sLatitud;
            Velocidad = sVelocidad;
            Altitud = sAltitud;
            Rumbo = sRumbo;
            Fecha = sFecha;
            Hora = sHora;
            EstadoGps = sEstadoGps;
        }
        catch(Exception ex)
        {
            Log.e("ParameterInyection", "Error!", ex);
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        try {
            super.onStartCommand(intent, flags, startId);
            DoBackgroundTask entity = new DoBackgroundTask();
            entity.execute(IdVisita, IdUsuario, Imei, Longitud, Latitud, Velocidad, Altitud, Rumbo, Fecha, Hora, EstadoGps);
            if (resultPost = true) {
                Toast.makeText(this, "Insertado", Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
            //Toast.makeText(this, "Insertado: " + resultPost, Toast.LENGTH_SHORT).show();
            //return super.onStartCommand(intent, flags, startId);
        }
        catch(Exception ex)
        {
            Toast.makeText(this, "Error try", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }
    }

    private class DoBackgroundTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            //Tarea Asincrona para llamar al WS de insercion en segundo plano
            String resul = "false";
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(Urls.URL_InsertMapping);
            post.setHeader("content-type", "application/json");

            try {
                JSONObject data = new JSONObject();
                //Construimos el objeto cliente en formato JSON
                data.put("IdVisita", params[0]);
                data.put("IdUsuario", params[1]);
                data.put("Imei", params[2]);
                data.put("Longitud", params[3]);
                data.put("Latitud", params[4]);
                data.put("Velocidad", params[5]);
                data.put("Altitud", params[6]);
                data.put("Rumbo", params[7]);
                data.put("Fecha", params[8]);
                data.put("Hora", params[9]);
                data.put("EstadoGps", params[10]);

                StringEntity entity = new StringEntity(data.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);

                boolean state = false;
                String httpStatusCode;
                int codError;
                String descripcionError;

                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);

                    state = obj.getBoolean("State");
                    httpStatusCode = obj.getString("HttpStatusCode");
                    codError = obj.getInt("CodError");
                    descripcionError = obj.getString("DescripcionError");
                }
                if (state == true)
                {
                    resul = "true";
                }
                else
                {
                    resul = "false";
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                resul = "false";
            }
            return resul;
        }

        @Override
        protected void onPostExecute(String result) {
        super.onPostExecute(result);
            if(result == "true")
            {
                resultPost = true;
            }
            resultPost = false;
        }
    }
}

