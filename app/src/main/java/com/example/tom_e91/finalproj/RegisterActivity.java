package com.example.tom_e91.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

import static com.example.tom_e91.finalproj.util.util_func.isEmpty;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "nadir " + RegisterActivity.class.getSimpleName();
    private EditText emailText, passText, confirmPassText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.register_email_tv);
        passText = findViewById(R.id.register_pass_tv);
        confirmPassText = findViewById(R.id.register_confirm_pass_tv);
        findViewById(R.id.register_btn).setOnClickListener(this);
        findViewById(R.id.back_to_login_btn).setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
    }

    private void register() {
        // Check valid input
        String email = emailText.getText().toString();
        String password = passText.getText().toString();
        String confirmPassword = confirmPassText.getText().toString();
        if (!isEmpty(emailText) && !isEmpty(passText) && !isEmpty(confirmPassText)) {
            if (password.equals(confirmPassword)) {
                // Register new user
                registerNewEmail(email, password);
            } else {
                Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(RegisterActivity.this, "Fill out all fields", Toast.LENGTH_LONG).show();
        }
    }

    private void registerNewEmail(final String email, String password) {
        Log.d(LOG_TAG, "registerNewEmail() email: " + email + " password: " + password);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOG_TAG, "createUserWithEmailAndPassword() onComplete is " + task.isSuccessful());
                if (task.isSuccessful()) {
                    // TODO check FirebaseAuth.getInstance().getCurrentUser().getUid() vs user_uid
                    Log.d(LOG_TAG, "createUserWithEmailAndPassword() AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // Settings
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    db.setFirestoreSettings(settings);

                    // Create User
                    String user_uid = FirebaseAuth.getInstance().getUid();
                    String username = email.substring(0, email.indexOf("@"));
                    User user = new User(email, username, user_uid);
                    Map<String, Object> user_map = new HashMap<>();
                    user_map.put("email", email); user_map.put("username", username); user_map.put("user_uid", user_uid);
                    DocumentReference newUserRef = db.collection("Users").document(user_uid);
                    newUserRef.set(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "newUserRef.set() success added new user to db");
                                redirectToLoginScreen();
                            } else {
                                Log.d(LOG_TAG, "newUserRef.set() fail to added new user to db: " + task.getException().toString());
                                Toast.makeText(RegisterActivity.this, "Registration not Successful", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(LOG_TAG, "newUserRef.set() failure: " + e.toString());
                        }
                    });
                } else {
                    Log.d(LOG_TAG, "createUserWithEmailAndPassword() fail " + task.getException().toString());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_btn:
                Log.d(LOG_TAG, "onClick() clicked register");
                register();
                break;

            case R.id.back_to_login_btn:
                Log.d(LOG_TAG, "onClick() clicked back to login");
                redirectToLoginScreen();
        }
    }

    private void redirectToLoginScreen() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
