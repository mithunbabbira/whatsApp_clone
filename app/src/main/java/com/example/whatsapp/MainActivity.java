package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText mPhoneNumber, mCode;
    private Button mSend;
    private TextView mView;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //check whether the user is already logged in or not
        userIsLoggedIn();


        mPhoneNumber = findViewById(R.id.phoneNumber);
        mCode = findViewById(R.id.code);
        mSend = findViewById(R.id.verify);
        mView = findViewById(R.id.texttt);

        //


        //#1
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //verifcation
                if (mVerificationId != null) {
                    mView.setText("yes");
                    verifyPhoneNumberWithCode();
                } else {

                    startPhoneNumberVerification();

                }

            }
        });


        //#3
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                mView.setText("noo");
               Log.d("complete", "onVerificationCompleted:" + phoneAuthCredential);

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Log.w( "Failed", e);


                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mView.setText("failed");
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    mView.setText("morefailed");

                    // The SMS quota for the project has been exceeded
                    // ...
                }




            }

            @Override
            public void onCodeSent(@NonNull String VerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(VerificationId, forceResendingToken);




                mVerificationId = VerificationId;
                Log.d("sent", "Sent:" + mVerificationId);


                mSend.setText("Verify Code");
            }
        };
    }

    //#6
    private void verifyPhoneNumberWithCode() {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }



    //#4
    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener < AuthResult > () {
            @Override
            public void onComplete(@NonNull Task < AuthResult > task) {
                if (task.isSuccessful())
                    userIsLoggedIn();

            }
        });


    }


    //#5
    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;
        }
    }

    //#2
    private void startPhoneNumberVerification() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }
}