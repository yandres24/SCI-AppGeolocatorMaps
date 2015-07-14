package com.sci.www.sci_appgeolocator.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.sci.www.sci_appgeolocator.Repository.Urls;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RouteSkytechlogic extends Service {
    public static String Imei = "";
    public static String Trama = "";
    public static String Evento = "";
    public static String Mensaje = "";
    public static String Fecha = "";
    public boolean resultPost = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getSimpleName(), "Creating service");
    }

    public RouteSkytechlogic() {
    }

    public boolean ParametersInyection(String sImei, String sTrama, String sEvento, String sMensaje, String sFecha)
    {
        try{
            Imei = sImei;
            Trama = sTrama;
            Evento = sEvento;
            Mensaje = sMensaje;
            Fecha = sFecha;
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
            entity.execute(Imei, Trama, Evento, Mensaje, Fecha);
            if (resultPost = true) {
                Toast.makeText(this, "Ubicacion Realizada.", Toast.LENGTH_SHORT).show();
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
            HttpPost post = new HttpPost(Urls.URL_InsertRouteSkytechlogic);
            post.setHeader("content-type", "application/json");

            try {
                JSONObject data = new JSONObject();
                //Construimos el objeto cliente en formato JSON
                data.put("id_punto_destino", params[0]);
                data.put("id_objeto_externo", params[1]);
                data.put("latitud", params[2]);
                data.put("longitud", params[3]);
                data.put("fecha", params[4]);

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
