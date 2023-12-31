package com.example.androidbarberstaffapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.IBarberServicesLoadListener;
import com.example.androidbarberstaffapp.Common.Interface.IOnShoppingItemSelected;
import com.example.androidbarberstaffapp.Fragments.ShoppingFragment;
import com.example.androidbarberstaffapp.Fragments.TotalPriceFragment;
import com.example.androidbarberstaffapp.Model.BarberServices;
import com.example.androidbarberstaffapp.Model.CartItem;
import com.example.androidbarberstaffapp.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.androidbarberstaffapp.Model.ShoppingItem;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DoneServicesActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSelected {

    private static final int MY_CAMERA_REQUEST_CODE = 1000;
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

    @BindView(R.id.rdi_no_picture)
    RadioButton rdi_no_picture;

    @BindView(R.id.rdi_picture)
    RadioButton rdi_picture;

    AlertDialog dialog;

    IBarberServicesLoadListener iBarberServicesLoadListener;

    HashSet<BarberServices> servicesAdded = new HashSet<>();
//    List<ShoppingItem> shoppingItems = new ArrayList<>();

    LayoutInflater inflater;
    Uri fireUri;
    StorageReference storageReference;


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

        rdi_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                img_customer_hair.setVisibility(View.VISIBLE);
                btn_finish.setEnabled(false);
            }
        });

        rdi_no_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                img_customer_hair.setVisibility(View.GONE);
                btn_finish.setEnabled(true);
            }
        });

        getSupportActionBar().setTitle("Checkout");

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rdi_no_picture.isChecked())
                {
                    dialog.dismiss();
                    TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(servicesAdded));
//                    bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(), "Price");
                }
                else
                {
                    uploadPicture(fireUri);
                }
            }
        });

        img_customer_hair.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fireUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fireUri);
            startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
        });

        add_shopping.setOnClickListener(view -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServicesActivity.this);
            shoppingFragment.show(getSupportFragmentManager(), "Shopping");
        });
    }

    private void uploadPicture(Uri fireUri) {
        if(fireUri != null)
        {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(), fireUri);
            String path = new StringBuilder("Customer_Pictures/")
                    .append(fileName)
                    .toString();

            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fireUri);

            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
                if(!task1.isSuccessful())
                    Toast.makeText(DoneServicesActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();

                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task12 -> {
                if(task12.isSuccessful())
                {
                    String url = task12.getResult().toString().substring(0, task12.getResult().toString().indexOf("&token"));
                    Log.d("DOWNLOADABLE_LINK", url);
                    dialog.dismiss();
                    Toast.makeText(DoneServicesActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                    // Create Fragment total price
                    TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(servicesAdded));
//                    bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                    bundle.putString(Common.IMAGE_DOWNLOADABLE_URL, url);
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(), "Price");

                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(DoneServicesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }
        else {
            Toast.makeText(this, "Image is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "BarberStaffApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir())
                return null;
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + time_stamp + "_" + new Random().nextInt() + ".jpg");
        return mediaFile;
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

        loadExtraItems();

    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
//        shoppingItems.add(shoppingItem);

        // Create new cart item
        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductName(shoppingItem.getName());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductPrice(shoppingItem.getPrice());
        cartItem.setProductQuantity(1);
        cartItem.setUserEmail(Common.currentBookingInformation.getCustomerEmail());

        if(Common.currentBookingInformation.getCartItemList() == null)
            Common.currentBookingInformation.setCartItemList(new ArrayList<>());

        boolean flag = false;
        for(int i = 0; i < Common.currentBookingInformation.getCartItemList().size(); i++) {
            if(Common.currentBookingInformation.getCartItemList().get(i).getProductName()
                    .equals(shoppingItem.getName()))
            {
                flag = true;
                CartItem itemUpdate = Common.currentBookingInformation.getCartItemList().get(i);
                itemUpdate.setProductQuantity(itemUpdate.getProductQuantity() + 1);
                Common.currentBookingInformation.getCartItemList().set(i, itemUpdate); // Set at index i
            }
        }

        if(!flag) {
            Common.currentBookingInformation.getCartItemList().add(cartItem);

            final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
            item.setText(cartItem.getProductName());
            item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

            item.setOnCloseIconClickListener(view -> {
                chip_group_shopping.removeView(view);
                Common.currentBookingInformation.getCartItemList().remove((int) item.getTag());
            });

            chip_group_shopping.addView(item);
        }
        else
        {
            chip_group_shopping.removeAllViews();
            loadExtraItems();
        }


    }

    private void loadExtraItems() {
        if(Common.currentBookingInformation.getCartItemList() != null) {
            for(CartItem cartItem : Common.currentBookingInformation.getCartItemList()) {
                final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(new StringBuilder(cartItem.getProductName())
                        .append(" x")
                        .append(cartItem.getProductQuantity()));
                item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

                item.setOnCloseIconClickListener(view -> {
                    chip_group_shopping.removeView(view);
                    Common.currentBookingInformation.getCartItemList().remove((int) item.getTag());
                });

                chip_group_shopping.addView(item);
            }
        }
        dialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                ExifInterface ei = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fireUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fireUri));

                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateBitmap = null;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitmap = rotateBitmap(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateBitmap(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateBitmap(bitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotateBitmap = bitmap;
                            break;
                    }
                    img_customer_hair.setImageBitmap(rotateBitmap);
                    btn_finish.setEnabled(true);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate(i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }



    // Event Bus

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void dismissDialog(DismissFromBottomSheetEvent event) {
        if(event.isButtonClick()) {
//            btn_finish.setEnabled(false);
//            img_customer_hair.setImageResource(R.drawable.user_placeholder);
//            edt_services.setText("");
//            chip_group_shopping.removeAllViews();
//            chip_group_services.removeAllViews();
//            servicesAdded.clear();
//            shoppingItems.clear();

            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}