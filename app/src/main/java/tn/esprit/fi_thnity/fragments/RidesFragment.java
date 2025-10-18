package tn.esprit.fi_thnity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.adapters.RidesAdapter;
import tn.esprit.fi_thnity.models.Ride;

public class RidesFragment extends Fragment {

    private RecyclerView recyclerRides;
    private View layoutEmpty;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipRequests, chipOffers;

    private RidesAdapter adapter;
    private DatabaseReference ridesRef;
    private List<Ride> allRides;
    private ValueEventListener ridesListener;

    private enum FilterType {
        ALL, REQUESTS, OFFERS
    }

    private FilterType currentFilter = FilterType.ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        ridesRef = FirebaseDatabase.getInstance("https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("rides");

        // Initialize views
        recyclerRides = view.findViewById(R.id.recyclerRides);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipRequests = view.findViewById(R.id.chipRequests);
        chipOffers = view.findViewById(R.id.chipOffers);

        // Initialize list
        allRides = new ArrayList<>();

        // Setup RecyclerView
        recyclerRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RidesAdapter(requireContext());
        adapter.setOnRideClickListener(ride -> {
            // TODO: Open ride details dialog or activity
            Toast.makeText(requireContext(), "Ride from " + ride.getOrigin().getAddress(), Toast.LENGTH_SHORT).show();
        });
        recyclerRides.setAdapter(adapter);

        // Setup filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = FilterType.ALL;
            } else if (checkedId == R.id.chipRequests) {
                currentFilter = FilterType.REQUESTS;
            } else if (checkedId == R.id.chipOffers) {
                currentFilter = FilterType.OFFERS;
            }
            filterAndDisplayRides();
        });

        // Load rides from Firebase
        loadRides();
    }

    private void loadRides() {
        ridesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allRides.clear();

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && ride.isActive()) {
                        allRides.add(ride);
                    }
                }

                // Sort by creation time (newest first)
                Collections.sort(allRides, (r1, r2) ->
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));

                filterAndDisplayRides();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                    "Failed to load rides: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        };

        ridesRef.addValueEventListener(ridesListener);
    }

    private void filterAndDisplayRides() {
        List<Ride> filteredRides = new ArrayList<>();

        for (Ride ride : allRides) {
            if (currentFilter == FilterType.ALL) {
                filteredRides.add(ride);
            } else if (currentFilter == FilterType.REQUESTS && ride.getRideType() == Ride.RideType.REQUEST) {
                filteredRides.add(ride);
            } else if (currentFilter == FilterType.OFFERS && ride.getRideType() == Ride.RideType.OFFER) {
                filteredRides.add(ride);
            }
        }

        adapter.setRides(filteredRides);
        showEmptyState(filteredRides.isEmpty());
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerRides.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerRides.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener to prevent memory leaks
        if (ridesListener != null && ridesRef != null) {
            ridesRef.removeEventListener(ridesListener);
        }
    }
}
