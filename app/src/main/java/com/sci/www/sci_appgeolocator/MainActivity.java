package com.sci.www.sci_appgeolocator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        Typeface fontRobo = Typeface.createFromAsset(this.getAssets(), "fonts/moolbor_0.ttf");
        TextView tVBienvenidaSCI= (TextView) findViewById(R.id.tVBienvenidaSCI);
        tVBienvenidaSCI.setTypeface(fontRobo);
        TextView tVBienvenidaLocator= (TextView) findViewById(R.id.tVBienvenidaLocator);
        tVBienvenidaLocator.setTypeface(fontRobo);

        TextView tVBienvenidaService= (TextView) findViewById(R.id.tVBienvenidaService);
        tVBienvenidaService.setTypeface(fontRobo);
        new RestOperation().execute(UrlRepository.URL_ImeiIsExist, deviceId);
    }
    private class RestOperation  extends AsyncTask<String,Void,Void> {
        String content;
        String error;
        String data="";
        AlertDialog.Builder alerBuilder= new AlertDialog.Builder(MainActivity.this);
        ProgressBar pbInicial=(ProgressBar) findViewById(R.id.pbInicial);
        protected Void doInBackground(String...params){
            BufferedReader bf=null;
            try {
                HttpClient httpClient=new DefaultHttpClient();
                HttpGet httpGet=new HttpGet(params[0]+"?imeiDispositive="+params[1]);
                try
                {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    InputStream inputStream=httpResponse.getEntity().getContent();
                    bf= new BufferedReader(new InputStreamReader(inputStream));
                    StringBuffer sb=new StringBuffer();
                    String line=null;
                    while ((line=bf.readLine())!=null){
                        System.out.println(line);
                        sb.append(line);
                        sb.append(System.getProperty("line.separator"));

                    }
                    content=sb.toString();
                }
                catch(Exception ex)
                {
                    alerBuilder.setMessage(ex.getMessage());
                    alerBuilder.show();
                }
                HttpResponse httpResponse= httpClient.execute(httpGet);
                InputStream inputStream=httpResponse.getEntity().getContent();
                bf= new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer sb=new StringBuffer();
                String line=null;
                while ((line=bf.readLine())!=null){
                    System.out.println(line);
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));

                }
                content=sb.toString();
            } catch (MalformedURLException ex) {
                error=ex.getMessage();
                System.out.println(error);
            } catch (IOException ex) {
                error=ex.getMessage();
                System.out.println(error);
            }finally {
                try {
                    bf.close();
                } catch (IOException ex) {
                    System.out.println(error);
                }
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (error!=null){
                alerBuilder.setMessage(error);
                alerBuilder.show();
            }else{
                Boolean state;
                String descripcionError;
                JSONObject jsonObject;

                try {
                    jsonObject =new JSONObject(content);
                    state=jsonObject.getBoolean("State");
                    descripcionError=jsonObject.getString("DescripcionError");
                    if (state)
                    {
                        redirection(true);
                    }
                    else{
                        System.out.println(descripcionError);
                        if(descripcionError.equals("Ninguno"))
                        {
                            redirection(false);
                        }else{
                            alerBuilder.setMessage("Se ha Presentado un Error: "+descripcionError);
                            alerBuilder.show();
                        }

                    }
                } catch (JSONException ex) {
                    System.out.println(ex.getMessage());
                    alerBuilder.setMessage(ex.getMessage());
                    alerBuilder.show();
                }

            }
        }

    }
    private void redirection(boolean IsRegister){
        if (IsRegister)
        {
            startActivity(new Intent(this, HomeActivity.class));
        }
        else{
            startActivity(new Intent(this, RegisterActivity.class));
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
