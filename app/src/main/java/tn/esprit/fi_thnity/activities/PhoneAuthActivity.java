package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import tn.esprit.fi_thnity.R;

public class PhoneAuthActivity extends AppCompatActivity {

    private TextInputEditText etCountryCode, etPhoneNumber;
    private MaterialButton btnSendOTP;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        etCountryCode = findViewById(R.id.etCountryCode);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        progressBar = findViewById(R.id.progressBar);

        // Send OTP button click
        btnSendOTP.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (validatePhoneNumber(phoneNumber)) {
                sendOTP(phoneNumber);
            }
        });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return false;
        }

        if (phoneNumber.length() != 8) {
            etPhoneNumber.setError("Phone number must be 8 digits");
            etPhoneNumber.requestFocus();
            return false;
        }

        return true;
    }

    private void sendOTP(String phoneNumber) {
        // Show progress
        showLoading(true);

        String fullPhoneNumber = "+216" + phoneNumber;

        // Firebase Phone Authentication
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-verification completed (rare on real devices)
                        showLoading(false);
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        showLoading(false);
                        showCustomAlert("Verification Failed",
                            "Failed to send OTP: " + e.getMessage(), "OK", null);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Save verification ID and navigate to OTP screen
                        PhoneAuthActivity.this.verificationId = verificationId;
                        showLoading(false);

                        // Navigate to OTP Verification
                        Intent intent = new Intent(PhoneAuthActivity.this, OTPVerificationActivity.class);
                        intent.putExtra("phoneNumber", fullPhoneNumber);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // User signed in successfully, navigate to profile setup or main
                    navigateToNextScreen();
                })
                .addOnFailureListener(e -> {
                    showCustomAlert("Sign In Failed",
                        "Failed to sign in: " + e.getMessage(), "OK", null);
                });
    }

    private void navigateToNextScreen() {
        // Navigate to MainActivity (authentication check will be done in SplashActivity)
        Intent intent = new Intent(PhoneAuthActivity.this, ProfileSetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Custom Alert Dialog (no Android logo)
    private void showCustomAlert(String title, String message, String positiveText, Runnable positiveAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (positiveAction != null) positiveAction.run();
            dialog.dismiss();
        });
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendOTP.setEnabled(false);
            btnSendOTP.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendOTP.setEnabled(true);
            btnSendOTP.setText(R.string.send_otp);
        }
    }
}
