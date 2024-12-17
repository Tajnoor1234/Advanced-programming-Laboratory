package com.example.tunesphere;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText LoginEmail, LoginPassword;
    String Email, Password;
    Button LoginButton;
    ProgressDialog LoadingBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        LoadingBar = new ProgressDialog(this);
        LoginEmail = findViewById(R.id.login_email);
        LoginPassword = findViewById(R.id.login_password);
        LoginButton = findViewById(R.id.login_btn);
        LoadingBar.setTitle("Login Account");
        LoadingBar.setMessage("Please wait for a while...");
        LoadingBar.setCanceledOnTouchOutside(false);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = LoginEmail.getText().toString();
                Password = LoginPassword.getText().toString();
                LoginAccount(Email, Password);
            }
        });
    }
    private void LoginAccount(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        LoadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        LoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(LoginActivity.this, MusicLibActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        LoadingBar.dismiss();
                        String errorMessage = "Authentication failed. Please try again.";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            errorMessage = "User not found, please try again.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "Wrong password, please try again.";
                        } catch (Exception e) {
                            errorMessage = e.getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }
    private String decodeEmail(String encodedEmail) {
        return encodedEmail.replace(",", ".");
    }
}
