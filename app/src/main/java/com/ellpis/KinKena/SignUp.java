package com.ellpis.KinKena;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @BindView(R.id.signup_username)
    TextInputEditText username;
    @BindView(R.id.signup_email)
    TextInputEditText email;
    @BindView(R.id.signup_password)
    TextInputEditText password;
    @BindView(R.id.signup_conf_password)
    TextInputEditText confPassword;
    private String TAG="tag";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isLoading=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

    }

    public void signUp(View view){
        if(isLoading){
            return;
        }

        if(!validSignUPForm())
            return;
        isLoading=true;
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                                isLoading=false;

                            Map<String, String> newUserMap = new HashMap<>();
                            newUserMap.put("username", username.getText().toString());
                            newUserMap.put("email", email.getText().toString());
                            db.collection("Users").document(mAuth.getUid()).set(newUserMap);
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Authentication failed. "+task.getException(),
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isLoading=false;
            }
        });

    }

    private boolean validSignUPForm() {
        boolean isValid = true;
        if (!isValidEmail(email.getText().toString())) {
            email.setError("Email Not Valid");
            isValid = false;
        }
        if (username.getText().toString().isEmpty()) {
            username.setError("Username cannot be empty");
            isValid = false;
        }
        if (password.getText().toString().isEmpty()) {
            password.setError("Password cannot be empty");
            isValid = false;
        }
        if (confPassword.getText().toString().isEmpty()) {
            confPassword.setError("Reenter your password");
            isValid = false;
        }
        if (!confPassword.getText().toString().equals(password.getText().toString())) {
            Toast.makeText(this, "Passwords Do not Match",
                    Toast.LENGTH_LONG).show();
            isValid = false;
        }
        return isValid;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
