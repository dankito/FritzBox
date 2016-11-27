package net.dankito.fritzbox;

import android.app.Application;

import net.dankito.fritzbox.di.AndroidDiComponent;
import net.dankito.fritzbox.di.AndroidDiContainer;
import net.dankito.fritzbox.di.DaggerAndroidDiComponent;

/**
 * Created by ganymed on 27/11/16.
 */

public class FritzBoxAndroidApplication extends Application {

  protected AndroidDiComponent component;

  @Override
  public void onCreate() {
    super.onCreate();

    setupDependencyInjection();
  }

  protected void setupDependencyInjection() {
    component = DaggerAndroidDiComponent.builder()
        .androidDiContainer(new AndroidDiContainer(this))
        .build();
  }


  public AndroidDiComponent getComponent() {
    return component;
  }

}
