package com.sci.www.sci_appgeolocator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.sci.www.sci_appgeolocator.Classes.DrawerItem;
import com.sci.www.sci_appgeolocator.Classes.Login;
import com.sci.www.sci_appgeolocator.Repository.Urls;
import com.sci.www.sci_appgeolocator.Utils.ItemClickSupport;

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
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private static long SLEEP_TIME = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        //String deviceId = "354984054602948";
        //String deviceId = "454984054602948";

        setToolbar();
        new RestOperation().execute(Urls.URL_ImeiIsExist, deviceId);
    }

    public void run() {
        try {
            // Sleeping
            Thread.sleep(SLEEP_TIME*2000);
            //startActivity(new Intent(this, LoginActivity.class));
        } catch (Exception e) {

        }
        //MainActivity.this.startActivity(intent);
        //MainActivity.this.finish();
    }

    private class RestOperation  extends AsyncTask<String,Void,Void> {
        String content;
        String error;
        String data="";
        AlertDialog.Builder alerBuilder= new AlertDialog.Builder(MainActivity.this);

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
            run();
            startActivity(new Intent(this, HomeActivity.class));
        }
        else{
            run();
            startActivity(new Intent(this, LoginActivity.class));
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
    public void setToolbar(){
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Geolocator");
        setSupportActionBar(toolbar);
    }

}
