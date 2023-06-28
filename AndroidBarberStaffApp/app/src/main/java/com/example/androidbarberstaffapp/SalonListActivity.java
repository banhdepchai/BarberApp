package com.example.androidbarberstaffapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Adapter.MySalonAdapter;
import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.IBranchLoadListener;
import com.example.androidbarberstaffapp.Common.Interface.IGetBarberListener;
import com.example.androidbarberstaffapp.Common.Interface.IOnLoadCountSalon;
import com.example.androidbarberstaffapp.Common.Interface.IUserLoginRememberListener;
import com.example.androidbarberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbarberstaffapp.Model.Barber;
import com.example.androidbarberstaffapp.Model.Salon;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class SalonListActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener, IGetBarberListener, IUserLoginRememberListener {

    @BindView(R.id.txt_salon_count)
    TextView txt_salon_count;

    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;

    IOnLoadCountSalon iOnLoadCountSalon;
    IBranchLoadListener iBranchLoadListener;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);

        ButterKnife.bind(this);
        
        initView();

        init();

        loadSalonBaseOnCity(Common.state_name);
    }

    private void loadSalonBaseOnCity(String name) {
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(name)
                .collection("Branch")
                .get()
                .addOnFailureListener(e -> iBranchLoadListener.onBranchLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Salon> salonList = new ArrayList<>();
                        iOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());
                        for (DocumentSnapshot salonSnapShot : task.getResult()) {
                            Salon salon = salonSnapShot.toObject(Salon.class);
                            salon.setSalonId(salonSnapShot.getId());
                            salonList.add(salon);
                        }
                        iBranchLoadListener.onBranchLoadSuccess(salonList);
                    }
                });
    }

    private void init() {
        dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();

        iOnLoadCountSalon = this;
        iBranchLoadListener = this;
    }

    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onLoadCountSalonSuccess(int count) {
        txt_salon_count.setText(new StringBuilder("All Salon (")
                .append(count)
                .append(")"));
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> branchList) {
        MySalonAdapter adapter = new MySalonAdapter(this, branchList, this, this);
        recycler_salon.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {
        Common.currentBarber = barber;
        // Go to Staff Home
//        Paper.init(this);
        Paper.book().write(Common.BARBER_KEY, new Gson().toJson(barber));
    }

    @Override
    public void onUserLoginSuccess(String user) {
        // Save user
        Paper.init(this);
        Paper.book().write(Common.LOGGED_KEY, user);
        Paper.book().write(Common.STATE_KEY, Common.state_name);
        Paper.book().write(Common.SALON_KEY, new Gson().toJson(Common.selected_salon));
    }
}