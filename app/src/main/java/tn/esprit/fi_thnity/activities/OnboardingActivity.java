package tn.esprit.fi_thnity.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.adapters.OnboardingAdapter;
import tn.esprit.fi_thnity.models.OnboardingItem;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private MaterialButton btnNext, btnSkip;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> onboardingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        // Setup onboarding data
        setupOnboardingItems();

        // Setup adapter
        adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        // Setup indicators
        setupIndicators();
        setCurrentIndicator(0);

        // ViewPager listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);

                // Change button text on last page
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText(R.string.get_started);
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText(R.string.next);
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        // Next button click
        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < onboardingItems.size()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                navigateToAuth();
            }
        });

        // Skip button click
        btnSkip.setOnClickListener(v -> navigateToAuth());
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
        ));
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[onboardingItems.size()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.indicator_inactive
                ));
            }
        }
    }

    private void navigateToAuth() {
        // Mark onboarding as completed
        getSharedPreferences("FiThnityPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply();

        // Navigate to Phone Authentication Activity
        Intent intent = new Intent(OnboardingActivity.this, PhoneAuthActivity.class);
        startActivity(intent);
        finish();
    }
}
