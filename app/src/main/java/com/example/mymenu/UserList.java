package com.example.mymenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class UserList extends AppCompatActivity {
    CustomerAdapter adapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        recyclerView = findViewById(R.id.recyclerView);

        Intent receivedIntent = getIntent();
        //데이터베이스의 유저목록을 인텐트로 받음
        ArrayList<String> list = (ArrayList<String>) receivedIntent .getSerializableExtra("list");
        //각 유저의 총 이동거리를 인텐트로 받음
        ArrayList<Double> dis = (ArrayList<Double>) receivedIntent .getSerializableExtra("distance");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CustomerAdapter();
        for (int i=0;i<list.size();i++){
            String ans= String.valueOf(i+1);
            int aaa= (int)Math.round(dis.get(i));
            adapter.addItem(new Customer(list.get(i),ans,"이동거리: "+String.valueOf((aaa)+"M")));
        }
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnCustomerItemClickListener() {
            @Override
            public void onItemClick(CustomerAdapter.ViewHolder holder, View view, int position) {
                Customer item = adapter.getItem(position);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", item.getName());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                //Toast.makeText(getApplicationContext(), "아이템 선택됨 : " + item.getName(), Toast.LENGTH_LONG).show();
            }
        });
        Button button8 = findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //user만 선택하고 리턴해주는 형식으로 ㄲ
                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", "성공");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

    }
}