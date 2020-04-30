package com.ellpis.KinKena;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchPage extends AppCompatActivity {
    private static final String TAG = "TAG";
    private FirebaseAuth mAuth;
    @BindView(R.id.launch_email)
    TextInputEditText email;
    @BindView(R.id.launch_password)
    TextInputEditText password;

    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
        setContentView(R.layout.activity_launch_page);
        ButterKnife.bind(this);


    }

    public void logIn(View view) {
        if(email.getText().toString().isEmpty()||email.getText().toString()==null||password.getText().toString().isEmpty()||password.getText().toString()==null){
            Toast.makeText(this,"Username and password cannot be empty",Toast.LENGTH_LONG).show();
            return;
        }
        if(isLoading){
            return;
        }
        isLoading=true;
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            isLoading = false;
                            startActivity(new Intent(LaunchPage.this, MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LaunchPage.this, "Username & Password no found",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isLoading = false;
            }
        });
    }
    public void createAccount(View view) {
        startActivity(new Intent(this, SignUp.class));
    }
}
