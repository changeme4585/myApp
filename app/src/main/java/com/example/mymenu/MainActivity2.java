package com.example.mymenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class MainActivity2 extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{
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
    String userId= "no";
    @Override
    protected void onCreate(Bundle savedInstanceState) {  ///마스터 액티비티
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        drawer =  findViewById(R.id.drawer) ;
        tMapView = new TMapView(this);
        tMapGPS = new TMapGpsManager(this);
        // API Key
        tMapView.setSKTMapApiKey(API_Key);
        timer = new Timer();
        // Initial Setting
        tMapView.setZoomLevel(12);
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
        List<String> cctvMarkerId  = new ArrayList<>();
        try {
            readData();  //csv파일 불러오는 코드
        } catch (Exception e) {
            Log.d("라인 ",e.toString());
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
            public void onClick(View v) {
                String aaa = "";
                int cnt = 0;
                for (int i = 0;i<qqq.size();i++) {
                    aaa = "markerItem";
                    cnt++;
                    aaa += Integer.toString(cnt);
                    TMapMarkerItem markerItem1 = new TMapMarkerItem(); //무한반복문으로 마커 하나씩 생성
                    TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(qqq.get(i).getRow()), Double.parseDouble(qqq.get(i).getCol())); //
                    markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
                    markerItem1.setName("ㅃㅃ"); // 마커의 타이틀 지정
                    tMapView.addMarkerItem(aaa, markerItem1); //aaa는 마커의 고유 ID값(마커 매번 생성할때마다 전부 달라야함)
                }
            }
        });
        Button backButton2 = findViewById(R.id.button2);
        backButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cnt =0;
                String aaa="";
                while(cnt !=qqq.size()) {  //마커 삭제
                    aaa="markerItem";
                    cnt++;
                    aaa+=Integer.toString(cnt);
                    tMapView.removeMarkerItem(aaa);  //인자가 마커 고유 아이디
                }
            }
        });
        Button button3 = findViewById(R.id.button3);  //회원목록 리사이클러뷰 버튼
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserList.class);
                ArrayList<String> list = userList();
                intent.putExtra("list",list);
                ArrayList<Double> dis = new ArrayList<>();
                for (int i =0 ; i<list.size();i++){
                   dis.add(Calculate(list.get(i)));
                }
                intent.putExtra("distance",dis);
                mStartForResult.launch(intent);
            }
        });

        Button button5 = findViewById(R.id.button5);//경로출력 버튼
        //경로 출력할때 이전 마커 다 지우기
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int realcnt = 0;
                if (userId.equals("no")) {
                    Toast myToast = Toast.makeText(getApplicationContext(), "조회할 ID를 선택해주세요", Toast.LENGTH_LONG);
                    myToast.show();
                    return;
                }
                //기존의 cctv마커 제거 코드
                for (int i = 0; i < cctvMarkerId.size(); i++) {
                    tMapView.removeMarkerItem(cctvMarkerId.get(i));  //인자가 마커 고유 아이디
                }
                double distance;
                String aaa = "";
                int cnt = 0;
                ArrayList<TMapPoint> alTMapPoint = new ArrayList<TMapPoint>();
                List<Double> codinage = ShowData(userId); //지나온 좌표값 담은 배열
                if (codinage.size() != 0) {
                    Toast myToast = Toast.makeText(getApplicationContext(), String.valueOf(codinage.size()), Toast.LENGTH_LONG);
                    myToast.show();
//                    Log.d("배열길이", String.valueOf(codinage.length));
                }
                if (codinage.size() <= 200){
                    for (int idx = 0; idx < codinage.size(); idx += 2) {
                        if (codinage.get(idx) == null || codinage.get(idx + 1) == null) {
                            Log.d("스킵에러", "에러");
                            continue;
                        }
                        realcnt++;
                        try {
                            alTMapPoint.add(new TMapPoint(codinage.get(idx), codinage.get(idx + 1)));
                        } catch (Exception e) {
                            Log.d("에러", e.toString());
                        }
                        //거리계산
                        for (int i = 0; i < qqq.size(); i++) {
                            aaa = "marker";
                            cnt++;
                            aaa += Integer.toString(cnt);
                            Location locationA = new Location("point A");
                            locationA.setLatitude(codinage.get(idx));
                            locationA.setLongitude(codinage.get(idx + 1));
                            Location locationB = new Location("point B");
                            locationB.setLatitude(Double.parseDouble(qqq.get(i).getRow()));
                            locationB.setLongitude(Double.parseDouble(qqq.get(i).getCol()));
                            distance = locationA.distanceTo(locationB);
                            if (distance <= 100) {  //100m 거리 내에 마커 찍는 코드
                                TMapMarkerItem markerItem1 = new TMapMarkerItem(); //무한반복문으로 마커 하나씩 생성
                                TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(qqq.get(i).getRow()), Double.parseDouble(qqq.get(i).getCol())); //
                                markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                                markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
                                markerItem1.setName("ㅃㅃ"); // 마커의 타이틀 지정
                                cctvMarkerId.add(aaa);
                                tMapView.addMarkerItem(aaa, markerItem1); //aaa는 마커의 고유 ID값(마커 매번 생성할때마다 전부 달라야함)
                            }
                        }
                    }//여기까지
            }
                else if(codinage.size() > 200){
                    int a = codinage.size()/200;
                    for (int idx = 0; idx < codinage.size(); idx += 2*a) {
                        if (codinage.get(idx) == null || codinage.get(idx + 1) == null) {
                            Log.d("스킵에러", "에러");
                            continue;
                        }
                        realcnt++;
                        try {
                            alTMapPoint.add(new TMapPoint(codinage.get(idx), codinage.get(idx + 1)));
                        } catch (Exception e) {
                            Log.d("에러", e.toString());
                        }
                        //거리계산
                        for (int i = 0; i < qqq.size(); i++) {
                            aaa = "marker";
                            cnt++;
                            aaa += Integer.toString(cnt);
                            Location locationA = new Location("point A");
                            locationA.setLatitude(codinage.get(idx));
                            locationA.setLongitude(codinage.get(idx + 1));
                            Location locationB = new Location("point B");
                            locationB.setLatitude(Double.parseDouble(qqq.get(i).getRow()));
                            locationB.setLongitude(Double.parseDouble(qqq.get(i).getCol()));
                            distance = locationA.distanceTo(locationB);
                            if (distance <= 100) {  //100m 거리 내에 마커 찍는 코드
                                TMapMarkerItem markerItem1 = new TMapMarkerItem(); //무한반복문으로 마커 하나씩 생성
                                TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(qqq.get(i).getRow()), Double.parseDouble(qqq.get(i).getCol())); //
                                markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                                markerItem1.setTMapPoint(tMapPoint1); // 마커의 좌표 지정
                                markerItem1.setName("ㅃㅃ"); // 마커의 타이틀 지정
                                cctvMarkerId.add(aaa);
                                tMapView.addMarkerItem(aaa, markerItem1); //aaa는 마커의 고유 ID값(마커 매번 생성할때마다 전부 달라야함)
                            }
                        }
                    }
                }
                TMapPolyLine tMapPolyLine = new TMapPolyLine();
                tMapPolyLine.setLineColor(Color.RED);
                tMapPolyLine.setLineWidth(1);
                for( int i=0; i<alTMapPoint.size(); i++ ) { //경로 라인 그리는 코드
                    tMapPolyLine.addLinePoint( alTMapPoint.get(i) );
                }
                int point = codinage.size()/2;
                if(codinage.get(point)>100){
                    tMapView.setCenterPoint(codinage.get(point), codinage.get(point+1));  //지도중심 잡기
                }
                else if (codinage.get(point+1) > 100) {
                    tMapView.setCenterPoint(codinage.get(point + 1), codinage.get(point));  //지도중심 잡기
                }
                tMapView.setZoomLevel(14);
                tMapView.addTMapPolyLine("Line1", tMapPolyLine);
//                Log.d("CCTV개수", String.valueOf(cctvMarkerId.size()));
                Log.d("마커개수", String.valueOf(codinage.size()));
                Toast myToast = Toast.makeText(getApplicationContext(),String.valueOf(realcnt),Toast.LENGTH_LONG);
                myToast.show();
            }
        });
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sqlDB.execSQL("DROP TABLE groupTBL");// DROP을 걸었기 때문에 select할려면 테이블 다시 create해야함
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
    @Override   //이게 진짜 중용한 코드(인덴트 넘긴 액티비티로부터 닥시 액티비티 받는 코드)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //보낸쪽에서 다시 인텐트 받는 코드
        userId = data.getExtras().getString("message");
    }
    public ArrayList<String> userList(){
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //데이터베이스에서 좌표정보 전부 가져옴
        ArrayList<String> tmp = new ArrayList<>();
        while(cursor3.moveToNext()){
            if(!tmp.contains(cursor3.getString(4))) {
                tmp.add(cursor3.getString(4));
            }
        }
        return tmp;
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
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //result.getResultCode()를 통하여 결과값 확인
                if(result.getResultCode() == RESULT_OK) {  //응답이 왔을때 (Back버튼을 눌렀을때)

                }
                if(result.getResultCode() == RESULT_CANCELED){
                    //ToDo
                }
            }
    );
    public  List<Double> ShowData(String id){ //데이터베이스 내용  문자열에 저장
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //데이터베이스에서 좌표정보 전부 가져옴
        int cnt=0;
        //Double[] tmp = new Double[cursor3.getCount()*2];
        List<Double> tmp = new ArrayList<Double>();
        while (cursor3.moveToNext()) { //마커 생성
            if(cursor3.getString(0).equals("null") ||cursor3.getString(1).equals("null")){
                continue;
            }
           if(cursor3.getString(4).equals(id) &&cursor3.getString(0)!=null && cursor3.getString(1)!=null) {
               tmp.add(Double.parseDouble(cursor3.getString(0)));
               tmp.add(Double.parseDouble(cursor3.getString(1)));
               //Log.d("시간",cursor3.getString(3));
           }
        }
//        Toast myToast = Toast.makeText(getApplicationContext(),String.valueOf(tmp.length),Toast.LENGTH_LONG);
//        myToast.show();
        Log.d("배열길이", String.valueOf(tmp.size()));
        return  tmp;
    }
    public int timePath() {  //총몇분 이동 했는지
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //데이터베이스에서 좌표정보 전부 가져옴
        List<Integer> tmp = new ArrayList<>();
        while (cursor3.moveToNext()) {
            String [] temp =cursor3.getString(3).split("/");
            tmp.add(Integer.valueOf(temp[0])*3600+Integer.valueOf(temp[1])*60+Integer.valueOf(temp[2]));
        }
        return tmp.get(tmp.size()-1) - tmp.get(0);
    }

    public Double Calculate(String id) {  //총이동거리 , 이동시간 , 평균 속력
        double distance =0.0; // 단위는 미터임
        cursor3 = sqlDB.rawQuery("SELECT * FROM groupTBL;", null); //데이터베이스에서 좌표정보 전부 가져옴
       List<List<Double>> locPos = new ArrayList<>();
        while (cursor3.moveToNext()) {
            List<Double> tmp = new ArrayList<>();
            if(cursor3.getString(0).equals("null") ||cursor3.getString(1).equals("null")){
                continue;
            }
            if(cursor3.getString(4).equals(id)) {
                tmp.add(Double.parseDouble(cursor3.getString(0)));
                tmp.add(Double.parseDouble(cursor3.getString(1)));
                locPos.add(tmp);
            }
        }
        for(int idx = 0;idx<locPos.size()-1;idx++){
            Location locationA = new Location( "point A" );
            locationA.setLatitude(locPos.get(idx).get(0));
            locationA.setLongitude(locPos.get(idx).get(1));

            Location locationB = new Location( "point B" );
            locationB.setLatitude(locPos.get(idx+1).get(0));
            locationB.setLongitude(locPos.get(idx+1).get(1));
            distance += locationA.distanceTo( locationB );
        }
//        Toast myToast = Toast.makeText(getApplicationContext(),String.valueOf(distance)+"M",Toast.LENGTH_LONG);
//        myToast.show();
        return distance;
    }
    @Override
    public void onLocationChange(Location location) {
        latitude = location.getLatitude();
        longitude= location.getLongitude();
//        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
//        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
//        Toast myToast = Toast.makeText(this.getApplicationContext(),String.valueOf(latitude)+String.valueOf(longitude), Toast.LENGTH_SHORT);
//        myToast.show();
    }
    private class myDBHelper extends SQLiteOpenHelper {
        public  myDBHelper (Context context) {
            super(context,"groupDB",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //위치 정보와 현재시간을 데이터 베이스에 저장할거임 10초에 한번씩
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
}
