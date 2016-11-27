package net.dankito.fritzbox.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 26/11/16.
 */

public class AlarmManagerCronService implements ICronService {

  protected static final String CRON_JOB_TOKEN_NUMBER_EXTRA_NAME = "CronJobTokenNumber";

  private static final Logger log = LoggerFactory.getLogger(AlarmManagerCronService.class);


  protected static Map<Integer, PendingIntent> startedJobs = new ConcurrentHashMap<>();

  protected static int NextCronJobTokenNumber = 1;


  protected Context context;


  // needed for Entry in AndroidManifest.xml, in this way AlarmManagerCronService gets waked up by Broadcast
  public AlarmManagerCronService() {

  }

  public AlarmManagerCronService(Context context) {
    this.context = context;
  }


  /**
   *
   * @param periodicalCheckTimeMillis
   * @param classThatReceivesBroadcastWhenPeriodElapsed
   * @return The cron job token number to uniquely identify started cron job
   */
  public int startPeriodicalJob(long periodicalCheckTimeMillis, Class<? extends BroadcastReceiver> classThatReceivesBroadcastWhenPeriodElapsed) {
    Calendar startTime = Calendar.getInstance();
    startTime.setTimeInMillis(System.currentTimeMillis());
    startTime.add(Calendar.MILLISECOND, (int)periodicalCheckTimeMillis);

    return startPeriodicalJob(startTime, periodicalCheckTimeMillis, classThatReceivesBroadcastWhenPeriodElapsed);
  }

  /**
   *
   * @param startTime
   * @param classThatReceivesBroadcastWhenPeriodElapsed
   * @return The cron job token number to uniquely identify started cron job
   */
  public int startPeriodicalJob(Calendar startTime, long intervalMillis, Class<? extends BroadcastReceiver> classThatReceivesBroadcastWhenPeriodElapsed) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    // if this time today has already passed, schedule for next day
    if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= startTime.get(Calendar.HOUR_OF_DAY) &&
        Calendar.getInstance().get(Calendar.MINUTE) >= startTime.get(Calendar.MINUTE)) {
      calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    calendar.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
    calendar.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
    calendar.set(Calendar.SECOND, 0);

    AlarmManager alarmManager = getAlarmManager();
    int tokenNumber = NextCronJobTokenNumber++;

    Intent intent = new Intent(context, classThatReceivesBroadcastWhenPeriodElapsed);
    intent.putExtra(CRON_JOB_TOKEN_NUMBER_EXTRA_NAME, tokenNumber);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, tokenNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
        intervalMillis, pendingIntent);

    startedJobs.put(tokenNumber, pendingIntent);

    log.info("Started a periodical cron job with token number " + tokenNumber + " for " + calendar.getTime());

    return tokenNumber;
  }


  public boolean cancelPeriodicalJob(int cronJobTokenNumber) {
    log.info("Trying to cancel cron job with token number " + cronJobTokenNumber);
    PendingIntent pendingIntent = startedJobs.remove(cronJobTokenNumber);

    if(pendingIntent != null) {
      AlarmManager alarmManager = getAlarmManager();

      alarmManager.cancel(pendingIntent);
      log.info("Cancelled cron job with token number " + cronJobTokenNumber);
      return true;
    }

    log.warn("No cron job found for token number " + cronJobTokenNumber);
    return false;
  }


  protected AlarmManager getAlarmManager() {
    return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
  }

}
