package com.example.androidbarberstaffapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.IRecyclerItemSelectedListener;
import com.example.androidbarberstaffapp.DoneServicesActivity;
import com.example.androidbarberstaffapp.Model.BookingInformation;
import com.example.androidbarberstaffapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> cardViewList;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.cardViewList = new ArrayList<>();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_time_slot = (CardView)itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyTimeSlotAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyTimeSlotAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());
        if(timeSlotList.size() == 0) // If all position is available, just show list
        {
            holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

            holder.txt_time_slot_description.setText("Available");
            holder.txt_time_slot_description.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            holder.txt_time_slot.setTextColor(context.getResources()
                    .getColor(android.R.color.black));

            holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int pos) {
                    // Loop all card in card list
//                    for(CardView cardView:cardViewList)
//                    {
//                        if(cardView.getTag() == null) // Only available card time slot be change
//                            cardView.setCardBackgroundColor(context.getResources()
//                                    .getColor(android.R.color.white));
//                    }
//
//                    // Our selected card will be change color
//                    holder.card_time_slot.setCardBackgroundColor(context.getResources()
//                            .getColor(android.R.color.holo_orange_dark));

                }
            });
        }
        else // If have position is full (booked)
        {
            for(BookingInformation slotValue:timeSlotList)
            {
                // Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if(slot == position) // If slot == position
                {
                    if(!slotValue.isDone()) {

                    holder.card_time_slot.setTag(Common.DISABLE_TAG);
                    holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    holder.txt_time_slot_description.setText("Full");
                    holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                    holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                        @Override
                        public void onItemSelectedListener(View view, int pos) {
                            // Only add for gray card
                            FirebaseFirestore.getInstance()
                                    .collection("AllSalon")
                                    .document(Common.state_name)
                                    .collection("Branch")
                                    .document(Common.selected_salon.getSalonId())
                                    .collection("Barber")
                                    .document(Common.currentBarber.getBarberId())
                                    .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                    .document(slotValue.getSlot().toString())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful())
                                        {
                                            if(task.getResult().exists()) // If have any booking information
                                            {
                                                Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                                Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                context.startActivity(new Intent(context, DoneServicesActivity.class));
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
                }
                    else {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                        holder.txt_time_slot_description.setText("Done");
                        holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                // Only add for gray card
//                                FirebaseFirestore.getInstance()
//                                        .collection("AllSalon")
//                                        .document(Common.state_name)
//                                        .collection("Branch")
//                                        .document(Common.selected_salon.getSalonId())
//                                        .collection("Barber")
//                                        .document(Common.currentBarber.getBarberId())
//                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
//                                        .document(slotValue.getSlot().toString())
//                                        .get()
//                                        .addOnCompleteListener(task -> {
//                                            if(task.isSuccessful())
//                                            {
//                                                if(task.getResult().exists()) // If have any booking information
//                                                {
//                                                    Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
//                                                    Common.currentBookingInformation.setBookingId(task.getResult().getId());
//                                                    context.startActivity(new Intent(context, DoneServicesActivity.class));
//                                                }
//                                            }
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        });
                            }
                        });
                    }
                }
                else {
                    if(holder.getiRecyclerItemSelectedListener() == null)
                    {
                        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                // Loop all card in card list
//                            for(CardView cardView:cardViewList)
//                            {
//                                if(cardView.getTag() == null) // Only available card time slot be change
//                                    cardView.setCardBackgroundColor(context.getResources()
//                                            .getColor(android.R.color.white));
//                            }
//
//                            // Our selected card will be change color
//                            holder.card_time_slot.setCardBackgroundColor(context.getResources()
//                                    .getColor(android.R.color.holo_orange_dark));
//
//                            // After that, send broadcast to enable button next
//                            Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
//                            intent.putExtra(Common.KEY_TIME_SLOT,position); // Put index of time slot we have selected
//                            intent.putExtra(Common.KEY_STEP,3); // Go to step 3
//                            LocalBroadcastManager.getInstance(context)
//                                    .sendBroadcast(intent);
                            }
                        });
                    }
                }
            }
        }

        // Add all card to list (20 card because we have 20 time slot)
        // No add card already in cardViewList
        if(!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);

        // Check if card time slot is available

    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }


}
