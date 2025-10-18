package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.models.User;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private FloatingActionButton fabCamera;
    private TextInputEditText etName, etBio;
    private MaterialButton btnComplete;
    private ProgressBar progressBar;
    private android.widget.TextView tvTitle, tvSubtitle;

    private Uri selectedImageUri;
    private String currentPhotoUrl;
    private boolean isEditMode = false;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private StorageReference storageRef;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivProfilePhoto.setImageURI(selectedImageUri);
                    ivProfilePhoto.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("profile_photos");

        // Check if in edit mode
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        // Initialize views
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        fabCamera = findViewById(R.id.fabCamera);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        btnComplete = findViewById(R.id.btnComplete);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        // Update UI based on mode
        if (isEditMode) {
            tvTitle.setText("Edit Profile");
            tvSubtitle.setText("Update your information");
            btnComplete.setText("Save Changes");
            loadExistingProfile();
        }

        // Camera/Gallery button click
        fabCamera.setOnClickListener(v -> openImagePicker());

        // Complete button click
        btnComplete.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (validateInput(name)) {
                completeSetup(name, bio);
            }
        });
    }

    private void loadExistingProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        showLoading(true);

        usersRef.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        showLoading(false);

                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                // Pre-fill name
                                etName.setText(user.getName());

                                // Load profile photo
                                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                                    currentPhotoUrl = user.getPhotoUrl();
                                    com.bumptech.glide.Glide.with(ProfileSetupActivity.this)
                                            .load(user.getPhotoUrl())
                                            .circleCrop()
                                            .into(ivProfilePhoto);
                                    ivProfilePhoto.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        showLoading(false);
                        showCustomAlert("Error",
                            "Failed to load profile: " + error.getMessage(), "OK", null);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private boolean validateInput(String name) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return false;
        }

        return true;
    }

    private void completeSetup(String name, String bio) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            showCustomAlert("Error", "Please sign in first", "OK", null);
            return;
        }

        // Show progress
        showLoading(true);

        // If user selected a new photo, upload it first
        if (selectedImageUri != null) {
            uploadPhotoAndSaveProfile(firebaseUser, name, bio);
        } else {
            // Keep existing photo URL if in edit mode and no new photo selected
            String photoUrl = isEditMode ? currentPhotoUrl : null;
            saveUserProfile(firebaseUser, name, bio, photoUrl);
        }
    }

    private void uploadPhotoAndSaveProfile(FirebaseUser firebaseUser, String name, String bio) {
        String fileName = "profile_" + firebaseUser.getUid() + ".jpg";
        StorageReference photoRef = storageRef.child(fileName);

        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    photoRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                saveUserProfile(firebaseUser, name, bio, uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Photo upload failed, but continue without photo
                                saveUserProfile(firebaseUser, name, bio, null);
                            });
                })
                .addOnFailureListener(e -> {
                    // Photo upload failed, but continue without photo
                    saveUserProfile(firebaseUser, name, bio, null);
                });
    }

    private void saveUserProfile(FirebaseUser firebaseUser, String name, String bio, String photoUrl) {
        // Update Firebase Auth profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUrl != null ? Uri.parse(photoUrl) : null)
                .build();

        firebaseUser.updateProfile(profileUpdates);

        if (isEditMode) {
            // In edit mode, only update specific fields
            usersRef.child(firebaseUser.getUid()).child("name").setValue(name);
            usersRef.child(firebaseUser.getUid()).child("photoUrl").setValue(photoUrl)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to profile screen
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showCustomAlert("Error",
                            "Failed to update profile: " + e.getMessage(), "OK", null);
                    });
        } else {
            // Create new user profile with all fields
            User user = new User(firebaseUser.getUid(), name, firebaseUser.getPhoneNumber());
            user.setPhotoUrl(photoUrl);

            usersRef.child(firebaseUser.getUid()).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate to MainActivity for new users
                        Intent intent = new Intent(ProfileSetupActivity.this, tn.esprit.fi_thnity.activities.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showCustomAlert("Error",
                            "Failed to save profile: " + e.getMessage(), "OK", null);
                    });
        }
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
            btnComplete.setEnabled(false);
            btnComplete.setText("");
            fabCamera.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnComplete.setEnabled(true);
            btnComplete.setText(R.string.complete_setup);
            fabCamera.setEnabled(true);
        }
    }
}
