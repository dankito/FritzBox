package net.dankito.fritzbox.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.dankito.fritzbox.FritzBoxAndroidApplication;
import net.dankito.fritzbox.R;
import net.dankito.fritzbox.adapter.CallListAdapter;
import net.dankito.fritzbox.listener.CallListListener;
import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.CallListObserver;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListFragment extends Fragment {

  @Inject
  protected CallListObserver callListObserver;

  @Inject
  protected UserSettings userSettings;

  protected CallListAdapter callListAdapter;


  public CallListFragment() {
    setHasOptionsMenu(true);
  }


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    injectComponents();

    View view = inflater.inflate(R.layout.fragment_call_list, container, false);

    callListAdapter = new CallListAdapter(getActivity(), userSettings, callListObserver.getLastRetrievedCallList());

    ListView lstvwCallList = (ListView)view.findViewById(R.id.lstvwCallList);
    lstvwCallList.setAdapter(callListAdapter);

    return view;
  }


  protected void injectComponents() {
    ((FritzBoxAndroidApplication)getContext().getApplicationContext()).getComponent().inject(this);

    callListObserver.addCallListRetrievedListener(new CallListListener() {
      @Override
      public void callListUpdated(List<Call> callList) {
        callListAdapter.setCallListThreadSafe(callList);
      }
    });
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_fragment_call_list, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmRefreshCallList) {
      refreshCallList();
      return true;
    }
    else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected void refreshCallList() {
    callListObserver.refreshCallList();
  }

}
