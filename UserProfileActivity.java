package com.example.tunesphere;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private EditText userNameTV, currentPassword, newPassword;
    private TextView userEmailTV;
    private Button updateNameButton, updatePasswordButton, deleteAccountButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userNameTV = findViewById(R.id.userNameTV);
        userEmailTV = findViewById(R.id.userEmailTV);
        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        updateNameButton = findViewById(R.id.updateNameButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmailTV.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("Name").getValue(String.class);
                        if (name != null) {
                            userNameTV.setText(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Failed to read user data: " + error.getMessage());
                }
            });
        }

        updateNameButton.setOnClickListener(view -> updateUserName());
        updatePasswordButton.setOnClickListener(view -> updatePassword());
        deleteAccountButton.setOnClickListener(view -> deleteAccount());
    }

    private void updateUserName() {
        String newName = userNameTV.getText().toString();
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef.child("Name").setValue(newName).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    handleError(task);
                }
            });
        } else {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String currentPwd = currentPassword.getText().toString();
        String newPwd = newPassword.getText().toString();

        if (TextUtils.isEmpty(currentPwd) || TextUtils.isEmpty(newPwd)) {
            Toast.makeText(this, "Passwords cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), currentPwd);

            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    currentUser.updatePassword(newPwd).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            signOutAndNavigate();
                        } else {
                            handleError(task1);
                        }
                    });
                } else {
                    handleError(task);
                }
            });
        }
    }

    private void deleteAccount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentPwd = currentPassword.getText().toString();

            if (TextUtils.isEmpty(currentPwd)) {
                Toast.makeText(UserProfileActivity.this, "Please enter your current password", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), currentPwd);

            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // First remove the user data from the database
                    userRef.removeValue().addOnCompleteListener(databaseTask -> {
                        if (databaseTask.isSuccessful()) {
                            // Then delete the user account
                            currentUser.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(UserProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    handleError(deleteTask);
                                }
                            });
                        } else {
                            handleError(databaseTask);
                        }
                    });
                } else {
                    handleError(task);
                }
            });
        } else {
            Toast.makeText(UserProfileActivity.this, "No user is logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOutAndNavigate() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
        finish();
    }

    private void handleError(@NonNull Task<?> task) {
        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
        Log.e("FirebaseError", errorMessage);
        Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}