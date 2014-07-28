package com.anescobar.whatshapp2.whatshapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.anescobar.whatshapp2.whatshapp.R;
import com.anescobar.whatshapp2.whatshapp.interfaces.SearchFilterViewFragmentListener;
import com.anescobar.whatshapp2.whatshapp.models.Search;

/**
 * Created by Andres Escobar on 6/17/14.
 * Fragment for events filter/search view
 */
public class SearchFilterViewFragment extends Fragment {
    private Spinner mEventsCategorySpinner;
    private EditText mLocationSearchField;
    private EditText mEventsSearchField;
    private SeekBar mSearchRadiusSeekBar;
    private SearchFilterViewFragmentListener mListener;

    public SearchFilterViewFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SearchFilterViewFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SearchFilterViewFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_filter_view, container, false);
        mEventsCategorySpinner = (Spinner) view.findViewById(R.id.searchFilterView_spinner_eventCatSpinner);
        mSearchRadiusSeekBar = (SeekBar) view.findViewById(R.id.searchFilterView_seekBar_searchRadiusSelector);
        mEventsSearchField = (EditText) view.findViewById(R.id.searchFilterView_editText_eventsSearchBar);
        mLocationSearchField = (EditText) view.findViewById(R.id.searchFilterView_editText_locationSearchBar);
        final TextView radiusSelectedLabel = (TextView) view.findViewById(R.id.searchFilterView_textView_radiusSelectedLabel);
        final ImageButton clearEventsSearchFieldButton = (ImageButton) view.findViewById(R.id.searchFilterView_imageButton_eventSearchBarClearTextButton);
        final ImageButton clearLocationSearchFieldButton = (ImageButton) view.findViewById(R.id.searchFilterView_imageButton_locationSearchBarClearTextButton);
        final Button resetButton = (Button) view.findViewById(R.id.searchFilterView_button_resetFilterSearchButton);
        final Button submitButton = (Button) view.findViewById(R.id.searchFilterView_button_submitFilterSearchButton);
        //set to default search radius of 10 to start
        mSearchRadiusSeekBar.setProgress(4);
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.event_categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEventsCategorySpinner.setAdapter(adapter);

        //gets seekBar value and updates the textView with it
        mSearchRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;
            String distanceUnits = " miles";

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    radiusSelectedLabel.setText(progress + 1 + distanceUnits);
                }
                //adding 1 so that user can never see a value of 0 and thus arent confused
                progressChanged = progress + 1;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progressChanged == 1) {
                    distanceUnits = " mile";
                }
                radiusSelectedLabel.setText(progressChanged + distanceUnits);
                //setting distanceUnites back to "miles"
                distanceUnits = " miles";
                if (progressChanged == 5) {
                    resetButton.setEnabled(false);
                } else {
                    resetButton.setEnabled(true);
                }
            }
        });
        //sets listener to display clear text button only when events textField is not empty
        mEventsSearchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0) {
                    clearEventsSearchFieldButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(true);
                } else if (s.length() == 0) {
                    clearEventsSearchFieldButton.setVisibility(View.GONE);
                    resetButton.setEnabled(false);
                }
            }
        });

        //sets Onclick action to clear editText when event events searchbar clearText button is tapped
        clearEventsSearchFieldButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearEventSearchField();
            }
        });
        //sets listener to display clear text button only when location searchBar is not empty
        mLocationSearchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0) {
                    //changes text size so its more visible to user
                    mLocationSearchField.setTextSize(16);
                    clearLocationSearchFieldButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(true);
                } else if (s.length() == 0) {
                    //decreases text size to 12, so that full hint is displayed
                    mLocationSearchField.setTextSize(12);
                    clearLocationSearchFieldButton.setVisibility(View.GONE);
                    resetButton.setEnabled(false);
                }
            }
        });
//        sets Onclick action to clear editText when location searchBar clearText button is tapped
        clearLocationSearchFieldButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearLocationSearchField();
            }
        });

        //onClick for submit button, performs search
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int categoryPosition = mEventsCategorySpinner.getSelectedItemPosition();
                String category = getActivity().getResources().getStringArray(R.array.event_category_ids_array)[categoryPosition];
                String locationSearch = mLocationSearchField.getText().toString();
                if (locationSearch.length() == 0) {
                    locationSearch = null;
                }
                Search searchParams = new Search(null, locationSearch, mSearchRadiusSeekBar.getProgress() + 1,
                        mEventsSearchField.getText().toString(), category, 1
                );
                mListener.performEventSearch(searchParams);
                mListener.displayEventsViewFragment();

            }
        });

        //onClick for reset button, resets all fields in screen to default values
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetAllFields();
            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem refresh_icon = menu.findItem(R.id.action_refreshIcon);
        MenuItem location_search_icon = menu.findItem(R.id.action_searchIcon);

        refresh_icon.setVisible(false);
        location_search_icon.setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    //resets all search fields to their default
    private void resetAllFields() {
        mEventsCategorySpinner.setSelection(0);
        clearEventSearchField();
        clearLocationSearchField();
        mSearchRadiusSeekBar.setProgress(4);
    }

    private void clearEventSearchField() {
        mEventsSearchField.setText("");
    }

    private void clearLocationSearchField() {
        mLocationSearchField.setText("");
    }

}