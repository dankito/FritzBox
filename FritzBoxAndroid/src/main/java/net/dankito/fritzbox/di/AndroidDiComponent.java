package net.dankito.fritzbox.di;

import net.dankito.fritzbox.MainActivity;
import net.dankito.fritzbox.activity.SettingsActivity;
import net.dankito.fritzbox.fragments.CallListFragment;
import net.dankito.fritzbox.services.CallListObserver;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ganymed on 03/11/16.
 */
@Singleton
@Component(modules = { AndroidDiContainer.class } )
public interface AndroidDiComponent {

  // to update the fields in your activities
  void inject(MainActivity activity);

  void inject(SettingsActivity settingsActivity);

  void inject(CallListFragment callListFragment);

  void inject(CallListObserver callListObserver);

}
