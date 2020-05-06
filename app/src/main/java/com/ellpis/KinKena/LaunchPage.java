package com.ellpis.KinKena;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchPage extends AppCompatActivity {
    private static final String TAG = "TAG";
    private FirebaseAuth mAuth;
    @BindView(R.id.launch_email)
    TextInputEditText email;
    @BindView(R.id.launch_password)
    TextInputEditText password;
    @BindView(R.id.launch_create_account_tv)
    TextView create_account_tv;

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
        String text=getString(R.string.create_account);
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        create_account_tv.setText(content);

    }

    public void logIn(View view) {
        if(email.getText().toString().isEmpty()||email.getText().toString()==null||password.getText().toString().isEmpty()||password.getText().toString()==null){
            Toast.makeText(this,"Username and password cannot be empty",Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            startActivity(new Intent(LaunchPage.this, MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            setLoading(false);
                            Toast.makeText(LaunchPage.this, "Username & Password don't match",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(task->{
            setLoading(false);
            if(task instanceof FirebaseAuthInvalidCredentialsException || task instanceof FirebaseAuthInvalidUserException){
                Toast.makeText(LaunchPage.this, "Sign in failed. Username and Password do not match",
                        Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(LaunchPage.this, "Sign in failed. "+task.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }


        });
    }

    private void setLoading(boolean isLoading){
        if(isLoading){
            findViewById(R.id.launch_container).setVisibility(View.GONE);
        }else{
            findViewById(R.id.launch_container).setVisibility(View.VISIBLE);
        }

    }
    public void createAccount(View view) {
        startActivity(new Intent(this, SignUp.class));
    }
}
