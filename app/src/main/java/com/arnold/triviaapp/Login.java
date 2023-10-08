package com.arnold.triviaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button loginBtn;
    private TextView signUp;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        signUp = findViewById(R.id.signUpText);

        // get the instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // if the user clicks login
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // if the user clicks sign up do this
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));
            }
        });
    }

    private void loginUser() {
        String emailReceived = email.getText().toString();
        String passwordReceived = password.getText().toString();
        if(TextUtils.isEmpty(emailReceived)){
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(passwordReceived)){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
        }
        else if(passwordReceived.length() < 6){
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
        } else if (isValidPassword(passwordReceived) == false) {
            Toast.makeText(this, "Password not in the right format", Toast.LENGTH_SHORT).show();
        }
        else{
            // create the user with that email and password
            firebaseAuth.signInWithEmailAndPassword(emailReceived,passwordReceived).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Login.this, "User successfully logged in", Toast.LENGTH_SHORT).show();
                        // bring user to menu page
                        startActivity(new Intent(Login.this,MainActivity.class));
                    }
                    else{
                        // display the error
                        Toast.makeText(Login.this, "Login Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}