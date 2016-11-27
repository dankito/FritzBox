package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.listener.UserSettingsManagerListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 27/11/16.
 */

public class UserSettingsManager {

  protected static final String USER_SETTINGS_FILENAME = "UserSettings.json";


  protected IFileStorageService fileStorageService;

  protected List<UserSettingsManagerListener> listeners = new CopyOnWriteArrayList<>();


  public UserSettingsManager(IFileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }


  public UserSettings deserializeUserSettings() {
    try {
      return fileStorageService.readObjectFromFile(USER_SETTINGS_FILENAME, UserSettings.class);
    } catch(Exception e) {
      return createDefaultUserSettings();
    }
  }

  protected UserSettings createDefaultUserSettings() {
    UserSettings defaultSettings = new UserSettings("", "");

    defaultSettings.setEnablePeriodicalMissedCallsCheck(false);
    defaultSettings.setPeriodicalMissedCallsCheckInterval(2 * 60 * 1000); // every 2 minutes

    return defaultSettings;
  }


  public void saveUserSettings(UserSettings userSettings) throws Exception {
    fileStorageService.writeObjectToFile(userSettings, USER_SETTINGS_FILENAME);

    callListeners(userSettings);
  }


  public boolean addListener(UserSettingsManagerListener listener) {
    return listeners.add(listener);
  }

  public boolean removeListener(UserSettingsManagerListener listener) {
    return listeners.remove(listener);
  }

  protected void callListeners(UserSettings updatedSettings) {
    for(UserSettingsManagerListener listener : listeners) {
      listener.userSettingsUpdated(updatedSettings);
    }
  }

}
