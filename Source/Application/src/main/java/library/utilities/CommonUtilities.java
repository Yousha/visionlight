package library.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import application.MainActivity;
import yousha.application.visionlight.BuildConfig;
import yousha.application.visionlight.R;

public final class CommonUtilities // Prevents subclass.
{
   // Used in getSourceFromURL() method.
   @SuppressWarnings("CanBeFinal")
   public static AtomicBoolean doAbortHTTPConnection = new AtomicBoolean();
   private static final String LOG_TAG = CommonUtilities.class.getSimpleName();
   private volatile static String exceptionMessage = "";

   private CommonUtilities() // Prevents instantiation and subclass from outside.
   {
      throw new AssertionError(); // Prevents instantiation from inside(inner class).
   }

   public static void improveMemory()
   {
      System.runFinalization();
      System.gc();
   }

   @SuppressWarnings("SpellCheckingInspection")
   public static long[] getRuntimeHeapMemory()
   {
      // Max. JVM's memory, App. allocated heap memory, App. free memory, App. used memory.
      return new long[]{Runtime.getRuntime().maxMemory(),
         Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory(),
         Runtime.getRuntime().totalMemory() -
            Runtime.getRuntime().freeMemory()};
   }

   @SuppressWarnings("SpellCheckingInspection")
   public static String formatSize(final long input)
   {
      if (input < 1024)
      {
         return input + " B";
      }

      final int z = (63 - Long.numberOfLeadingZeros(input)) / 10;
      return String.format("%.1f %sB", (double) input / (1L << (z * 10)),
         " KMGTPEZY".charAt(z));
   }

   public static boolean isEmpty(final String input)
   {
      return (((input == null) ||
         (input.trim().replaceAll("\\s+", "").equals(""))));
   }

   public static String getBetweenString(final String input, final String start,
      final String end)
   {
      final String _output =
         input.substring(input.indexOf(start) + start.length());
      return _output.substring(0, _output.indexOf(end));
   }

   public static boolean isValidEmail(final String email)
   {
      @SuppressWarnings("RegExpRedundantEscape")
      final Pattern _pattern = Pattern.compile(
         "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
      final Matcher _matcher = _pattern.matcher(email);
      return _matcher.matches();
   }

   public static String streamToString(final InputStream input_stream,
      final String encoding) throws IOException
   {
      StringBuilder _output = new StringBuilder();
      String _line;
      final BufferedReader _bufferReader =
         new BufferedReader(new InputStreamReader(input_stream, encoding));

      while ((_line = _bufferReader.readLine()) != null)
      {
         if (CommonUtilities.doAbortHTTPConnection.get())
         {
            CommonUtilities.doAbortHTTPConnection.set(false);
            Log.i(CommonUtilities.LOG_TAG, "HTTP connection aborted.");
            break;
         }
         _output.append(_line);
         if (!_bufferReader.ready())
         {
            break;
         }
      }

      _bufferReader.close();
      return (_output.toString().startsWith("null", 0) ?
         _output.toString().replaceFirst("null", "") : _output.toString());
   }

   public static String getDebugInformation(final Activity activity)
      throws IOException, XmlPullParserException,
      PackageManager.NameNotFoundException
   {
      return "Application\n" + "Name: " +
         activity.getString(R.string.title_appname) + "\n" + "Version: " +
         BuildConfig.VERSION_NAME + "\n" + "Build: " +
         BuildConfig.VERSION_CODE + "\n" + "Architecture: " +
         //BuildConfig.APPLICATION_ARCHITECTURE + "\n" + "Uptime: " +
         SystemClock.elapsedRealtime() + "\n" + // XXX
         "Cache directory: " + activity.getCacheDir().getPath() + "\n" + // XXX
         "Allocated memory: " +
         formatSize(CommonUtilities.getRuntimeHeapMemory()[1]) + "\n" +
         "Used memory: " +
         formatSize(CommonUtilities.getRuntimeHeapMemory()[3]) + "\n" +
         "Free memory: " +
         formatSize(CommonUtilities.getRuntimeHeapMemory()[32]) + "\n" +
         "Total threads: " + Thread.getAllStackTraces().keySet().size() + "\n" +
         "\n" + "Java compiler:\n" + "Name: " +
         activity.getString(R.string.title_appname) + "\n" + "\n" +
         "Framework/API\n" + "Name: Android Framework\n" + "Version: " +
         CommonUtilities.getUsedAPIVersions(activity)[0] + "\n" + "\n" +
         "\nJava vendor: " + System.getProperty("java.vendor") +
         "\nJava version: " + System.getProperty("java.version") +
         "\nJava arch.: " + System.getProperty("sun.arch.data.models") +
         "\nVM version: " + System.getProperty("java.vm.version") + "\n" +
         "\nMax. JVM's heap memory: " +
         CommonUtilities.formatSize(CommonUtilities.getRuntimeHeapMemory()[0]) +
         "\nAllocated heap memory: " +
         CommonUtilities.formatSize(CommonUtilities.getRuntimeHeapMemory()[1]) +
         "\nFree heap memory: " +
         CommonUtilities.formatSize(CommonUtilities.getRuntimeHeapMemory()[2]) +
         "\nUsed heap memory: " +
         CommonUtilities.formatSize(CommonUtilities.getRuntimeHeapMemory()[3]) +
         "\n" + "\nUser name: " + System.getProperty("user.name") +
         "\nUser home: " + System.getProperty("user.home") + "\nUser domain: " +
         System.getenv("USERDOMAIN") +
         //"\nUser timezone: " + TimeZone.getDefault().getID() +
         "\nUser locale: " + System.getProperty("user.country") + "-" +
         System.getProperty("user.language") + "\n" + "\nOS name: " +
         System.getProperty("os.name") + "\nKernel version: " +
         System.getProperty("os.version") + "\nOS arch.: " +
         System.getProperty("os.arch") + "\nOS path separator: " +
         File.separator + "\n" + "\nJVM's core(s): " +
         Runtime.getRuntime().availableProcessors() + "\nCPU ID: " +
         System.getenv("PROCESSOR_IDENTIFIER") + "\nCPU arch.: " +
         System.getenv("PROCESSOR_ARCHITECTURE") + "\nCPU cores: " +
         System.getenv("NUMBER_OF_PROCESSORS");
   }

   public synchronized static void addException(final Exception exception)
   {
      final StringWriter _stringWriter =
         new StringWriter(); // No need to use close();
      exception.printStackTrace(new PrintWriter(_stringWriter, true));
      Log.e("", _stringWriter.toString());

      if (exceptionMessage.length() < 250000)
      {
         exceptionMessage += _stringWriter + "\n";
      }
   }

   public static String getException()
   {
      return exceptionMessage;
   }

   public static void cancelNotification(final Context context, final int id)
   {
      final NotificationManager _notificationManager =
         (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);

      if (_notificationManager != null)
      {
         _notificationManager.cancel(id);
      }
   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   public static void clearCache(final Context context)
   {
      final File[] _files = context.getCacheDir().listFiles();

      if (_files != null)
      {
         for (final File _file : _files)
         {
            _file.delete();
         }
      }
   }

   public static void copyToClipboard(final Activity activity,
      final EditText edittext)
   {
      final ClipboardManager _clipboardManager = (ClipboardManager) activity
         .getSystemService(Context.CLIPBOARD_SERVICE);

      if (_clipboardManager != null)
      {
         _clipboardManager.setText(edittext.getText().toString());
      }
   }

   @SuppressWarnings("ConstantConditions")
   public static Dialog createDialog(final Activity activity, final int layout,
      final boolean cancel_on_touch, final int title, final boolean full_screen)
      throws NullPointerException
   {
      final Dialog _dialog = new Dialog(activity);
      _dialog.setContentView(layout);
      _dialog.setOwnerActivity(activity);
      _dialog.setCancelable(true);
      _dialog.setCanceledOnTouchOutside(cancel_on_touch);
      _dialog.setTitle(title);

      if (full_screen)
      {
         // Must be used after `setContentView`.
         _dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
      }

      return _dialog;
   }

   public static void createShortcut(final Class<?> main_class,
      final Activity activity, final String title)
   {
      Log.i(CommonUtilities.LOG_TAG, "Creating shortcut...");
      final Intent _shortcutIntent = new Intent(activity, main_class);
      _shortcutIntent.setAction(Intent.ACTION_MAIN);
      final Intent _addIntent = new Intent();
      _addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, _shortcutIntent);
      _addIntent.putExtra("duplicate", false);
      _addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
      _addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
         Intent.ShortcutIconResource
            .fromContext(activity, R.drawable.ic_launcher));
      _addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
      activity.sendBroadcast(_addIntent);
   }

   public static void removeShortcut(final Class<?> main_class,
      final Activity activity, final String title)
   {
      Log.i(CommonUtilities.LOG_TAG, "Removing shortcut...");
      final Intent _shortcutIntent = new Intent(activity, main_class);
      _shortcutIntent.setAction(Intent.ACTION_MAIN);
      final Intent _removeIntent = new Intent();
      _removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, _shortcutIntent);
      _removeIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
      _removeIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
      activity.sendBroadcast(_removeIntent);
   }

   @SuppressLint("ObsoleteSdkInt")
   public static void enableStrictMode()
   {
      if (Build.VERSION.SDK_INT > 9) // 9 = Gingerbread = Android 2.3-2.3.2
      {
         StrictMode.setThreadPolicy(
            new StrictMode.ThreadPolicy.Builder().permitAll().penaltyLog()
               .build());
      }
   }

   // Requires android.permission.INTERNET permission.
   public static String getDataFromURL(final String url, final String encoding,
      final int timeout) throws IOException
   {
      String _response;
      final String _url = url.trim();

      if (VERSION.SDK_INT >= 10)
      {
         final URLConnection _uc = new URL(_url).openConnection();
         _uc.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*"); // Fixes FileNotFoundException in some cases.
         _uc.setRequestProperty("Upgrade-Insecure-Requests", "1");
         _uc.setRequestProperty("User-Agent",
            "Mozilla/5.0 ( compatible ) "); // Fixes FileNotFoundException in some cases.
         _uc.setRequestProperty("Accept-Language", "en-US,en;");
         _uc.setRequestProperty("Cache-Control", "no-cache");
         _uc.setUseCaches(false);
         _uc.setDoInput(
            true); // false = Ignores response body, and disallows use of getResponseCode().
         _uc.setDoOutput(
            false); // true = Force POST, and allows use of getOutputStream().
         _uc.setConnectTimeout(timeout);
         _uc.setReadTimeout(timeout);
         _response =
            CommonUtilities.streamToString(_uc.getInputStream(), encoding);
      }
      else
      {
         final HttpGet _httpGet = new HttpGet(_url);
         _httpGet.setHeader("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*"); // Fixes FileNotFoundException in some cases.
         _httpGet.setHeader("Upgrade-Insecure-Requests", "1");
         _httpGet.setHeader("User-Agent",
            "Mozilla/5.0 ( compatible ) "); // Fixes FileNotFoundException in some cases.
         _httpGet.setHeader("Accept-Language", "en-US,en;");
         _httpGet.setHeader("Cache-Control", "no-cache");
         final HttpClient _httpClient = new DefaultHttpClient();
         final HttpParams _httpParameters = _httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(_httpParameters, timeout);
         HttpConnectionParams.setSoTimeout(_httpParameters, timeout);
         _httpGet.setParams(_httpParameters);
         final HttpResponse _httpResponse = _httpClient.execute(_httpGet);
         _response = CommonUtilities
            .streamToString(_httpResponse.getEntity().getContent(), encoding);
         _httpResponse.getEntity().consumeContent();
         _httpClient.getConnectionManager().shutdown();
         _httpGet.abort();
      }

      return _response;
   }

   public static String getOSVersionFromAPILevel(final int api_level)
   {
      final String[] _versionNumbers =
         {"?", "1.0", "1.1", "1.5", "1.6", "2.0", "2.0.1", "2.1", "2.2", "2.3",
            "2.3.3", "3.0", "3.1", "3.2", "4.0", "4.0.4", "4.1", "4.2", "4.3",
            "4.4", "4.4W", "5.0", "5.1", "6.0", "7.0", "7.1", "8.0", "8.1",
            "9.0"};

      return _versionNumbers[api_level];
   }

   public static String[] getPermissionLists(final Activity activity,
      final String package_name) throws PackageManager.NameNotFoundException
   {
      final PackageInfo _packageInfo = activity.getPackageManager()
         .getPackageInfo(package_name, PackageManager.GET_PERMISSIONS);
      return _packageInfo.requestedPermissions; // <uses-permission> tag.
   }

   public static int[] getUsedAPIVersions(final Activity activity)
      throws PackageManager.NameNotFoundException, IOException,
      XmlPullParserException
   {
      final int[] _output = new int[3];
      final XmlResourceParser _xrp =
         activity.createPackageContext(activity.getPackageName(), 0).getAssets()
            .openXmlResourceParser(0, "AndroidManifest.xml");
      int _eventType = _xrp.getEventType();

      while (_eventType != XmlPullParser.END_DOCUMENT)
      {
         if ((_eventType == XmlPullParser.START_TAG) &&
            "uses-sdk".equals(_xrp.getName()))
         {
            for (int i = 0; i < _xrp.getAttributeCount(); i++)
            {
               _output[i] = Integer.parseInt(_xrp.getAttributeValue(i));
            }
         }
         _eventType = _xrp.nextToken();
      }

      // Prevents memory leak.
      _xrp.close();
      return _output;
   }

   public static void goToActivity(final Activity current,
      final Class<? extends Activity> class_name)
   {
      current.startActivityForResult(new Intent(current, class_name)
         .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), -1); // || startActivity()
   }

   @SuppressWarnings("WeakerAccess")
   public static boolean hasPermission(final Activity activity,
      final String permission)
   {
      return (activity.checkCallingOrSelfPermission(permission) ==
         PackageManager.PERMISSION_GRANTED);
   }

   public static boolean hasPermission(final Activity activity,
      final String... permissions)
   {
      for (final String _permission : permissions)
      {
         if (!CommonUtilities.hasPermission(activity, _permission))
         {
            Log.wtf(CommonUtilities.LOG_TAG, _permission + " Not found!");
            return false;
         }
      }

      return true;
   }

   // Requires android.permission.ACCESS_NETWORK_STATE permission.
   @SuppressWarnings("ConstantConditions")
   public static boolean isInternetAvailable(final Activity activity)
   {
      final NetworkInfo _networkInformation = ((ConnectivityManager) activity
         .getSystemService(Context.CONNECTIVITY_SERVICE))
         .getActiveNetworkInfo();
      return ((_networkInformation != null) &&
         _networkInformation.isConnected());
   }

   public static void iToast(final Activity activity, final int resource_id)
   {
      CommonUtilities.iToast(activity, activity.getString(resource_id));
   }

   public static void iToast(final Activity activity, final String message)
   {
      if (activity == null)
      {
         return;
      }

      activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), message,
         Toast.LENGTH_LONG).show());
   }

   @SuppressLint("ObsoleteSdkInt")
   public static Intent openApplicationDetailIntent(final String package_name)
   {
      if (Build.VERSION.SDK_INT >= 9)
      {
         final Intent _intent = new Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
         _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         _intent.setData(Uri.parse("package:" + package_name));
         return _intent;
      }

      final Intent _intent = new Intent(Intent.ACTION_VIEW);
      _intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      _intent.setClassName("com.android.settings",
         "com.android.settings.InstalledAppDetails");
      _intent.putExtra("com.android.settings.ApplicationPkgName", package_name);
      return _intent;
   }

   // Uses application's context, because of Fragment.
   public static void showKeyboard(final Context context, final View view)
   {
      final InputMethodManager _imm = (InputMethodManager) context
         .getSystemService(Context.INPUT_METHOD_SERVICE);

      if (_imm != null)
      {
         view.setFocusable(true);
         view.requestFocus();
         _imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT,
            null); // || _imm.showSoftInput(view, InputMethodManager.SHOW_FORCED, null);
      }
   }

   // Uses application's context, because of Fragment.
   public static void hideKeyboard(final Context context, final View view)
   {
      if (view == null)
      {
         return;
      }

      final InputMethodManager _imm = (InputMethodManager) context
         .getSystemService(Context.INPUT_METHOD_SERVICE);

      if (_imm != null)
      {
         _imm.hideSoftInputFromWindow(view.getWindowToken(), 0, null);
      }

      view.clearFocus();
      view.setFocusable(false);
   }

   public static synchronized void showNotification(final Context context,
      final int icon, final String ticker, final String title,
      final String description, final int id)
   {

      final NotificationManager _notificationManager =
         (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
      final Notification _notification =
         new Notification(icon, ticker, System.currentTimeMillis());
      _notification.flags |= Notification.FLAG_AUTO_CANCEL;
      final Intent _intent = new Intent(context, MainActivity.class);
      final PendingIntent _pendingIntent = PendingIntent
         .getActivity(context, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
      _notification
         .setLatestEventInfo(context, title, description, _pendingIntent);

      if (_notificationManager != null)
      {
         _notificationManager.notify(null, id, _notification);
      }
   }

   public static void unbindDrawables(final View view)
   {
      if (view != null)
      {
         if (view.getBackground() != null)
         {
            view.getBackground().setCallback(null);
            view.setBackgroundDrawable(null);
         }
      }

      if (view instanceof ViewGroup)
      {
         for (byte i = 0; i < ((ViewGroup) view).getChildCount(); i++)
         {

            CommonUtilities.unbindDrawables(((ViewGroup) view).getChildAt(i));
         }
         ((ViewGroup) view).removeAllViews();
      }
   }

   public static void _postFeedback(final String url, final String name,
      final String email, final String message, final String referer,
      final String encoding, final int timeout) throws IOException
   {
      final String _url = url.trim();

      if (VERSION.SDK_INT >= 10)
      {
         final URLConnection _uc = new URL(_url).openConnection();
         final String _parameters = String.format(
            "fullname=%s&email=%s&website=&comment=%s&settings_WITH_JS=1&commentJsError=&hide_mail=0",
            URLEncoder.encode(name, encoding),
            URLEncoder.encode(email, encoding),
            URLEncoder.encode(message, encoding));
         _uc.setRequestProperty("Cache-Control:", "no-cache");
         _uc.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*");
         _uc.setRequestProperty("Origin", "http://yousha.blog.ir");
         _uc.setRequestProperty("Upgrade-Insecure-Requests", "1");
         // Fixes FileNotFoundException in some cases.
         _uc.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
         _uc.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded;charset=" + encoding);
         _uc.setRequestProperty("Referer", referer);
         _uc.setRequestProperty("Accept-Encoding", "gzip, deflate, lzma");
         _uc.setRequestProperty("Accept-Language", "en-US,en;");
         // Parameters encoding.
         _uc.setRequestProperty("Accept-Charset", encoding);
         _uc.setConnectTimeout(timeout);
         _uc.setReadTimeout(timeout);
         _uc.setUseCaches(false);
         _uc.setDoInput(
            true); // false = Ignores response body, and disallows use of getResponseCode().
         _uc.setDoOutput(
            true); // true = POST/Put, and allows use of getOutputStream().
         _uc.getOutputStream()
            .write(_parameters.getBytes(encoding)); // Put/POST.
         _uc.getInputStream().close();
      }
      else
      {
         final HttpPost _httpPost = new HttpPost(_url);
         _httpPost.addHeader("Connection:", "keep-alive");
         _httpPost.addHeader("Cache-Control:", "no-cache");
         _httpPost.addHeader("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*");
         _httpPost.addHeader("Origin", "http://yousha.blog.ir");
         _httpPost.addHeader("Upgrade-Insecure-Requests", "1");
         _httpPost.addHeader("User-Agent", "Mozilla/5.0 ( compatible ) ");
         _httpPost.setHeader("Content-Type",
            "application/x-www-form-urlencoded;charset=" + encoding);
         _httpPost.setHeader("Referer", referer);
         _httpPost.addHeader("Accept-Encoding", "gzip, deflate, lzma");
         _httpPost.addHeader("Accept-Language", "en-US,en;");
         // Parameters encoding.
         _httpPost.setHeader("Accept-Charset", encoding);
         final List<NameValuePair> _field = new ArrayList<NameValuePair>();
         _field.add(new BasicNameValuePair("fullname", name));
         _field.add(new BasicNameValuePair("email", email));
         _field.add(new BasicNameValuePair("website", ""));
         _field.add(new BasicNameValuePair("comment", message));
         _field.add(new BasicNameValuePair("settings_WITH_JS", "1"));
         _field.add(new BasicNameValuePair("commentJsError", ""));
         _field.add(new BasicNameValuePair("hide_mail", "0"));
         _httpPost.setEntity(new UrlEncodedFormEntity(_field, encoding));
         final HttpClient _httpClient = new DefaultHttpClient();
         final HttpParams _httpParameters = _httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(_httpParameters, timeout);
         HttpConnectionParams.setSoTimeout(_httpParameters, timeout);
         _httpPost.setParams(_httpParameters);
         _httpClient.execute(_httpPost);
         _httpClient.getConnectionManager().shutdown();
         _httpPost.abort();
      }
   }
}
