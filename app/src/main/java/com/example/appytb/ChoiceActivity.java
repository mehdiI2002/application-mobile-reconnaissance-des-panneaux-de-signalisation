package com.example.appytb;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ChoiceActivity extends AppCompatActivity {
          Button btnImage;
          Button btnVideo;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        btnImage=findViewById(R.id.btnImage);
        btnVideo=findViewById(R.id.btnVideo);


        btnImage.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        btnVideo.setOnClickListener(view -> {
            Intent intent2 = new Intent(this, ThirdActivity3.class);
            startActivity(intent2);
        });
    }
}