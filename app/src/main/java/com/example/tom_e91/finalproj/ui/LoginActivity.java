package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "nadir " + LoginActivity.class.getSimpleName();

    // UI Components
    TextView statusDisplay;
    Button loginButton;
    EditText passText, emailText;

    // Firebase Components
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate() entered");
        // configures UI components
        loginButton = findViewById(R.id.login_button);
        passText = findViewById(R.id.login_password_et);
        emailText = findViewById(R.id.login_email_et);
        statusDisplay = findViewById(R.id.statusDisplay);
        findViewById(R.id.link_register).setOnClickListener(this);
        loginButton.setOnClickListener(this);

        // Set TextWatchers
        watchTextChanges(emailText);
        watchTextChanges(passText);

        setupFirebaseAuth();
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                login();

            case R.id.link_register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }
    }

    private void setupFirebaseAuth() {
        Log.d(LOG_TAG, "setupFirebaseAuth() started");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(LOG_TAG, "onAuthStateChanged() signed_in, user_uid: " + user.getUid());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    db.setFirestoreSettings(settings);

                    DocumentReference userRef = db.collection("Users").document(user.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "onAuthStateChanged() successfully signed in");
                                User user = task.getResult().toObject(User.class);
                                ((MyApplication)getApplicationContext()).setUser(user);
                            }
                        }
                    });

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged() signed_out");
                }
            }
        };
    }

    private void login() {
        // Check valid input
        if (isInputValid()) {
            String email = emailText.getText().toString();
            String password = passText.getText().toString();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(LOG_TAG, "signInWithEmailAndPassword() successful");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(LOG_TAG, "signInWithEmailAndPassword() fail: " + e.toString());
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Forces the user to enter the fields
    private void watchTextChanges(final EditText textEdit) {
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isEmailEmpty = emailText.getText().toString().isEmpty();
                boolean isPassEmpty = passText.getText().toString().isEmpty();
                loginButton.setEnabled((!isEmailEmpty) && (!isPassEmpty));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    private boolean checkEMailValidity (String emailAddr) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailAddr.matches(regex);
    }
    private boolean checkPassword (String pwd) {
        // TODO- METHOD for checking PASSWORD validity
//        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
//        return pwd.matches(pattern);
        return true;
    }
    private boolean isInputValid() {
        boolean returnValue = true;
        String currentStatus = ""; // String represents the current status

        // Checks whether the editText contains a valid Email address
        String email = emailText.getText().toString();
        if (!checkEMailValidity(email)) {
            returnValue = false;
            currentStatus += "Need to insert a valid Email address \n";
        }

        // Checks whether the editText contains a valid password
        String pass = passText.getText().toString();
        if (!checkPassword(pass)) {
            returnValue = false;
            currentStatus += "Need to insert a valid password \n";
        }

        if (currentStatus.length() == 0) {
            currentStatus += "Ready\n";
        }
        statusDisplay.setText(currentStatus);
        return returnValue;
    }

    @Override protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }
    @Override protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
    }

}
