package com.example.androidbarberstaffapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberstaffapp.Adapter.MyShoppingItemAdapter;
import com.example.androidbarberstaffapp.Common.Interface.IOnShoppingItemSelected;
import com.example.androidbarberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbarberstaffapp.DoneServicesActivity;
import com.example.androidbarberstaffapp.Model.ShoppingItem;
import com.example.androidbarberstaffapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingDataLoadListener, IOnShoppingItemSelected {

    Unbinder unbinder;
    IOnShoppingItemSelected callBackToActivity;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    CollectionReference shoppingItemRef;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;
    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @BindView(R.id.chip_body_care)
    Chip chip_body_care;

    @OnClick(R.id.chip_wax)
    void waxLoadClick() {
        setSelectedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @OnClick(R.id.chip_spray)
    void sprayLoadClick() {
        setSelectedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @OnClick(R.id.chip_hair_care)
    void hairCareLoadClick() {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("Hair Care");
    }

    @OnClick(R.id.chip_body_care)
    void bodyCareLoadClick() {
        setSelectedChip(chip_body_care);
        loadShoppingItem("Body Care");
    }

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IOnShoppingItemSelected iOnShoppingItemSelected) {
        return instance == null ? new ShoppingFragment(iOnShoppingItemSelected) : instance;
    }

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("Items");

        // Get data
        shoppingItemRef.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        List<ShoppingItem> shoppingItems = new ArrayList<>();
                        for(DocumentSnapshot itemSnapShot:task.getResult())
                        {
                            ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                            shoppingItem.setId(itemSnapShot.getId());
                            shoppingItems.add(shoppingItem);
                        }
                        iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                    }
                }).addOnFailureListener(e -> iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage()));
    }

    private void setSelectedChip(Chip chip) {
        // Set color
        for(int i = 0; i < chipGroup.getChildCount(); i++)
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);
            if(chipItem.getId() != chip.getId()) // If not selected chip
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else // If selected chip
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    public ShoppingFragment() {
    }

    public ShoppingFragment(IOnShoppingItemSelected callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);

        unbinder = ButterKnife.bind(this, itemView);

        init();
        initView();

//        chip_wax.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setSelectedChip(chip_wax);
//                loadShoppingItem("Wax");
//            }
//        });
//
//        chip_spray.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setSelectedChip(chip_spray);
//                loadShoppingItem("Spray");
//            }
//        });
//
//        chip_hair_care.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setSelectedChip(chip_hair_care);
//                loadShoppingItem("Hair Care");
//            }
//        });
//
//        chip_body_care.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setSelectedChip(chip_body_care);
//                loadShoppingItem("Body Care");
//            }
//        });

        return itemView;
    }

    private void init() {
        iShoppingDataLoadListener = this;
    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        // Create adapter
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(), shoppingItemList, this);
        // Set adapter
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        callBackToActivity.onShoppingItemSelected(shoppingItem);
    }
}
