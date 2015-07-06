package com.sci.www.sci_appgeolocator.Services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class InsertMapping extends Service {
    //Tarea As íncrona para llamar al WS de inserción en segundo plano
    public class PostInsertMapping extends AsyncTask<String,Integer,Boolean> {
        protected Boolean doInBackground(String... params) {
            boolean resul = true;

            HttpClient httpClient = new DefaultHttpClient();
            //HttpPost post = new HttpPost("http://192.168.0.100:8082/DeveloperServices/api/Usuarios/InsertAgente");
            HttpPost post = new HttpPost("http://186.147.35.26:8082/DeveloperServices/api/Trazo/InsertTrazoVisita");
            post.setHeader("content-type", "application/json");

            try
            {
                JSONObject data = new JSONObject();

                //Construimos el objeto cliente en formato JSON
                //dato.put("Id", Integer.parseInt(txtId.getText().toString()));
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

                for(int i=0; i<respJSON.length(); i++)
                {
                    JSONObject obj = respJSON.getJSONObject(i);

                    state = obj.getBoolean("State");
                    httpStatusCode = obj.getString("HttpStatusCode");
                    codError = obj.getInt("CodError");
                    descripcionError = obj.getString("DescripcionError");
                }
                resul = state;
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest", "Error!", ex);
                resul = false;
            }

            return resul;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
