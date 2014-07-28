package com.anescobar.whatshapp2.whatshapp.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Andres Escobar on 6/18/14.
 * represents one search and all of its params
 */
public class Search {
    public LatLng locationCoordinates;
    public String locationQuery;
    public int radius;
    public String eventQuery;
    public String eventCategory;
    public Integer pageNumber;

    public Search(LatLng locationCoordinates, String locationQuery, int radius, String eventQuery,
                  String eventCategory, int pageNumber) {
        this.locationCoordinates = locationCoordinates;
        this.locationQuery = locationQuery;
        this.radius = radius;
        this.eventQuery = eventQuery;
        this.eventCategory = eventCategory;
        this.pageNumber = pageNumber;
    }
}
