package com.example.tunesphere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListView listView;
    private ArrayList<String> localSongList, jsonSongList, combinedSongList;
    private ArrayList<Uri> localSongUris;
    private ArrayList<String> jsonSongUrls;
    private ArrayAdapter<String> adapter;
    private ImageButton playButton, pauseButton, nextButton, previousButton, pickAudioButton, downloadButton;
    private SeekBar seekBar;
    private TextView currentSongTextView;
    private EditText searchBar;
    private DownloadManager downloadManager;
    private long downloadReference;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initializeViews();

        if (checkPermissions()) {
            loadSongsFromMediaStore();
        } else {
            requestPermissions();
        }

        loadSongsFromJson();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            currentSongIndex = position;
            String songUri = (position < jsonSongUrls.size()) ? jsonSongUrls.get(position) : localSongUris.get(position - jsonSongUrls.size()).toString();
            playSong(songUri);
        });

        playButton.setOnClickListener(v -> {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });

        pauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentSongIndex < combinedSongList.size() - 1) {
                currentSongIndex++;
                String songUri = (currentSongIndex < jsonSongUrls.size()) ? jsonSongUrls.get(currentSongIndex) : localSongUris.get(currentSongIndex - jsonSongUrls.size()).toString();
                playSong(songUri);
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                currentSongIndex--;
                String songUri = (currentSongIndex < jsonSongUrls.size()) ? jsonSongUrls.get(currentSongIndex) : localSongUris.get(currentSongIndex - jsonSongUrls.size()).toString();
                playSong(songUri);
            }
        });

        pickAudioButton.setOnClickListener(v -> {
            Toast.makeText(this, "Pick audio feature is not implemented yet.", Toast.LENGTH_SHORT).show();
        });

        downloadButton.setOnClickListener(v -> {
            String url = jsonSongUrls.get(currentSongIndex); // Assuming you want to download the current song
            downloadSong(url);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterSongs(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void initializeViews() {
        listView = findViewById(R.id.listView);
        searchBar = findViewById(R.id.search_bar);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        pickAudioButton = findViewById(R.id.pick_audio_button);
        downloadButton = findViewById(R.id.download_button);
        seekBar = findViewById(R.id.seekBar);
        currentSongTextView = findViewById(R.id.currentSongTextView);

        localSongList = new ArrayList<>();
        jsonSongList = new ArrayList<>();
        combinedSongList = new ArrayList<>();
        localSongUris = new ArrayList<>();
        jsonSongUrls = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, combinedSongList);
        listView.setAdapter(adapter);

        mediaPlayer = new MediaPlayer();
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    private void loadSongsFromMediaStore() {
        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA };

        try (Cursor cursor = contentResolver.query(musicUri, projection, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    localSongList.add(title);
                    localSongUris.add(Uri.parse(data));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("Local Songs Error", "Error loading local songs: " + e.getMessage());
        }

        combinedSongList.clear();
        combinedSongList.addAll(jsonSongList);
        combinedSongList.addAll(localSongList);
        adapter.notifyDataSetChanged();
    }

    private void loadSongsFromJson() {
        String jsonUrl = "https://api.myjson.online/v1/records/d96750d5-bdcf-4284-8cb9-cd11067365a5";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, jsonUrl, null,
                response -> {
                    try {
                        jsonSongList.clear();
                        jsonSongUrls.clear();

                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject songObject = dataArray.getJSONObject(i);
                            String songTitle = songObject.getString("title");
                            String songUrl = songObject.getString("url");

                            jsonSongList.add(songTitle);
                            jsonSongUrls.add(songUrl);
                        }

                        combinedSongList.clear();
                        combinedSongList.addAll(jsonSongList);
                        combinedSongList.addAll(localSongList);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(DownloadActivity.this, "Failed to load songs from JSON.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(DownloadActivity.this, "Failed to load songs.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playSong(String songUri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            Uri uri = Uri.parse(songUri);
            if (songUri.startsWith("http")) {
                mediaPlayer.setDataSource(songUri);
            } else {
                mediaPlayer.setDataSource(this, uri);
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                updateSeekBar();
                currentSongTextView.setText(combinedSongList.get(currentSongIndex));
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing song.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSeekBar() {
        int duration = mediaPlayer.getDuration();
        seekBar.setMax(duration);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (mediaPlayer.isPlaying()) {
                    seekBar.postDelayed(this, 1000);
                }
            }
        };
        seekBar.post(runnable);
    }

    private void filterSongs(String query) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String song : combinedSongList) {
            if (song.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
        if (query.isEmpty()) {
            currentSongIndex = -1;
        }
    }

    private void downloadSong(String url) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "Downloaded_Song.mp3");
        downloadReference = downloadManager.enqueue(request);
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
    }
}
