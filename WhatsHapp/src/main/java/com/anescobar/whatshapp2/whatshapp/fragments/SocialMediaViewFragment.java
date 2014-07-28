package com.anescobar.whatshapp2.whatshapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.whatshapp2.whatshapp.R;

/**
 * Created by Andres Escobar on 6/17/14.
 * Fragment for Social Media view
 * Displays instagram and twitter posts
 */

public class SocialMediaViewFragment extends Fragment {

    public SocialMediaViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_social_media_view, container, false);
    }
}