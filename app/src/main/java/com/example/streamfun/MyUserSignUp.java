package com.example.streamfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyUserSignUp extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    EditText email, pass;
    Button signUpbtn;
    FirebaseAuth mAuth;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), HomePage.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_user_sign_up);


        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        signUpbtn = findViewById(R.id.signup_btn);

        mAuth = FirebaseAuth.getInstance();
    }

    public void signUp(View view) {
        String emailAddress = email.getText().toString();
        String password = pass.getText().toString();
        if(emailAddress.equals("") && password.equals("")){
            email.setError("enter email address");
            pass.setError("enter password");
            Toast.makeText(getApplicationContext(),"Enter email and password",Toast.LENGTH_SHORT).show();
        }else if(emailAddress.equals("")){
            email.setError("enter email address");
        }else if(password.equals("")){
            pass.setError("enter password");
        } else{
            mAuth.createUserWithEmailAndPassword(emailAddress,password)
                    .addOnCompleteListener(MyUserSignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                email.setText("");
                                pass.setText("");
                                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                startActivity(intent);
                            }else{
                                email.setText("");
                                pass.setText("");
                                Toast.makeText(getApplicationContext(),"OoopsS! somthing went wrong, try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void signIn(View view) {
        Intent intent = new Intent(getApplicationContext(),MyUserSignIn.class);
        startActivity(intent);
    }
}
