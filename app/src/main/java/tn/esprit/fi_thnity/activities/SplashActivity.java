package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.esprit.fi_thnity.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds
    private static final String PREFS_NAME = "FiThnityPrefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Check authentication and navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthenticationAndNavigate, SPLASH_DURATION);
    }

    private void checkAuthenticationAndNavigate() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, check if profile exists
            checkUserProfileAndNavigate(currentUser);
        } else {
            // User is not signed in, check if onboarding was completed
            checkOnboardingAndNavigate();
        }
    }

    private void checkUserProfileAndNavigate(FirebaseUser firebaseUser) {
        usersRef.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User profile exists, go to MainActivity
                            navigateToMain();
                        } else {
                            // User signed in but no profile, go to ProfileSetup
                            navigateToProfileSetup();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Error checking profile, go to onboarding to be safe
                        checkOnboardingAndNavigate();
                    }
                });
    }

    private void checkOnboardingAndNavigate() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);

        if (onboardingCompleted) {
            // Onboarding completed, go directly to phone auth
            navigateToPhoneAuth();
        } else {
            // First time, show onboarding
            navigateToOnboarding();
        }
    }

    private void navigateToOnboarding() {
        Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToPhoneAuth() {
        Intent intent = new Intent(SplashActivity.this, PhoneAuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
