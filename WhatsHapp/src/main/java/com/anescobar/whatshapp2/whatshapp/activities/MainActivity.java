package com.anescobar.whatshapp2.whatshapp.activities;

import android.content.Context;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.whatshapp2.whatshapp.R;
import com.anescobar.whatshapp2.whatshapp.fragments.EventsViewFragment;
import com.anescobar.whatshapp2.whatshapp.fragments.SearchFilterViewFragment;
import com.anescobar.whatshapp2.whatshapp.interfaces.EventsSlideFragmentListener;
import com.anescobar.whatshapp2.whatshapp.interfaces.MainActivityDelegate;
import com.anescobar.whatshapp2.whatshapp.interfaces.SearchFilterViewFragmentListener;
import com.anescobar.whatshapp2.whatshapp.models.Event;
import com.anescobar.whatshapp2.whatshapp.models.Search;
import com.anescobar.whatshapp2.whatshapp.utilsAndHelpers.EventFinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andres Escobar on 6/17/14.
 * Main activity
 * Has container on which fragments are placed
 * Acts as view controller for app
 */
public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, MainActivityDelegate, EventsSlideFragmentListener,
        SearchFilterViewFragmentListener {

    boolean mFirstTimeSearchPerformed = false;
    private LocationClient mLocationClient;
    private EventFinder mEventFinder;
    private EventsViewFragment mEventsViewFragment;
    private EventsViewFragment mEventsViewFragmentClass;
    private TextView mMessageBar;
    private SearchFilterViewFragment mSearchFilterViewFragmentClass;
    private Search mLastSearch; //caches last search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mEventsViewFragmentClass = new EventsViewFragment();
        mSearchFilterViewFragmentClass = new SearchFilterViewFragment();

        //adds both fragments to activity, but hides SearchFilterView until called
        getFragmentManager().beginTransaction()
                .add(R.id.mainActivity_frameLayout_content, mEventsViewFragmentClass, "eventsViewFragment")
                .add(R.id.mainActivity_frameLayout_content, mSearchFilterViewFragmentClass, "eventsSearchFilterViewFragment")
                .hide(mSearchFilterViewFragmentClass)
                .commit();

        mLocationClient = new LocationClient(this, this, this);
        mEventFinder = new EventFinder(this, this, mLocationClient);
        mEventsViewFragment = (EventsViewFragment) getFragmentManager().findFragmentByTag("eventsViewFragment");
        mMessageBar = (TextView) findViewById(R.id.mainActivity_textView_messageBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //refreshes events displayed using last successful search
            case R.id.action_refreshIcon:
                //will only call refresh events method if there has previously been a successful search
                if (mLastSearch != null) {
                    performEventSearch(mLastSearch);
                }
                return true;
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                }
                return true;
            //displays searchFilterView
            case R.id.action_searchIcon:
                displaySearchFilterViewFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //first search is a default search for current location with radius set to 10 and no filters
        if (mLastSearch == null) {
            mLastSearch = new Search(new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude())
                    , null, 5, null, null, 1);
        }
        if (!mFirstTimeSearchPerformed) {
            performEventSearch(mLastSearch);
            mFirstTimeSearchPerformed = true;
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean networkAvailable = false;
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        Log.w("INTERNET:", "connected!");
                        networkAvailable = true;
                    }
                }
            }
        }
        return networkAvailable;
    }

    //populates the actionbar message bar with the searched location name
    public void displayLocationInMessageBar(Address address) {
        String searchedLocation = "Searched Location: ";

        if (address.getLocality() == null) {
            searchedLocation += " " + address.getFeatureName();
        } else {
            searchedLocation += " " + address.getLocality();
        }

        if (address.getAdminArea() != null) {
            searchedLocation += " " + address.getAdminArea();
        }

        if (address.getCountryCode() != null) {
            searchedLocation += " " + address.getCountryName();
        }
        mMessageBar.setText(searchedLocation);
    }

    //moves map camera in eventsView fragment to that of location provided
    @Override
    public void moveMapCamera(LatLng location, int zoom) {
        if (mEventsViewFragment == null) {
            mEventsViewFragment = (EventsViewFragment)
                    getFragmentManager().findFragmentByTag("eventsViewFragment");
        }
        mEventsViewFragment.moveMapCamera(location, zoom);
    }

    @Override
    public void displayEvents(String result) {
        ArrayList<Event> eventList = new ArrayList<Event>();
        String imageUrl = null;
        if (mEventsViewFragment == null) {
            mEventsViewFragment = (EventsViewFragment) getFragmentManager().findFragmentByTag("eventsViewFragment");
        }

        try {
            JSONObject resultJSONObject = new JSONObject(result);

            //no events found for location, will display appropriate message
            if (resultJSONObject.getString("total_items").equals("0")) {
                //sending empty arraylist to fragment where it will deal with it and display appropriate message
                mEventsViewFragment.displayEvents(eventList);
                //only one event found for location
            } else if (resultJSONObject.getString("total_items").equals("1")) {
                final JSONObject event = resultJSONObject.getJSONObject("events").getJSONObject("event");

                mEventsViewFragment.clearEvents();

                //not all events have images, this ensures that event has images before getting image url
                if (!event.isNull("image")) {
                    imageUrl = event.getJSONObject("image").getJSONObject("medium").getString("url");
                }
                //adds event object to arrayList
                eventList.add(new Event(
                        event.getString("title"), event.getString("venue_name"), event.getString("venue_address"),
                        event.getString("city_name"), event.getString("region_name"), event.getString("country_abbr"),
                        event.optDouble("latitude", mLastSearch.locationCoordinates.latitude), event.optDouble("longitude", mLastSearch.locationCoordinates.longitude),
                        imageUrl, event.getString("url")));
                mEventsViewFragment.createMapMarker(
                        new LatLng(eventList.get(0).venueLat, eventList.get(0).venueLng), 0);
                mEventsViewFragment.displayEvents(eventList);
                //more than 1 event was found for location
            } else {
                final JSONArray events = resultJSONObject.getJSONObject("events").getJSONArray("event");
                final int length = events.length();

                mEventsViewFragment.clearEvents();

                //loops through response and adds events to arrayList of events
                for (int i = 0; i < length; i++) {
                    //not all events have images, this ensures that event has images before getting image url
                    if (!events.getJSONObject(i).isNull("image")) {
                        imageUrl = events.getJSONObject(i).getJSONObject("image").getJSONObject("medium").getString("url");
                    }
                    //adds event object to arrayList
                    eventList.add(new Event(
                            events.getJSONObject(i).getString("title"),
                            events.getJSONObject(i).getString("venue_name"),
                            events.getJSONObject(i).getString("venue_address"),
                            events.getJSONObject(i).getString("city_name"),
                            events.getJSONObject(i).getString("region_name"),
                            events.getJSONObject(i).getString("country_abbr"),
                            events.getJSONObject(i).optDouble("latitude", mLastSearch.locationCoordinates.latitude),
                            events.getJSONObject(i).optDouble("longitude", mLastSearch.locationCoordinates.longitude),
                            imageUrl,
                            events.getJSONObject(i).getString("url")
                    ));
                    //set imageUrl back to null
                    imageUrl = null;
                    mEventsViewFragment.createMapMarker(
                            new LatLng(eventList.get(i).venueLat, eventList.get(i).venueLng), i);
                }
                mEventsViewFragment.displayEvents(eventList);
            }
        } catch (JSONException e) {
            //if response from backend is not as expected, throws error message on screen
            Log.w("error", e);
            displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
        }
    }

    @Override
    public void performEventSearch(Search searchParams) {
        if (isNetworkAvailable(this)) {
            //when location search field is left blank we need to use users current location in query
            if (searchParams.locationQuery == null && searchParams.locationCoordinates == null) {
                searchParams.locationCoordinates = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                        mLocationClient.getLastLocation().getLongitude());
            }
            //performs search
            mEventFinder.getEvents(searchParams);
            //caches this search's parameters so that it can be called again on refresh
            mLastSearch = new Search(searchParams.locationCoordinates, searchParams.locationQuery,
                    searchParams.radius, searchParams.eventQuery, searchParams.eventCategory, searchParams.pageNumber);
        } else {
            System.out.println("network not available");
            displayMessageInEventsContainer(R.string.eventsView_generic_error_message);
        }
    }

    @Override
    public void clearEventView() {
        if (mEventsViewFragment == null) {
            mEventsViewFragment = (EventsViewFragment)
                    getFragmentManager().findFragmentByTag("eventsViewFragment");
        }
        mEventsViewFragment.clearEvents();
    }

    @Override
    public void onPreviousEventButtonTap(int position) {
        mEventsViewFragment.displaySpecificEvent(position - 1);
    }

    @Override
    public void onNextEventButtonTap(int position) {
        mEventsViewFragment.displaySpecificEvent(position + 1);
    }

    @Override
    public void displayMessageInEventsContainer(int message) {
        if (mEventsViewFragment == null) {
            mEventsViewFragment = (EventsViewFragment)
                    getFragmentManager().findFragmentByTag("eventsViewFragment");
        }
        mEventsViewFragment.clearEvents();
        mEventsViewFragment.setEventsProgressBarInvisible();
        mEventsViewFragment.displayMessageInEventsMessageHolder(message);
    }

    @Override
    public void displayEventsViewFragment() {
        getFragmentManager().beginTransaction()
                .show(mEventsViewFragment)
                .hide(mSearchFilterViewFragmentClass)
                .addToBackStack(null)
                .commit();
    }

    public void displaySearchFilterViewFragment() {
        getFragmentManager().beginTransaction()
                .show(mSearchFilterViewFragmentClass)
                .hide(mEventsViewFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void displayToast(int toastMessage) {
        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void setLastSearchLatLng(LatLng location) {
        mLastSearch.locationCoordinates = location;
    }

}