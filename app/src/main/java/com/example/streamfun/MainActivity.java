package com.example.streamfun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView img1, img2;
    Animation top, bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);

        top = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.logo_animation);
        bottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_name_animation);

        img1.setAnimation(top);
        img2.setAnimation(bottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MyUserSignUp.class);
                startActivity(intent);
            }
        }, 2500);

    }
}
