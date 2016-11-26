package net.dankito.fritzbox.services;

import android.content.Context;
import android.content.res.Resources;

import net.dankito.fritzbox.R;
import net.dankito.fritzbox.listener.CallListListener;
import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListObserver {

  protected static final String COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG = "CouldNotGetCallList";

  protected static final String MISSED_CALL_NOTIFICATION_TAG = "MissedCall";


  protected Context context;

  protected FritzBoxClient fritzBoxClient;

  protected ICronService cronService;

  protected NotificationsService notificationsService;

  protected UserSettings userSettings;

  protected List<CallListListener> callListListeners = new CopyOnWriteArrayList<>();


  public CallListObserver(Context context, FritzBoxClient fritzBoxClient, ICronService cronService, NotificationsService notificationsService, UserSettings userSettings) {
    this.context = context;
    this.fritzBoxClient = fritzBoxClient;
    this.cronService = cronService;
    this.notificationsService = notificationsService;
    this.userSettings = userSettings;

    if(userSettings.isPeriodicalMissedCallsCheckEnabled() && userSettings.getPeriodicalMissedCallsCheckInterval() > 0) {
      startPeriodicalMissedCallsCheck(cronService, userSettings.getPeriodicalMissedCallsCheckInterval());
    }
    else { // if periodical check is disabled, get it at least at start up
      getCallListAsync();
    }
  }

  protected void startPeriodicalMissedCallsCheck(ICronService cronService, long periodicalMissedCallsCheckInterval) {
    cronService.startPeriodicalJob(periodicalMissedCallsCheckInterval, checkForMissedCallsRunnable);
  }

  protected Runnable checkForMissedCallsRunnable = new Runnable() {
    @Override
    public void run() {
      getCallListAsync();
    }
  };

  protected void getCallListAsync() {
    fritzBoxClient.getCallListAsync(new GetCallListCallback() {
      @Override
      public void completed(GetCallListResponse response) {
        if(response.isSuccessful() == false) {
          showCouldNotRetrieveCallListNotification(response);
        }
        else {
          retrievedCallList(response);
        }
      }
    });
  }

  protected void showCouldNotRetrieveCallListNotification(GetCallListResponse response) {
    Resources resources = context.getResources();
    String title = resources.getString(R.string.notification_could_not_get_call_list);
    int iconId = resources.getIdentifier("@android:drawable/stat_notify_error", null, null);

    notificationsService.showNotification(title, response.getError(), iconId, COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG);
  }

  protected void retrievedCallList(GetCallListResponse response) {
    List<Call> missedCalls = getNewMissedCalls(response.getCallList());

    if(missedCalls.size() > 0) {
      Resources resources = context.getResources();
      String title = resources.getString(R.string.notification_missed_call);
      int iconId = resources.getIdentifier("@android:drawable/stat_notify_missed_call", null, null);

      String missedCallsText = "";
      for(Call missedCall : missedCalls) {
        missedCallsText += missedCall.getCallerNumber() + "\n";
      }

      notificationsService.showNotification(title, missedCallsText, iconId, MISSED_CALL_NOTIFICATION_TAG);
    }

    callCallListRetrievedListeners(response.getCallList());
  }

  protected List<Call> getNewMissedCalls(List<Call> callList) {
    List<Call> missedCalls = new ArrayList<>();

    // TODO

    return missedCalls;
  }


  public boolean addCallListRetrievedListener(CallListListener listener) {
    return callListListeners.add(listener);
  }

  public boolean removeCallListRetrievedListener(CallListListener listener) {
    return callListListeners.remove(listener);
  }

  protected void callCallListRetrievedListeners(List<Call> callList) {
    for(CallListListener listener : callListListeners) {
      listener.callListUpdated(callList);
    }
  }

}
