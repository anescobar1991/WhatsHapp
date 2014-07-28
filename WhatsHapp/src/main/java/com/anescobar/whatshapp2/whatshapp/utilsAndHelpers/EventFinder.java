package com.anescobar.whatshapp2.whatshapp.utilsAndHelpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.whatshapp2.whatshapp.R;
import com.anescobar.whatshapp2.whatshapp.interfaces.MainActivityDelegate;
import com.anescobar.whatshapp2.whatshapp.models.Search;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * Created by Andres Escobar on 6/21/14.
 * Performs events query!
 * Firstly resolves location using Google geocoder
 * Then uses Eventful API to search for events
 * Lastly makes calls to mainActivity to update UI as necessary
 */
public class EventFinder {

    private MainActivityDelegate mMainActivityDelegate;
    private LocationClient mLocationClient;
    private Context mContext;
    private Geocoder mGeocoder;

    public EventFinder(Context context, MainActivityDelegate mainActivityDelegate,
                       LocationClient locationClient) {
        this.mMainActivityDelegate = mainActivityDelegate;
        this.mLocationClient = locationClient;
        this.mContext = context;
        mGeocoder = new Geocoder(mContext, Locale.getDefault());
    }


    public void getEvents(Search searchParams) {
        //if search params does not include a LatLng then geocoding needs to happen before performing events search
        if (searchParams.locationCoordinates == null) {
            new GetAddressFromStringTask(searchParams).execute(searchParams.locationQuery);
        } else {
            mMainActivityDelegate.setLastSearchLatLng(searchParams.locationCoordinates);

            String baseUrl = mContext.getString(R.string.eventfulAPIBaseUrl) + mContext.getString(R.string.eventfulAPIKey) + "&where=" + searchParams.locationCoordinates.latitude + "," + searchParams.locationCoordinates.longitude + "&within=" + searchParams.radius + "&page_size=100" + "&date=Next%20Week" + "&sort_order=date";

            //appends to request url based on parameters
            //sanitizes query before appending it to url
            if (searchParams.eventQuery != null) {
                baseUrl += "&keywords=" + searchParams.eventQuery.replace(" ", "+");
            }

            if (searchParams.eventCategory != null) {
                baseUrl += "&category=" + searchParams.eventCategory.replace(" ", "+");
            }

            if (searchParams.pageNumber != null) {
                baseUrl += "&page_number=" + searchParams.pageNumber;
            }
            System.out.println(baseUrl);
            try {
                new GetAddressFromLatLngTask(searchParams).execute(searchParams.locationCoordinates);
                new GetEventsTask().execute(new URL(baseUrl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mMainActivityDelegate.displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
            }
        }
    }

    private void moveMapCamera(LatLng centerPoint, int radius) {
        //logic to create proper zoom level on map based on user specified search radius
        int zoomLevel;

        if (radius >= 1 && radius < 5) {
            zoomLevel = 12;
        } else if (radius >= 5 && radius < 10) {
            zoomLevel = 11;
        } else if (radius >= 10 && radius < 14) {
            zoomLevel = 10;
        } else if (radius >= 14 && radius < 19) {
            zoomLevel = 9;
        } else {
            zoomLevel = 8;
        }

        mMainActivityDelegate.moveMapCamera(centerPoint, zoomLevel);
    }

    private class GetEventsTask extends AsyncTask<URL, Void, String> {

        protected String doInBackground(URL... urls) {
            String url = null;
            try {
                url = HTTPUtil.downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                //returns null if there is some kind of connectivity issue
            }
            return url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMainActivityDelegate.clearEventView();
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                mMainActivityDelegate.displayEvents(result);
                //result is null if there was some connectivity issue
            } else {
                mMainActivityDelegate.displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
            }
        }
    }

    private class GetAddressFromStringTask extends AsyncTask<String, Void, List<Address>> {
        private Search search;

        public GetAddressFromStringTask(Search search) {
            this.search = search;
        }

        @Override
        protected List<Address> doInBackground(String... queries) {
            List<Address> addresses = null;
            try {
                addresses = mGeocoder.getFromLocationName(queries[0], 1);
            } catch (IOException e) {
                //return null if there was some connectivity issue
                e.printStackTrace();
            }
            return addresses;
        }

        protected void onPostExecute(List<Address> addresses) {
            //addresses is returned as null if there was some connectivity issue
            if (addresses == null) {
                mMainActivityDelegate.displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
            } else if (addresses.isEmpty()) {
                //calls delegate that tells activity to display invalid location message to move map camera to current location
                mMainActivityDelegate.displayToast(R.string.invalid_location_message);
                //sets search to current location search because it couldnt resolve searched location
                search.locationCoordinates = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                        mLocationClient.getLastLocation().getLongitude());
            } else {
                //calls delegate that tells activity to display location in actionBar and to move map camera to searched location
                mMainActivityDelegate.displayLocationInMessageBar(addresses.get(0));
                //sets search location to reverse geocoded LagLng
                search.locationCoordinates = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                moveMapCamera(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), search.radius);
            }
            getEvents(search);
        }
    }

    private class GetAddressFromLatLngTask extends AsyncTask<LatLng, Void, Address> {
        private Search search;

        public GetAddressFromLatLngTask(Search search) {
            this.search = search;
        }

        @Override
        protected Address doInBackground(LatLng... latLngs) {
            //returns null if there was connectivity issue
            Address address = null;
            try {
                address = mGeocoder.getFromLocation(latLngs[0].latitude, latLngs[0].longitude, 1).get(0);
            } catch (IOException e) {
                Log.w("error", e);
            }
            return address;
        }

        protected void onPostExecute(Address address) {
            //address is returned as null if there was some connectivity issue
            if (address == null) {
                mMainActivityDelegate.displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
            } else {
                //calls delegate that tells activity to display location in actionBar and to move map camera to searched location
                mMainActivityDelegate.displayLocationInMessageBar(address);
                moveMapCamera(new LatLng(address.getLatitude(), address.getLongitude()), search.radius);
            }
        }
    }

}
