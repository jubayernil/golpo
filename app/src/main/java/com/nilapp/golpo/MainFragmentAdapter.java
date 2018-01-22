package com.nilapp.golpo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by mac on 4/12/17.
 */

class MainFragmentAdapter extends FragmentPagerAdapter{
    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RequestsFragment mRequestsFragment = new RequestsFragment();
                return mRequestsFragment;
            case 1:
                ChatsFragment mChatsFragment = new ChatsFragment();
                return mChatsFragment;
            case 2:
                FriendsFragment mFriendsFragment = new FriendsFragment();
                return mFriendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0: return "REQUESTS";
            case 1: return "CHATS";
            case 2: return "FRIENDS";
            default: return null;
        }
    }
}
