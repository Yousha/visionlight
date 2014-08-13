/*
 * Name: Flashlight
 * Description: An class/wrapper for Android's camera.
 * Version: 1.1.5-7
 * Language: English
 * License: BSD-3
 * Last update: 2019/1398
 * Architecture: 32bit, 64bit
 * API: Android 2.3.3
 * Compiler: Oracle JDK 1.6.0 64bit
 * Builder: Gradle 4
 * Producer: Yousha Aleayoub
 * Maintainer: Yousha Aleayoub
 * Contact: yousha.a@hotmail.com
 * Link: http://yousha.blog.ir
 */

package library;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;
import library.utilities.CommonUtilities;

public final class Flashlight // `final` prevents subclass.
{
   private static final String LOG_TAG = Flashlight.class.getSimpleName();
   private static Camera camera = null; // Or won't reset in onDestroy().
   private boolean isCameraOpen;
   private boolean isParametersInitialized;
   private Parameters cameraParameters = null;

   public Flashlight()
   {
      Log.i(LOG_TAG, "Initializing  " + LOG_TAG + "...");
      this.open();
   }

   public boolean hasFlashlight()
   {
      Log.i(LOG_TAG, "Checking for back-camera...");
      boolean _result = false;
      final int _cameras = Camera.getNumberOfCameras();

      if (_cameras > 0)
      {
         final CameraInfo _cameraInfo = new CameraInfo();
         for (byte i = 0; i < _cameras; i++)
         {
            Camera.getCameraInfo(i, _cameraInfo);
            if (_cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
            {
               Log.i(LOG_TAG, "Found the back camera.");
               _result = true;
               break;
            }
         }
      }

      return _result;
   }

   public synchronized void open()
   {
      if (!this.isCameraOpen && (camera == null))
      {
         Log.i(LOG_TAG, "Opening camera...");
         try
         {
            camera =
               Camera.open(); // || open(Camera.CameraInfo.CAMERA_FACING_BACK)
            this.isCameraOpen = true;
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
            this.isCameraOpen = false;
         }
      }
   }

   public synchronized void initialize()
   {
      if (this.isCameraOpen && !this.isParametersInitialized)
      {
         Log.i(LOG_TAG, "Initializing camera...");
         try
         {
            this.cameraParameters = camera.getParameters();
            if (this.cameraParameters.getFlashMode() == null)
            {
               this.cameraParameters.setFlashMode("off");
            }
            this.isParametersInitialized = true;
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
            this.isParametersInitialized = false;
         }
      }
   }

   @Override
   public synchronized void finalize()
   {
      if (this.isCameraOpen && this.isParametersInitialized)
      {
         this.cameraParameters.setFlashMode("on");
         camera.setParameters(this.cameraParameters);
         Log.i(LOG_TAG, "Releasing camera...");
         try
         {
            camera.release();
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
            this.cameraParameters.setFlashMode("torch");
            camera.setParameters(this.cameraParameters);
            try
            {
               camera.release();
            }
            catch (final Exception exception2)
            {
               CommonUtilities.addException(exception2);
            }
         }
         camera = null;
         this.isCameraOpen = false;
      }
   }

   public synchronized void on() // Need to block the current thread.
   {
      if (this.isCameraOpen && this.isParametersInitialized)
      {
         this.cameraParameters.setFlashMode("torch");
         camera.setParameters(this.cameraParameters);
         Log.i(LOG_TAG, "Turning camera on...");
         try
         {
            camera.startPreview();
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
            this.cameraParameters.setFlashMode("on");
            camera.setParameters(this.cameraParameters);
            try
            {
               camera.startPreview();
            }
            catch (final Exception exception2)
            {
               CommonUtilities.addException(exception2);
            }
         }
         camera.autoFocus(new AutoFocusCallback()
         {
            @Override
            public void onAutoFocus(final boolean arg0, final Camera arg1)
            {

            }
         });
      }
   }

   public synchronized void off()
   {
      if (this.isCameraOpen && this.isParametersInitialized)
      {
         this.cameraParameters.setFlashMode("off");
         camera.setParameters(this.cameraParameters);
         Log.i(LOG_TAG, "Turning camera off...");
         try
         {
            camera.setPreviewCallback(null);
            camera.stopPreview();
         }
         catch (final Exception exception)
         {
            CommonUtilities.addException(exception);
         }
      }
   }
}
