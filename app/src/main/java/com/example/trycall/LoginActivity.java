package com.example.trycall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText edEmail,edPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edEmail= (EditText)findViewById(R.id.editText);
        edPassword= (EditText)findViewById(R.id.editTextPassword);
        auth= FirebaseAuth.getInstance();
    }

    public void login (View v) {
        String email=edEmail.getText().toString();
        String password=edPassword.getText().toString();

        if(!email.equals(" ") && !password.equals(" ")) {
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //open another activity
                                Toast.makeText(getApplicationContext(),"Logged in succesfully",Toast.LENGTH_LONG).show();
                                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(i);
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"User could not be logged in",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    public void goToRegister(View v) {
        Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(i);

    }
}
