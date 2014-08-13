package application;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import library.utilities.CommonUtilities;
import library.utilities.HardwareUtilities;
import yousha.application.visionlight.BuildConfig;
import yousha.application.visionlight.R;

public final class OptionsPreference extends PreferenceActivity implements
   View.OnClickListener, AlertDialog.OnClickListener,
   Preference.OnPreferenceClickListener
{
   private static final String LOG_TAG =
      OptionsPreference.class.getSimpleName();
   private static final String PREFERENCE_CHECK_FOR_UPDATE =
      "pref_options_checkforupdate";
   private static final String PREFERENCE_FEEDBACK = "pref_options_feedback";
   private static final String PREFERENCE_ERROR_LOGS = "pref_options_errorlogs";
   private static final String PREFERENCE_IMPROVE_MEMORY =
      "pref_options_improvememory";
   private static final String PREFERENCE_CLEAR_CACHE =
      "pref_options_clearcache";
   private static final String PREFERENCE_LATEST_CHANGES =
      "pref_options_latestchanges";
   private static final byte DIALOG_NO_INTERNET = 1;
   private static final byte DIALOG_NEW_VERSION = 2;
   private static final byte DIALOG_FEEDBACK = 3;
   private static final byte DIALOG_ERRORLOGS = 4;
   private static final byte DIALOG_LATEST_CHANGES = 5;
   private static final String[] REMOTE_STRUCTURE =
      {"https://bayanbox.ir/view/4158407049242975527/VERSION.txt",
         "https://yousha.blog.ir/post/23", "id=\"bComFormElem\" action=\"",
         "\" method=\"post\">"};
   private boolean isConnecting;
   private String urlContent;
   // Preference
   private Preference preferenceCheckForUpdate;
   private Preference preferenceFeedback;
   private Preference preferenceErrorLogs;
   private Preference preferenceImproveMemory;
   private Preference preferenceClearCache;
   private Preference preferenceLatestChanges;
   // AlertDialog
   private AlertDialog alertdialogNoInternet;
   private AlertDialog alertdialogNewVersion;
   // Dialog
   private Dialog dialogFeedback;
   private Dialog dialogErrorLogs;
   private Dialog dialogLatestChanges;
   // EditText
   private EditText edittextFeedback_Name;
   private EditText edittextFeedback_Email;
   private EditText edittextFeedback_Message;
   private EditText edittextErrorLogs_Logs;
   // TextView
   private TextView textviewLatestChanges_Version;
   // Button
   private Button buttonFeedback_Send;
   private Button buttonFeedback_Cancel;
   private Button buttonErrorLogs_Cancel;
   private Button buttonErrorLogs_Copy;
   private Button buttonLatestChanges_OK;

   @Override
   public void onCreate(final Bundle savedInstanceState)
   {
      Log.i(LOG_TAG, "Initializing  " + LOG_TAG + "...");
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preference_options);
      Log.i(LOG_TAG, "Creating views...");
      this.createViews();
      Log.i(LOG_TAG, "Finding views...");
      this.findViews();
      Log.i(LOG_TAG, "Installing view listeners...");
      this.installViewListeners();
      Log.i(LOG_TAG, "Updating views...");
      this.updateViews();

      if (this.isConnecting)
      {
         Log.i(LOG_TAG, "Aborting HTTP connection...");
         CommonUtilities.doAbortHTTPConnection.set(true);
         this.isConnecting = false;
      }
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

      if (!CommonUtilities.isEmpty(CommonUtilities.getException()))
      {
         this.preferenceErrorLogs
            .setTitle(getString(R.string.title_errorlogs) + " (!)");
      }

      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);
      MainActivity.canNotify = true;
   }

   @Override
   protected void onPause()
   {
      this.urlContent = null;

      if (this.isConnecting)
      {
         Log.i(LOG_TAG, "Aborting HTTP connection...");
         CommonUtilities.doAbortHTTPConnection.set(true);
         this.isConnecting = false;
      }

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
   protected void onDestroy()
   {
      if (this.alertdialogNoInternet != null)
      {
         this.alertdialogNoInternet.dismiss();
         this.alertdialogNoInternet = null;
      }

      if (this.alertdialogNewVersion != null)
      {
         this.alertdialogNewVersion.dismiss();
         this.alertdialogNewVersion = null;
      }

      if (this.dialogFeedback != null)
      {
         this.dialogFeedback.dismiss();
         this.dialogFeedback = null;
      }

      if (this.dialogErrorLogs != null)
      {
         this.dialogErrorLogs.dismiss();
         this.dialogErrorLogs = null;
      }

      if (this.dialogLatestChanges != null)
      {
         this.dialogLatestChanges.dismiss();
         this.dialogLatestChanges = null;
      }

      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);
      CommonUtilities.unbindDrawables(findViewById(R.id.scroll_feedback));
      CommonUtilities.unbindDrawables(findViewById(R.id.layout_errorlogs));
      CommonUtilities.unbindDrawables(findViewById(R.id.scroll_latestchanges));
      super.onDestroy();
   }

   @Override
   public void onBackPressed()
   {
      finish();
   }

   @Override
   public boolean onCreateOptionsMenu(final Menu menu)
   {
      this.getMenuInflater().inflate(R.menu.menu_options, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.menu_options_back:
         {
            finish();
            return true;
         }
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   protected Dialog onCreateDialog(final int id)
   {
      switch (id)
      {
         case DIALOG_NO_INTERNET:
         {
            return this.alertdialogNoInternet;
         }

         case DIALOG_NEW_VERSION:
         {
            return this.alertdialogNewVersion;
         }

         case DIALOG_FEEDBACK:
         {
            return this.dialogFeedback;
         }

         case DIALOG_ERRORLOGS:
         {
            return this.dialogErrorLogs;
         }

         case DIALOG_LATEST_CHANGES:
         {
            return this.dialogLatestChanges;
         }
      }

      return super.onCreateDialog(id);
   }

   @Override
   protected void onPrepareDialog(final int id, final Dialog dialog)
   {
      super.onPrepareDialog(id, dialog);

      switch (id)
      {
         case DIALOG_NO_INTERNET:
         {
            if (!HardwareUtilities.hasWiFi(this))
            {
               ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                  .setVisibility(View.GONE);
            }
            if (!HardwareUtilities.hasTelephony(this))
            {
               ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                  .setVisibility(View.GONE);
            }
            break;
         }

         case DIALOG_NEW_VERSION:
         {
            ((AlertDialog) dialog).setMessage(Html.fromHtml(
               (getString(R.string.message_newversionisavailable) +
                  "<br/><br/>" +
                  getString(R.string.currentversion, BuildConfig.VERSION_NAME) +
                  "<br/><font color=\"#00FF00\">" +
                  getString(R.string.newversion, this.urlContent) +
                  "</font><br/><br/>" +
                  getString(R.string.message_gotomarketdownload))));
            break;
         }

         case DIALOG_FEEDBACK:
         {
            this.edittextFeedback_Name.getText().clear();
            this.edittextFeedback_Name.setError(null);
            this.edittextFeedback_Name.requestFocus();
            this.edittextFeedback_Email.getText().clear();
            this.edittextFeedback_Email.setError(null);
            this.edittextFeedback_Message.getText().clear();
            this.edittextFeedback_Message.setError(null);
            break;
         }

         case DIALOG_ERRORLOGS:
         {
            this.edittextErrorLogs_Logs.setText(CommonUtilities.getException());
            this.buttonErrorLogs_Cancel.setText(R.string.action_cancel);
            this.buttonErrorLogs_Copy.setEnabled(true);
            break;
         }
      }
   }

   @Override
   public boolean onPreferenceClick(final Preference preference)
   {
      preference.setEnabled(false);
      final String _key = preference.getKey();

      if (_key.equals(PREFERENCE_CHECK_FOR_UPDATE))
      {
         if (CommonUtilities.isInternetAvailable(this))
         {
            preference.setTitle(R.string.checking);
            final Handler _handler = new Handler();
            _handler.postDelayed(new Runnable()
            {
               @Override
               public void run()
               {
                  OptionsPreference.this.isConnecting = true;
                  Log.i(LOG_TAG, "Checking for update...");
                  try
                  {
                     OptionsPreference.this.urlContent = CommonUtilities
                        .getDataFromURL(REMOTE_STRUCTURE[0], "UTF-8", 30000);
                     if (!CommonUtilities
                        .isEmpty(OptionsPreference.this.urlContent))
                     {
                        if (Integer.parseInt(
                           OptionsPreference.this.urlContent.replace(".", "")
                              .replace("-", ""), 10) > Integer.parseInt(
                           BuildConfig.VERSION_NAME.replace(".", "") +
                              BuildConfig.VERSION_CODE, 10))
                        {
                           preference.setSummary(
                              R.string.message_newversionisavailable);
                           if (!OptionsPreference.this.isFinishing())
                           {
                              showDialog(DIALOG_NEW_VERSION, null);
                           }
                        }
                        else
                        {
                           preference
                              .setSummary(R.string.message_appisuptodate);
                           CommonUtilities.iToast(OptionsPreference.this,
                              R.string.message_appisuptodate);
                        }
                     }
                  }
                  catch (final UnknownHostException exception)
                  {
                     Log.w(LOG_TAG,
                        "Update-check failed due to UnknownHostException.");
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_processingrequest);
                  }
                  catch (final SocketTimeoutException exception)
                  {
                     Log.w(LOG_TAG,
                        "Update-check failed due to SocketTimeoutException.");
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_requesttimedout);
                  }
                  catch (final Exception exception)
                  {
                     CommonUtilities.addException(exception);
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_sendingrequest);
                  }
                  OptionsPreference.this.isConnecting = false;
                  preference.setEnabled(true);
                  preference.setTitle(R.string.caption_checkforupdate);
               }
            }, 300);
            return true;
         }
         else
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_NO_INTERNET, null);
            }
         }
      }
      else if (_key.equals(PREFERENCE_FEEDBACK))
      {
         if (CommonUtilities.isInternetAvailable(this))
         {
            preference.setTitle(R.string.preparing);
            final Handler _handler = new Handler();
            _handler.postDelayed(new Runnable()
            {
               @Override
               public void run()
               {
                  OptionsPreference.this.isConnecting = true;
                  Log.i(LOG_TAG, "Downloading feedback URL...");
                  try
                  {
                     OptionsPreference.this.urlContent = CommonUtilities
                        .getDataFromURL(REMOTE_STRUCTURE[1], "UTF-8", 30000);
                     if (!CommonUtilities
                        .isEmpty(OptionsPreference.this.urlContent))
                     {
                        if (!OptionsPreference.this.isFinishing())
                        {
                           showDialog(DIALOG_FEEDBACK, null);
                        }
                     }
                  }
                  catch (final UnknownHostException exception)
                  {
                     Log.w(LOG_TAG,
                        "Download failed due to UnknownHostException.");
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_processingrequest);
                  }
                  catch (final SocketTimeoutException exception)
                  {
                     Log.w(LOG_TAG,
                        "Download failed due to SocketTimeoutException.");
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_requesttimedout);
                  }
                  catch (final Exception exception)
                  {
                     CommonUtilities.addException(exception);
                     CommonUtilities.iToast(OptionsPreference.this,
                        R.string.error_sendingrequest);
                  }
                  OptionsPreference.this.isConnecting = false;
                  preference.setEnabled(true);
                  preference.setTitle(R.string.title_feedback);
               }
            }, 500);
            return true;
         }
         else
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_NO_INTERNET, null);
            }
         }
      }
      else if (_key.equals(PREFERENCE_ERROR_LOGS))
      {
         if (!CommonUtilities.isEmpty(CommonUtilities.getException()))
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_ERRORLOGS, null);
            }
         }
         else
         {
            CommonUtilities.iToast(this, R.string.message_nologs);
         }
      }
      else if (_key.equals(PREFERENCE_IMPROVE_MEMORY))
      {
         preference.setTitle(R.string.processing);
         final long _usedMemory = CommonUtilities.getRuntimeHeapMemory()[3];
         CommonUtilities.improveMemory();
         final Handler _handler = new Handler();
         _handler.postDelayed(new Runnable()
         {
            @Override
            public void run()
            {
               final String _result = getString(R.string.message_memoryfreed,
                  Formatter.formatFileSize(OptionsPreference.this,
                     _usedMemory - CommonUtilities.getRuntimeHeapMemory()[3]));
               preference.setSummary(_result);
               CommonUtilities.iToast(OptionsPreference.this, _result);
               preference.setTitle(R.string.caption_improvememory);
            }
         }, 500);
         return true;
      }
      else if (_key.equals(PREFERENCE_CLEAR_CACHE))
      {
         preference.setTitle(R.string.processing);
         Log.i(LOG_TAG, "Clearing application's cache...");
         CommonUtilities.clearCache(MainActivity.context);
         preference.setTitle(R.string.caption_clearcache);
         return true;
      }
      else if (_key.equals(PREFERENCE_LATEST_CHANGES))
      {
         if (!CommonUtilities
            .isEmpty(getString(R.string.summary_latestchanges)))
         {
            if (!this.isFinishing())
            {
               showDialog(DIALOG_LATEST_CHANGES, null);
            }
         }
         else
         {
            CommonUtilities.iToast(this, R.string.message_nonewchanges);
         }
      }

      preference.setEnabled(true);
      return false;
   }

   // Custom dialogs.
   @Override
   public void onClick(final View view)
   {
      view.setEnabled(false);

      switch (view.getId())
      {
         case R.id.button_feedback_send:
         {
            final String _name =
               this.edittextFeedback_Name.getText().toString();
            if (CommonUtilities.isEmpty(_name))
            {
               this.edittextFeedback_Name
                  .setError(getString(R.string.message_cantbeempty));
               CommonUtilities.showKeyboard(MainActivity.context,
                  this.edittextFeedback_Name);
               break;
            }
            this.edittextFeedback_Name.setError(null);
            final String _email =
               this.edittextFeedback_Email.getText().toString();
            if (!CommonUtilities.isEmpty(_email))
            {
               if (!CommonUtilities.isValidEmail(_email))
               {
                  this.edittextFeedback_Email
                     .setError(getString(R.string.message_invalidemail));
                  CommonUtilities.showKeyboard(MainActivity.context,
                     this.edittextFeedback_Email);
                  break;
               }
            }
            this.edittextFeedback_Email.setError(null);
            final String _message =
               this.edittextFeedback_Message.getText().toString();
            if (CommonUtilities.isEmpty(_message))
            {
               this.edittextFeedback_Message
                  .setError(getString(R.string.message_cantbeempty));
               CommonUtilities
                  .showKeyboard(MainActivity.context, edittextFeedback_Message);
               break;
            }
            this.edittextFeedback_Message.setError(null);
            Log.i(LOG_TAG, "Sending feedback...");
            try
            {
               this.isConnecting = true;
               CommonUtilities._postFeedback("http://yousha.blog.ir" +
                     CommonUtilities
                        .getBetweenString(this.urlContent, REMOTE_STRUCTURE[2],
                           REMOTE_STRUCTURE[3]), _name, _email, _message,
                  REMOTE_STRUCTURE[1], "UTF-8", 30000);
               CommonUtilities
                  .hideKeyboard(MainActivity.context, this.getCurrentFocus());
               CommonUtilities.iToast(this, R.string.message_messagesent);
            }
            catch (final UnknownHostException exception)
            {
               Log.w(LOG_TAG,
                  "Feedback send failed due to UnknownHostException.");
               CommonUtilities.iToast(this, R.string.error_processingrequest);
            }
            catch (final SocketTimeoutException exception)
            {
               Log.w(LOG_TAG,
                  "Feedback send failed due to SocketTimeoutException.");
               CommonUtilities.iToast(this, R.string.error_requesttimedout);
            }
            catch (final Exception exception)
            {
               CommonUtilities.addException(exception);
               CommonUtilities.iToast(this, R.string.error_sendingrequest);
            }
            this.dialogFeedback.dismiss();
            this.isConnecting = false;
            break;
         }

         case R.id.button_feedback_cancel:
         {
            if (this.isConnecting)
            {
               Log.i(LOG_TAG, "Aborting HTTP connection...");
               CommonUtilities.doAbortHTTPConnection.set(true);
               this.isConnecting = false;
            }
            CommonUtilities
               .hideKeyboard(MainActivity.context, this.getCurrentFocus());
            this.dismissDialog(DIALOG_FEEDBACK);
            break;
         }

         case R.id.button_errorlogs_cancel:
         {
            this.dismissDialog(DIALOG_ERRORLOGS);
            break;
         }

         case R.id.button_errorlogs_copy:
         {
            if (!CommonUtilities
               .isEmpty(this.edittextErrorLogs_Logs.getText().toString()))
            {
               CommonUtilities
                  .copyToClipboard(this, this.edittextErrorLogs_Logs);
               this.buttonErrorLogs_Cancel.setText(R.string.action_close);
               return;
            }
            break;
         }

         case R.id.button_latestchanges_ok:
         {
            this.dismissDialog(DIALOG_LATEST_CHANGES);
            break;
         }
      }

      view.setEnabled(true);
   }

   // Alert dialogs.
   @Override
   public void onClick(final DialogInterface dialog, final int which)
   {
      dialog.dismiss();

      // Prevents crash.
      if (this.alertdialogNoInternet != null)
      {
         if (this.alertdialogNoInternet.equals(dialog))
         {
            switch (which)
            {
               case DialogInterface.BUTTON_POSITIVE:
               {
                  this.startActivityForResult(
                     new Intent("android.settings.WIFI_SETTINGS"), -1);
                  break;
               }

               case DialogInterface.BUTTON_NEGATIVE:
               {
                  /*
                   * startActivityForResult(new
                   * Intent("android.settings.DATA_ROAMING_SETTINGS").
                   * setComponent(new ComponentName("com.android.phone",
                   * "com.android.phone.Settings")), -1);
                   */
                  this.startActivityForResult(
                     new Intent("android.settings.DATA_ROAMING_SETTINGS"), -1);
                  break;
               }

               case DialogInterface.BUTTON_NEUTRAL:
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
      AlertDialog.Builder _builder = new AlertDialog.Builder(this);
      _builder.setCancelable(true);
      _builder.setIcon(0);
      _builder.setMessage(R.string.message_nointernet);

      if (HardwareUtilities.hasWiFi(this))
      {
         _builder.setPositiveButton(R.string.action_wifisettings, this);
      }

      if (HardwareUtilities.hasTelephony(this))
      {
         _builder.setNegativeButton(R.string.action_roamingsettings, this);
      }

      _builder.setNeutralButton(R.string.action_ok, this);
      this.alertdialogNoInternet = _builder.create();
      _builder = new AlertDialog.Builder(this);
      _builder.setCancelable(true);
      _builder.setIcon(0);
      _builder.setMessage(""); // Initialize -> Android bug.
      _builder.setNeutralButton(R.string.action_ok, this);
      this.alertdialogNewVersion = _builder.create();
      // Dialog
      this.dialogFeedback = CommonUtilities
         .createDialog(this, R.layout.dialog_feedback, true,
            R.string.title_feedback, true);
      this.dialogErrorLogs = CommonUtilities
         .createDialog(this, R.layout.dialog_errorlogs, true,
            R.string.title_errorlogs, false);
      this.dialogLatestChanges = CommonUtilities
         .createDialog(this, R.layout.dialog_latestchanges, true,
            R.string.caption_latestchanges, false);
   }

   private void findViews()
   {
      // Preference
      this.preferenceCheckForUpdate =
         findPreference(PREFERENCE_CHECK_FOR_UPDATE);
      this.preferenceFeedback = findPreference(PREFERENCE_FEEDBACK);
      this.preferenceErrorLogs = findPreference(PREFERENCE_ERROR_LOGS);
      this.preferenceImproveMemory = findPreference(PREFERENCE_IMPROVE_MEMORY);
      this.preferenceClearCache = findPreference(PREFERENCE_CLEAR_CACHE);
      this.preferenceLatestChanges = findPreference(PREFERENCE_LATEST_CHANGES);
      // EditText
      this.edittextFeedback_Name =
         (EditText) this.dialogFeedback.findViewById(R.id.edit_feedback_name);
      this.edittextFeedback_Email =
         (EditText) this.dialogFeedback.findViewById(R.id.edit_feedback_email);
      this.edittextFeedback_Message = (EditText) this.dialogFeedback
         .findViewById(R.id.edit_feedback_message);
      this.edittextErrorLogs_Logs =
         (EditText) this.dialogErrorLogs.findViewById(R.id.edit_errorlogs_logs);
      // TextView
      this.textviewLatestChanges_Version = (TextView) this.dialogLatestChanges
         .findViewById(R.id.text_latestchanges_version);
      // Button
      this.buttonFeedback_Send =
         (Button) this.dialogFeedback.findViewById(R.id.button_feedback_send);
      this.buttonFeedback_Cancel =
         (Button) this.dialogFeedback.findViewById(R.id.button_feedback_cancel);
      this.buttonErrorLogs_Cancel = (Button) this.dialogErrorLogs
         .findViewById(R.id.button_errorlogs_cancel);
      this.buttonErrorLogs_Copy =
         (Button) this.dialogErrorLogs.findViewById(R.id.button_errorlogs_copy);
      this.buttonLatestChanges_OK = (Button) this.dialogLatestChanges
         .findViewById(R.id.button_latestchanges_ok);
   }

   private void installViewListeners()
   {
      // Preference
      this.preferenceCheckForUpdate.setOnPreferenceClickListener(this);
      this.preferenceFeedback.setOnPreferenceClickListener(this);
      this.preferenceErrorLogs.setOnPreferenceClickListener(this);
      this.preferenceImproveMemory.setOnPreferenceClickListener(this);
      this.preferenceClearCache.setOnPreferenceClickListener(this);
      this.preferenceLatestChanges.setOnPreferenceClickListener(this);
      // Button
      this.buttonFeedback_Send.setOnClickListener(this);
      this.buttonFeedback_Cancel.setOnClickListener(this);
      this.buttonErrorLogs_Copy.setOnClickListener(this);
      this.buttonErrorLogs_Cancel.setOnClickListener(this);
      this.buttonLatestChanges_OK.setOnClickListener(this);
   }

   @SuppressLint("SetTextI18n")
   private void updateViews()
   {
      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);
      this.textviewLatestChanges_Version.setText(
         getString(R.string.caption_version) + " " + BuildConfig.VERSION_NAME +
            "-" + BuildConfig.VERSION_CODE);
   }
}
