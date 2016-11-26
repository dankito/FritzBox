package net.dankito.fritzbox.services;

import java.util.Calendar;

/**
 * Created by ganymed on 26/11/16.
 */

public interface ICronService {

  int startPeriodicalJob(long periodicalCheckTimeMillis, Runnable runnableToExecute);

  int startPeriodicalJob(Calendar startTime, long intervalMillis, Runnable runnableToExecute);

  boolean cancelPeriodicalJob(int cronJobTokenNumber);

}
