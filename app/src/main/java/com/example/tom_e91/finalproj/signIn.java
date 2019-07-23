package com.example.tom_e91.finalproj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class signIn extends AppCompatActivity {
    // Define a LOG TAG
    private static final String LOG_TAG = signIn.class.getSimpleName();

    // UI Components
    TextView signInText, statusDisplay;
    Button loginButton;
    EditText passText, emailText;
    boolean isEmailEmpty = true, isPassEmpty = true;

    // Firebase Components
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // A Collection contains the stored Messages on the remote DB
    CollectionReference MessagesCollection = db.collection("Trips");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // configures UI components
        signInText = findViewById(R.id.signinText);
        loginButton = findViewById(R.id.loginButton);
        passText = findViewById(R.id.passEditText);
        emailText = findViewById(R.id.emailEditText);
        statusDisplay = findViewById(R.id.statusDisplay);

        // Set TextWatchers
        watchTextChanges(emailText);
        watchTextChanges(passText);


    }

    public void loginButtonOnClick(View view)
    {
        // Makes sure for valid input
        if (isValidInput()) {
            startActivity(new Intent(this, Home.class));
        }
    }

    // Forces the user to enter the fields
    private void watchTextChanges(final EditText textEdit) {
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int editTextId = textEdit.getId();
                switch (editTextId) {
                    case R.id.emailEditText:
                        isEmailEmpty = emailText.getText().toString().isEmpty();
                        break;
                    case R.id.passEditText:
                        isPassEmpty = passText.getText().toString().isEmpty();
                        break;
                }
                loginButton.setEnabled((!isPassEmpty) && (!isPassEmpty));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private boolean isValidInput()
    {

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


    private boolean checkEMailValidity (String emailAddr)
    {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailAddr.matches(regex);
    }

    private boolean checkPassword (String pwd)
    {
        // TODO- METHOD for checking PASSWORD validity
//        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
//        return pwd.matches(pattern);
        return true;
    }
}
