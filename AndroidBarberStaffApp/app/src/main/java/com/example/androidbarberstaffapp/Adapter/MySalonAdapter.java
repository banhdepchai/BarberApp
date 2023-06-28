package com.example.androidbarberstaffapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.CustomLoginDialog;
import com.example.androidbarberstaffapp.Common.Interface.IDialogClickListener;
import com.example.androidbarberstaffapp.Common.Interface.IGetBarberListener;
import com.example.androidbarberstaffapp.Common.Interface.IRecyclerItemSelectedListener;
import com.example.androidbarberstaffapp.Common.Interface.IUserLoginRememberListener;
import com.example.androidbarberstaffapp.Model.Barber;
import com.example.androidbarberstaffapp.Model.Salon;
import com.example.androidbarberstaffapp.R;
import com.example.androidbarberstaffapp.StaffHomeActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {
    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        this.iUserLoginRememberListener = iUserLoginRememberListener;
        this.iGetBarberListener = iGetBarberListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_salon_name, txt_salon_address;
        CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_salon = (CardView)itemView.findViewById(R.id.card_salon);
            txt_salon_name = (TextView)itemView.findViewById(R.id.txt_salon_name);
            txt_salon_address = (TextView)itemView.findViewById(R.id.txt_salon_address);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salon, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(salonList.get(position).getName());
        holder.txt_salon_address.setText(salonList.get(position).getAddress());

        if(!cardViewList.contains(holder.card_salon))
            cardViewList.add(holder.card_salon);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {

                Common.selected_salon = salonList.get(position);
                showLoginDialog();

            }
        });
    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance()
                .showLoginDialog("STAFF LOGIN",
                        "LOGIN",
                        "CANCEL",
                        context,
                        this);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(DialogInterface dialogInterface, String userName, String password) {
        //Show loading dialog
        AlertDialog loading = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();

        loading.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .whereEqualTo("username", userName)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnFailureListener(e -> {
                    loading.dismiss();
//                    dialogInterface.dismiss();
//
//                    Common.selectedSalon = null;
//                    Common.currentBarber = null;

                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        if(task.getResult().size() > 0)
                        {
                            loading.dismiss();
                            dialogInterface.dismiss();

                            //Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show();

                            iUserLoginRememberListener.onUserLoginSuccess(userName);

                            //Create Barber
                            Barber barber = new Barber();
                            for (DocumentSnapshot barberSnapShot:task.getResult())
                            {
                                barber = barberSnapShot.toObject(Barber.class);
                                barber.setBarberId(barberSnapShot.getId());
                            }

                            iGetBarberListener.onGetBarberSuccess(barber);

//
                            //Start Activity
                            Intent staffHome = new Intent(context, StaffHomeActivity.class);
                            staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(staffHome);

                        }
                        else
                        {
                            loading.dismiss();
//                            dialogInterface.dismiss();
                            Toast.makeText(context, "Wrong username / password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
    }
}
