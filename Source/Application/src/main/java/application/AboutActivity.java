package application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import library.utilities.CommonUtilities;
import yousha.application.visionlight.BuildConfig;
import yousha.application.visionlight.R;

public final class AboutActivity extends Activity implements
   View.OnClickListener
{
   private static final String LOG_TAG = AboutActivity.class.getSimpleName();
   // TextView
   private TextView textviewVersion;
   private TextView textviewLicense;
   private TextView textviewContact;
   private TextView textviewPlatform;
   private TextView textviewArchitecture;
   // Button
   private Button buttonOK;

   @Override
   public void onCreate(final Bundle savedInstanceState)
   {
      Log.i(LOG_TAG, "Initializing  " + LOG_TAG + "...");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_about);
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
      CommonUtilities.cancelNotification(MainActivity.context,
         MainActivity.NOTIFICATION_ID);
      MainActivity.canNotify = true;
   }

   @Override
   protected void onPause()
   {
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
      CommonUtilities.unbindDrawables(findViewById(R.id.scroll_about));
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
      this.getMenuInflater().inflate(R.menu.menu_about, menu);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.menu_about_back:
         {
            finish();
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
         case R.id.button_about_ok:
         {
            finish();
            break;
         }
      }

      view.setEnabled(true);
   }

   @SuppressWarnings({"EmptyMethod"})
   private void createViews()
   {

   }

   private void findViews()
   {
      // TextView
      this.textviewVersion =
         (TextView) findViewById(R.id.text_about_versionsummary);
      this.textviewLicense =
         (TextView) findViewById(R.id.text_about_licensesummary);
      this.textviewContact =
         (TextView) findViewById(R.id.text_about_contactsummary);
      this.textviewPlatform =
         (TextView) findViewById(R.id.text_about_platformsummary);
      this.textviewArchitecture =
         (TextView) findViewById(R.id.text_about_architecturesummary);
      // Button
      this.buttonOK = (Button) findViewById(R.id.button_about_ok);
   }

   private void installViewListeners()
   {
      // Button
      this.buttonOK.setOnClickListener(this);
   }

   @SuppressLint("SetTextI18n")
   private void updateViews()
   {
      try
      {
         // TextView
         this.textviewVersion
            .setText(BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE);
         //this.textviewLicense.setText(BuildConfig.APPLICATION_LICENSE);
         //this.textviewContact.setText(BuildConfig.APPLICATION_CONTACT);
         final int[] _apiVersions = CommonUtilities.getUsedAPIVersions(this);
         this.textviewPlatform.setText(getString(R.string.android,
            CommonUtilities.getOSVersionFromAPILevel(_apiVersions[0]),
            CommonUtilities.getOSVersionFromAPILevel(_apiVersions[1])));
         //this.textviewArchitecture.setText(BuildConfig.APPLICATION_ARCHITECTURE);
      }
      catch (final PackageManager.NameNotFoundException exception)
      {
         CommonUtilities.addException(exception);
      }
      catch (final Exception exception2)
      {
         CommonUtilities.addException(exception2);
      }
   }
}
