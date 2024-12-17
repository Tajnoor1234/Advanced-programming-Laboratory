package com.example.tunesphere;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class MusicLibActivity extends AppCompatActivity {
    private static final String TAG = "MusicLibActivity";
    ImageView MusicLibraryIV, SubscriptionIV, userProfileIV;
    TextView userNameTV, userEmailTV;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_lib);
        mAuth = FirebaseAuth.getInstance();
        MusicLibraryIV = findViewById(R.id.musicLibraryIV);
        SubscriptionIV = findViewById(R.id.subscriptionIV);
        userProfileIV = findViewById(R.id.userProfileIV);
        userNameTV = findViewById(R.id.userNameTV);
        userEmailTV = findViewById(R.id.userEmailTV);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTV.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("Name").getValue(String.class);
                        userNameTV.setText(name);
                    } else {
                        Log.e(TAG, "No user data found in database.");
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.e(TAG, "User not authenticated.");
            Intent intent = new Intent(MusicLibActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        MusicLibraryIV.setOnClickListener(view -> {
           Intent i = new Intent(MusicLibActivity.this, SongListActivity.class);
            startActivity(i);
        });
        SubscriptionIV.setOnClickListener(view -> {
            Intent i = new Intent(MusicLibActivity.this, SubscriptionActivity.class);
            startActivity(i);
        });
        userProfileIV.setOnClickListener(view -> {
            Intent intent = new Intent(MusicLibActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }
}
