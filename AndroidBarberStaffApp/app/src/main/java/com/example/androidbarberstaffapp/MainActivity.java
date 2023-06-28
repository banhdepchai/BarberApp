package com.example.androidbarberstaffapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Adapter.MyStateAdapter;
import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbarberstaffapp.Common.Interface.IOnAllStateLoadListener;
import com.example.androidbarberstaffapp.Model.Barber;
import com.example.androidbarberstaffapp.Model.City;
import com.example.androidbarberstaffapp.Model.Salon;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {

    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    CollectionReference allSalonCollection;

    IOnAllStateLoadListener iOnAllStateLoadListener;

    MyStateAdapter adapter;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Paper.init(this);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if(TextUtils.isEmpty(user)) {
            setContentView(R.layout.activity_main);

            ButterKnife.bind(this);

            initView();

            init();

            loadAllStateFromFireStore();
        } else {
            // Auto login start
            Gson gson = new Gson();
            Common.state_name = Paper.book().read(Common.STATE_KEY);
            Common.selected_salon = gson.fromJson(Paper.book().read(Common.SALON_KEY, ""),
                    new TypeToken<Salon>(){}.getType());
            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY, ""),
                    new TypeToken<Barber>(){}.getType());

            Intent intent = new Intent(this, StaffHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }


    }

    private void loadAllStateFromFireStore() {
        dialog.show();

        allSalonCollection
                .get()
                .addOnFailureListener(e -> iOnAllStateLoadListener.onAllStateLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<City> cities = new ArrayList<>();
                        for (DocumentSnapshot citySnapShot : task.getResult()) {
                            City city = citySnapShot.toObject(City.class);
                            cities.add(city);
                        }
                        iOnAllStateLoadListener.onAllStateLoadSuccess(cities);
                    }
                });
    }

    private void init() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        iOnAllStateLoadListener = this;
        dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();
    }

    private void initView() {
        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllStateLoadSuccess(List<City> cityList) {
        adapter = new MyStateAdapter(this, cityList);
        recycler_state.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onAllStateLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }
}