package com.project.musicianhub.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * View Pager Adapter class for the Fragments
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fragments = new ArrayList<>();
    ArrayList<String> tabTitles = new ArrayList<>();

    /**
     * Adds fragments to the tabs
     *
     * @param fragment fragment objects
     * @param title    fragment title
     */
    public void addFragments(Fragment fragment, String title) {
        this.fragments.add(fragment);
        this.tabTitles.add(title);
    }

    /**
     * Constructor for initializing view page adapter
     *
     * @param fm fragment manager object
     */
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Gets the fragment item
     *
     * @param position current fragment
     * @return fragment object
     */
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    /**
     * Gets fragment count
     *
     * @return fragment count
     */
    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * Gets the page title
     *
     * @param position current fragment position
     * @return get page title
     */
    @Override
    public CharSequence getPageTitle(int position) {

        return tabTitles.get(position);

    }

}
