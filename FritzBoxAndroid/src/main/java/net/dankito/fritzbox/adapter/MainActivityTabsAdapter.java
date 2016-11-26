package net.dankito.fritzbox.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.dankito.fritzbox.R;
import net.dankito.fritzbox.fragments.CallListFragment;

/**
 * Created by ganymed on 26/11/16.
 */

public class MainActivityTabsAdapter extends FragmentPagerAdapter {

  protected Activity activity;

  protected FragmentManager fragmentManager;

  protected CallListFragment callListFragment = null;


  public MainActivityTabsAdapter(Activity activity, FragmentManager fragmentManager) {
    super(fragmentManager);

    this.activity = activity;
    this.fragmentManager = fragmentManager;
  }

  @Override
  public int getCount() {
    return 1;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    Resources resources = activity.getResources();

    switch (position) {
      case 0:
        return resources.getString(R.string.tab_title_call_list);
    }

    return null;
  }

  @Override
  public Fragment getItem(int position) {
    if(position == 0) {
      if(callListFragment == null) {
        callListFragment = new CallListFragment();
      }
      return callListFragment;
    }

    return null;
  }
}
