package com.project.myntratrojanhorse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ImageView b1= findViewById(R.id.vcall);

        b1.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),VideoCall.class));
        });

        ImageView b2= findViewById(R.id.chatbot);

        b2.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),ChatActivity.class));
        });

        Button b3=findViewById(R.id.upload);
        b3.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),DetectionActivity.class));
        });


    }
}