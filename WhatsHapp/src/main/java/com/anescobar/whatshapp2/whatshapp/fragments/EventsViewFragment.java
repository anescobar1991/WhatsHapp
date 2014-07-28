package com.anescobar.whatshapp2.whatshapp.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anescobar.whatshapp2.whatshapp.R;
import com.anescobar.whatshapp2.whatshapp.models.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Andres Escobar on 6/17/14.
 * Fragment for Event View (Home screen)
 * Displays map with all events as well as a view pager of EventSlideFragments
 */

public class EventsViewFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
    private View mView;
    private GoogleMap mMap;
    private ProgressBar mEventsProgressBar;
    private ViewPager mPager;
    private int mEventCount;
    private TextView mEventsMessageHolder;
    private ArrayList<LatLng> mMarkers = new ArrayList<LatLng>();

    public EventsViewFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
        }

        try {
            mView = inflater.inflate(R.layout.fragment_events_view, container, false);
            mPager = (ViewPager) mView.findViewById(R.id.eventsView_viewPager_eventsPager);
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.eventsView_MapFragment_map)).getMap();
            mMap.setMyLocationEnabled(true);
            mEventsProgressBar = (ProgressBar) mView.findViewById(R.id.eventsView_progressBar_eventsProgressBar);
            mEventsMessageHolder = (TextView) mView.findViewById(R.id.eventsView_textView_eventsSlideMessageContainer);
            mMap.setOnMarkerClickListener(this);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        return mView;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        displaySpecificEvent(Integer.parseInt(marker.getSnippet()));
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(marker.getPosition(), 15));

        return true;
    }

    public void moveMapCamera(LatLng location, int zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
    }

    public void createMapMarker(LatLng latLng, int position) {
        //if there is already a marker for same spot, creates new marker in a very slightly different spot than original
        //that way user can see ALL markers for same location
        if (mMarkers.contains(latLng)) {
            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latLng.latitude * (Math.random() * (1.000001 - .999999) + .999999),
                                    latLng.longitude * (Math.random() * (1.000001 - .999999) + .999999)))
                            .snippet(Integer.toString(position))
            );
        } else {
            mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .snippet(Integer.toString(position))
            );
        }
        mMarkers.add(latLng);
    }

    //makes sure that when updating events old events are removed from map and events pager
    // and progress bar is displayed
    public void clearEvents() {
        mMap.clear();
        mPager.removeAllViews();
        mEventsProgressBar.setVisibility(View.VISIBLE);
        mEventsMessageHolder.setVisibility(View.GONE);
    }

    //initializes viewPager that holds events
    public void displayEvents(ArrayList<Event> eventList) {
        //if no events found for location
        if (eventList.isEmpty()) {
            displayMessageInEventsMessageHolder(R.string.no_events_found_message);
        } else {
            PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), eventList);
            mPager.setAdapter(pagerAdapter);
        }
        setEventsProgressBarInvisible();
    }

    public void displaySpecificEvent(int position) {
        mPager.setCurrentItem(position);
    }

    public void displayMessageInEventsMessageHolder(int message) {
        mEventsMessageHolder.setVisibility(View.VISIBLE);
        mEventsMessageHolder.setText(message);
    }

    public void setEventsProgressBarInvisible() {
        mEventsProgressBar.setVisibility(View.GONE);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Event> eventList;

        public ScreenSlidePagerAdapter(FragmentManager fragmentManager, ArrayList<Event> eventList) {
            super(fragmentManager);
            this.eventList = eventList;
            mEventCount = eventList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return EventsSlideFragment.newInstance(eventList, position, getCount());
        }

        @Override
        public int getCount() {
            return mEventCount;
        }

    }

}