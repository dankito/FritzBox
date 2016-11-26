package net.dankito.fritzbox.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dankito.fritzbox.MainActivity;
import net.dankito.fritzbox.R;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    injectComponents();

    View view = inflater.inflate(R.layout.fragment_call_list, container, false);

    return view;
  }

  protected void injectComponents() {
    ((MainActivity) getActivity()).getComponent().inject(this);
  }

}
