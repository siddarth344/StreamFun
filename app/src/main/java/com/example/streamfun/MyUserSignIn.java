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

public class MyUserSignIn extends AppCompatActivity {

    EditText email, pass;
    Button signIpbtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_user_sign_in);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void signIn(View view) {
        String emailAddress = email.getEditableText().toString();
        String password = pass.getEditableText().toString();
        if(emailAddress.equals("") && password.equals("")){
            email.setError("enter email address");
            pass.setError("enter password");
            Toast.makeText(getApplicationContext(),"Enter email and password",Toast.LENGTH_SHORT).show();
        }else if(emailAddress.equals("")){
            email.setError("enter email address");
        }else if(password.equals("")){
            pass.setError("enter password");
        } else{
            mAuth.signInWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(MyUserSignIn.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"successful loged in",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                startActivity(intent);
                            }else{
                                email.setText("");
                                pass.setText("");
                                Toast.makeText(getApplicationContext(),task.toString(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
}
