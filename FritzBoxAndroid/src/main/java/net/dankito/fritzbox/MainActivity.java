package net.dankito.fritzbox;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.dankito.fritzbox.adapter.MainActivityTabsAdapter;
import net.dankito.fritzbox.model.UserSettings;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

  protected MainActivityTabsAdapter tabsAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((FritzBoxAndroidApplication)getApplicationContext()).getComponent().inject(this);

    setupUi();

  protected void setupDependencyInjection() {
    component = DaggerAndroidDiComponent.builder()
        .androidDiContainer(new AndroidDiContainer(this))
        .build();

    component.inject(this);
  }

  protected void setupUi() {
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    tabsAdapter = new MainActivityTabsAdapter(this, getSupportFragmentManager());

    ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
    viewPager.setAdapter(tabsAdapter);

    TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


}
