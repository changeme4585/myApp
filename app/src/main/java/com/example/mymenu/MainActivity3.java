package com.example.mymenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity3 extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{
    DrawerLayout drawer;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    Handler handler= new Handler();;
    String API_Key = "l7xxb258efb177f344c38e5c7d9a262e250e";
    Double latitude;
    Double longitude;
    // T Map View
    Timer timer;
    TMapView tMapView = null;
    TMapGpsManager tMapGPS = null;
    Cursor cursor3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        drawer =  findViewById(R.id.drawer) ;
        tMapView = new TMapView(this);
        tMapGPS = new TMapGpsManager(this);
        // API Key
        tMapView.setSKTMapApiKey(API_Key);
        timer = new Timer();
        // Initial Setting
        tMapView.setZoomLevel(13);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        LinearLayout map =  (LinearLayout)findViewById(R.id.qqq);
        tMapView.setCenterPoint(128.5911308712179, 35.84377679724957);
        map.addView(tMapView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

// GPS using T Map
        tMapGPS = new TMapGpsManager(this);
        tMapGPS.setMinTime(1);
        tMapGPS.setMinDistance(1);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
//tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);
        tMapGPS.OpenGps();
        myHelper = new myDBHelper(this.getApplicationContext());
        sqlDB = myHelper.getWritableDatabase();
        myHelper.onCreate(sqlDB);
        //????????? ??? ?????? ?????? ?????? ??????
        Intent receivedIntent = getIntent();
        String username = receivedIntent.getStringExtra("username");  //??????????????? ??????
        Toast myToast = Toast.makeText(getApplicationContext(),username,Toast.LENGTH_LONG);
        myToast.show();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            //tMapView.setLocationPoint(longitude,latitude);
            //tMapView.setCenterPoint( longitude,latitude);
            GPSListener gpsListener  = new GPSListener();

            long minTime =5;
            float minDistance = 1;
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,gpsListener);
        }catch (SecurityException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }


        try {
            readData();  //csv?????? ???????????? ??????
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Button nav = findViewById(R.id.navbtn);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        Button backButton1 = findViewById(R.id.button);
        backButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {  //?????? ????????? ????????????????????? ?????? ?????? ?????? ???????????? ?????? ?????? ?????? (??????!!!)
                String aaa = "";
                int cnt = 0;
                for (int i = 0;i<qqq.size();i++) {
                    aaa = "markerItem";
                    cnt++;
                    aaa += Integer.toString(cnt);
                    TMapMarkerItem markerItem1 = new TMapMarkerItem(); //????????????????????? ?????? ????????? ??????
                    TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(qqq.get(i).getRow()), Double.parseDouble(qqq.get(i).getCol())); //
                    markerItem1.setPosition(0.5f, 1.0f); // ????????? ???????????? ??????, ???????????? ??????
                    markerItem1.setTMapPoint(tMapPoint1); // ????????? ?????? ??????
                    markerItem1.setName("??????"); // ????????? ????????? ??????
                    tMapView.addMarkerItem(aaa, markerItem1); //aaa??? ????????? ?????? ID???(?????? ?????? ?????????????????? ?????? ????????????)
                }
            }
        });
        Button backButton2 = findViewById(R.id.button2);
        backButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cnt =0;
                String aaa="";
                while(cnt !=qqq.size()) {  //?????? ??????
                    aaa="markerItem";
                    cnt++;
                    aaa+=Integer.toString(cnt);
                    tMapView.removeMarkerItem(aaa);  //????????? ?????? ?????? ?????????
                }
            }
        });
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    TimerTask TT = new TimerTask() {
                        @Override
                        public void run() {
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdf = new SimpleDateFormat("hh/mm/ss");
                            SimpleDateFormat fds = new SimpleDateFormat("MM,dd");
                            String getTime = sdf.format(date);
                            String getDate =fds.format(date);
                            InsertData(String.valueOf(latitude), String.valueOf(longitude),getDate,getTime,username);
                        }
                    };
                    timer.schedule(TT, 0, 50); //Timer ??????
                }catch (Exception e){
                    Log.i("????????????",e.toString());
                }
            }
        });
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();//?????????
            }
        });

        Button drop  = findViewById(R.id.drop);
        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB.execSQL("DROP TABLE IF EXISTS groupTBL");
                myHelper.onCreate(sqlDB);
            }
        });
        Button button7 = findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", "result message is OK!");

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
    private List<Sample> qqq = new ArrayList<>();
    public  void readData() throws IOException {
        InputStream is  = getResources().openRawResource(R.raw.cctvdata);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("EUC-KR"))
        );
        String line="";
        reader.readLine();
        while( (line = reader.readLine()) != null) {
            String[] tok = line.split(",");
            int idx = tok.length;
            Sample sample = new Sample();
            if (tok[idx - 2].length() > 0) {
                sample.setRow(tok[idx - 2]);
            }
            if (tok[idx - 1].length() > 0) {
                sample.setCol(tok[idx - 1]);
            }
            qqq.add(sample);
        }
    }



    public  Double[] ShowData(){ //?????????????????? ??????  ???????????? ??????
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //???????????????????????? ???????????? ?????? ?????????
        int cnt=0;
        Double[] tmp = new Double[cursor3.getCount()*2];
        while (cursor3.moveToNext()) { //?????? ??????
//            t
//            tmp[cnt++]=Double.parseDouble(cursor3.getString(0));
//            tmp[cnt++]=Double.parseDouble(cursor3.getString(1));
            Log.d("????????????",cursor3.getString(3));
        }
        return  tmp;
    }
    public void InsertData (String aa  , String bb,String cc, String dd,String ee) {
        try {
            sqlDB.execSQL("insert into " + "groupTBL" +
                    "(gNAME , gNUMBER,gDate,gTime,gId) values ('" + aa + "', '" + bb + "','" + cc + "','" + dd + "','"+ee+"');");
        }catch (Exception e) {
         Log.d("??????",e.toString());
        }
    }
    public void Calcuate() {  //??????????????? , ???????????? , ?????? ??????
        double distance =0.0; // ????????? ?????????
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //???????????????????????? ???????????? ?????? ?????????
        List<List<Double>> locPos = new ArrayList<>();
        while (cursor3.moveToNext()) {
            List<Double> tmp = new ArrayList<>();
            tmp.add(Double.parseDouble(cursor3.getString(0)));
            tmp.add(Double.parseDouble(cursor3.getString(1)));
            locPos.add(tmp);
        }
        for(int idx = 0;idx<locPos.size()-1;idx++){
            Location locationA = new Location( "point B" );
            locationA.setLatitude(locPos.get(idx).get(0));
            locationA.setLongitude(locPos.get(idx).get(1));

            Location locationB = new Location( "point B" );
            locationB.setLatitude(locPos.get(idx).get(0));
            locationB.setLongitude(locPos.get(idx).get(1));
            distance += locationA.distanceTo( locationB );
        }
        Toast myToast = Toast.makeText(getApplicationContext(),String.valueOf(distance)+"M",Toast.LENGTH_LONG);
        myToast.show();
    }
    @Override
    public void onLocationChange(Location location) {
        //latitude = location.getLatitude();
        //longitude= location.getLongitude();
    }
    private class myDBHelper extends SQLiteOpenHelper {
        public  myDBHelper (Context context) {
            super(context,"groupDB",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //?????? ????????? ??????????????? ????????? ???????????? ??????????????? 10?????? ?????????
            db.execSQL("CREATE TABLE if not exists  " +
                    "groupTBL (gNAME CHAR(100) PRIMARY KEY, " +
                    "gNUMBER CHAR(100)," +
                    "gDate CHAR(100)," +
                    "gTime CHAR(100)," +
                    "gId CHAR(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS groupTBL");
            //onCreate(db);
        }
    }
    class  GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }
        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
        public void onStatusChanged(String provider, int status, Bundle extras){

        }
    }
}
