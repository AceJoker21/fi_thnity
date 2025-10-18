package tn.esprit.fi_thnity.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.fi_thnity.R;
import tn.esprit.fi_thnity.models.Ride;

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.RideViewHolder> {

    private final Context context;
    private List<Ride> rides;
    private OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public RidesAdapter(Context context) {
        this.context = context;
        this.rides = new ArrayList<>();
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
        notifyDataSetChanged();
    }

    public void setOnRideClickListener(OnRideClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rides.get(position);
        holder.bind(ride);
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    class RideViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardTypeBadge;
        private final TextView tvRideType, tvTransportType, tvSeats;
        private final ImageView ivUserPhoto;
        private final TextView tvUserName, tvOrigin, tvDestination, tvTime;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTypeBadge = itemView.findViewById(R.id.cardTypeBadge);
            tvRideType = itemView.findViewById(R.id.tvRideType);
            tvTransportType = itemView.findViewById(R.id.tvTransportType);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvOrigin = itemView.findViewById(R.id.tvOrigin);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTime = itemView.findViewById(R.id.tvTime);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRideClick(rides.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Ride ride) {
            // Ride type badge
            tvRideType.setText(ride.getRideType().name());
            if (ride.getRideType() == Ride.RideType.REQUEST) {
                cardTypeBadge.setCardBackgroundColor(context.getResources().getColor(R.color.accent, null));
            } else {
                cardTypeBadge.setCardBackgroundColor(context.getResources().getColor(R.color.primary, null));
            }

            // Transport type with emoji
            if (ride.getTransportType() != null) {
                tvTransportType.setText(ride.getTransportType().toString());
            }

            // Seats (only show for shareable transport and offers)
            if (ride.getTransportType() != null &&
                ride.getTransportType().isShareable() &&
                ride.getRideType() == Ride.RideType.OFFER &&
                ride.getAvailableSeats() > 0) {
                tvSeats.setVisibility(View.VISIBLE);
                tvSeats.setText(ride.getAvailableSeats() + (ride.getAvailableSeats() == 1 ? " seat" : " seats"));
            } else {
                tvSeats.setVisibility(View.GONE);
            }

            // User info
            tvUserName.setText(ride.getUserName() != null ? ride.getUserName() : "Unknown User");

            // User photo (placeholder for now, can be enhanced with actual photos)
            if (ride.getUserPhotoUrl() != null && !ride.getUserPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(ride.getUserPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_onboarding_1)
                        .into(ivUserPhoto);
            }

            // Origin and destination
            if (ride.getOrigin() != null) {
                tvOrigin.setText(ride.getOrigin().getAddress() != null ?
                    ride.getOrigin().getAddress() : "Unknown location");
            }

            if (ride.getDestination() != null) {
                tvDestination.setText(ride.getDestination().getAddress() != null ?
                    ride.getDestination().getAddress() : "Unknown location");
            }

            // Time ago
            long now = System.currentTimeMillis();
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    ride.getCreatedAt(),
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            tvTime.setText("Posted " + timeAgo);

            // Highlight expired rides
            if (ride.isExpired()) {
                itemView.setAlpha(0.6f);
                tvTime.setText("Expired");
                tvTime.setTextColor(context.getResources().getColor(R.color.accent, null));
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }
}
