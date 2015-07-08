package com.sci.www.sci_appgeolocator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.sci.www.sci_appgeolocator.Adapters.DrawerAdapter;
import com.sci.www.sci_appgeolocator.Classes.DrawerItem;
import com.sci.www.sci_appgeolocator.Classes.GpsBo;
import com.sci.www.sci_appgeolocator.Services.Notifications;
import com.sci.www.sci_appgeolocator.Utils.CircleTransform;
import com.sci.www.sci_appgeolocator.Utils.ItemClickSupport;
import com.sci.www.sci_appgeolocator.Services.InsertMapping;
import java.util.ArrayList;
import com.squareup.picasso.Picasso;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends ActionBarActivity {

    // Objetos de Diseño
    Toolbar toolbar;
    ListView listView;
    GoogleMap mapa;
    String deviceId;
    Button btnActualizar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    FrameLayout statusBar, frameLayoutSetting1;
    RelativeLayout relativeLayoutScrollViewChild;
    ScrollView scrollViewNavigationDrawerContent;
    ViewTreeObserver viewTreeObserverNavigationDrawerScrollView;
    ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
    ItemClickSupport itemClickSupport1, itemClickSupport2, itemClickSupport3;
    ArrayList<DrawerItem> drawerItems1, drawerItems2, drawerItems3, drawerItemsSettings;
    RecyclerView.Adapter drawerAdapter1, drawerAdapter2, drawerAdapter3, drawerAdapterSettings;
    RecyclerView recyclerViewDrawer1, recyclerViewDrawer2, recyclerViewDrawer3, recyclerViewDrawerSettings;
    LinearLayoutManager linearLayoutManager, linearLayoutManager2, linearLayoutManager3, linearLayoutManagerSettings;
    TypedValue typedValueColorPrimary, typedValueTextColorPrimary, typedValueTextColorControlHighlight, typedValueColorBackground;
    private static PendingIntent pendingIntent;

    Button m_btnAlarma = null;

    //Variables
    float drawerHeight, scrollViewHeight;
    int colorPrimary, textColorPrimary, colorControlHighlight, colorBackground;

    //Objetos Gps
    LocationManager locationManager = null;

    //Classes
    private GpsBo classGps = new GpsBo();
    private Notifications  serviceNotifications = new Notifications();

    //Services
    private InsertMapping mBoundService = new InsertMapping();
    private Timer timer;

    //Shared Preferences
    SharedPreferences sharedPreferences;

    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    // Notification Count
    private int mNotificationCount;

    // Notification Text Elements
    private final CharSequence tickerText = "Prueba!";
    private final CharSequence contentTitle = "Prueba Notificacion Geolocalizador";
    private final CharSequence contentText = "Prueba Usted esta siendo notificado.!";

    // Notification Action Elements
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    // Notification Sound and Vibration on Arrival
    private Uri soundURI = Uri
            .parse("android.resource://course.examples.notification.statusbar/"
                    + R.raw.alarm_rooster);
    private long[] mVibratePattern = { 0, 200, 200, 300 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup toolbar and statusBar (really FrameLayout)
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Geolocator Project");
        statusBar = (FrameLayout) findViewById(R.id.statusBar);

        //setToolbar();
        // Setup navigation drawer
        setupNavigationDrawer();
        setupMap();
        setupListView();

        //btnActualizar = (Button)findViewById(R.id.BtnActualizar);
        //btnActualizar.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {
                //setupMap();
            //}
        //});

        try {
            timer = new Timer();
            SecondPlanTaskNotifications servicioNotificacion = new SecondPlanTaskNotifications();
            long timeEjecution = 600000;
            timer.scheduleAtFixedRate(servicioNotificacion, 0, timeEjecution);
        }
        catch (Exception ex)
        {
            Log.e("ServicioNotificaciones","Error!", ex);
        }
    }

    class SecondPlanTaskNotifications extends TimerTask{
        @Override
        public void run(){
            EjecutarNotificacion();
        }
    }

    //Cargar lista direcciones
    private void setupListView() {
        // Obtener el IMEI del telefono
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();

        // TODO: A traves del IEMI de l telefono verificar las visitas del usuario
        listView = (ListView) findViewById(R.id.listView1);

        TareaWSListar tarea = new TareaWSListar();
        tarea.execute(deviceId);
    }

    //Tarea Asincrona para cargar la lista de las direcciones dependientdo el usuario
    private class TareaWSListar extends AsyncTask<String,Integer,Boolean> {

        private String[] visitas;
        private String[] posiciones;

        protected Boolean doInBackground(String... params) {

            boolean resul = true;
            String id = params[0];

            HttpGet del =
                    new HttpGet("http://186.147.35.26:8082/DeveloperServices/api/ProgVisita/ObtenerVisitas?idUser=" + id);

            del.setHeader("content-type", "application/json");
            HttpClient httpClient = new DefaultHttpClient();

            try
            {
                HttpResponse resp = httpClient.execute(del);
                String respStr = EntityUtils.toString(resp.getEntity());

                JSONArray respJSON = new JSONArray(respStr);

                visitas = new String[respJSON.length()];
                posiciones = new String[respJSON.length()];

                for(int i=0; i<respJSON.length(); i++)
                {
                    JSONObject obj = respJSON.getJSONObject(i);

                    int idVisitaPk = obj.getInt("progvis_IdVisita_Pk");
                    String Direccion = obj.getString("progvis_Direccion");
                    String Latitud = obj.getString("progvis_Latitud");
                    String Longitud = obj.getString("progvis_Longitd");

                    visitas[i] = "" + idVisitaPk + "-" + Direccion;
                    posiciones[i] = Latitud + "," + Longitud;
                }
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }

            return resul;
        }

        protected void onPostExecute(Boolean result) {

            if (result)
            {
                //Rellenamos la lista con los nombres de los clientes
                //Rellenamos la lista con los resultados
                ArrayAdapter<String> adapter= new ArrayAdapter<String>(HomeActivity.this,android.R.layout.simple_list_item_single_choice,visitas);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String Objeto[] = posiciones[position].split(",");
                        String Latitud = Objeto[0];
                        String Longitud = Objeto[1];
                        Toast.makeText(getApplicationContext(), "Latitud:  " + Latitud + ", Longitud: " + Longitud, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    //Instalar el mapa
    private void setupMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapa = supportMapFragment.getMap();
        // establecer tipo de mapa
        //mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.setMyLocationEnabled(true);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                updateLocation(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) {}
        };
        try{
            boolean isActive = locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER));
            if(isActive){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
            //updateLocation(mapa.getMyLocation());
        }
        catch(Exception ex){
            Log.e("Error", ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    //Actualizar Ubicacion
    private void updateLocation(Location location) {
        try {
            TextView tv = (TextView) findViewById(R.id.txtGeolocation);
            if (location != null) {
                location = mapa.getMyLocation();
                //navigator.geolocation.getCurrentPosition(funcExito, funcError, opciones);
                String locationText = "Latitud :" + location.getLatitude() + "/ Longitud :" + location.getLongitude() +
                        "/ Velocidad: " + location.getSpeed() + "/ Altitud: " + location.getAltitude() + "/ Status: " + classGps.onGpsStatus(GpsStatus.GPS_EVENT_SATELLITE_STATUS);;

                tv.setText(locationText);

                //String IdVisita = "1";
                //String IdUsuario = "354984054602948".toString();
                //String Imei = deviceId;
                //String Longitud = "" + location.getLongitude();
                //String Latitud = "" + location.getLatitude();
                //String Velocidad = "" + location.getSpeed();
                //String Altitud = "" + location.getAltitude();
                //String Rumbo = "-53.000000";
                //String Fecha = "" + location.getTime();
                //String Hora = "" + location.getTime();
                //String EstadoGps = "" + GpsStatus.GPS_EVENT_SATELLITE_STATUS;

                //boolean parameterInyection = mBoundService.ParametersInyection(IdVisita.toString(), IdUsuario.toString(), Imei.toString(), Longitud.toString(),
                  //Latitud.toString(), Velocidad.toString(), Altitud.toString(), Rumbo.toString(), Fecha.toString(), Hora.toString(), EstadoGps.toString());

                //if(parameterInyection == true)
                //{
                    //PendingIntent pendingResult = createPendingResult(
                            //0, new Intent(), 0);
                    //Intent intent = new Intent(getApplicationContext(), InsertMapping.class);
                    //startService(intent);
                //}
                //else
                //{
                    //tv.setText("Sin datos");
                //}
            } else {
                tv.setText("Sin datos");
            }
        }
        catch(Exception ex){
            Log.e("Error", ex.getMessage());
        }
    }

    private void EjecutarNotificacion()
    {
        try {
            //Intent myIntent = new Intent(HomeActivity.this, Notifications.class);
            //pendingIntent = PendingIntent.getService(HomeActivity.this, 0, myIntent, 0);
            Class<HomeActivity> activity = HomeActivity.class;
            mNotificationIntent = new Intent(getApplicationContext(),
                    activity);
            mContentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

            Notification.Builder notificationBuilder = new Notification.Builder(
                    getApplicationContext())
                    .setTicker(tickerText)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(
                            contentText + " (" + ++mNotificationCount + ")")
                    .setContentIntent(mContentIntent).setSound(soundURI)
                    .setVibrate(mVibratePattern);

            // Pass the Notification to the NotificationManager:
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(MY_NOTIFICATION_ID,
                    notificationBuilder.build());
            //serviceNotifications.SendNotification();

        }
        catch (Exception Ex){
            Toast.makeText(HomeActivity.this, "A ocurrido un error", Toast.LENGTH_LONG).show();
        }
    }

    //Instalar Thema por defecto de la app :).
    public void setupTheme() {
        // TODO: Desde un servicio llenar las preferencias de tema del usuario
        sharedPreferences = getSharedPreferences("VALUES", MODE_PRIVATE);
        switch (sharedPreferences.getString("THEME", "INDIGOLIGHT")) {
            case "REDLIGHT":
                setTheme(R.style.AppThemeRedLight);
                break;
            case "REDDARK":
                setTheme(R.style.AppThemeRedDark);
                break;
            case "INDIGOLIGHT":
                setTheme(R.style.AppThemeIndigoLight);
                break;
            case "INDIGODARK":
                setTheme(R.style.AppThemeIndigoDark);
                break;
        }
    }

    //Menu, De aqui hasta el final son metodos para el Menu.
    public void setupNavigationDrawer() {
        // Setup Navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Setup Drawer Icon
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();

        String urlPictureMain, urlCoverMain, urlPictureSecond;
        //TODO: Recoger estas variables a traves de un servicio
        urlPictureMain = "https://lh3.googleusercontent.com/gnejDEWM0uGk0dcz2xTvb0DnDRwZKNWzV79y17ERHx0B=w538-h955-no";
        urlCoverMain = "https://lh6.googleusercontent.com/-2RQc20WjV-8/VRIFuAiNzbI/AAAAAAAAArk/dEZZEWJqkUs/w1474-h829-no/cover-no-words.png";
        urlPictureSecond = "https://lh3.googleusercontent.com/-fIIhUhsMF3k/AAAAAAAAAAI/AAAAAAAAAp4/RPUESUibS6U/s120-c/photo.jpg";

        ImageView imageViewPictureMain, imageViewCoverMain, imageViewPictureSecond;
        imageViewPictureMain = (ImageView) findViewById(R.id.imageViewPictureMain);
        imageViewCoverMain = (ImageView) findViewById(R.id.imageViewCover);
        imageViewPictureSecond = (ImageView) findViewById(R.id.imageViewPictureSecond);

        Picasso.with(getApplicationContext()).load(urlPictureMain).transform(new CircleTransform()).into(imageViewPictureMain);
        Picasso.with(getApplicationContext()).load(urlCoverMain).into(imageViewCoverMain);
        Picasso.with(getApplicationContext()).load(urlPictureSecond).transform(new CircleTransform()).into(imageViewPictureSecond);

        TypedValue typedValue = new TypedValue();
        HomeActivity.this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        final int color = typedValue.data;
        drawerLayout.setStatusBarBackgroundColor(color);

        // no se necesita por el momento (dropdownToogle)
        //toggleButtonDrawer = (ToggleButton) findViewById(R.id.toggleButtonDrawer);
        //toggleButtonDrawer.setOnClickListener(this);

        // Hide Settings and Feedback buttons when navigation drawer is scrolled
        hideNavigationDrawerSettingsAndFeedbackOnScroll();

        // Setup RecyclerViews inside drawer
        setupNavigationDrawerRecyclerViews();
    }

    private void hideNavigationDrawerSettingsAndFeedbackOnScroll() {

        scrollViewNavigationDrawerContent = (ScrollView) findViewById(R.id.scrollViewNavigationDrawerContent);
        relativeLayoutScrollViewChild = (RelativeLayout) findViewById(R.id.relativeLayoutScrollViewChild);
        frameLayoutSetting1 = (FrameLayout) findViewById(R.id.frameLayoutSettings1);

        viewTreeObserverNavigationDrawerScrollView = relativeLayoutScrollViewChild.getViewTreeObserver();

        if (viewTreeObserverNavigationDrawerScrollView.isAlive()) {
            viewTreeObserverNavigationDrawerScrollView.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    if (Build.VERSION.SDK_INT > 16) {
                        relativeLayoutScrollViewChild.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        relativeLayoutScrollViewChild.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }

                    drawerHeight = relativeLayoutScrollViewChild.getHeight();
                    scrollViewHeight = scrollViewNavigationDrawerContent.getHeight();

                    if (drawerHeight > scrollViewHeight) {
                        frameLayoutSetting1.setVisibility(View.VISIBLE);
                    }

                    if (drawerHeight < scrollViewHeight) {
                        frameLayoutSetting1.setVisibility(View.GONE);
                    }
                }
            });
        }

        onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                scrollViewNavigationDrawerContent.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_MOVE:
                                if (scrollViewNavigationDrawerContent.getScrollY() != 0) {
                                    frameLayoutSetting1.animate().translationY(frameLayoutSetting1
                                            .getHeight()).setInterpolator(new AccelerateInterpolator(5f)).setDuration(400);
                                }
                                break;

                            case MotionEvent.ACTION_UP:
                                if (scrollViewNavigationDrawerContent.getScrollY() != 0) {
                                    frameLayoutSetting1.animate().translationY(frameLayoutSetting1
                                            .getHeight()).setInterpolator(new AccelerateInterpolator(5f)).setDuration(400);
                                }
                                break;
                        }
                        return false;
                    }
                });

                if (scrollViewNavigationDrawerContent.getScrollY() == 0) {
                    frameLayoutSetting1.animate().translationY(0)
                            .setInterpolator(new DecelerateInterpolator(5f)).setDuration(600);
                }
            }
        };

        scrollViewNavigationDrawerContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ViewTreeObserver observer;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        observer = scrollViewNavigationDrawerContent.getViewTreeObserver();
                        observer.addOnScrollChangedListener(onScrollChangedListener);
                        break;

                    case MotionEvent.ACTION_UP:
                        observer = scrollViewNavigationDrawerContent.getViewTreeObserver();
                        observer.addOnScrollChangedListener(onScrollChangedListener);
                        break;
                }

                return false;
            }
        });
    }

    private void setupNavigationDrawerRecyclerViews() {
        // RecyclerView 1
        recyclerViewDrawer1 = (RecyclerView) findViewById(R.id.recyclerViewDrawer1);
        linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        recyclerViewDrawer1.setLayoutManager(linearLayoutManager);

        drawerItems1 = new ArrayList<>();
        drawerItems1.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_content_inbox), "Bandeja"));
        drawerItems1.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_device_access_time), "Servicios"));
        drawerItems1.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_navigation_check), "Servicios realizados"));

        drawerAdapter1 = new DrawerAdapter(drawerItems1);
        recyclerViewDrawer1.setAdapter(drawerAdapter1);

        recyclerViewDrawer1.setMinimumHeight(convertDpToPx(144));
        recyclerViewDrawer1.setHasFixedSize(true);

        // RecyclerView 2
        recyclerViewDrawer2 = (RecyclerView) findViewById(R.id.recyclerViewDrawer2);
        linearLayoutManager2 = new LinearLayoutManager(HomeActivity.this);
        recyclerViewDrawer2.setLayoutManager(linearLayoutManager2);

        drawerItems2 = new ArrayList<>();
        drawerItems2.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_content_drafts), "Historial"));
        drawerItems2.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_content_send), "Enviar mensaje"));
        drawerItems2.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_social_notifications_on), "Recordatorios"));

        drawerAdapter2 = new DrawerAdapter(drawerItems2);
        recyclerViewDrawer2.setAdapter(drawerAdapter2);

        recyclerViewDrawer2.setMinimumHeight(convertDpToPx(144));
        recyclerViewDrawer2.setHasFixedSize(true);

        // RecyclerView 3
        recyclerViewDrawer3 = (RecyclerView) findViewById(R.id.recyclerViewDrawer3);
        linearLayoutManager3 = new LinearLayoutManager(HomeActivity.this);
        recyclerViewDrawer3.setLayoutManager(linearLayoutManager3);

        drawerItems3 = new ArrayList<>();
        drawerItems3.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_action_label), "En servicio"));
        drawerItems3.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_action_label), "Desconectar"));
        drawerItems3.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_action_label), "Servicio Finalizado"));

        drawerAdapter3 = new DrawerAdapter(drawerItems3);
        recyclerViewDrawer3.setAdapter(drawerAdapter3);

        recyclerViewDrawer3.setMinimumHeight(convertDpToPx(144));
        recyclerViewDrawer3.setHasFixedSize(true);

        // RecyclerView Settings
        recyclerViewDrawerSettings = (RecyclerView) findViewById(R.id.recyclerViewDrawerSettings);
        linearLayoutManagerSettings = new LinearLayoutManager(HomeActivity.this);
        recyclerViewDrawerSettings.setLayoutManager(linearLayoutManagerSettings);

        drawerItemsSettings = new ArrayList<>();
        drawerItemsSettings.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_action_settings), "Configuracion"));
        drawerItemsSettings.add(new DrawerItem(getResources().getDrawable(R.drawable.ic_action_action_help), "Ayuda"));

        drawerAdapterSettings = new DrawerAdapter(drawerItemsSettings);
        recyclerViewDrawerSettings.setAdapter(drawerAdapterSettings);

        recyclerViewDrawerSettings.setMinimumHeight(convertDpToPx(96));
        recyclerViewDrawerSettings.setHasFixedSize(true);

        // Why have I to calc recyclerView height?
        // Because recyclerView at this moment doesn't support wrap_content, this cause an height of 0 px

        // Get colorPrimary, textColorPrimary, colorControlHighlight and background to apply to selected items
        typedValueColorPrimary = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValueColorPrimary, true);
        colorPrimary = typedValueColorPrimary.data;

        typedValueTextColorPrimary = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValueTextColorPrimary, true);
        textColorPrimary = typedValueTextColorPrimary.data;

        typedValueTextColorControlHighlight = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorControlHighlight, typedValueTextColorControlHighlight, true);
        colorControlHighlight = typedValueTextColorControlHighlight.data;

        typedValueColorBackground = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorBackground, typedValueColorBackground, true);
        colorBackground = typedValueColorBackground.data;

        // Set icons alpha at start
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after some time
                for (int i = 0; i < recyclerViewDrawer1.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    if (i == 0) {
                        imageViewDrawerItemIcon.setColorFilter(colorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(255);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(255);
                        }
                        textViewDrawerItemTitle.setTextColor(colorPrimary);
                        linearLayoutItem.setBackgroundColor(colorControlHighlight);
                    } else {
                        imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(138);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(138);
                        }
                        textViewDrawerItemTitle.setTextColor(textColorPrimary);
                        linearLayoutItem.setBackgroundColor(colorBackground);
                    }
                }
                for (int i = 0; i < recyclerViewDrawer2.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
                for (int i = 0; i < recyclerViewDrawer3.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(67);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(67);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
                for (int i = 0; i < recyclerViewDrawerSettings.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawerSettings.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawerSettings.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawerSettings.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }

                ImageView imageViewSettingsIcon = (ImageView) findViewById(R.id.imageViewSettingsIcon);
                TextView textViewSettingsTitle = (TextView) findViewById(R.id.textViewSettingsTitle);
                imageViewSettingsIcon.setColorFilter(textColorPrimary);
                if (Build.VERSION.SDK_INT > 15) {
                    imageViewSettingsIcon.setImageAlpha(138);
                } else {
                    imageViewSettingsIcon.setAlpha(138);
                }
                textViewSettingsTitle.setTextColor(textColorPrimary);
                ImageView imageViewHelpIcon = (ImageView) findViewById(R.id.imageViewHelpIcon);
                TextView textViewHelpTitle = (TextView) findViewById(R.id.textViewHelpTitle);
                imageViewHelpIcon.setColorFilter(textColorPrimary);
                if (Build.VERSION.SDK_INT > 15) {
                    imageViewHelpIcon.setImageAlpha(138);
                } else {
                    imageViewHelpIcon.setAlpha(138);
                }
                textViewHelpTitle.setTextColor(textColorPrimary);
            }
        }, 250);

        itemClickSupport1 = ItemClickSupport.addTo(recyclerViewDrawer1);
        itemClickSupport1.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {

                setClickNavDrawer(parent, view, position, id, recyclerViewDrawer1);

                for (int i = 0; i < recyclerViewDrawer1.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    if (i == position) {
                        imageViewDrawerItemIcon.setColorFilter(colorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(255);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(255);
                        }
                        textViewDrawerItemTitle.setTextColor(colorPrimary);
                        linearLayoutItem.setBackgroundColor(colorControlHighlight);
                    } else {
                        imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(138);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(138);
                        }
                        textViewDrawerItemTitle.setTextColor(textColorPrimary);
                        linearLayoutItem.setBackgroundColor(colorBackground);
                    }
                }
                for (int i = 0; i < recyclerViewDrawer2.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
                for (int i = 0; i < recyclerViewDrawer3.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(67);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(67);
                    }
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
            }
        });

        itemClickSupport2 = ItemClickSupport.addTo(recyclerViewDrawer2);
        itemClickSupport2.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {

                setClickNavDrawer(parent, view, position, id, recyclerViewDrawer2);

                for (int i = 0; i < recyclerViewDrawer2.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    if (i == position) {
                        imageViewDrawerItemIcon.setColorFilter(colorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(255);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(255);
                        }
                        textViewDrawerItemTitle.setTextColor(colorPrimary);
                        linearLayoutItem.setBackgroundColor(colorControlHighlight);
                    } else {
                        imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(138);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(138);
                        }
                        textViewDrawerItemTitle.setTextColor(textColorPrimary);
                        linearLayoutItem.setBackgroundColor(colorBackground);
                    }
                }
                for (int i = 0; i < recyclerViewDrawer1.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
                for (int i = 0; i < recyclerViewDrawer3.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(67);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(67);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
            }
        });
        itemClickSupport3 = ItemClickSupport.addTo(recyclerViewDrawer3);
        itemClickSupport3.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                setClickNavDrawer(parent, view, position, id, recyclerViewDrawer3);
                for (int i = 0; i < recyclerViewDrawer3.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer3.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    if (i == position) {
                        imageViewDrawerItemIcon.setColorFilter(colorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(138);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(138);
                        }
                        textViewDrawerItemTitle.setTextColor(colorPrimary);
                        linearLayoutItem.setBackgroundColor(colorControlHighlight);
                    } else {
                        imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                        if (Build.VERSION.SDK_INT > 15) {
                            imageViewDrawerItemIcon.setImageAlpha(67);
                        } else {
                            imageViewDrawerItemIcon.setAlpha(67);
                        }
                        textViewDrawerItemTitle.setTextColor(textColorPrimary);
                        linearLayoutItem.setBackgroundColor(colorBackground);
                    }
                }
                for (int i = 0; i < recyclerViewDrawer1.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer1.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
                for (int i = 0; i < recyclerViewDrawer2.getChildCount(); i++) {
                    ImageView imageViewDrawerItemIcon = (ImageView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.imageViewDrawerItemIcon);
                    TextView textViewDrawerItemTitle = (TextView) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.textViewDrawerItemTitle);
                    LinearLayout linearLayoutItem = (LinearLayout) recyclerViewDrawer2.getChildAt(i).findViewById(R.id.linearLayoutItem);
                    imageViewDrawerItemIcon.setColorFilter(textColorPrimary);
                    if (Build.VERSION.SDK_INT > 15) {
                        imageViewDrawerItemIcon.setImageAlpha(138);
                    } else {
                        imageViewDrawerItemIcon.setAlpha(138);
                    }
                    textViewDrawerItemTitle.setTextColor(textColorPrimary);
                    linearLayoutItem.setBackgroundColor(colorBackground);
                }
            }
        });
    }

    private void setClickNavDrawer(RecyclerView parent, View view, int position, long id, RecyclerView rv) {
        ImageView imageViewDrawerItemIcon = (ImageView) rv.getChildAt(position).findViewById(R.id.imageViewDrawerItemIcon);
        TextView textViewDrawerItemTitle = (TextView) rv.getChildAt(position).findViewById(R.id.textViewDrawerItemTitle);
        LinearLayout linearLayoutItem = (LinearLayout) rv.getChildAt(position).findViewById(R.id.linearLayoutItem);
    }

    public int convertDpToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}
