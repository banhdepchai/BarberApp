package com.example.androidbarberstaffapp.Common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.example.androidbarberstaffapp.Model.Barber;
import com.example.androidbarberstaffapp.Model.BookingInformation;
import com.example.androidbarberstaffapp.Model.MyToken;
import com.example.androidbarberstaffapp.Model.Salon;
import com.example.androidbarberstaffapp.R;
import com.example.androidbarberstaffapp.service.MyFCMService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Random;

import io.paperdb.Paper;

public class Common {
    public static final Object DISABLE_TAG = "DISABLE";
    public static final int TIME_SLOT_TOTAL = 20;
    public static final String LOGGED_KEY = "LOGGED";
    public static final String STATE_KEY = "STATE";
    public static final String SALON_KEY = "SALON";
    public static final String BARBER_KEY = "BARBER";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "body";
    public static final String SERVICES_ADDED = "SERVICES_ADDED";
    public static final double DEFAULT_PRICE = 10000;
    public static final String MONEY_SIGN = "VND";
    public static final String SHOPPING_LIST = "SHOPPING_LIST_ITEMS";
    public static final String IMAGE_DOWNLOADABLE_URL = "DOWNLOADABLE_URL";
    public static final int MAX_NOTIFICATION_PER_LOAD = 10;
    public static String state_name ="";
    public static Barber currentBarber;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // only use this format
    public static Calendar bookingDate = Calendar.getInstance();
    public static Salon selected_salon;
    public static BookingInformation currentBookingInformation;

    // Copy from Barber app
    public static String convertTimeSlotToString(int slot) {
        switch (slot) {
            case 0:
                return "9:00 - 9:30";
            case 1:
                return "9:30 - 10:00";
            case 2:
                return "10:00 - 10:30";
            case 3:
                return "10:30 - 11:00";
            case 4:
                return "11:00 - 11:30";
            case 5:
                return "11:30 - 12:00";
            case 6:
                return "12:00 - 12:30";
            case 7:
                return "12:30 - 13:00";
            case 8:
                return "13:00 - 13:30";
            case 9:
                return "13:30 - 14:00";
            case 10:
                return "14:00 - 14:30";
            case 11:
                return "14:30 - 15:00";
            case 12:
                return "15:00 - 15:30";
            case 13:
                return "15:30 - 16:00";
            case 14:
                return "16:00 - 16:30";
            case 15:
                return "16:30 - 17:00";
            case 16:
                return "17:00 - 17:30";
            case 17:
                return "17:30 - 18:00";
            case 18:
                return "18:00 - 18:30";
            case 19:
                return "18:30 - 19:00";
            default:
                return "Closed";
        }
    }

//    public static void showNotification(Context context, int notification_id, String title, String content, Intent intent) {
//
//        PendingIntent pendingIntent = null;
//        if (intent != null)
//            pendingIntent = PendingIntent.getActivity(context, notification_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        String NOTIFICATION_CHANNEL_ID = "android_barber_staff_app_channel_01";
//        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
//                    "Android Barber Staff App", NotificationManager.IMPORTANCE_DEFAULT);
//
//            notificationChannel.setDescription("Staff app");
//            notificationChannel.enableLights(true);
////            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
//            notificationChannel.enableVibration(true);
//
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
//        builder.setContentTitle(title)
//                .setContentText(content)
//                .setAutoCancel(false)
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
//
//        if (pendingIntent != null)
//            builder.setContentIntent(pendingIntent);
//        Notification notification = builder.build();
//
//        notificationManager.notify(notification_id, notification);
//    }

    public static void showNotification(Context context, int notification_id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, notification_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Barber_App_Staff");

        builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX);

//        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
//        bigTextStyle.bigText(title);
//        bigTextStyle.setBigContentTitle(title);
//        bigTextStyle.setSummaryText("title");
//
//        builder.setStyle(bigTextStyle);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "barber.app.staff.channel.id";
            NotificationChannel channel = new NotificationChannel(channelId, "Barber App Staff Channel", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        Notification notification = builder.build();

        manager.notify(notification_id, notification);

    }


    public static String formatShoppingItemName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0, 10)).append("...").toString() : name;
    }

    @SuppressLint("Range")
    public static String getFileName(ContentResolver contentResolver, Uri fireUri) {
        String result = null;
        if (fireUri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(fireUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = fireUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }

    public enum TOKEN_TYPE {
        CLIENT,
        BARBER,
        MANAGER
    }

    public static void updateToken(Context context, String token) {
        Paper.init(context);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if (user != null)
        {
            if(!TextUtils.isEmpty(user)) {
                MyToken myToken = new MyToken();
                myToken.setToken(token);
                myToken.setTokenType(TOKEN_TYPE.BARBER);
                myToken.setUser(user);

                // Submit to Firebase
                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(user)
                        .set(myToken)
                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful())
//                            {
//                                Paper.init(context);
//                                Paper.book().write(Common.STATE_KEY, Common.state_name);
//                                Paper.book().write(Common.SALON_KEY, Common.selected_salon);
//                            }
                        });
            }
        }
    }
    //==================================================================================================
}
