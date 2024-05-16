package com.example.appytb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity {
    //Button btnnext;
   // int progressInt=0;
    //ProgressBar probar = findViewById(R.id.progressBar);
//awal interface ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(FirstActivity.this, ChoiceActivity.class));
                finish();
            }
        },6000);



    }
}
