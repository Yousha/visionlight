package library.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.ImageView;
import library.Flashlight;
import yousha.application.visionlight.R;

public final class HardwareUtilities // Prevents subclass.
{
   private HardwareUtilities() // Prevents instantiation and subclass from outside.
   {
      throw new AssertionError(); // Prevents instantiation from inside(inner class).
   }

   public static float getCurrentBatteryLevel(final Activity activity)
   {
      final Intent _batteryIntent = activity.registerReceiver(null,
         new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

      if (_batteryIntent != null)
      {
         return (((float) _batteryIntent
            .getIntExtra(BatteryManager.EXTRA_LEVEL, -1) /
            (float) _batteryIntent
               .getIntExtra(BatteryManager.EXTRA_SCALE, -1)) * 100.0f);
      }

      return 0.0F;
   }

   public static boolean hasTelephony(final Activity activity)
   {
      return activity.getPackageManager()
         .hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
   }

   // Requires android.permission.VIBRATE permission.
   public static boolean hasVibrator(final Activity activity)
   {
      return (activity.getSystemService(Context.VIBRATOR_SERVICE) != null);
   }

   public static boolean hasWiFi(final Activity activity)
   {
      return activity.getPackageManager()
         .hasSystemFeature(PackageManager.FEATURE_WIFI);
   }

   public static boolean isEmulator()
   {
      return (Build.FINGERPRINT.startsWith("generic") ||
         Build.FINGERPRINT.startsWith("unknown") ||
         Build.MODEL.contains("google_sdk") ||
         Build.MODEL.contains("Emulator") ||
         Build.MODEL.contains("Android SDK built for x86") ||
         Build.MANUFACTURER.contains("Genymotion") ||
         (Build.BRAND.startsWith("generic") &&
            Build.DEVICE.startsWith("generic")) ||
         "google_sdk".equals(Build.PRODUCT));
   }

   // Requires android.permission.VIBRATE permission.
   @SuppressWarnings("ConstantConditions")
   public static void vibrate(final Activity activity, final Long duration)
      throws NullPointerException
   {
      ((Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE))
         .vibrate(duration);
   }

   public static boolean _setFlashlightState(final Activity activity,
      final boolean state, final ImageView imageview,
      final Flashlight flashlight)
   {
      if (state)
      {
         imageview.setImageResource(R.drawable.ic_switchon);
         activity.getWindow()
            .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         if (flashlight.hasFlashlight())
         {
            flashlight.on();
         }
         else
         {
            activity.getWindow()
               .addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
            WindowManager.LayoutParams _windowManager =
               activity.getWindow().getAttributes();
            _windowManager.screenBrightness = 1F;
            activity.getWindow().setAttributes(_windowManager);
            activity.getWindow().findViewById(R.id.frame_main)
               .setBackgroundResource(R.color.white);
         }
      }
      else
      {
         imageview.setImageResource(R.drawable.ic_switchoff);
         activity.getWindow().setFlags(0,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // || clearFlags()
         if (flashlight.hasFlashlight())
         {
            flashlight.off();
         }
         else
         {
            activity.getWindow().findViewById(R.id.frame_main)
               .setBackgroundResource(R.color.black);
         }
      }

      imageview.destroyDrawingCache();
      return state;
   }
}
