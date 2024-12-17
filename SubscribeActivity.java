package com.example.tunesphere;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SubscribeActivity extends AppCompatActivity {

    private EditText etName, etCardNumber;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        etName = findViewById(R.id.etName);
        etCardNumber = findViewById(R.id.etCardNumber);
        Button btnSubscribe = findViewById(R.id.btnSubscribe);

        databaseReference = FirebaseDatabase.getInstance().getReference("Subscribers");

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String cardNumber = etCardNumber.getText().toString().trim();

                if (!name.isEmpty() && !cardNumber.isEmpty()) {
                    databaseReference.child(name).setValue(cardNumber);
                    Toast.makeText(SubscribeActivity.this, "Subscription Successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SubscribeActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
