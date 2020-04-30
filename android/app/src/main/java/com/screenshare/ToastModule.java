package com.screenshare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;

import java.util.Map;
import java.util.HashMap;

public class ToastModule extends ReactContextBaseJavaModule implements WebRtcClient.RtcListener {
  private static ReactApplicationContext reactContext;
  private static final String TAG = "MyApp";

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";


  private WebRtcClient mWebRtcClient;
  private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
  //    private EglBase rootEglBase;
  private static Intent mMediaProjectionPermissionResultData;
  private static int mMediaProjectionPermissionResultCode;

  public static String STREAM_NAME_PREFIX = "android_device_stream";
  // List of mandatory application permissions.／
  private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
          "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

  //    private SurfaceViewRenderer pipRenderer;
//    private SurfaceViewRenderer fullscreenRenderer;
  public static int sDeviceWidth;
  public static int sDeviceHeight;
  private final ActivityEventListener activityEventListener;
  public static final int SCREEN_RESOLUTION_SCALE = 2;




  ToastModule(ReactApplicationContext context) {
    super(context);

    reactContext = context;


    this.activityEventListener = new BaseActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
        if (requestCode == CAPTURE_PERMISSION_REQUEST_CODE) {
          if (resultCode != Activity.RESULT_OK) {
//            promise.reject("DOMException", "NotAllowedError");
//            promise = null;
            return;
          }
          mMediaProjectionPermissionResultCode = resultCode;
          mMediaProjectionPermissionResultData = data;
          init();
        }
      }
    };
    reactContext.addActivityEventListener(this.activityEventListener);
  }

    @Override
  public String getName() {
    return "ToastExample";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  @ReactMethod
  public void NavigatorNavigate(){
    Activity activity = reactContext.getCurrentActivity();

    Log.i(TAG, "activity " + activity);
//    activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
//    activity.getWindow().addFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//    activity.setContentView(R.layout.activity_rtc);
    DisplayMetrics metrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
    sDeviceWidth = metrics.widthPixels;
    sDeviceHeight = metrics.heightPixels;


    Log.i(TAG, "sDeviceWidth " + sDeviceWidth);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      startScreenCapture();
    } else {
      Log.i(TAG, "PackageManager.PERMISSION_GRANTED " +PackageManager.PERMISSION_GRANTED);
      init();
    }


//    Intent intent = new Intent(reactContext, ScreenShareActivity.class);
//    if(intent.resolveActivity(reactContext.getPackageManager()) !=null){
//      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//      reactContext.startActivity(intent);
//    }
  }







  @TargetApi(21)
  private void startScreenCapture() {
    final Activity activity = getCurrentActivity();
    MediaProjectionManager mediaProjectionManager =
            (MediaProjectionManager) activity.getApplication().getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE);
    activity.startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
  }

  @TargetApi(21)
  private VideoCapturer createScreenCapturer() {
    if (mMediaProjectionPermissionResultCode != Activity.RESULT_OK) {
      report("User didn't give permission to capture the screen.");
      return null;
    }
    return new ScreenCapturerAndroid(
            mMediaProjectionPermissionResultData, new MediaProjection.Callback() {
      @Override
      public void onStop() {
        report("User revoked permission to capture the screen.");
      }
    });
  }

//  @Override
//  public void onActivityResult(int requestCode, int resultCode, Intent data) {
//    super.onActivityResult(requestCode, resultCode, data);
//    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
//      return;
//    mMediaProjectionPermissionResultCode = resultCode;
//    mMediaProjectionPermissionResultData = data;
//
//    init();
//  }

  private void init() {

    Log.i(TAG, "private void init() " );
    PeerConnectionClient.PeerConnectionParameters peerConnectionParameters =
            new PeerConnectionClient.PeerConnectionParameters(true, false,
                    true, sDeviceWidth / SCREEN_RESOLUTION_SCALE, sDeviceHeight / SCREEN_RESOLUTION_SCALE, 0,
                    0, "VP8",
                    false,
                    true,
                    0,
                    "OPUS", false, false, false, false, false, false, false, false, null);
//        mWebRtcClient = new WebRtcClient(getApplicationContext(), this, pipRenderer, fullscreenRenderer, createScreenCapturer(), peerConnectionParameters);
    mWebRtcClient = new WebRtcClient(reactContext, this, createScreenCapturer(), peerConnectionParameters);

  }

  public void report(String info) {
    Log.e(TAG, info);
  }

//  @Override
//  public void onPause() {
//    super.onPause();
//  }
//
//  @Override
//  public void onResume() {
//    super.onResume();
//  }
//
//  @Override
//  public void onDestroy() {
//    if (mWebRtcClient != null) {
////            mWebRtcClient.onDestroy();
//    }
//    super.onDestroy();
//  }

  @Override
  public void onReady(String callId) {
    mWebRtcClient.start(STREAM_NAME_PREFIX);
  }

  @Override
  public void onCall(final String applicant) {
//    runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//      }
//    });
  }

  @Override
  public void onHandup() {

  }

  @Override
  public void onStatusChanged(final String newStatus) {
//    runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        Toast.makeText(reactContext, newStatus, Toast.LENGTH_SHORT).show();
//      }
//    });
  }

}