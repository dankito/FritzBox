package net.dankito.fritzbox.services;

import android.content.Context;
import android.content.res.Resources;

import net.dankito.fritzbox.R;
import net.dankito.fritzbox.listener.CallListListener;
import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.CallType;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListObserver {

  protected static final String CALL_LIST_FILENAME = "CallList.json";

  protected static final String COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG = "CouldNotGetCallList";

  protected static final String MISSED_CALL_NOTIFICATION_TAG = "MissedCall";


  private static final Logger log = LoggerFactory.getLogger(CallListObserver.class);


  protected Context context;

  protected FritzBoxClient fritzBoxClient;

  protected ICronService cronService;

  protected NotificationsService notificationsService;

  protected IFileStorageService fileStorageService;

  protected UserSettings userSettings;

  protected List<Call> callList = new ArrayList<>();

  protected List<CallListListener> callListListeners = new CopyOnWriteArrayList<>();


  public CallListObserver(Context context, FritzBoxClient fritzBoxClient, ICronService cronService, NotificationsService notificationsService,
                          IFileStorageService fileStorageService, UserSettings userSettings) {
    this.context = context;
    this.fritzBoxClient = fritzBoxClient;
    this.cronService = cronService;
    this.notificationsService = notificationsService;
    this.fileStorageService = fileStorageService;
    this.userSettings = userSettings;

    if(userSettings.isPeriodicalMissedCallsCheckEnabled() && userSettings.getPeriodicalMissedCallsCheckInterval() > 0) {
      startPeriodicalMissedCallsCheck(cronService, userSettings.getPeriodicalMissedCallsCheckInterval());
    }

    readStoredCallListAndThenUpdate();
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


  protected void readStoredCallListAndThenUpdate() {
    try {
      this.callList = new ArrayList<>(Arrays.asList(fileStorageService.readObjectFromFile(CALL_LIST_FILENAME, Call[].class)));

      callCallListUpdatedListeners(callList);
    } catch(Exception e) {
      log.error("Could not read call list from file " + CALL_LIST_FILENAME, e);
    }

    getCallListAsync();
  }


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
    List<Call> newlyRetrievedCalls = response.getCallList();
    List<Call> missedCalls = getNewMissedCalls(newlyRetrievedCalls);

    if(missedCalls.size() > 0) {
      showMissedCallsNotification(missedCalls);
    }

    handleCallListUpdate(response, newlyRetrievedCalls);
  }

  protected List<Call> getNewMissedCalls(List<Call> newlyRetrievedCalls) {
    List<Call> missedCalls = new ArrayList<>();

    if(this.callList.size() > 0) { // don't show all the missed calls on first app run
      for (Call call : newlyRetrievedCalls) {
        if(call.getType() == CallType.MISSED_CALL) {
          if(this.callList.contains(call) == false) {
            missedCalls.add(call);
          }
          else { // from now on callList only contains already known calls
            break;
          }
        }
      }
    }

    return missedCalls;
  }

  protected void showMissedCallsNotification(List<Call> missedCalls) {
    Resources resources = context.getResources();
    String title = resources.getString(R.string.notification_missed_call);
    int iconId = resources.getIdentifier("@android:drawable/stat_notify_missed_call", null, null);

    String missedCallsText = "";
    for(Call missedCall : missedCalls) {
      missedCallsText += missedCall.getCallerNumber() + "\n";
    }

    notificationsService.showNotification(title, missedCallsText, iconId, MISSED_CALL_NOTIFICATION_TAG);
  }

  protected void handleCallListUpdate(GetCallListResponse response, List<Call> newlyRetrievedCalls) {
    int countCallsBeforeMerge = callList.size();
    this.callList = mergeWithNewlyRetrievedCalls(newlyRetrievedCalls);

    if(callList.size() > countCallsBeforeMerge) { // call list updated
      callCallListUpdatedListeners(response.getCallList());

      try {
        fileStorageService.writeObjectToFile(callList, CALL_LIST_FILENAME);
      } catch(Exception e) {
        log.error("Could not write call list to file " + CALL_LIST_FILENAME, e);
      }
    }
  }

  protected List<Call> mergeWithNewlyRetrievedCalls(List<Call> newlyRetrievedCalls) {
    if(this.callList.size() == 0) {
      this.callList = newlyRetrievedCalls;
    }
    else {
      for (int i = 0; i < newlyRetrievedCalls.size(); i++) {
        Call retrievedCall = newlyRetrievedCalls.get(i);

        if(this.callList.contains(retrievedCall) == false) {
          this.callList.add(i, retrievedCall);
        }
        else { // the other calls we know already
          break;
        }
      }
    }

    return this.callList;
  }


  public boolean addCallListRetrievedListener(CallListListener listener) {
    return callListListeners.add(listener);
  }

  public boolean removeCallListRetrievedListener(CallListListener listener) {
    return callListListeners.remove(listener);
  }

  protected void callCallListUpdatedListeners(List<Call> callList) {
    for(CallListListener listener : callListListeners) {
      listener.callListUpdated(callList);
    }
  }


  public List<Call> getCallList() {
    return callList;
  }

}
