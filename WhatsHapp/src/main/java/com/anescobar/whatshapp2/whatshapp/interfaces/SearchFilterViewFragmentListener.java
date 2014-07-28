package com.anescobar.whatshapp2.whatshapp.interfaces;

import com.anescobar.whatshapp2.whatshapp.models.Search;

/**
 * Created by Andres Escobar on 6/21/14.
 * interface for searchFilterView
 */
public interface SearchFilterViewFragmentListener {

    public void performEventSearch(Search searchParams);

    public void displayEventsViewFragment();
}
