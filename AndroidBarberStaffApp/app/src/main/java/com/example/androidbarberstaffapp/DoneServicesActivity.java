package com.example.androidbarberstaffapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.IBarberServicesLoadListener;
import com.example.androidbarberstaffapp.Common.Interface.IOnShoppingItemSelected;
import com.example.androidbarberstaffapp.Fragments.ShoppingFragment;
import com.example.androidbarberstaffapp.Model.BarberServices;
import com.example.androidbarberstaffapp.Model.ShoppingItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DoneServicesActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSelected {

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_email)
    TextView txt_customer_email;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;

    @BindView(R.id.edt_services)
    AppCompatAutoCompleteTextView edt_services;

    @BindView(R.id.img_customer_hair)
    ImageView img_customer_hair;

    @BindView(R.id.add_shopping)
    ImageView add_shopping;

    @BindView(R.id.btn_finish)
    Button btn_finish;

    AlertDialog dialog;

    IBarberServicesLoadListener iBarberServicesLoadListener;

    HashSet<BarberServices> servicesAdded = new HashSet<>();
    List<ShoppingItem> shoppingItems = new ArrayList<>();

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_services);

        ButterKnife.bind(this);

        init();

        initView();

        setCustomerInformation();

        loadBarberServices();
    }

    private void initView() {

        getSupportActionBar().setTitle("Checkout");

        add_shopping.setOnClickListener(view -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServicesActivity.this);
            shoppingFragment.show(getSupportFragmentManager(), "Shopping");
        });
    }

    private void init() {
        dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();

        inflater = LayoutInflater.from(this);

        iBarberServicesLoadListener = this;
    }

    private void loadBarberServices() {
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Services")
                .get()
                .addOnFailureListener(e -> {
                    iBarberServicesLoadListener.onBarberServicesLoadFailed(e.getMessage());
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BarberServices> barberServicesList = new ArrayList<>();
                        for (DocumentSnapshot barberSnapShot : task.getResult()) {
                            BarberServices services = barberSnapShot.toObject(BarberServices.class);
                            barberServicesList.add(services);
                        }
                        iBarberServicesLoadListener.onBarberServicesLoadSuccess(barberServicesList);
                    }
                });
    }

    private void setCustomerInformation() {
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_email.setText(Common.currentBookingInformation.getCustomerEmail());
    }

    @Override
    public void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList) {
        List<String> nameServices = new ArrayList<>();
        // Sort alphabet
        Collections.sort(barberServicesList, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices barberServices, BarberServices t1) {
                return barberServices.getName().compareTo(t1.getName());
            }
        });

        for(BarberServices barberServices : barberServicesList)
            nameServices.add(barberServices.getName());

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, nameServices);
        edt_services.setThreshold(1); // Start from first character
        edt_services.setAdapter(adapter);
        edt_services.setOnItemClickListener((parent, view, position, id) -> {
            // Add to chip group
            int index = nameServices.indexOf(edt_services.getText().toString().trim());

            if(!servicesAdded.contains(barberServicesList.get(index))) {
                servicesAdded.add(barberServicesList.get(index));
                final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(edt_services.getText().toString());
                item.setTag(index);
                edt_services.setText("");

                item.setOnCloseIconClickListener(view1 -> {
                    chip_group_services.removeView(view1);
                    servicesAdded.remove((int) view1.getTag());
                });

                chip_group_services.addView(item);
            }
            else {
                edt_services.setText("");
            }
        });

        dialog.dismiss();

    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        shoppingItems.add(shoppingItem);

        final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
        item.setText(shoppingItem.getName());
        item.setTag(shoppingItems.indexOf(shoppingItem));
        edt_services.setText("");

        item.setOnCloseIconClickListener(view -> {
            chip_group_shopping.removeView(view);
            shoppingItems.remove((int)view.getTag());
        });

        chip_group_shopping.addView(item);
    }
}