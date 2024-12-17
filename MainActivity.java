package com.example.tunesphere;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    LinearLayout L1,L2;
    TextView tv;
    Animation DowntoTop,Fade;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        L1=(LinearLayout) findViewById(R.id.l1);
        L2=(LinearLayout) findViewById(R.id.l2);
        tv=(TextView) findViewById(R.id.tag);
        DowntoTop= AnimationUtils.loadAnimation(this,R.anim.downtotop);
        Fade= AnimationUtils.loadAnimation(this,R.anim.fade);
        tv.setAnimation(DowntoTop);
        tv.startAnimation(DowntoTop);
        L2.setAnimation(Fade);
        L2.startAnimation(Fade);
        Intent i=new Intent(MainActivity.this,HomeActivity.class);
        Thread thread=new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    startActivity(i);
                    finish();
                }
            }
        };thread.start();
    }
}