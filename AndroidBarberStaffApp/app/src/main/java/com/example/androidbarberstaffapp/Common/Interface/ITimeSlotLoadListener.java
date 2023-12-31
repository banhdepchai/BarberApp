package com.example.androidbarberstaffapp.Common.Interface;

import com.example.androidbarberstaffapp.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
