package com.anescobar.whatshapp2.whatshapp.interfaces;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

public interface MainActivityDelegate {

    void displayLocationInMessageBar(Address address);

    void moveMapCamera(LatLng location, int zoom);

    void displayEvents(String result);

    void clearEventView();

    void displayMessageInEventsContainer(int message);

    void displayToast(int toastMessage);

    void setLastSearchLatLng(LatLng location);

}
