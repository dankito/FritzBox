package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.UserSettings;

/**
 * Created by ganymed on 27/11/16.
 */

public class UserSettingsManager {

  protected static final String USER_SETTINGS_FILENAME = "UserSettings.json";


  protected IFileStorageService fileStorageService;


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
    return new UserSettings("", "");
  }


  public void saveUserSettings(UserSettings userSettings) throws Exception {
    fileStorageService.writeObjectToFile(userSettings, USER_SETTINGS_FILENAME);
  }
}
