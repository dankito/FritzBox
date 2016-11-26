package net.dankito.fritzbox.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.dankito.fritzbox.MainActivity;
import net.dankito.fritzbox.R;
import net.dankito.fritzbox.adapter.CallListAdapter;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

import javax.inject.Inject;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListFragment extends Fragment {

  @Inject
  protected FritzBoxClient fritzBoxClient;

  @Inject
  protected UserSettings userSettings;

  protected CallListAdapter callListAdapter;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    injectComponents();

    View view = inflater.inflate(R.layout.fragment_call_list, container, false);

    callListAdapter = new CallListAdapter(getActivity(), userSettings);

    ListView lstvwCallList = (ListView)view.findViewById(R.id.lstvwCallList);
    lstvwCallList.setAdapter(callListAdapter);

    getCallList();

    return view;
  }


  protected void getCallList() {
    fritzBoxClient.getCallListAsync(new GetCallListCallback() {
      @Override
      public void completed(GetCallListResponse response) {
        if(response.isSuccessful() == false) {
          // TODO: show error message
        }
        else {
          callListAdapter.setCallListThreadSafe(response.getCallList());
        }
      }
    });
  }


  protected void injectComponents() {
    ((MainActivity) getActivity()).getComponent().inject(this);
  }

}
