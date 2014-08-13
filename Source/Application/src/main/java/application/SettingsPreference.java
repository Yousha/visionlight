package application;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import library.utilities.CommonUtilities;
import library.utilities.HardwareUtilities;
import yousha.application.visionlight.R;

public final class SettingsPreference extends PreferenceActivity implements
   SharedPreferences.OnSharedPreferenceChangeListener,
   Preference.OnPreferenceClickListener
{
   static final String PREFERENCE_AUTO_CLEAR_CACHE =
      "pref_settings_autoclearcache";
   static final String PREFERENCE_VIBRATE = "pref_settings_vibrate";
   private static final String LOG_TAG =
      SettingsPreference.class.getSimpleName();
   private static final String PREFERENCE_RESTORE_DEFAULT =
      "pref_settings_restoredefault";
   private static final String PREFERENCE_ADD_SHORTCUT =
      "pref_settings_addshortcut";
   private static final String PREFERENCE_VIEW_APPSYS_SETTINGS =
      "pref_settings_viewappsyssettings";
   // Preference
   private Preference preferenceRestoreDefault;
   private Preference preferenceViewAppSysSettings;
   // CheckBoxPreference
   private CheckBoxPreference cbpAutoClearCache;
   private CheckBoxPreference cbpVibrate;
   private CheckBoxPreference cbpAddShortcut;

   @Override
   protected void onCreate(final Bundle savedInstanceState)
   {
      Log.i(LOG_TAG, "Initializing  " + LOG_TAG + "...");
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preference_settings);
      Log.i(LOG_TAG, "Creating views...");
      this.createViews();
      Log.i(LOG_TAG, "Finding views...");
      this.findViews();
      Log.i(LOG_TAG, "Installing view listeners...");
      this.installViewListeners();
      Log.i(LOG_TAG, "Updating views...");
      this.updateViews();
   }

   @Override
   public void onLowMemory()
   {
      CommonUtilities.improveMemory();
      CommonUtilities.iToast(this, R.string.message_memoryislow);
      super.onLowMemory();
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      getPreferenceManager().getSharedPreferences()
         .registerOnSharedPreferenceChangeListener(this);
      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);
      MainActivity.canNotify = true;
   }

   @Override
   protected void onPause()
   {
      getPreferenceManager().getSharedPreferences()
         .unregisterOnSharedPreferenceChangeListener(this);
      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);

      if (MainActivity.canNotify && !this.isFinishing())
      {
         CommonUtilities.showNotification(MainActivity.context,
            R.drawable.ic_stat_notify_launcher,
            getString(R.string.ishere, getString(R.string.title_appname)),
            getString(R.string.title_appname),
            getString(R.string.summary_touchopenapp),
            MainActivity.NOTIFICATION_ID);
      }

      super.onPause();
   }

   @Override
   public void onBackPressed()
   {
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(final Menu menu)
   {
      this.getMenuInflater().inflate(R.menu.menu_settings, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.menu_settings_back:
         {
            finish();
            return true;
         }
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onSharedPreferenceChanged(
      final SharedPreferences sharedPreferences, final String key)
   {
      if (key.contains(PREFERENCE_ADD_SHORTCUT))
      {
         try
         {
            if (sharedPreferences.getBoolean(PREFERENCE_ADD_SHORTCUT, true))
            {
               CommonUtilities.createShortcut(MainActivity.class, this,
                  getString(R.string.title_appname));
            }
            else
            {
               CommonUtilities.removeShortcut(MainActivity.class, this,
                  getString(R.string.title_appname));
            }
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
         }
      }
   }

   @Override
   public boolean onPreferenceClick(final Preference preference)
   {
      preference.setEnabled(false);
      final String _key = preference.getKey();

      if (_key.equals(PREFERENCE_RESTORE_DEFAULT))
      {
         Log.i(LOG_TAG, "Restoring settings default...");
         this.cbpAutoClearCache.setChecked(true);
         this.cbpVibrate.setChecked(true);
         this.cbpAddShortcut.setChecked(false);
      }
      else if (_key.equals(PREFERENCE_VIEW_APPSYS_SETTINGS))
      {
         Log.i(LOG_TAG, "Opening application\'s system settings...");
         startActivityForResult(
            CommonUtilities.openApplicationDetailIntent(getPackageName()), -1);
      }

      preference.setEnabled(true);
      return false;
   }

   @SuppressWarnings({"EmptyMethod"})
   private void createViews()
   {

   }

   private void findViews()
   {
      // Preference
      this.preferenceRestoreDefault =
         findPreference(PREFERENCE_RESTORE_DEFAULT);
      this.preferenceViewAppSysSettings =
         findPreference(PREFERENCE_VIEW_APPSYS_SETTINGS);
      // CheckBoxPreference
      this.cbpAutoClearCache =
         (CheckBoxPreference) findPreference(PREFERENCE_AUTO_CLEAR_CACHE);
      this.cbpVibrate = (CheckBoxPreference) findPreference(PREFERENCE_VIBRATE);
      this.cbpAddShortcut =
         (CheckBoxPreference) findPreference(PREFERENCE_ADD_SHORTCUT);
   }

   private void installViewListeners()
   {
      // Preference
      this.preferenceRestoreDefault.setOnPreferenceClickListener(this);
      this.preferenceViewAppSysSettings.setOnPreferenceClickListener(this);
   }

   private void updateViews()
   {
      if (!HardwareUtilities.hasVibrator(this))
      {
         this.cbpVibrate.setEnabled(false);
         this.cbpVibrate.setChecked(false);
      }
   }
}
