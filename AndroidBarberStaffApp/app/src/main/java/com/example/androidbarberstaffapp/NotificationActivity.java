package com.example.androidbarberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.androidbarberstaffapp.Adapter.MyNotificationAdapter;
import com.example.androidbarberstaffapp.Common.Common;
import com.example.androidbarberstaffapp.Common.Interface.INotificationLoadListener;
import com.example.androidbarberstaffapp.Model.MyNotification;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationActivity extends AppCompatActivity implements INotificationLoadListener {

    @BindView(R.id.recycler_notification)
    RecyclerView recycler_notification;

    CollectionReference notificationCollection;

    INotificationLoadListener iNotificationLoadListener;

    int total_item = 0, last_visible_item;
    boolean isLoading = false, isMaxData = false, isLoadFirst = false;
    DocumentSnapshot finalDoc;
    MyNotificationAdapter adapter;
    List<MyNotification> firstList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ButterKnife.bind(this);

        init();
        initView();

        loadNotification(null);
    }

    private void initView() {
        recycler_notification.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_notification.setLayoutManager(layoutManager);
        recycler_notification.addItemDecoration(new DividerItemDecoration(this, new LinearLayoutManager(this).getOrientation()));

        recycler_notification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recycler_notification, newState);
                total_item = layoutManager.getItemCount();
                last_visible_item = layoutManager.findLastVisibleItemPosition();

                if (!isLoading && total_item <= (last_visible_item + Common.MAX_NOTIFICATION_PER_LOAD)) {
                    loadNotification(finalDoc);
                    isLoading = true;
                }
            }
        });
    }

    private void loadNotification(DocumentSnapshot lastDoc) {

        notificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        if (lastDoc == null) {
            notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                    .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                    .get()
                    .addOnFailureListener(e -> iNotificationLoadListener.onNotificationLoadFailed(e.getMessage()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<MyNotification> myNotifications = new ArrayList<>();
                            DocumentSnapshot finalDoc = null;
                            for (DocumentSnapshot notificationSnapshot : task.getResult()) {
                                MyNotification myNotification = notificationSnapshot.toObject(MyNotification.class);
//                                myNotification.setUid(notificationSnapshot.getId());
                                myNotifications.add(myNotification);
                                finalDoc = notificationSnapshot;
                            }
                            iNotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                        }
                    });
        }
        else
        {
            if (!isMaxData)
            {
                notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDoc)
                        .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                        .get()
                        .addOnFailureListener(e -> iNotificationLoadListener.onNotificationLoadFailed(e.getMessage()))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<MyNotification> myNotifications = new ArrayList<>();
                                DocumentSnapshot finalDoc = null;
                                for (DocumentSnapshot notificationSnapshot : task.getResult()) {
                                    MyNotification myNotification = notificationSnapshot.toObject(MyNotification.class);
                                myNotification.setUid(notificationSnapshot.getId());
                                    myNotifications.add(myNotification);
                                    finalDoc = notificationSnapshot;
                                }
                                iNotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                            }
                        });
            }
        }

    }

    private void init() {
        iNotificationLoadListener = this;
    }

    @Override
    public void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument) {
        if (lastDocument != null)
        {
            if(lastDocument.equals(finalDoc))
            {
                isMaxData = true;
            }
            else
            {
                finalDoc = lastDocument;
                isMaxData = false;
            }

            if(adapter == null && firstList.size() == 0)
            {
                adapter = new MyNotificationAdapter(this, myNotificationList);
                firstList = myNotificationList;
            }
            else
            {
                if(!firstList.equals(firstList))
                {
                    adapter.updateList(myNotificationList);
                }
            }

            recycler_notification.setAdapter(adapter);
        }
    }

    @Override
    public void onNotificationLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}