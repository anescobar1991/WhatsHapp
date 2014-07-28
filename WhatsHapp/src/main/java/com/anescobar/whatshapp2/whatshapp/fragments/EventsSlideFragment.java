package com.anescobar.whatshapp2.whatshapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anescobar.whatshapp2.whatshapp.R;
import com.anescobar.whatshapp2.whatshapp.interfaces.EventsSlideFragmentListener;
import com.anescobar.whatshapp2.whatshapp.models.Event;
import com.anescobar.whatshapp2.whatshapp.utilsAndHelpers.ImageDownloader;

import java.util.ArrayList;

/**
 * Created by Andres Escobar on 6/17/14.
 * Fragment for Events Slide
 * Single instance of this fragment displays data for one event
 */
public class EventsSlideFragment extends Fragment {

    private Event mEvent;
    private EventsSlideFragmentListener mListener;


    public static EventsSlideFragment newInstance(ArrayList<Event> eventList, int eventIndex, int eventCount) {
        EventsSlideFragment eventsSlideFragment = new EventsSlideFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("eventList", eventList);
        args.putInt("eventIndex", eventIndex);
        args.putInt("eventCount", eventCount);
        eventsSlideFragment.setArguments(args);

        return eventsSlideFragment;
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof EventsSlideFragmentListener) {
            mListener = (EventsSlideFragmentListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement EventsSlideFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEvent = (Event) getArguments().getParcelableArrayList("eventList").get(getArguments().getInt("eventIndex"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_slide, container, false);
        Button eventNameTextView = (Button) view.findViewById(R.id.eventsSlide_textView_eventName);
        TextView venueNameTextView = (TextView) view.findViewById(R.id.eventsSlide_textView_venueName);
        TextView venueAddressTextView = (TextView) view.findViewById(R.id.eventsSlide_textView_venueAddress);
        TextView venueCityRegionCountryTextView = (TextView) view.findViewById(R.id.eventsSlide_textView_venueCityRegionCountry);
        String eventCityRegionCountry = mEvent.venueCity;
        ImageView eventImage = (ImageView) view.findViewById(R.id.eventsSlide_imageView_event_image);
        ImageView previousEvent = (ImageView) view.findViewById(R.id.eventsSlide_imageView_previous_event);
        ImageView nextEvent = (ImageView) view.findViewById(R.id.eventsSlide_imageView_next_event);
        int eventIndex = getArguments().getInt("eventIndex");

        //displays previous event button only if there is a previous event
        if (eventIndex == 0) {
            previousEvent.setVisibility(View.INVISIBLE);
        }
        //displays next event button only if there is a next event
        if (eventIndex == getArguments().getInt("eventCount") - 1) {
            nextEvent.setVisibility(View.INVISIBLE);
        }

        eventNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewEventDetailsInBrowser(v);
            }
        });

        previousEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPreviousEvent(v);
            }
        });

        nextEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextEvent(v);
            }
        });

        //handles logic for displaying events details
        if (!mEvent.venueRegion.equals("null")) {
            eventCityRegionCountry += " " + mEvent.venueRegion;
        }
        eventCityRegionCountry += " " + mEvent.venueCountryAbbr;
        eventNameTextView.setText(mEvent.eventName);
        if (!mEvent.venueName.equals("null")) {
            venueNameTextView.setText(mEvent.venueName);
        } else {
            venueNameTextView.setVisibility(View.GONE);
        }
        if (!mEvent.venueAddress.equals("null")) {
            venueAddressTextView.setText(mEvent.venueAddress);
        } else {
            venueAddressTextView.setVisibility(View.GONE);
        }
        venueCityRegionCountryTextView.setText(eventCityRegionCountry);

        if (mEvent.imageUrl != null) {
            new ImageDownloader(eventImage).execute(mEvent.imageUrl);
        } else {
            eventImage.setVisibility(View.GONE);
        }

        return view;
    }

    private void viewEventDetailsInBrowser(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mEvent.url));
        startActivity(browserIntent);
    }

    public void navigateToPreviousEvent(View v) {
        mListener.onPreviousEventButtonTap(getArguments().getInt(("eventIndex")));
    }

    public void navigateToNextEvent(View v) {
        mListener.onNextEventButtonTap(getArguments().getInt(("eventIndex")));
    }
}