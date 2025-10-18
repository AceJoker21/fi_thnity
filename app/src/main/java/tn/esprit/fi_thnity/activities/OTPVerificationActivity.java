package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.esprit.fi_thnity.R;

public class OTPVerificationActivity extends AppCompatActivity {

    private TextView tvPhoneNumber, tvResendTimer;
    private TextInputEditText etOTP;
    private MaterialButton btnVerify, btnResend;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String phoneNumber;
    private String verificationId;
    private CountDownTimer countDownTimer;
    private static final long RESEND_TIMEOUT = 60000; // 60 seconds

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Get phone number and verification ID from intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        verificationId = getIntent().getStringExtra("verificationId");

        // Initialize views
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Display phone number
        tvPhoneNumber.setText(getString(R.string.otp_sent_to, phoneNumber));

        // Start resend timer
        startResendTimer();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Verify button
        btnVerify.setOnClickListener(v -> {
            String otp = etOTP.getText().toString().trim();
            if (validateOTP(otp)) {
                verifyOTP(otp);
            }
        });

        // Resend button
        btnResend.setOnClickListener(v -> {
            resendOTP();
            startResendTimer();
        });
    }

    private boolean validateOTP(String otp) {
        if (TextUtils.isEmpty(otp)) {
            etOTP.setError("OTP is required");
            etOTP.requestFocus();
            return false;
        }

        if (otp.length() != 6) {
            etOTP.setError("OTP must be 6 digits");
            etOTP.requestFocus();
            return false;
        }

        return true;
    }

    private void verifyOTP(String otp) {
        // Show progress
        showLoading(true);

        // Create credential with verification ID and OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        // Sign in with credential
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Check if user profile exists in database
                        checkUserProfileExists(user);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showCustomAlert("Verification Failed",
                        "Invalid OTP. Please try again.", "OK", null);
                });
    }

    private void checkUserProfileExists(FirebaseUser firebaseUser) {
        usersRef.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showLoading(false);

                        if (snapshot.exists()) {
                            // User profile exists, go to MainActivity
                            Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // New user, go to ProfileSetupActivity
                            Intent intent = new Intent(OTPVerificationActivity.this, ProfileSetupActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showLoading(false);
                        showCustomAlert("Error",
                            "Failed to check user profile: " + error.getMessage(), "OK", null);
                    }
                });
    }

    private void resendOTP() {
        Toast.makeText(this, "Please go back and request a new OTP", Toast.LENGTH_SHORT).show();
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

    private void startResendTimer() {
        btnResend.setEnabled(false);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(RESEND_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                tvResendTimer.setText(getString(R.string.resend_otp_in, (int) secondsRemaining));
            }

            @Override
            public void onFinish() {
                btnResend.setEnabled(true);
                tvResendTimer.setText("");
            }
        }.start();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);
            btnVerify.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            btnVerify.setEnabled(true);
            btnVerify.setText(R.string.verify);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
