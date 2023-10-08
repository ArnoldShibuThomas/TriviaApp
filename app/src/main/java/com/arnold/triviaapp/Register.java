package com.arnold.triviaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    Button signUp;
    TextView loginText;
    EditText email;
    EditText password;
    EditText confirmPassword;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;

    EditText username;

    LoadingDialog loadingDialog = new LoadingDialog(Register.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        signUp = findViewById(R.id.signUpBtn);
        loginText = findViewById(R.id.loginText);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        confirmPassword = findViewById(R.id.confirmPasswordInput);
        username = findViewById(R.id.userNameInput);
        firebaseAuth = FirebaseAuth.getInstance();

        // set the onclick for register
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        // this is the on click to go back to login
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });
    }

    private void createUser() {
        // instantiate everything locally
        String emailReceived = email.getText().toString();
        String passwordReceived = password.getText().toString();
        String confirmPasswordReceived = confirmPassword.getText().toString();
        String userNameReceived = username.getText().toString().replaceAll("\\s", "");

        if(TextUtils.isEmpty(emailReceived)){
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
        }
        else if(userNameReceived.length() < 1){
            Toast.makeText(this, "Username need to be more than 1 letter", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(passwordReceived)){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmPasswordReceived)){
            Toast.makeText(this, "Confirm Password", Toast.LENGTH_SHORT).show();
        }
        else if(confirmPasswordReceived.equals(passwordReceived) == false){
            Toast.makeText(this, "Check if passwords match", Toast.LENGTH_SHORT).show();
        }
        else if(passwordReceived.length() < 6){
            Toast.makeText(this, "Password needs to be 6 letter minimum", Toast.LENGTH_SHORT).show();
        } else if (isValidPassword(passwordReceived) == false) {
            Toast.makeText(this, "Password needs to have 1 letter, 1 number and 1 special character", Toast.LENGTH_SHORT).show();
        }
        else{
            // start loading
            loadingDialog.startLoadingDialog();
            // create the user with that email and password
            firebaseAuth.createUserWithEmailAndPassword(emailReceived,passwordReceived).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        // Get the current user's uid
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        // get firebase instance
                        rootNode = FirebaseDatabase.getInstance();
                        // get reference
                        reference = rootNode.getReference(uid);
                        // create the user
                        UserHelperClass userCreated = new UserHelperClass(userNameReceived,0);
                        // set the value
                        reference.setValue(userCreated).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Register.this, "User signed up successfully", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(Register.this, "Creation Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        // create a delay
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismissDialog();
                            }
                        }, 600);
                        // bring user back to login
                        startActivity(new Intent(Register.this,Login.class));
                    }
                    else{
                        // create a delay
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismissDialog();
                            }
                        }, 600);
                        // display the error
                        Toast.makeText(Register.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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