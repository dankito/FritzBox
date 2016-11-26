package net.dankito.fritzbox.di;

import android.app.Activity;

import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.CsvParser;
import net.dankito.fritzbox.services.DigestService;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.services.ICsvParser;
import net.dankito.fritzbox.services.IDigestService;
import net.dankito.fritzbox.utils.web.IWebClient;
import net.dankito.fritzbox.utils.web.OkHttpWebClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ganymed on 03/11/16.
 */
@Module
public class AndroidDiContainer {

  protected final Activity activity;


  public AndroidDiContainer (Activity activity) {
    this.activity = activity;
  }


  @Provides //scope is not necessary for parameters stored within the module
  public Activity getActivity() {
    return activity;
  }


  @Provides
  @Singleton
  public UserSettings provideUserSettings() {
    UserSettings settings = new UserSettings("", ""); // TODO: read from storage

    return settings;
  }

  @Provides
  @Singleton
  public IWebClient provideWebClient() {
    return new OkHttpWebClient();
  }

  @Provides
  @Singleton
  public IDigestService provideDigestService() {
    return new DigestService();
  }

  @Provides
  @Singleton
  public ICsvParser provideCsvParser() {
    return new CsvParser();
  }

  @Provides
  @Singleton
  public FritzBoxClient provideFritzBoxClient(UserSettings userSettings, IWebClient webClient, IDigestService digestService, ICsvParser csvParser) {
    return new FritzBoxClient(userSettings, webClient, digestService, csvParser);
  }

}
