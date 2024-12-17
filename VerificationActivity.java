package com.example.tunesphere;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerificationActivity extends AppCompatActivity {

    private EditText etNameVerify, etCardNumberVerify;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        etNameVerify = findViewById(R.id.etNameVerify);
        etCardNumberVerify = findViewById(R.id.etCardNumberVerify);
        Button btnVerify = findViewById(R.id.btnVerify);
        databaseReference = FirebaseDatabase.getInstance().getReference("Subscribers");

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etNameVerify.getText().toString().trim();
                String cardNumber = etCardNumberVerify.getText().toString().trim();

                if (!name.isEmpty() && !cardNumber.isEmpty()) {
                    databaseReference.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue(String.class).equals(cardNumber)) {
                                Toast.makeText(VerificationActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(VerificationActivity.this, SubscriberActivity.class));
                            } else {
                                Toast.makeText(VerificationActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(VerificationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(VerificationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
