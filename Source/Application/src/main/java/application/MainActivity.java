/*
 * Name: Visionlight
 * Description: An free torch application for Android based devices. Lightweight, bug-free and without ads!
 * Version: 1.1.10-8
 * Locale: en_International, fa_IR
 * Last update: 2020/1399
 * Architecture: multi-arch
 * API: Android 2.3.3
 * Compiler: Oracle JDK 1.6.0 64bit
 * Builder: Gradle 4
 * License: BSD-3
 * Copyright: Copyright © 2020 Yousha Aleayoub.
 * Producer: Yousha Aleayoub
 * Maintainer: Yousha Aleayoub
 * Contact: yousha.a@hotmail.com
 * Link: http://yousha.blog.ir
 */

package application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import library.Flashlight;
import library.Preferences;
import library.utilities.CommonUtilities;
import library.utilities.HardwareUtilities;
import yousha.application.visionlight.BuildConfig;
import yousha.application.visionlight.R;

public final class MainActivity extends Activity implements
   View.OnClickListener, AlertDialog.OnClickListener
{
   static final int NOTIFICATION_ID = 8118;
   @SuppressLint("StaticFieldLeak")
   static Context context;
   static boolean canNotify;
   static long appLoadTime;
   private static final String LOG_TAG = MainActivity.class.getSimpleName();
   private static final byte DIALOG_EXIT = 1;
   private boolean canVibrate;
   private boolean isWarned;
   private boolean flashlightState;
   private Flashlight flashLight;
   // AlertDialog
   private AlertDialog alertdialogExit;
   // ImageView
   private ImageView imageviewKey;

   @Override
   public void onCreate(final Bundle savedInstanceState)
   {
      Log.i(LOG_TAG, "Initializing  " + LOG_TAG + "...");
      final long _startTime = System.currentTimeMillis();
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Log.i(LOG_TAG, "Creating views...");
      this.createViews();
      Log.i(LOG_TAG, "Finding views...");
      this.findViews();
      Log.i(LOG_TAG, "Installing view listeners...");
      this.installViewListeners();
      Log.i(LOG_TAG, "Updating views...");
      this.updateViews();
      CommonUtilities.hideKeyboard(this, this.getCurrentFocus());

      if (BuildConfig.DEBUG)
      {
         Log.w(LOG_TAG, "DEBUG-MODE IS ONE!");
      }
      else
      {
         if (HardwareUtilities.isEmulator())
         {
            Log.e(LOG_TAG, "PRODUCTION RELEASE IN EMULATOR!");
            //finish();
         }
      }

      // The markets launches main activity on top of other activities, check if we are the root.
      if (!this.isTaskRoot())
      {
         final Intent _intent = getIntent();
         final String _action = _intent.getAction();
         if (_intent.hasCategory("android.intent.category.LAUNCHER") &&
            (_action != null) && _action.equals("android.intent.action.MAIN"))
         {
            Log.e(LOG_TAG, "Multiple instance detected!");
            finish();
         }
      }

      try
      {
         if (!CommonUtilities.hasPermission(this,
            CommonUtilities.getPermissionLists(this, getPackageName())))
         {
            CommonUtilities
               .iToast(this, getString(R.string.message_appdoesnothaveperm));
         }
      }
      catch (final PackageManager.NameNotFoundException exception)
      {
         CommonUtilities.addException(exception);
      }

      CommonUtilities.enableStrictMode();
      this.flashLight = new Flashlight();

      if (this.imageviewKey != null)
      {
         this.imageviewKey.destroyDrawingCache();
      }

      appLoadTime = System.currentTimeMillis() - _startTime;
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

      if (context == null)
      {
         context = getApplicationContext();
      }

      Preferences.load(context);

      if (HardwareUtilities.getCurrentBatteryLevel(this) < 10.0f)
      {
         if (!this.isWarned)
         {
            this.isWarned = true;
            CommonUtilities.iToast(this, R.string.message_batteryislow);
         }
      }

      this.canVibrate = HardwareUtilities.hasVibrator(this) &&
         Preferences.getBoolean(SettingsPreference.PREFERENCE_VIBRATE, true);

      if (this.flashLight.hasFlashlight())
      {
         this.flashLight.open();
         this.flashLight.initialize();
      }

      CommonUtilities.cancelNotification(context, NOTIFICATION_ID);
      canNotify = true;
   }

   @Override
   protected void onPause()
   {
      if (this.flashlightState)
      {
         this.flashlightState = HardwareUtilities
            ._setFlashlightState(this, false, this.imageviewKey,
               this.flashLight);
      }

      if (this.flashLight.hasFlashlight())
      {
         //noinspection FinalizeCalledExplicitly
         this.flashLight.finalize();
      }

      CommonUtilities.cancelNotification(context, NOTIFICATION_ID);

      if (canNotify && !this.isFinishing())
      {
         CommonUtilities
            .showNotification(context, R.drawable.ic_stat_notify_launcher,
               getString(R.string.ishere, getString(R.string.title_appname)),
               getString(R.string.title_appname),
               getString(R.string.summary_touchopenapp), NOTIFICATION_ID);
      }

      super.onPause();
   }

   @Override
   protected void onDestroy()
   {
      if (this.alertdialogExit != null)
      {
         this.alertdialogExit.dismiss();
         this.alertdialogExit = null;
      }

      if (Preferences
         .getBoolean(SettingsPreference.PREFERENCE_AUTO_CLEAR_CACHE, true))
      {
         CommonUtilities.clearCache(context);
      }

      CommonUtilities.cancelNotification(context, NOTIFICATION_ID);
      CommonUtilities.unbindDrawables(findViewById(R.id.frame_main));
      // Call at last.
      super.onDestroy();
   }

   @Override
   public void onBackPressed()
   {
      if (!this.isFinishing())
      {
         showDialog(DIALOG_EXIT, null);
      }
   }

   @Override
   public boolean onKeyDown(final int keyCode, final KeyEvent event)
   {
      switch (keyCode)
      {
         case KeyEvent.KEYCODE_HOME:
         case KeyEvent.KEYCODE_SEARCH:
         case KeyEvent.KEYCODE_CAMERA:
         {
            finish();
            return true;
         }

         case KeyEvent.KEYCODE_DPAD_CENTER:
         {
            if (this.canVibrate)
            {
               HardwareUtilities.vibrate(this, 50L);
            }
            this.flashlightState = HardwareUtilities
               ._setFlashlightState(this, !this.flashlightState,
                  this.imageviewKey, this.flashLight);
            return true;
         }

         case KeyEvent.KEYCODE_BACK: // Earlier versions.
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_EXIT, null);
            }
            return true;
         }
      }

      return super.onKeyDown(keyCode, event);
   }

   @Override
   public boolean onKeyUp(final int keyCode, final KeyEvent event)
   {
      if ((keyCode == KeyEvent.KEYCODE_SEARCH) && event.isTracking() &&
         !event.isCanceled())
      {
         finish();
         return true;
      }

      return super.onKeyUp(keyCode, event);
   }

   // Called once.
   @Override
   public boolean onCreateOptionsMenu(final Menu menu)
   {
      this.getMenuInflater()
         .inflate(R.menu.menu_main, menu); // Adds menu items.
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(final Menu menu)
   {
      if (this.flashlightState)
      {
         menu.findItem(R.id.menu_main_key).setTitle(R.string.action_turnoff);
      }
      else
      {
         menu.findItem(R.id.menu_main_key).setTitle(R.string.action_turnon);
      }

      return super.onPrepareOptionsMenu(menu);
   }

   // Used in Android < 2.3.x
   @Override
   public boolean onOptionsItemSelected(final MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.menu_main_key:
         {
            if (this.canVibrate)
            {
               HardwareUtilities.vibrate(this, 50L);
            }
            this.flashlightState = HardwareUtilities
               ._setFlashlightState(this, !this.flashlightState,
                  this.imageviewKey, this.flashLight);
            return true;
         }

         case R.id.menu_main_settings:
         {
            canNotify = false;
            CommonUtilities.goToActivity(this, SettingsPreference.class);
            return true;
         }

         case R.id.menu_main_options:
         {
            canNotify = false;
            CommonUtilities.goToActivity(this, OptionsPreference.class);
            return true;
         }

         case R.id.menu_main_about:
         {
            canNotify = false;
            CommonUtilities.goToActivity(this, AboutActivity.class);
            return true;
         }

         case R.id.menu_main_exit:
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_EXIT, null);
            }
            return true;
         }
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onClick(final View view)
   {
      view.setEnabled(false);

      switch (view.getId())
      {
         case R.id.image_main_key:
         {
            if (this.canVibrate)
            {
               HardwareUtilities.vibrate(this, 50L);
            }
            this.flashlightState = HardwareUtilities
               ._setFlashlightState(this, !this.flashlightState,
                  this.imageviewKey, this.flashLight);
            break;
         }
      }

      view.setEnabled(true);
   }

   // Called once.
   @Override
   protected Dialog onCreateDialog(final int id)
   {
      switch (id)
      {
         case DIALOG_EXIT:
         {
            return this.alertdialogExit;
         }
      }

      return super.onCreateDialog(id);
   }

   // Alert dialogs.
   @Override
   public void onClick(final DialogInterface dialog, final int which)
   {
      dialog.dismiss();

      // Prevents crash.
      if (this.alertdialogExit != null)
      {
         if (this.alertdialogExit.equals(dialog))
         {
            switch (which)
            {
               case DialogInterface.BUTTON_POSITIVE:
               {
                  finish();
                  break;
               }

               case DialogInterface.BUTTON_NEGATIVE:
               {
                  break;
               }
            }
         }
      }
   }

   private void createViews()
   {
      // AlertDialog
      final AlertDialog.Builder _builder = new AlertDialog.Builder(this);
      _builder.setIcon(0);
      _builder.setCancelable(true);
      _builder.setMessage(R.string.message_exitapplication);
      _builder.setPositiveButton(R.string.action_yes, this);
      _builder.setNegativeButton(R.string.action_no, this);
      this.alertdialogExit = _builder.create();
   }

   private void findViews()
   {
      this.imageviewKey = (ImageView) findViewById(R.id.image_main_key);

   }

   private void installViewListeners()
   {
      this.imageviewKey.setOnClickListener(this);
   }

   @SuppressWarnings("EmptyMethod")
   private void updateViews()
   {

   }
}
