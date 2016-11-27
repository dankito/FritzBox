package net.dankito.fritzbox.services;

import android.content.BroadcastReceiver;

import java.util.Calendar;

/**
 * Created by ganymed on 26/11/16.
 */

public interface ICronService {

  int startPeriodicalJob(long periodicalCheckTimeMillis, Class<? extends BroadcastReceiver> classThatReceivesBroadcastWhenPeriodElapsed);

  int startPeriodicalJob(Calendar startTime, long intervalMillis, Class<? extends BroadcastReceiver> classThatReceivesBroadcastWhenPeriodElapsed);

  boolean cancelPeriodicalJob(int cronJobTokenNumber);

}
