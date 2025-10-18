package tn.esprit.fi_thnity.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.activities.OnboardingActivity;
import tn.esprit.fi_thnity.activities.ProfileSetupActivity;
import tn.esprit.fi_thnity.models.User;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePhoto;
    private TextView tvName, tvPhone, tvTotalRides;
    private MaterialCardView cardEditProfile, cardMyRides, cardSettings;
    private MaterialButton btnLogout;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        // Initialize views
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvTotalRides = view.findViewById(R.id.tvTotalRides);
        cardEditProfile = view.findViewById(R.id.cardEditProfile);
        cardMyRides = view.findViewById(R.id.cardMyRides);
        cardSettings = view.findViewById(R.id.cardSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Load user data from Firebase
        loadUserProfile();

        // Edit Profile
        cardEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileSetupActivity.class);
            intent.putExtra("isEditMode", true);
            startActivity(intent);
        });

        // My Rides
        cardMyRides.setOnClickListener(v -> {
            // TODO: Navigate to My Rides screen
            Toast.makeText(requireContext(), "My Rides", Toast.LENGTH_SHORT).show();
        });

        // Settings
        cardSettings.setOnClickListener(v -> {
            // TODO: Navigate to Settings screen
            Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        usersRef.child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                // Update UI with user data
                                tvName.setText(user.getName());
                                tvPhone.setText(user.getPhoneNumber());
                                tvTotalRides.setText(String.valueOf(user.getTotalRides()));

                                // Load profile photo using Glide
                                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                                    Glide.with(ProfileFragment.this)
                                            .load(user.getPhotoUrl())
                                            .circleCrop()
                                            .placeholder(R.drawable.ic_onboarding_1)
                                            .into(ivProfilePhoto);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(),
                            "Failed to load profile: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        firebaseAuth.signOut();

        // Clear onboarding preference
        requireActivity().getSharedPreferences("FiThnityPrefs", requireContext().MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", false)
                .apply();

        // Navigate to onboarding
        Intent intent = new Intent(requireContext(), OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data when fragment resumes (e.g., after editing)
        loadUserProfile();
    }
}
