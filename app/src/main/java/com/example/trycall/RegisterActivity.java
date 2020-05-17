package com.example.trycall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.trycall.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText ed_name, ed_password, ed_email;
    DatabaseReference reference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ed_name=(EditText)findViewById(R.id.editText5);
        ed_email=(EditText)findViewById(R.id.editText6);
        ed_password=(EditText)findViewById(R.id.editText7);
        auth= FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void register(View v) {
        final String name=ed_name.getText().toString();
        final String email=ed_email.getText().toString();
        final String password=ed_password.getText().toString();

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener((task) -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        User user = new User(name, email, password, firebaseUser.getUid());

                        reference.child(firebaseUser.getUid()).setValue(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            finish();
                                            Intent i = new Intent(RegisterActivity.this,MainActivity.class);
                                            startActivity(i);
                                            Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "User could not be registered", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });
                    }
                });


    }
}
