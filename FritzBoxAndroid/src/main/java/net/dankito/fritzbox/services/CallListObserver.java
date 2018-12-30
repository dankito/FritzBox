package net.dankito.fritzbox.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import net.dankito.fritzbox.FritzBoxAndroidApplication;
import net.dankito.fritzbox.R;
import net.dankito.fritzbox.listener.CallListListener;
import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.CallType;
import net.dankito.fritzbox.model.NotificationConfig;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.listener.UserSettingsManagerListener;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListObserver extends BroadcastReceiver {

  protected static final String CALL_LIST_FILENAME = "CallList.json";

  protected static final String COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG = "CouldNotGetCallList";

  protected static final String MISSED_CALL_NOTIFICATION_TAG = "MissedCall";

  protected static final int CRON_JOB_TOKEN_NOT_SET = -1;

  protected static final DateFormat TIME_ONLY_MISSED_CALL_DATE_FORMAT = new SimpleDateFormat("HH:mm"); // TODO: make international

  protected static final DateFormat TIME_AND_DATE_MISSED_CALL_DATE_FORMAT = new SimpleDateFormat("dd.MM HH:mm");


  private static final Logger log = LoggerFactory.getLogger(CallListObserver.class);


  protected Context context;

  @Inject
  protected FritzBoxClient fritzBoxClient;

  @Inject
  protected ICronService cronService;

  @Inject
  protected NotificationsService notificationsService;

  @Inject
  protected IFileStorageService fileStorageService;

  @Inject
  protected UserSettings userSettings;

  @Inject
  protected UserSettingsManager userSettingsManager;

  @Inject
  protected INetworkService networkService;


  protected List<Call> callList = new ArrayList<>();

  protected int cronJobToken = CRON_JOB_TOKEN_NOT_SET;

  protected List<CallListListener> callListListeners = new CopyOnWriteArrayList<>();


  public CallListObserver() {

  }

  public CallListObserver(Context context) {
    this.context = context;

    setupDependencyInjection(context);

    mayStartPeriodicalMissedCallsCheck();

    getCallListIfInHomeNetworkAsync();
  }

  protected void setupDependencyInjection(Context context) {
    ((FritzBoxAndroidApplication)context.getApplicationContext()).getComponent().inject(this);

    userSettingsManager.addListener(userSettingsManagerListener);

    readStoredCallList();
  }

  protected void mayStartPeriodicalMissedCallsCheck() {
    if(userSettings.isPeriodicalMissedCallsCheckEnabled() && userSettings.getPeriodicalMissedCallsCheckInterval() > 0) {
      startPeriodicalMissedCallsCheck(cronService, userSettings.getPeriodicalMissedCallsCheckInterval());
    }
  }

  protected void startPeriodicalMissedCallsCheck(ICronService cronService, long periodicalMissedCallsCheckInterval) {
    cronJobToken = cronService.startPeriodicalJob(periodicalMissedCallsCheckInterval, CallListObserver.class);
  }

  protected void stopPeriodicalMissedCallsCheck() {
    cronService.cancelPeriodicalJob(cronJobToken);
    cronJobToken = CRON_JOB_TOKEN_NOT_SET;
  }


  protected void readStoredCallList() {
    try {
      this.callList = new ArrayList<>(Arrays.asList(fileStorageService.readObjectFromFile(CALL_LIST_FILENAME, Call[].class)));

      callCallListUpdatedListeners(callList);
    } catch(Exception e) {
      log.error("Could not read call list from file " + CALL_LIST_FILENAME, e);
    }
  }


  public void refreshCallList() {
    getCallListAsync();
  }

  protected void getCallListIfInHomeNetworkAsync() {
    if(userSettings.isCheckOnlyInHomeNetwork() == false || isInHomeNetwork()) {
      getCallListAsync();
    }
  }

  protected void getCallListAsync() {
    fritzBoxClient.getCallListAsync(new GetCallListCallback() {
      @Override
      public void completed(GetCallListResponse response) {
        getCallListAsyncCompleted(response);
      }
    });
  }

  protected void getCallListIfInHomeNetworkSynchronously() {
    if(userSettings.isCheckOnlyInHomeNetwork() == false || isInHomeNetwork()) {
      getCallListSynchronously();
    }
    else {
      log.info("Is in network '" + networkService.getCurrentSsid() + "' not in home network '" + userSettings.getHomeNetworkSsid() + "'");
    }
  }

  protected void getCallListSynchronously() {
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    fritzBoxClient.getCallListAsync(new GetCallListCallback() {
      @Override
      public void completed(GetCallListResponse response) {
        getCallListAsyncCompleted(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(30, TimeUnit.SECONDS); } catch(Exception e) { }
  }

  protected boolean isInHomeNetwork() {
    return networkService.isInHomeNetwork(userSettings.getHomeNetworkSsid());
  }

  protected void getCallListAsyncCompleted(GetCallListResponse response) {
    if(response.isSuccessful() == false) {
      showCouldNotRetrieveCallListNotification(response);
    }
    else {
      retrievedCallList(response);
    }
  }

  protected void showCouldNotRetrieveCallListNotification(GetCallListResponse response) {
    if(userSettings.isFritzBoxAddressSet() == false && userSettings.isFritzBoxPasswordSet() == false) { // start-up, settings not set yet
      return;
    }

    Resources resources = context.getResources();
    String title = resources.getString(R.string.notification_could_not_get_call_list);
    int iconId = resources.getIdentifier("@android:drawable/stat_notify_error", null, null);

    notificationsService.showNotification(new NotificationConfig(title, response.getError(), iconId), COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG);
  }

  protected void retrievedCallList(GetCallListResponse response) {
    notificationsService.dismissNotification(COULD_NOT_GET_CALL_LIST_NOTIFICATION_TAG); // dismiss if there has been any

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

    String missedCallsText = createMissedCallsText(missedCalls);

    NotificationConfig config = new NotificationConfig(title, missedCallsText, iconId, true);
    config.setLetLedBlink(0xFF0000FF, 400, 500);

    notificationsService.showNotification(config, MISSED_CALL_NOTIFICATION_TAG);
  }

  @NonNull
  protected String createMissedCallsText(List<Call> missedCalls) {
    String missedCallsText = "";
    int todaysDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    for(Call missedCall : missedCalls) {
      Calendar dayOfCall = Calendar.getInstance();
      dayOfCall.setTime(missedCall.getDate());

      if(todaysDayOfMonth != dayOfCall.get(Calendar.DAY_OF_MONTH)) {
        missedCallsText += TIME_AND_DATE_MISSED_CALL_DATE_FORMAT.format(missedCall.getDate());
      }
      else {
        missedCallsText += TIME_ONLY_MISSED_CALL_DATE_FORMAT.format(missedCall.getDate());
      }
      missedCallsText += "   " + missedCall.getCallerNumber() + "\n";
    }

    if(missedCallsText.length() > 0) {
      missedCallsText = missedCallsText.substring(0, missedCallsText.length() - 1); // cut off last '\n'
    }

    return missedCallsText;
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


  protected UserSettingsManagerListener userSettingsManagerListener = new UserSettingsManagerListener() {
    @Override
    public void userSettingsUpdated(UserSettings updatedSettings) {
      getCallListIfInHomeNetworkAsync();

      checkIfPeriodicCallListUpdateSettingsChanged(updatedSettings);
    }
  };

  protected void checkIfPeriodicCallListUpdateSettingsChanged(UserSettings updatedSettings) {
    if(updatedSettings.isPeriodicalMissedCallsCheckEnabled() == false && cronJobToken != CRON_JOB_TOKEN_NOT_SET) {
      stopPeriodicalMissedCallsCheck();
    }
    else if(updatedSettings.isPeriodicalMissedCallsCheckEnabled()) {
      if(cronJobToken != CRON_JOB_TOKEN_NOT_SET) {
        stopPeriodicalMissedCallsCheck();
      }

      startPeriodicalMissedCallsCheck(cronService, updatedSettings.getPeriodicalMissedCallsCheckInterval());
    }

    setPeriodicalMissingCallsCheckOnBoot(updatedSettings.isPeriodicalMissedCallsCheckEnabled());
  }

  protected void setPeriodicalMissingCallsCheckOnBoot(boolean enablePeriodicalChecksOnBoot) {
    ComponentName receiver = new ComponentName(context, CallListObserver.class);
    PackageManager pm = context.getPackageManager();

    int enableOrDisable = enablePeriodicalChecksOnBoot ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

    pm.setComponentEnabledSetting(receiver,
        enableOrDisable,
        PackageManager.DONT_KILL_APP);
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


  public List<Call> getLastRetrievedCallList() {
    return callList;
  }


  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    setupDependencyInjection(context);

    if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) { // Android system has booted
      systemHasBooted(context);
    }
    else { // fired by ICronService
      periodToCheckForMissedCallsElapsed();
    }
  }

  protected void systemHasBooted(Context context) {
    try {
      getCallListIfInHomeNetworkSynchronously();

      mayStartPeriodicalMissedCallsCheck();
    } catch(Exception e) {
      log.error("Could not start periodical missed call check on ACTION_BOOT_COMPLETED broadcast", e);
    }
  }

  protected void periodToCheckForMissedCallsElapsed() {
    log.info("Running periodical missed call check ...");
    getCallListIfInHomeNetworkSynchronously();
  }

}
