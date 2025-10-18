package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.models.Location;
import tn.esprit.fi_thnity.models.Ride;
import tn.esprit.fi_thnity.models.TransportType;

public class BroadcastRideActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_TYPE = "ride_type";
    private static final int DEFAULT_SEATS = 1;

    // Views
    private MaterialToolbar toolbar;
    private TextView tvRideTypeInfo, tvOriginAddress, tvDestinationAddress;
    private TextView tvSeatCount, tvMaxSeats;
    private MaterialCardView cardOrigin, cardDestination, cardSeatSelection;
    private ChipGroup chipGroupTransport;
    private Chip chipTaxi, chipTaxiCollectif, chipPrivateCar, chipMetro, chipBus;
    private MaterialButton btnDecrease, btnIncrease, btnBroadcast;

    // Data
    private Ride.RideType rideType;
    private Location origin, destination;
    private TransportType selectedTransport;
    private int seatCount = DEFAULT_SEATS;
    private int maxSeats = 0;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference ridesRef;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> originLauncher;
    private ActivityResultLauncher<Intent> destinationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_ride);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        ridesRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("rides");

        // Get ride type from intent
        String rideTypeStr = getIntent().getStringExtra(EXTRA_RIDE_TYPE);
        rideType = rideTypeStr != null ? Ride.RideType.valueOf(rideTypeStr) : Ride.RideType.REQUEST;

        // Initialize views
        initializeViews();

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup activity result launchers
        setupActivityResultLaunchers();

        // Setup location selection
        cardOrigin.setOnClickListener(v -> selectOrigin());
        cardDestination.setOnClickListener(v -> selectDestination());

        // Setup transport type selection
        setupTransportTypeSelection();

        // Setup seat counter
        btnDecrease.setOnClickListener(v -> decreaseSeatCount());
        btnIncrease.setOnClickListener(v -> increaseSeatCount());

        // Setup broadcast button
        btnBroadcast.setOnClickListener(v -> broadcastRide());

        // Update UI based on ride type
        updateRideTypeUI();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvRideTypeInfo = findViewById(R.id.tvRideTypeInfo);
        tvOriginAddress = findViewById(R.id.tvOriginAddress);
        tvDestinationAddress = findViewById(R.id.tvDestinationAddress);
        tvSeatCount = findViewById(R.id.tvSeatCount);
        tvMaxSeats = findViewById(R.id.tvMaxSeats);
        cardOrigin = findViewById(R.id.cardOrigin);
        cardDestination = findViewById(R.id.cardDestination);
        cardSeatSelection = findViewById(R.id.cardSeatSelection);
        chipGroupTransport = findViewById(R.id.chipGroupTransport);
        chipTaxi = findViewById(R.id.chipTaxi);
        chipTaxiCollectif = findViewById(R.id.chipTaxiCollectif);
        chipPrivateCar = findViewById(R.id.chipPrivateCar);
        chipMetro = findViewById(R.id.chipMetro);
        chipBus = findViewById(R.id.chipBus);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnBroadcast = findViewById(R.id.btnBroadcast);
    }

    private void setupActivityResultLaunchers() {
        originLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra(LocationSelectionActivity.EXTRA_LATITUDE, 0);
                        double lng = data.getDoubleExtra(LocationSelectionActivity.EXTRA_LONGITUDE, 0);
                        String address = data.getStringExtra(LocationSelectionActivity.EXTRA_ADDRESS);

                        origin = new Location(lat, lng, address);
                        tvOriginAddress.setText(address != null && !address.isEmpty() ? address : "Unknown location");
                    }
                }
        );

        destinationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra(LocationSelectionActivity.EXTRA_LATITUDE, 0);
                        double lng = data.getDoubleExtra(LocationSelectionActivity.EXTRA_LONGITUDE, 0);
                        String address = data.getStringExtra(LocationSelectionActivity.EXTRA_ADDRESS);

                        destination = new Location(lat, lng, address);
                        tvDestinationAddress.setText(address != null && !address.isEmpty() ? address : "Unknown location");
                    }
                }
        );
    }

    private void selectOrigin() {
        Intent intent = new Intent(this, LocationSelectionActivity.class);
        intent.putExtra(LocationSelectionActivity.EXTRA_LOCATION_TYPE, "origin");
        originLauncher.launch(intent);
    }

    private void selectDestination() {
        Intent intent = new Intent(this, LocationSelectionActivity.class);
        intent.putExtra(LocationSelectionActivity.EXTRA_LOCATION_TYPE, "destination");
        destinationLauncher.launch(intent);
    }

    private void setupTransportTypeSelection() {
        chipGroupTransport.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedTransport = null;
                cardSeatSelection.setVisibility(View.GONE);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipTaxi) {
                selectedTransport = TransportType.TAXI;
            } else if (checkedId == R.id.chipTaxiCollectif) {
                selectedTransport = TransportType.TAXI_COLLECTIF;
            } else if (checkedId == R.id.chipPrivateCar) {
                selectedTransport = TransportType.PRIVATE_CAR;
            } else if (checkedId == R.id.chipMetro) {
                selectedTransport = TransportType.METRO;
            } else if (checkedId == R.id.chipBus) {
                selectedTransport = TransportType.BUS;
            }

            updateSeatSelection();
        });
    }

    private void updateSeatSelection() {
        if (selectedTransport == null) {
            cardSeatSelection.setVisibility(View.GONE);
            return;
        }

        // Show seat selection only for shareable transport types
        if (selectedTransport.isShareable() && rideType == Ride.RideType.OFFER) {
            cardSeatSelection.setVisibility(View.VISIBLE);
            maxSeats = selectedTransport.getMaxSeats();
            seatCount = Math.min(seatCount, maxSeats);
            seatCount = Math.max(seatCount, 1);
            tvSeatCount.setText(String.valueOf(seatCount));
            tvMaxSeats.setText("Max: " + maxSeats + " seats");
        } else {
            cardSeatSelection.setVisibility(View.GONE);
            seatCount = selectedTransport.isShareable() ? DEFAULT_SEATS : 0;
        }
    }

    private void decreaseSeatCount() {
        if (seatCount > 1) {
            seatCount--;
            tvSeatCount.setText(String.valueOf(seatCount));
        }
    }

    private void increaseSeatCount() {
        if (seatCount < maxSeats) {
            seatCount++;
            tvSeatCount.setText(String.valueOf(seatCount));
        }
    }

    private void updateRideTypeUI() {
        if (rideType == Ride.RideType.REQUEST) {
            toolbar.setTitle(R.string.broadcast_request);
            tvRideTypeInfo.setText(R.string.i_need_ride);
        } else {
            toolbar.setTitle(R.string.broadcast_offer);
            tvRideTypeInfo.setText(R.string.i_offer_ride);
        }
    }

    private void broadcastRide() {
        // Validation
        if (origin == null) {
            showCustomAlert("Error", getString(R.string.select_origin_first), "OK", null);
            return;
        }

        if (destination == null) {
            showCustomAlert("Error", getString(R.string.select_destination_first), "OK", null);
            return;
        }

        if (selectedTransport == null) {
            showCustomAlert("Error", getString(R.string.select_transport_first), "OK", null);
            return;
        }

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showCustomAlert("Error", "Please sign in to broadcast a ride", "OK", null);
            return;
        }

        // Create ride object
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
        Ride ride = new Ride(userId, userName, rideType, selectedTransport, origin, destination, seatCount);

        // Post to Firebase
        String rideId = ridesRef.push().getKey();
        if (rideId != null) {
            ride.setRideId(rideId);
            ridesRef.child(rideId).setValue(ride)
                    .addOnSuccessListener(aVoid -> {
                        showCustomAlert("Success", getString(R.string.ride_broadcasted), "OK", () -> finish());
                    })
                    .addOnFailureListener(e -> {
                        showCustomAlert("Error", "Failed to broadcast ride: " + e.getMessage(), "OK", null);
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
}
