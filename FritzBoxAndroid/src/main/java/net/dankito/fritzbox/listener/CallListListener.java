package net.dankito.fritzbox.listener;

import net.dankito.fritzbox.model.Call;

import java.util.List;

/**
 * Created by ganymed on 26/11/16.
 */

public interface CallListListener {

  void callListUpdated(List<Call> callList);

}
