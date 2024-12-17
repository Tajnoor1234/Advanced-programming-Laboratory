package com.example.tunesphere;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {
    private static final String[] FREE_MUSIC_APIS = {
            "https://api.myjson.online/v1/records/2409d1de-6929-485e-8328-73bbf81c3dd1",
            "https://api.myjson.online/v1/records/eb431417-44e7-4158-8d53-beb5702be112",
            "https://api.myjson.online/v1/records/0493e722-591e-4ebf-8940-fa75612b0e5c"
    };
    private ListView listView;
    private EditText searchBar;
    private MediaPlayer mediaPlayer;
    private TextView currentSongTextView;
    private SeekBar seekBar;
    private ArrayList<String> songList;
    private ArrayList<String> songUrls;
    private ArrayAdapter<String> adapter;
    private int currentSongIndex = 0;
    private RequestQueue requestQueue;
    private Handler handler;
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        initializeViews();
        setupSeekBar();
        setupMediaPlayerAndLists();
        fetchSongs();
        setupSongListClickListener();
        setupSearchFunctionality();
        setupPlaybackControls();
    }
    private void initializeViews() {
        listView = findViewById(R.id.listView);
        searchBar = findViewById(R.id.search_bar);
        currentSongTextView = findViewById(R.id.currentSongTextView);
        seekBar = findViewById(R.id.seekBar);
        handler = new Handler();
    }
    private void setupMediaPlayerAndLists() {
        mediaPlayer = new MediaPlayer();
        songList = new ArrayList<>();
        songUrls = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        listView.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
    }
    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.post(updateSeekBar);
                }
            }
        });
    }

    private void setupPlaybackControls() {
        ImageButton playButton = findViewById(R.id.playButton);
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        ImageButton previousButton = findViewById(R.id.previousButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        playButton.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                handler.post(updateSeekBar);
            }
        });
        pauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentSongIndex < songUrls.size() - 1) {
                currentSongIndex++;
                playSong(songUrls.get(currentSongIndex));
            } else {
                Toast.makeText(this, "No more songs", Toast.LENGTH_SHORT).show();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                currentSongIndex--;
                playSong(songUrls.get(currentSongIndex));
            } else {
                Toast.makeText(this, "First song", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSongs() {
        if (isNetworkAvailable()) {
            fetchOnlineSongs();
        }
        fetchLocalSongs();
    }
    private void fetchOnlineSongs() {
        for (String apiUrl : FREE_MUSIC_APIS) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    apiUrl,
                    null,
                    response -> {
                        try {
                            JSONObject dataObject = response.getJSONObject("data");
                            JSONArray songsArray = dataObject.getJSONArray("songs");
                            for (int i = 0; i < songsArray.length(); i++) {
                                JSONObject song = songsArray.getJSONObject(i);
                                String songTitle = song.getString("title");
                                String songUrl = song.getString("url");
                                songList.add(songTitle);
                                songUrls.add(songUrl);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e("API_ERROR", error.toString())
            );
            requestQueue.add(jsonObjectRequest);
        }
    }

    private void fetchLocalSongs() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
        };
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(audioUri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String songTitle = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String songUrl = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                songList.add(songTitle);
                songUrls.add(songUrl);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }
    private void setupSongListClickListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            currentSongIndex = position;
            playSong(songUrls.get(position));
        });
    }
    private void playSong(String songUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                currentSongTextView.setText(songList.get(currentSongIndex));
                seekBar.setMax(mp.getDuration());
                handler.post(updateSeekBar);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupSearchFunctionality() {
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateSeekBar);
    }
}
