package com.example.tom_e91.finalproj.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "nadir" + EmailPasswordActivity.class.getSimpleName();

    // Views
    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // ------------------------------- LifeCycle ------------------------------- //

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        // Buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        setupFirebaseAuthListener();
    }

    @Override protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override protected void onStop() {
        super.onStop();
        hideProgressDialog();
        mAuth.removeAuthStateListener(mAuthListener);

    }

    @Override public void onClick(View view) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        switch (view.getId()) {
            case R.id.emailCreateAccountButton:
                createAccount(email, password);
                break;

            case R.id.emailSignInButton:
                signIn(email, password);
                break;

        }
    }

    // ------------------------------- Account ------------------------------- //

    private void signIn(String email, String password) {
        Log.d(LOG_TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "signInWithEmail:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(LOG_TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed, Toast.LENGTH_LONG).show();
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                    }
                });
        hideProgressDialog();
    }

    private void createAccount(final String email, String password) {
        Log.d(LOG_TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "createUserWithEmail:success");
                            addUserToDB(new User(email));
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.d(LOG_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.failed_to_create, Toast.LENGTH_LONG).show();
                            mStatusTextView.setText(R.string.failed_to_create);
                        }
                    }
                });
        hideProgressDialog();
    }

    private void setupFirebaseAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Set Current User to MyApplication
                    User user = new User(currentUser.getEmail());
                    ((MyApplication)(getApplicationContext())).setUser(user);
                    // Change activity to home
                    startActivity(new Intent(EmailPasswordActivity.this, HomeActivity.class));
                    finish();
                }

                // Else, if No user is currently signed in
                else {
                    mStatusTextView.setText(R.string.signed_out);
                }
            }
        };
    }

    // ------------------------------- Utilities ------------------------------- //

    private void addUserToDB(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getString(R.string.collection_users)).document(user.getEmail()).set(user).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "addUserToDB() failed");
            }
        });

    }

    private boolean validateForm() {
        // Validate Email
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            return false;
        } else
            mEmailField.setError(null);

        // Validate password
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            return false;
        } else
            mPasswordField.setError(null);

        return true;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}
