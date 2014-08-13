package library;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Preferences // `final` prevents subclass.
{
   //private static final String LOG_TAG = Preferences.class.getSimpleName();
   private static SharedPreferences preferences;

   private Preferences() // Prevents instantiation and subclass from outside.
   {
      throw new AssertionError(); // Prevents instantiation from inside(inner class).
   }

   public static boolean getBoolean(final String key,
      final boolean defaultValue)
   {
      return preferences.getBoolean(key, defaultValue);
   }

   public static void load(final Context context)
   {
      preferences = PreferenceManager.getDefaultSharedPreferences(context);
   }

   /*public static void setBoolean(final String key, final boolean value)
   {
      edit().putBoolean(key, value);
      doCommit();
   }*/

   /*@SuppressWarnings("WeakerAccess")
   public static SharedPreferences.Editor edit()
   {
      return preferences.edit();
   }*/

   /*@SuppressLint("ObsoleteSdkInt")
   private static void doCommit()
   {
      Log.i(LOG_TAG, "Committing preferences...");
      try
      {
         if (Build.VERSION.SDK_INT >= 9) // Gingerbread
         {
            edit().apply();
         }
         else
         {
            new Thread()
            {
               @Override
               public void run()
               {
                  edit().commit();
               }
            }.start();
         }
      }
      catch (final Exception exception)
      {
         CommonUtilities.addException(exception);
      }
   }*/
}
