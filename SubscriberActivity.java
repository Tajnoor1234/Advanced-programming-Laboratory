package com.example.tunesphere;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SubscriberActivity extends AppCompatActivity {

    private Button uploadButton, downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        uploadButton = findViewById(R.id.btnUpload);
        downloadButton = findViewById(R.id.btnDownload);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriberActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriberActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });
    }
}