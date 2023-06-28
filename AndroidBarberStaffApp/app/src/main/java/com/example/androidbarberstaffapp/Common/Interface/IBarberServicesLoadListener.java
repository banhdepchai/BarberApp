package com.example.androidbarberstaffapp.Common.Interface;

import com.example.androidbarberstaffapp.Model.Barber;
import com.example.androidbarberstaffapp.Model.BarberServices;

import java.util.List;

public interface IBarberServicesLoadListener {
    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);
    void onBarberServicesLoadFailed(String message);
}
