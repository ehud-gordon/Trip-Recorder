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
import android.widget.ProgressBar;
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
    private TextView mStatusDisplay;
    private Button mLoginButton;
    private EditText mPassText, mEmailText;
    private ProgressBar mProgressBar;

    // Firebase Components
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate() entered");
        // configures UI components
        mLoginButton = findViewById(R.id.login_button);
        mPassText = findViewById(R.id.login_password_et);
        mEmailText = findViewById(R.id.login_email_et);
        mStatusDisplay = findViewById(R.id.statusDisplay);
        mProgressBar = findViewById(R.id.login_progress_bar);

        findViewById(R.id.link_register).setOnClickListener(this);
        mLoginButton.setOnClickListener(this);

        // Set TextWatchers
        watchTextChanges(mEmailText);
        watchTextChanges(mPassText);
        hideDialog();
        setupFirebaseAuth();
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                login();
                break;
            case R.id.link_register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
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
                    Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    db.setFirestoreSettings(settings);

                    DocumentReference userRef = db.collection(getString(R.string.collection_users)).document(user.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "userRef.get() successfully signed in");
                                // Set Application's user to this signed-in user
                                User user = task.getResult().toObject(User.class);
                                ((MyApplication)getApplicationContext()).setUser(user);
                                // Change activity to home
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(LOG_TAG, "userRef.get() failure: " + e.toString());
                        }
                    });

                } else {
                    // if user signed out
                    Log.d(LOG_TAG, "onAuthStateChanged() signed_out");
                }
            }
        };
    }

    private void login() {
        // Check valid input
        if (isInputValid()) {
            String email = mEmailText.getText().toString();
            String password = mPassText.getText().toString();
            showDialog();
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
            hideDialog();
        }
    }

    // Forces the user to enter the fields
    private void watchTextChanges(final EditText textEdit) {
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable login button when text fields aren't empty
                boolean isEmailEmpty = mEmailText.getText().toString().isEmpty();
                boolean isPassEmpty = mPassText.getText().toString().isEmpty();
                mLoginButton.setEnabled((!isEmailEmpty) && (!isPassEmpty));
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
        String email = mEmailText.getText().toString();
        if (!checkEMailValidity(email)) {
            returnValue = false;
            currentStatus += "Need to insert a valid Email address \n";
        }

        // Checks whether the editText contains a valid password
        String pass = mPassText.getText().toString();
        if (!checkPassword(pass)) {
            returnValue = false;
            currentStatus += "Need to insert a valid password \n";
        }

        if (currentStatus.length() == 0) {
            currentStatus += "Ready\n";
        }
        mStatusDisplay.setText(currentStatus);
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

    private void showDialog(){ mProgressBar.setVisibility(View.VISIBLE); }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}
