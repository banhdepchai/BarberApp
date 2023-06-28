package com.example.androidbarberstaffapp;

import static android.app.PendingIntent.getActivity;


import static com.example.androidbarberstaffapp.Common.Common.simpleDateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Adapter.MyTimeSlotAdapter;
import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.INotificationCountListener;
import com.example.androidbarberstaffapp.Common.Interface.ITimeSlotLoadListener;
import com.example.androidbarberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbarberstaffapp.Model.TimeSlot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker;
import com.harrywhewell.scrolldatepicker.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    TextView txt_barber_name;

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    // Copy from Barber Booking App (client app)
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog alertDialog;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.day_date_picker)
    DayScrollDatePicker calendarView;
    //==================================================END OF CODE FROM Barber Booking App (client app)==================================================

    TextView txt_notification_badge;
    CollectionReference notificationCollection;
    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener iNotificationCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);

        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.menu_exit){
                    logOut();
                }

                return true;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        txt_barber_name = (TextView)headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText(Common.currentBarber.getName());

        // Copy from Barber Booking App (client app)
        alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Please wait...")
                .create();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0); // Add current date
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(layoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);

        int day = startDate.get(Calendar.DAY_OF_MONTH);
        int month = startDate.get(Calendar.MONTH);
        int year = startDate.get(Calendar.YEAR);
        calendarView.setStartDate(day, month+1, year);

        calendarView.getSelectedDate(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@Nullable Date date) {
//                if(date != null){
//                    selected_date.setTime(date);
//                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), simpleDateFormat.format(date.getTime()));
//
//                }

                if(Common.bookingDate.getTimeInMillis() != date.getTime()){
                    Common.bookingDate.setTime(date);
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), simpleDateFormat.format(date.getTime()));
                }
//                else {
//                    Toast.makeText(getContext(), "Please choose another date", Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    private void logOut() {
        // Just delete all remember key and start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.LOGGED_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);

        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to logout the app?")
                .setPositiveButton("OK", (dialogInterface, i) -> {

                    Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .show();
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate) {
        // Copy from Barber Booking App (client app)
        alertDialog.show();

        

        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()) {
                        // Get information of booking
                        // If not created, return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selected_salon.getSalonId())
                                .collection("Barber")
                                .document(barberId)
                                .collection(bookDate); // format is dd_MM_yyyy = 28_03_2019

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            QuerySnapshot querySnapshot = task.getResult();
                                            if(querySnapshot.isEmpty()){
                                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                            } else {
                                                // If have appointment
                                                List<TimeSlot> timeSlots = new ArrayList<>();
                                                for(QueryDocumentSnapshot document:task.getResult()){
                                                    timeSlots.add(document.toObject(TimeSlot.class));
                                                }
                                                iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                    }
                }
                else {

                }
            }
        });
        //==================================================END OF CODE FROM Barber Booking App (client app)==================================================
    }

    private void init() {
        iTimeSlotLoadListener = this;
        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    private void initBookingRealtimeUpdate() {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        // Get current date
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0); // Add current date
        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        Common.simpleDateFormat.format(date.getTime()));
            }
        };

        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));

        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);
    }

    private void initNotificationRealtimeUpdate() {
        // Copy from Barber Booking App (client app)
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        notificationEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(queryDocumentSnapshots.size() > 0){
                    loadNotification();
                }
            }
        };

        notificationListener = notificationCollection.whereEqualTo("read", false)
                .addSnapshotListener(notificationEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to exit the app?")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Toast.makeText(this, "Fake function exit!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_HOME);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        // Copy from Barber Booking App (client app)
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();
        //==================================================END OF CODE FROM Barber Booking App (client app)==================================================
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {

    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        alertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        txt_notification_badge = menuItem.getActionView().findViewById(R.id.notification_badge);

        loadNotification();

        menuItem.getActionView().setOnClickListener(view -> onOptionsItemSelected(menuItem));

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read", false)
                .get()
                .addOnFailureListener(e -> Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                    }
                });
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        if(count == 0){
            txt_notification_badge.setVisibility(View.INVISIBLE);
        } else {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if(count <= 9){
                txt_notification_badge.setText(String.valueOf(count));
            } else {
                txt_notification_badge.setText("9+");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        if (notificationListener != null) {
            notificationListener.remove();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        if (notificationListener != null) {
            notificationListener.remove();
        }
        super.onDestroy();
    }
}