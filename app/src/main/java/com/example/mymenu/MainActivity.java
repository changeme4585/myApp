package com.example.mymenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
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
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity{
    EditText usernameInput;
    EditText passwordInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                if(username.equals("master")&&!password.equals("")) { //아이디가 마스터이고 비밀번호가 공란이 아닐때
                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                mStartForResult.launch(intent);
                }
                else if (username.equals("")){
                    Toast myToast = Toast.makeText(getApplicationContext(),"아이디를 입력해주세요",Toast.LENGTH_LONG);
                    myToast.show();
                }
                else if (password.equals("")){
                    Toast myToast = Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요",Toast.LENGTH_LONG);
                    myToast.show();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    mStartForResult.launch(intent);
                }
            }
        });

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //result.getResultCode()를 통하여 결과값 확인
                if(result.getResultCode() == RESULT_OK) {  //응답이 왔을때 (Back버튼을 눌렀을때)
                    passwordInput.setText("");
                    usernameInput.setText("");
                }
                if(result.getResultCode() == RESULT_CANCELED){
                    //ToDo
                }
            }
    );
}
