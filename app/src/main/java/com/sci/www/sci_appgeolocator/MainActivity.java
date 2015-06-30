package com.sci.www.sci_appgeolocator;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


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
