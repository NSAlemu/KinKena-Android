package com.ellpis.KinKena;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
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
    private String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

    }

    public void signUp(View view) {
        if (!validSignUPForm())
            return;
        setLoading(true);
        UserRepository.createUser(username.getText().toString().trim(), email.getText().toString(), password.getText().toString(), this, task -> {
            // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success");
                Map<String, String> newUserMap = new HashMap<>();
                newUserMap.put("username", username.getText().toString());
                newUserMap.put("email", email.getText().toString());


                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getUid()).set(newUserMap)
                        .addOnCompleteListener(task1 -> {

                            Log.e(TAG, "signUp: " + task1.getResult());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "signUp: " + e.toString());
                        });
                startActivity(new Intent(SignUp.this, MainActivity.class));

        },e->{
            Toast.makeText(SignUp.this, "Sign in Failed. ", Toast.LENGTH_LONG).show();
            setLoading(false);
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

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            findViewById(R.id.signup_container).setVisibility(View.GONE);
        } else {
            findViewById(R.id.signup_container).setVisibility(View.VISIBLE);
        }

    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
