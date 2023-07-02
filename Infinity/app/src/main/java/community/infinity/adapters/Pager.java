package community.infinity.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import layout.InfiniteTimeline;
import layout.Messaging;
import layout.Notifications;
import layout.Society_Show;

/**
 * Created by Srinu on 05-08-2017.
 */
//Extending FragmentStatePagerAdapter
public class Pager extends FragmentStatePagerAdapter {
    Context context;
    //integer to count number of tabs
    int tabCount;

    ArrayList<String> names=new ArrayList<>();

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount,Context context) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
        this.context=context;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs


        switch (position) {
            case 0:
                return new Society_Show();
            case 1:
                return new InfiniteTimeline();
            case 2:
                return new Messaging();
            case 3:
                return new Notifications();
            default:
                return null;
        }


    }




    @Override
    public int getCount() {
        return tabCount;
    }
}
