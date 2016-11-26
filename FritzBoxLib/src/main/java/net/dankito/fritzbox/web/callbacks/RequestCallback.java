package net.dankito.fritzbox.web.callbacks;

import net.dankito.fritzbox.web.responses.WebClientResponse;

/**
 * Created by ganymed on 03/11/16.
 */

public interface RequestCallback {

  void completed(WebClientResponse response);

}
