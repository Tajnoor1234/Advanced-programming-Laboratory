package com.example.tunesphere;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private Button selectFileButton;
    private ListView uploadedFilesListView;
    private ArrayList<String> uploadedFiles = new ArrayList<>();
    private FileAdapter fileAdapter;
    private SharedPreferences sharedPreferences;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mDatabase = FirebaseDatabase.getInstance().getReference("uploaded_files");

        sharedPreferences = getSharedPreferences("uploaded_files", Context.MODE_PRIVATE);
        loadUploadedFiles();

        selectFileButton = findViewById(R.id.btnSelectFile);
        uploadedFilesListView = findViewById(R.id.uploadedFilesListView);
        fileAdapter = new FileAdapter(this, uploadedFiles);
        uploadedFilesListView.setAdapter(fileAdapter);

        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedFileUri = result.getData().getData();
                        String fileName = getFileName(selectedFileUri);
                        if (fileName != null && fileName.endsWith(".mp3")) {
                            uploadedFiles.add(fileName);
                            fileAdapter.notifyDataSetChanged();
                            saveUploadedFiles();
                            uploadFileToFirebase(fileName); // Upload file to Firebase Database
                            Toast.makeText(this, "File uploaded: " + fileName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Please select an MP3 file", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        selectFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            filePickerLauncher.launch(intent);
        });
    }

    private void loadUploadedFiles() {
        String savedFiles = sharedPreferences.getString("uploaded_files", "");
        if (!savedFiles.isEmpty()) {
            String[] files = savedFiles.split(",");
            for (String file : files) {
                uploadedFiles.add(file);
            }
        }
    }

    private void saveUploadedFiles() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String file : uploadedFiles) {
            stringBuilder.append(file).append(",");
        }
        sharedPreferences.edit().putString("uploaded_files", stringBuilder.toString()).apply();
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    private void uploadFileToFirebase(String fileName) {
        String fileId = mDatabase.push().getKey();
        if (fileId != null) {
            mDatabase.child(fileId).setValue(fileName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UploadActivity.this, "File uploaded to Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UploadActivity.this, "Error uploading file to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
