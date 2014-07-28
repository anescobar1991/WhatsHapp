package com.anescobar.whatshapp2.whatshapp.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andres Escobar on 6/17/14.
 * POJO for event
 * Represents one event and all of its data
 */

public class Event implements Parcelable {
    public static final Parcelable.Creator<Event> CREATOR =
            new Parcelable.Creator<Event>() {

                @Override
                public Event createFromParcel(Parcel source) {
                    return new Event(source);
                }

                @Override
                public Event[] newArray(int size) {
                    return new Event[size];
                }
            };
    public String eventName;
    public String venueName;
    public String venueAddress;
    public String venueCity;
    public String venueRegion;
    public String venueCountryAbbr;
    public double venueLat;
    public double venueLng;
    public String imageUrl;
    public String url;

    public Event(String eventName, String venueName, String venueAddress, String venueCity,
                 String venueRegion, String venueCountryAbbr, double venueLat, double venueLng,
                 String imageUrl, String url) {
        this.eventName = eventName;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueCity = venueCity;
        this.venueRegion = venueRegion;
        this.venueCountryAbbr = venueCountryAbbr;
        this.venueLat = venueLat;
        this.venueLng = venueLng;
        this.imageUrl = imageUrl;
        this.url = url;
    }

    public Event(Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(eventName);
        parcel.writeString(venueName);
        parcel.writeString(venueAddress);
        parcel.writeString(venueCity);
        parcel.writeString(venueRegion);
        parcel.writeString(venueCountryAbbr);
        parcel.writeDouble(venueLat);
        parcel.writeDouble(venueLng);
        parcel.writeString(imageUrl);
        parcel.writeString(url);
    }

    public void readFromParcel(Parcel source) {
        eventName = source.readString();
        venueName = source.readString();
        venueAddress = source.readString();
        venueCity = source.readString();
        venueRegion = source.readString();
        venueCountryAbbr = source.readString();
        venueLat = source.readDouble();
        venueLng = source.readDouble();
        imageUrl = source.readString();
        url = source.readString();
    }
}