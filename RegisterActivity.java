package com.example.tunesphere;
import static android.app.ProgressDialog.show;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText RegisterEmail, RegisterPassword, RegisterName;
    String Email, Password, Name;
    Button RegisterButton;
    ProgressDialog LoadingBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        RegisterEmail = findViewById(R.id.register_email);
        RegisterPassword = findViewById(R.id.register_password);
        RegisterName = findViewById(R.id.register_name);
        RegisterButton = findViewById(R.id.register_btn);
        LoadingBar = new ProgressDialog(this);
        LoadingBar.setTitle("Creating Account...");
        LoadingBar.setMessage("Please wait for a while...");
        LoadingBar.setCanceledOnTouchOutside(false);
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = RegisterEmail.getText().toString();
                Password = RegisterPassword.getText().toString();
                Name = RegisterName.getText().toString();
                CreateNewAccount(Email, Password, Name);
            }
        });
    }
    private void CreateNewAccount(String email, String password, String name) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        } else {
            LoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                storeAdditionalUserData(email, name);
                                LoadingBar.dismiss();
                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                LoadingBar.dismiss();
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                                Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void storeAdditionalUserData(String email, String name) {
        String sanitizedEmail = email.replace(".", ",");
        DatabaseReference nRef = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> userdata = new HashMap<>();
        userdata.put("Email", email);
        userdata.put("Name", name);
        nRef.child("Users").child(sanitizedEmail).updateChildren(userdata)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Failed to store user data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
