package com.hcmute.finalproject.musicApp_demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hcmute.finalproject.musicApp_demo.fragments.HomeFragment;
import com.hcmute.finalproject.musicApp_demo.fragments.LibraryFragment;
import com.hcmute.finalproject.musicApp_demo.fragments.SearchFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new SearchFragment();
            case 2:
                return new LibraryFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
