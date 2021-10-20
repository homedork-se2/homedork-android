package com.example.homedork.signup;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homedork.R;
import com.example.homedork.api.InitializeAPI;
import com.example.homedork.api.UserSpecificAPICall;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private TextView registerUser;
    private EditText fullName, textEmail, textPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();

        registerUser = (Button) findViewById(R.id.registerUser);
        registerUser.setOnClickListener(this);

        fullName = (EditText) findViewById(R.id.fulname);
        textEmail = (EditText) findViewById(R.id.email);
        textPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerUser:
                registerUser();
                break;
        }
    }

    private void registerUser() {

        String email = textEmail.getText().toString().trim();
        String password = textPassword.getText().toString().trim();
        String fullName = this.fullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            this.fullName.setError("Full name is required");
            this.fullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            textEmail.setError("Email is required");
            textEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textEmail.setError("Please provide valid email");
            textEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            textPassword.setError("Password is required");
            textPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            textPassword.setError("Password should be at least 6 characters");
            textPassword.requestFocus();
            return;
        }
        System.out.println(mAuth);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> {

                        User user = new User(fullName, email);
                        System.out.println(FirebaseDatabase.getInstance());
                        System.out.println(FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (task.isSuccessful()) {
                                    addUserToServer(user.getUid(), user.getDisplayName(), user.getEmail());
                                    Toast.makeText(RegisterUser.this, "User has been register successfully", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    user.sendEmailVerification();
                                    startActivity(new Intent(RegisterUser.this, LoginActivity.class));
                                } else
                                    Toast.makeText(RegisterUser.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                });


    }

   private void addUserToServer(String uuid, String name, String email){
       UserSpecificAPICall userSpecificAPICall = InitializeAPI.getRetrofitInstance().create(UserSpecificAPICall.class);
       userSpecificAPICall.addNewUserToServer(uuid, name, email)
               .enqueue(new Callback<com.example.homedork.api.model.user.User>() {
           @Override
           public void onResponse(Call<com.example.homedork.api.model.user.User> call, Response<com.example.homedork.api.model.user.User> response) {
               Log.e("test101", "onResponse: code: "+response.code());
           }

           @Override
           public void onFailure(Call<com.example.homedork.api.model.user.User> call, Throwable t) {
               Log.e("test101", "onFailure: "+t.getMessage());
           }
       });

   }


}