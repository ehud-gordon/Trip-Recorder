package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.data.User;
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
    private EditText mEmailText, mPassText, mConfirmPassText;
    private FirebaseFirestore mDb;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // UI Components
        mEmailText = findViewById(R.id.register_email_et);
        mPassText = findViewById(R.id.register_pass_et);
        mConfirmPassText = findViewById(R.id.register_confirm_pass_et);
        mProgressBar = findViewById(R.id.register_progressBar);

        findViewById(R.id.register_button).setOnClickListener(this);
        findViewById(R.id.back_to_login_button).setOnClickListener(this);
        // DB
        mDb = FirebaseFirestore.getInstance();

        hideDialog();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button:
                register();

            case R.id.back_to_login_button:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }
    }

    private void register() {
        // Extract Input
        String email = mEmailText.getText().toString();
        String password = mPassText.getText().toString();
        String confirmPassword = mConfirmPassText.getText().toString();
        // Register new User
        if (isInputValid(email, password, confirmPassword)) {
            registerNewEmail(email, password);
        }
    }

    private void registerNewEmail(final String email, final String password) {
        Log.d(LOG_TAG, "registerNewEmail() email: " + email + " password: " + password);
        showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "createUserWithEmailAndPassword() onComplete is " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            // TODO check FirebaseAuth.getInstance().getCurrentUser().getUid() vs user_uid
                            Log.d(LOG_TAG, "createUserWithEmailAndPassword() AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            // Settings
                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                            mDb.setFirestoreSettings(settings);

                            // Create User
                            String user_uid = FirebaseAuth.getInstance().getUid();
                            String username = email.substring(0, email.indexOf("@"));
                            User user = new User(email, username, user_uid);
                            Map<String, Object> user_map = new HashMap<>();
                            user_map.put("email", email);
                            user_map.put("username", username);
                            user_map.put("user_uid", user_uid);
                            DocumentReference newUserRef = mDb.collection(getString(R.string.collection_users)).document(user_uid);
                            // Add new User to DB
                            newUserRef.set(user_map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(LOG_TAG, "newUserRef.set() success added new user to mDb");
                                        hideDialog();
                                        // Change Activity to Login
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    } else {
                                        Log.d(LOG_TAG, "newUserRef.set() task not successful to added new user to mDb: " + task.getException().toString());
                                        Toast.makeText(RegisterActivity.this, "Registration not successful", Toast.LENGTH_LONG).show();
                                    }
                                }
                            })      // If newUserRef.set() failed
                                    .addOnFailureListener(new OnFailureListener() {
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
        hideDialog();
    }

    private boolean isInputValid(String email, String password, String confirmPassword) {
        if (!isEmpty(mEmailText) && !isEmpty(mPassText) && !isEmpty(mConfirmPassText)) {
            if (password.equals(confirmPassword)) {
                return true;
            } else {
                Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(RegisterActivity.this, "Fill out all fields", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void showDialog(){ mProgressBar.setVisibility(View.VISIBLE); }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
