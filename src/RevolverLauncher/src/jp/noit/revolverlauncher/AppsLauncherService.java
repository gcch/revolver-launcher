/*
 * Copyright (C) 2014-2015 NOIT, School a-apps project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.noit.revolverlauncher;


import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


/**
 * AppsLauncherService --- ランチャー起動用オーバーレイビュー管理サービス
 * @author hi, tag
 *
 */
public class AppsLauncherService extends Service {

  final String TAG = "AppsLauncherService";

  // スターターバー
  private WindowManager wm;
  private WindowManager.LayoutParams paramsLeft, paramsRight;
  private RelativeLayout rlLeft, rlRight;
  private FrameLayout flAppsLauncherItems;
  private View vLeft, vRight;

  // Flags
  private boolean enableAnimation = true;   // アニメーション
  private boolean closeLauncherWithBackKey = false;
  private boolean visibleLauncher = false;  // ランチャー起動状態
  private boolean rotateDisplay = false;  // 画面回転時の初期化用

  /**
   * onCreate --- Service 生成時の処理
   */
  @Override
  public void onCreate() {
    super.onCreate();
  }

  /**
   * onStartCommand
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    onStart(intent, startId);
    return START_STICKY;
  }

  /**
   * onStart
   */
  @Override
  public void onStart(Intent intent, int startId) {

    //    Log.d(TAG, "onStart");

    wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  // WindowManager の取得
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());  // ParameterManager の取得

    // 通知領域への表示
    startNotification();

    /*
     * ランチャー起動用バーの設定
     */
    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());  // LayoutInflater

    // 色の取得
    final int starterBarColor = parameterManager.getStarterBarColor();

    // 画面サイズ
    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
    final int dispHeight = dm.heightPixels;
    //    Log.d(TAG, "Display size: " + dm.widthPixels + ":" + dm.heightPixels);
    dm = null;

    // 左側
    paramsLeft = generateWMLayoutParamsWrapContent();
    paramsLeft.gravity = Gravity.LEFT;
    paramsLeft.y = dispHeight * (parameterManager.getStarterBarPositionLeft() - 50) / 100;
    vLeft = inflater.inflate(R.layout.starter_overlay_left, null);
    rlLeft = (RelativeLayout)vLeft.findViewById(R.id.leftBar);
    rlLeft.getLayoutParams().width = parameterManager.getStarterBarWidthLeft();  // 幅
    rlLeft.getLayoutParams().height = dispHeight * parameterManager.getStarterBarHeightLeft() / 100;
    rlLeft.setBackgroundColor(starterBarColor);        // 色
    vLeft.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          if (!visibleLauncher) {
            visibleLauncher = true;
            startLauncher(0, (int)event.getRawY());  // ランチャーの起動
          }
        }
        return false;
      }
    });
    if (parameterManager.getStatusStarterBarLeft()) {
      vLeft.setVisibility(View.VISIBLE);
    } else {
      vLeft.setVisibility(View.INVISIBLE);
    }
    wm.addView(vLeft, paramsLeft);  // 追加

    // 右側
    paramsRight = generateWMLayoutParamsWrapContent();
    paramsRight.gravity = Gravity.RIGHT;
    paramsRight.y = dispHeight * (parameterManager.getStarterBarPositionRight() - 50) / 100;
    vRight = inflater.inflate(R.layout.starter_overlay_right, null);
    rlRight = (RelativeLayout)vRight.findViewById(R.id.rightBar);
    rlRight.getLayoutParams().width = parameterManager.getStarterBarWidthRight();  // 幅
    rlRight.getLayoutParams().height = dispHeight * parameterManager.getStarterBarHeightRight() / 100;
    rlRight.setBackgroundColor(starterBarColor);        // 色
    vRight.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          if (!visibleLauncher) {
            visibleLauncher = true;
            DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
            startLauncher((int)dm.widthPixels, (int)event.getRawY());  // ランチャーの起動
          }
        }
        return false;
      }
    });
    if (parameterManager.getStatusStarterBarRight()) {
      vRight.setVisibility(View.VISIBLE);
    } else {
      vRight.setVisibility(View.INVISIBLE);
    }
    wm.addView(vRight, paramsRight);  // 追加


    // 解放
    inflater = null;
    parameterManager = null;
  }

  /**
   * startNotification --- 通知領域への表示 (通知領域 + 設定画面呼び出し + 死なない Service)
   */
  private void startNotification() {
    // IDs
    final int WIDGET_ID = 12;
    final int NOTIFICATION_ID = 1;

    Intent activityIntent = new Intent(getApplicationContext(), SettingActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), WIDGET_ID, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);


    // 通知領域に表示
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    builder.setContentIntent(contentIntent);       // タップ時に呼び出す Activity
    builder.setTicker(getResources().getString(R.string.notification_ticker));  // ステータスバーに表示されるテキスト
    builder.setContentTitle(getResources().getString(R.string.app_name));  // タイトル
    builder.setContentText(getResources().getString(R.string.notification_content_text));             // タイトル下の説明文
    builder.setSmallIcon(R.drawable.ic_launcher_white);     // アイコン
    builder.setWhen(0);   // 通知タイミング

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
      builder.setPriority(NotificationCompat.PRIORITY_LOW);
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
      builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
    }

    Notification notification = builder.build();   // (ビルド)
    notification.flags |= NotificationCompat.FLAG_NO_CLEAR;  // 常時表示

    // 通知マネージャ
    ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);

    // フォアグランドサービスとして開始
    startForeground(NOTIFICATION_ID, notification);

  }

  /**
   * 削除時の処理
   */
  @Override
  public void onDestroy() {
    //    Log.d(TAG, "onDestroy");
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());  // ParameterManager の取得
    if (!parameterManager.getStarterBarServiceStatus()) {
      super.onDestroy();
      WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  // WindowManager の取得
      if (parameterManager.getStatusStarterBarLeft()) {
        wm.removeViewImmediate(vLeft);  // 削除: 左側
      }
      if (parameterManager.getStatusStarterBarRight()) {
        wm.removeViewImmediate(vRight);  // 削除: 右側
      }
      //wm = null;
      ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();  // 通知の除去
      parameterManager = null;
    }

    super.onDestroy();
  }


  /**
   * onConfigurationChanged --- 画面回転を検出
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    //    Log.d(TAG, "onConfigurationChanged");
    super.onConfigurationChanged(newConfig);
    redrawBars();

    if (visibleLauncher) {

      // 画面サイズの取得
      DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
      final int dispWidth = dm.widthPixels;  // 長辺
      final int dispHeight = dm.heightPixels;  // 短辺

      // 不安なので、短辺／長辺の判定および保持
      int shortEdge = dispWidth < dispHeight ? dispWidth : dispHeight;
      int longEdge = dispWidth > dispHeight ? dispWidth : dispHeight;

      // 元々タッチされた位置
      int touchX = centerX - launcherRadius;
      int touchY = centerY - launcherRadius;
      // 移動先位置
      int newTouchX = touchX;
      int newTouchY = touchY;

      switch (newConfig.orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:  // 横長
        //        Log.d(TAG, "[Rotate] Orientation: Landscape");
        newTouchX = (int)((double)touchX * ((double)longEdge / shortEdge));
        newTouchY = (int)((double)touchY * ((double)shortEdge / longEdge));
        //        Log.d(TAG, "[Rotate] Move: " + touchX + ":" + touchY + " -> " + newTouchX + ":" + newTouchY);
        if (newTouchY > shortEdge) {  // 範囲外となってしまった場合 (おそらくないかと)
          newTouchX = touchX;
          newTouchY = touchY;
          //          Log.d(TAG, "[Rotate] Adjust: " + (centerX - launcherRadius) + ":" + (centerY - launcherRadius) + " -> " + newTouchX + ":" + newTouchY);
        }
        break;
      case Configuration.ORIENTATION_PORTRAIT:  // 縦長
        //        Log.d(TAG, "[Rotate] Orientation: Portrait");
        newTouchX = (int)((double)touchX * ((double)shortEdge / longEdge));
        newTouchY = (int)((double)touchY * ((double)longEdge / shortEdge));
        //        Log.d(TAG, "[Rotate] Move: " + touchX + ":" + touchY + " -> " + newTouchX + ":" + newTouchY);
        if (newTouchY > longEdge) {  // 範囲外となってしまった場合 (おそらくないかと)
          newTouchX = touchX;
          newTouchY = touchY;
          //          Log.d(TAG, "[Rotate] Adjust: " + (centerX - launcherRadius) + ":" + (centerY - launcherRadius) + " -> " + newTouchX + ":" + newTouchY);
        }
        break;
      }

      rotateDisplay = true;
      closeLauncher();
      initializeLauncher(newTouchX, newTouchY);
      openLauncher();
      rotateDisplay = false;
    }

  }




  private final IBinder binder = new AppsLauncherBinder();
  public class AppsLauncherBinder extends Binder {
    AppsLauncherService getService() {
      //      Log.d(TAG, "getService()");
      return AppsLauncherService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    //    Log.d(TAG, "onBind()");
    return binder;
  }
  @Override
  public boolean onUnbind(Intent intent) {
    //    Log.d(TAG, "onUnbind()");
    return true;
  }


  /**
   * generateWMLayoutParamsWrapContent --- WindowManager.LayoutParams の生成
   * @return
   */
  private WindowManager.LayoutParams generateWMLayoutParamsWrapContent() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_FULLSCREEN          // 全画面
        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR  //
        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS    // ステータスバーに被せる
        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN    //
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,    // タッチを伝播
        PixelFormat.TRANSLUCENT);
    params.windowAnimations = 0;  // アニメーション付加用
    return params;
  }


  /**
   * ランチャーの起動
   * @param x
   * @param y
   */
  private synchronized void startLauncher(int x, int y) {
    startGetBackKeyActivity();  // v1.0.2
    initializeLauncher(x, y);
    openLauncher();
  }

  /**
   * generateWMLayoutParamsMatchParent --- WindowManager.LayoutParams の生成
   * @return
   */
  private WindowManager.LayoutParams generateWMLayoutParamsMatchParent() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,         // システムアラートレイヤに表示
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE         // こうしないとタッチ操作が認識されない
        | WindowManager.LayoutParams.FLAG_FULLSCREEN          // 全画面
        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR  //
        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS    // ステータスバーに被せる
        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN    //
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,    // タッチを伝播
        PixelFormat.TRANSLUCENT // 背景透過
        );
    params.windowAnimations = 0;  // アニメーション付加用
    return params;
  }

  /**
   * redrawBars --- バーの再描画
   */
  public void redrawBars() {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());
    if (!parameterManager.getStarterBarServiceStatus()) {
      return;
    }
    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
    final int dispHeight = dm.heightPixels;
    dm = null;

    // 左側
    if (parameterManager.getStatusStarterBarLeft()) {
      rlLeft.getLayoutParams().width = parameterManager.getStarterBarWidthLeft();  // 幅
      rlLeft.getLayoutParams().height = dispHeight * parameterManager.getStarterBarHeightLeft() / 100;
      rlLeft.setBackgroundColor(parameterManager.getStarterBarColor());        // 色
      rlLeft.invalidate();
      paramsLeft.y = dispHeight * (parameterManager.getStarterBarPositionLeft() - 50) / 100;
      wm.updateViewLayout(vLeft, paramsLeft);
    }

    // 右側
    if (parameterManager.getStatusStarterBarRight()) {
      rlRight.getLayoutParams().width = parameterManager.getStarterBarWidthRight();  // 幅
      rlRight.getLayoutParams().height = dispHeight * parameterManager.getStarterBarHeightRight() / 100;
      rlRight.setBackgroundColor(parameterManager.getStarterBarColor());        // 色
      rlRight.invalidate();
      paramsRight.y = dispHeight * (parameterManager.getStarterBarPositionRight() - 50) / 100;
      wm.updateViewLayout(vRight, paramsRight);
    }
  }

  /**
   * vibivilityOfBars --- バーの表示／非表示
   */
  public void vibivilityOfBars() {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());

    // 左側
    if (parameterManager.getStatusStarterBarLeft()) {
      vLeft.setVisibility(View.VISIBLE);
    } else {
      vLeft.setVisibility(View.INVISIBLE);
    }

    // 右側
    if (parameterManager.getStatusStarterBarRight()) {
      vRight.setVisibility(View.VISIBLE);
    } else {
      vRight.setVisibility(View.INVISIBLE);
    }
  }

  public void redrawLeftBar(int width, int height, int color, int pos) {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());
    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
    final int dispHeight = dm.heightPixels;
    dm = null;

    // 左側
    if (parameterManager.getStatusStarterBarLeft()) {
      rlLeft.getLayoutParams().width = width;  // 幅
      rlLeft.getLayoutParams().height = dispHeight * height / 100;
      rlLeft.setBackgroundColor(color);        // 色
      rlLeft.invalidate();
      paramsLeft.y = dispHeight * (pos - 50) / 100;
      wm.updateViewLayout(vLeft, paramsLeft);
      if (parameterManager.getStatusStarterBarLeft()) {
        vLeft.setVisibility(View.VISIBLE);
      } else {
        vLeft.setVisibility(View.INVISIBLE);
      }
    }
  }

  public void redrawRightBar(int width, int height, int color, int pos) {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());
    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
    final int dispHeight = dm.heightPixels;
    dm = null;

    // 右側
    if (parameterManager.getStatusStarterBarRight()) {
      rlRight.getLayoutParams().width = width;  // 幅
      rlRight.getLayoutParams().height = dispHeight * height / 100;
      rlRight.setBackgroundColor(color);        // 色
      rlRight.invalidate();
      paramsRight.y = dispHeight * (pos - 50) / 100;
      wm.updateViewLayout(vRight, paramsRight);
      if (parameterManager.getStatusStarterBarRight()) {
        vRight.setVisibility(View.VISIBLE);
      } else {
        vRight.setVisibility(View.INVISIBLE);
      }
    }
  }



  /**
   * ランチャー部分
   **************************************************/

  // チェンジャー関連
  private int revolverCurrentNum = 0;           // 現在参照中のリボルバー番号

  private SerializableRevolverData serializableRevolverData;

  // 色々
  private ViewGroup launcherView; // ランチャ部分
  private WindowManager.LayoutParams launcherViewParams;

  // ランチャー周りのレイアウト
  private CircularLayout cl;             // CircularLayout
  private FrameLayout flAppsLauncher;    // 背景を除くランチャー
  private FrameLayout flCircularLayout;  // CircularLayout の入った FrameLayout
  private CurvedIndexView civ;           // カウンター
  private CurvedClock ccDate, ccTime;
  private CurvedBarMeterView cm, mm, bm;

  private boolean enableClock, enableResMeter;

  // 配置中心
  private int centerX, centerY;
  private int launcherRadius;

  // アイコンサイズ
  private int iconSize = 100;

  // アニメーション関連
  private static final int TRANSITION_VISIBLE = 300;
  private static final int TRANSITION_INVISIBLE = 300;
  private static final int CHANGE_REVOLVER = 200;
  private TransitionDrawable transitionLauncherBg;

  // CircularLayout で View をどっち周りで配置するか
  private boolean layoutClockwise = false; // (true: 時計回り / false: 反時計回り)

  private boolean isBlackMode = true;

  /**
   * initializeLauncher --- ランチャーの準備
   */
  private void initializeLauncher(int touchX, int touchY) {

    // LayoutInflater の作成
    LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

    // 画面サイズの取得
    //    WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  // WindowManager の取得
    //    Display disp = wm.getDefaultDisplay();
    //    Point p = new Point();
    //    disp.getSize(p);
    //    int dispVerticalCenter = p.x / 2; // 画面の半分 (横方向)

    DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
    //    Log.d(TAG, "Display size (initializeLauncher): " + dm.widthPixels + ":" + dm.heightPixels);
    int dispVerticalCenter = dm.widthPixels / 2;

    // WindowManager の生成
    launcherViewParams = generateWMLayoutParamsMatchParent();

    // ParameterManager の初期化
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());

    // 各種設定情報を取得
    enableAnimation = parameterManager.getEnableAnimationStatus();
    closeLauncherWithBackKey = parameterManager.getActionOfBackkeyStatus() == ParameterManager.BackKeyAction.CLOSE_LAUNCHER;
    isBlackMode = parameterManager.getAccessoriesColorIsBlack();

    // 色
    int barColor = isBlackMode ? Color.BLACK : Color.WHITE;
    int barBgColor = isBlackMode ? Color.argb(50, 0, 0, 0) : Color.argb(50, 255, 255, 255);
    int labelColor = isBlackMode ? Color.argb(200, 255, 255, 255) : Color.argb(50, 0, 0, 0);

    // 円の半径の設定
    launcherRadius = parameterManager.getRevolverRadius();

    if (dispVerticalCenter > touchX) { // 画面の左側
      layoutClockwise = true;
      centerX = 0;
      centerY = touchY + (int) launcherRadius;
    } else { // 画面の右側
      layoutClockwise = false;
      int[] posRightBar = new int[2];
      vRight.getLocationOnScreen(posRightBar);  // 右バーの位置を取得
      centerX = dm.widthPixels;
      //      centerX = posRightBar[0] + parameterManager.getStarterBarWidthRight();
      centerY = touchY + (int) launcherRadius;
    }

    // アイコンサイズ
    iconSize = parameterManager.getIconSize();
    // ランチャー周りのベース半径
    int aroundLauncherRadius = launcherRadius + iconSize / 2 + launcherRadius / 10;  // テキストのベースラインの半径

    // 連装数の初期化
    if (revolverCurrentNum >= parameterManager.getRevolverNum()) {
      revolverCurrentNum = 0;
    }


    /**
     * ランチャー部分の初期化
     */
    launcherView = (ViewGroup)layoutInflater.inflate(R.layout.apps_launcher, null);  // LayoutInflater
    launcherView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);  // ハードウェアアクセラレーション
    wm.addView(launcherView, launcherViewParams);  // 画面への追加

    // 表示形態 (全画面表示など) が変わった場合
    //    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {  // API 14 (Android 4.0) and later
    ////      Log.d(TAG, "setOnSystemUiVisibilityChangeListener!");
    //      // API 11 (Android 3.0) and later
    //      launcherView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
    //        @Override
    //        public void onSystemUiVisibilityChange(int visibility) {
    //          Log.d(TAG, "onSystemUiVisibilityChange");
    //          switch (visibility) {
    //          // Android 4.0 and later
    //          case View.SYSTEM_UI_FLAG_VISIBLE:  // 標準状態
    //            break;
    //          default:
    //            break;
    //          }
    //        }
    //      });
    //    }

    /**
     *  背景
     */
    ColorDrawable[] color = {new ColorDrawable(Color.argb(0, 0, 0, 0)), new ColorDrawable(parameterManager.getLauncherBgBarColor())};
    transitionLauncherBg = new TransitionDrawable(color);
    transitionLauncherBg.setCrossFadeEnabled(true);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {  // under API Level 16 (Android 4.1, 4.1.1)
      launcherView.setBackgroundDrawable(transitionLauncherBg);
    } else {  // over API Level 16 (Android 4.1, 4.1.1)
      launcherView.setBackground(transitionLauncherBg);
    }

    // ランチャーの各レイヤの取得
    flAppsLauncher = (FrameLayout)launcherView.findViewById(R.id.frameLayout_appsLauncher);  // 全てを管理するレイヤ
    flAppsLauncherItems = (FrameLayout)launcherView.findViewById(R.id.frameLayout_appsLauncherItems);  // 各パーツを保持しているレイヤ
    flCircularLayout = (FrameLayout)launcherView.findViewById(R.id.frameLayout_circularLayout);  // CircularLayout を保持しているレイヤ


    /**
     * アプリアイコン配置用 CircularLayout レイヤ
     */
    cl = (CircularLayout) launcherView.findViewById(R.id.circularLayout);
    cl.setCenter(centerX, centerY);
    cl.setRadius(launcherRadius);
    cl.setCircleDrawColor(barBgColor);
    //    cl.setCircleDrawStrokeWidth(1);
    cl.setChildSize(iconSize);
    //cl.setChildNum(iconNum);
    if (layoutClockwise) {
      cl.setLayoutCounterclockwise(false);
    }
    cl.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (!cl.onTouchEvent(event)) {
          v.setOnTouchListener(null);
          closeLauncher();
        }
        return false;
      }
    });
    setAppsIcons();  // アプリアイコンの設定

    /**
     * チェンジャーとインデックスビュー
     */
    if (parameterManager.getRevolverNum() > 1) {
      Button changeButton = new Button(getApplicationContext());
      Drawable drawable = getResources().getDrawable(isBlackMode ? R.drawable.roundbutton_bk : R.drawable.roundbutton_wh);
      int changerButtonRadius = (aroundLauncherRadius - iconSize) / 2;
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(changerButtonRadius * 2, changerButtonRadius * 2);
      lp.gravity = Gravity.NO_GRAVITY;
      lp.setMargins(centerX - changerButtonRadius, centerY - changerButtonRadius, 0, 0);
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {  // under API Level 16 (Android 4.1, 4.1.1)
        changeButton.setBackgroundDrawable(drawable);
      } else {  // over API Level 16 (Android 4.1, 4.1.1)
        changeButton.setBackground(drawable);
      }
      changeButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (!changing) {
            changeRevolver();  // チェンジャー機能
          }
        }
      });
      flAppsLauncherItems.addView(changeButton, lp);  // 画面への追加

      // リボルバーインデックス表示ビュー
      int civRadius = (aroundLauncherRadius - iconSize) * 5 / 8;
      int maxRevolver = parameterManager.getRevolverNum();
      civ = (CurvedIndexView)launcherView.findViewById(R.id.curvedindexview);
      civ.setDrawDirection(layoutClockwise);
      civ.setCenter(centerX, centerY);
      civ.setRadius(civRadius);
      civ.setViewAngle(180.0f - (float)(10 * maxRevolver / 2), 10 * maxRevolver);
      civ.setDotRadius(aroundLauncherRadius / 50);
      civ.setActiveDotColor(barColor);
      civ.setNoActiveDotColor(barBgColor);
      civ.setMaxIndex(maxRevolver);
      civ.setActiveIndex(revolverCurrentNum);
    }



    /**
     * 特殊キー
     */
    boolean[] spKeyStatus = new boolean[4];
    spKeyStatus[0] = parameterManager.getSpKeyBackStatus();
    spKeyStatus[1] = parameterManager.getSpKeyHomeStatus();
    spKeyStatus[2] = parameterManager.getSpKeyRecentAppsStatus();
    spKeyStatus[3] = parameterManager.getSpKeyNotificationsStatus();

    int specialKeysNum = 0;  // ボタン数
    for (int i = 0; i < 4; i++) {
      if (spKeyStatus[i]) {
        specialKeysNum++;
      }
    }

    Button[] specialKeys = new Button[specialKeysNum];
    int cnt = 0;
    if (spKeyStatus[0]) {
      specialKeys[cnt++] = makeBackButton();           // 戻るボタン
    }
    if (spKeyStatus[1]) {
      specialKeys[cnt++] = makeHomeButton();           // ホームボタン
    }
    if (spKeyStatus[2]) {
      specialKeys[cnt++] = makeRecentsButton();        // 最近使ったアプリボタン
    }
    if (spKeyStatus[3]) {
      specialKeys[cnt++] = makeNotificationsButton();  // 通知バーボタン
    }

    int specialKeysAngleInit = 90;                   // 初期角度
    int specialKeysSize = aroundLauncherRadius / 4;  // キーサイズ
    int specialKeysRadius = aroundLauncherRadius + (int)(Math.sqrt((double)(2 * specialKeysSize * specialKeysSize)) / 2);

    for (int i = 0; i < specialKeys.length; i++) {
      float angle = 22.5f * i + 11.25f;  // アイコン1つ当たりの角度 * アイコン番号 + アイコン半分の角度 (1個目が隠れないように)

      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(specialKeysSize, specialKeysSize);
      lp.gravity = Gravity.NO_GRAVITY;
      final double posX = specialKeysRadius * Math.cos(Math.toRadians(specialKeysAngleInit + angle));
      final double posY = specialKeysRadius * Math.sin(Math.toRadians(specialKeysAngleInit + angle));

      final int x = (int)(layoutClockwise ? -(posX + centerX) : (posX + centerX));
      final int y = (int)((-1) * posY) + centerY;
      final int rad = specialKeysSize / 2;
      lp.setMargins(x - rad, y - rad, x + rad, y + rad);
      flAppsLauncherItems.addView(specialKeys[i], lp);  // 画面への追加
      if (layoutClockwise) {
        specialKeys[i].setRotation(angle);
      } else {
        specialKeys[i].setRotation(-angle);
      }
    }


    // 大きさ
    float offset = aroundLauncherRadius / 20;
    int sizeunit = aroundLauncherRadius / 10;

    // 有効化状態
    enableClock = parameterManager.getClockStatus();
    enableResMeter = parameterManager.getResMeterStatus();

    /**
     * 時計
     */
    float clockStartAngle = layoutClockwise ? - 22.5f * (4 - specialKeys.length) + (specialKeys.length < 1 ? 3 : 0) : - 22.5f * (specialKeys.length) - 127 - (specialKeys.length < 1 ? 2 : 0);  // 開始角度 (右が0度、時計回り)
    if (enableClock) {
      // Date

      if (!parameterManager.getEnableClockAndDateCustomize()) {
        parameterManager.resetClockAndDateTextSizeAndBaseline();
      }
      float clockFontSize, clockBaseline, dateFontSize, dateBaseline;
      clockFontSize = parameterManager.getClockFontSize();
      clockBaseline = parameterManager.getClockBaseline();
      dateFontSize = parameterManager.getDateFontSize();
      dateBaseline = parameterManager.getDateBaseline();

      ccDate = (CurvedClock) launcherView.findViewById(R.id.curveclock_date);
      ccDate.setCenter(centerX, centerY);
      ccDate.setRadius(clockBaseline + aroundLauncherRadius);
      ccDate.setTextStartPointAngle(clockStartAngle);
      ccDate.setTextSize(clockFontSize);
      ccDate.setTextColor(barColor);
      ccDate.setTextOffset(offset);
      // Time
      ccTime = (CurvedClock) launcherView.findViewById(R.id.curveclock_time);
      ccTime.setCenter(centerX, centerY);
      ccTime.setRadius(dateBaseline + aroundLauncherRadius);
      ccTime.setTextStartPointAngle(clockStartAngle - 1);
      ccTime.setTextSize(dateFontSize);
      ccTime.setTextColor(barColor);
      ccTime.setTextOffset(offset);
    } else {
      FrameLayout flCurvedClock = (FrameLayout)launcherView.findViewById(R.id.frameLayout_curvedClock);
      flCurvedClock.setVisibility(View.GONE);
    }

    /**
     *  リソースメータ
     */
    if (enableResMeter) {
      float meterStartAngle = clockStartAngle + (layoutClockwise ? (enableClock ? 41 : -1) : (enableClock ? -51 : -50));  // 開始角度 (右が0度、時計回り)
      int meterAngleRange = layoutClockwise ? (enableClock ? 47 : 87) : (enableClock ? 47 : 87);              // バーの表示角度 (長さ)
      int meterBarWidth = sizeunit * 2 / 3;  // バーの幅
      offset += meterBarWidth / 2;
      // CPU
      cm = (CurvedCpuMeter) launcherView.findViewById(R.id.cpu_meter);
      cm.setCenter(centerX, centerY);
      cm.setRadius(aroundLauncherRadius + offset + (float) meterBarWidth * 6 / 2);
      cm.setBarAngle(meterStartAngle, meterAngleRange);
      cm.setBarWidth(meterBarWidth);
      cm.setBarColor(barColor);
      cm.setBarBackColor(barBgColor);
      cm.setLabel("CPU", meterBarWidth - 2, labelColor);
      // Memory
      mm = (CurvedMemMeter) launcherView.findViewById(R.id.mem_meter);
      mm.setCenter(centerX, centerY);
      mm.setRadius(aroundLauncherRadius + offset + (float) meterBarWidth * 3 / 2);
      mm.setBarAngle(meterStartAngle, meterAngleRange);
      mm.setBarWidth(meterBarWidth);
      mm.setBarColor(barColor);
      mm.setBarBackColor(barBgColor);
      mm.setLabel("Memory", meterBarWidth - 2, labelColor);
      // Battery
      bm = (CurvedBatMeter) launcherView.findViewById(R.id.bat_meter);
      bm.setCenter(centerX, centerY);
      bm.setRadius(aroundLauncherRadius + offset); //- (float) meterBarWidth * 1 / 2);
      bm.setBarAngle(meterStartAngle, meterAngleRange);
      bm.setBarWidth(meterBarWidth);
      bm.setBarColor(barColor);
      bm.setBarBackColor(barBgColor);
      bm.setLabel("Battery", meterBarWidth - 2, labelColor);
    } else {
      FrameLayout flCurvedResMeter = (FrameLayout)launcherView.findViewById(R.id.frameLayout_curvedResMeter);
      flCurvedResMeter.setVisibility(View.GONE);
    }

    if ( !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("kPremEd", false) ) {  // 通常版
      setupAds(getApplicationContext());
    }


    parameterManager = null;
  }

  /**
   * openLauncher()
   */
  private void openLauncher() {
    visibleLauncher = true;
    if (enableAnimation && !rotateDisplay) {
      openLauncherAnimation();
    } else {
      launcherView.setVisibility(View.VISIBLE);
      transitionLauncherBg.startTransition(0);
    }
    resumeAds();
  }

  /**
   * openLauncherAnimation --- ランチャーオープンアニメーション
   */
  private void openLauncherAnimation() {
    // 回転
    RotateAnimation animationRotate = new RotateAnimation(layoutClockwise ? -180.0f : 180.0f, 0.0f, centerX, centerY);
    // 透明度
    AlphaAnimation animationAlpha = new AlphaAnimation(0.0f, 1.0f);
    // アニメーションセット
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.addAnimation(animationRotate);
    animationSet.addAnimation(animationAlpha);
    animationSet.setFillAfter(true);
    animationSet.setInterpolator(new DecelerateInterpolator());
    animationSet.setDuration(TRANSITION_VISIBLE);
    LayoutAnimationController layoutAC = new LayoutAnimationController(animationSet);
    // ランチャーアニメーション
    flAppsLauncher.setVisibility(View.INVISIBLE);
    flAppsLauncher.setLayoutAnimation(layoutAC);
    flAppsLauncher.startLayoutAnimation();
    flAppsLauncher.setVisibility(View.VISIBLE);
    // 背景アニメーション
    transitionLauncherBg.startTransition(TRANSITION_VISIBLE);
  }

  /**
   * closeLauncher - ランチャーを閉じる
   */
  public void closeLauncher() {

    broadcastActivityFinish();  // v1.0.2

    pauseAds();
    destroyAds();

    if (enableClock) {
      ccDate.stopTicker();
      ccTime.stopTicker();
    }
    if (enableResMeter) {
      ((CurvedCpuMeter)cm).stopMonitor();
      ((CurvedMemMeter)mm).stopMonitor();
    }

    if (enableAnimation && !rotateDisplay) {
      closeLauncherAnimation();
    } else {
      WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  // WindowManager の取得
      launcherView.setVisibility(View.INVISIBLE);
      wm.removeViewImmediate(launcherView);
    }

    visibleLauncher = false;


  }

  /**
   * closeLauncherAnimation --- ランチャークローズアニメーション
   */
  private void closeLauncherAnimation() {


    // 回転
    RotateAnimation animationRotate = new RotateAnimation(0.0f, layoutClockwise ? -180.0f : 180.0f, centerX, centerY);
    // 透過度
    AlphaAnimation animationAlpha = new AlphaAnimation(1.0f, 0.0f);
    // アニメーションセット
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.addAnimation(animationRotate);
    animationSet.addAnimation(animationAlpha);
    animationSet.setFillAfter(true);
    animationSet.setInterpolator(new AccelerateInterpolator());
    animationSet.setDuration(TRANSITION_INVISIBLE);
    LayoutAnimationController layoutAC = new LayoutAnimationController(animationSet);
    // ランチャー全体
    flAppsLauncher.setLayoutAnimation(layoutAC);
    flAppsLauncher.setLayoutAnimationListener(new AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }
      @Override
      public void onAnimationEnd(Animation animation) {
        WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);  // WindowManager の取得
        flAppsLauncher.setVisibility(View.INVISIBLE);
        wm.removeViewImmediate(launcherView);
        //        backgroundViewParent.setVisibility(View.INVISIBLE);
        //        wm.removeViewImmediate(backgroundViewParent);
      }
      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });

    // 背景色
    transitionLauncherBg.reverseTransition(TRANSITION_INVISIBLE);

    // アニメーションのスタート
    flAppsLauncher.startLayoutAnimation();
    //    backgroundViewParent.startLayoutAnimation();
  }

  /**
   * setAppsIcons --- ランチャーにアイコンをセット
   */
  private void setAppsIcons() {

    // CircularLayout の初期化
    cl.removeAllViews();

    RevolverDataManager revolverDataManager = new RevolverDataManager(getApplicationContext());

    // serializableRevolverData に保存データをロード
    serializableRevolverData = (SerializableRevolverData)revolverDataManager.load();

    // 小ビュー (アプリアイコン) の配置
    ImageView[] iv = new ImageView[serializableRevolverData.getChildSize(revolverCurrentNum)];  // 配列の初期化

    for (int i = 0; i < serializableRevolverData.getChildSize(revolverCurrentNum); i++) {

      // 新たに ImageView を生成
      iv[i] = new ImageView(getApplicationContext());

      // アイコンを取得
      byte[] b = serializableRevolverData.getBitmapByte(revolverCurrentNum, i);

      if (b != null) {  // もしアイコンがあれば
        // アイコンデータの生成
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        // アイコンをセット
        iv[i].setImageBitmap(tmpBitmap);
        iv[i].setLayoutParams(new ViewGroup.LayoutParams(iconSize, iconSize));

        // ID をセット (onClick 時にどのアプリか判別するため)
        iv[i].setId(i);

        // OnClickListener をセット
        iv[i].setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            v.setOnClickListener(null);
            closeLauncher();
            PackageManager packageManager = getBaseContext().getPackageManager();  // アプリ管理マネージャの取得
            Intent intent = packageManager.getLaunchIntentForPackage(serializableRevolverData.getPackageName(revolverCurrentNum, v.getId()));  // アプリのインテントを取得
            if (intent != null) {
              startActivity(intent);  // アプリの実行
            }
          }
        });
      }

      // CircularLayout に追加
      cl.addView(iv[i]);
    }
  }


  /**
   * changeRevolver --- チェンジャー
   */
  private void changeRevolver() {
    // 入れ替え
    revolverCurrentNum = (revolverCurrentNum + 1) % (new ParameterManager(getApplicationContext())).getRevolverNum();
    civ.setActiveIndex(revolverCurrentNum);
    if (enableAnimation) {
      changeRevolverAnimation();
    } else {
      setAppsIcons();
    }
  }


  /**
   * changeRevolver --- リボルバーを次のものに入れ替える (アニメーション含む)
   */
  private boolean changing = false;
  private void changeRevolverAnimation() {
    changing = true;

    // 回転
    RotateAnimation animationRotate = new RotateAnimation(0.0f, layoutClockwise ? 180.0f : -180.0f, centerX, centerY);
    // 透過度
    // アニメーションセット
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.addAnimation(animationRotate);
    animationSet.setFillAfter(true);
    animationSet.setInterpolator(new AccelerateInterpolator());
    animationSet.setDuration(CHANGE_REVOLVER);
    LayoutAnimationController layoutAC = new LayoutAnimationController(animationSet);
    flCircularLayout.setLayoutAnimation(layoutAC);
    flCircularLayout.setLayoutAnimationListener(new AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }
      @Override
      public void onAnimationEnd(Animation animation) {  // アニメーション終了後
        // アニメーションリスナの初期化
        flCircularLayout.setLayoutAnimationListener(null);

        setAppsIcons();

        // 回転
        RotateAnimation animationRotate = new RotateAnimation(layoutClockwise ? -180.0f : 180.0f, 0.0f, centerX, centerY);
        // アニメーションセット
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(animationRotate);
        animationSet.setFillAfter(true);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setDuration(CHANGE_REVOLVER);
        LayoutAnimationController layoutAC = new LayoutAnimationController(animationSet);
        // ランチャーアニメーション
        flCircularLayout.setVisibility(View.INVISIBLE);
        flCircularLayout.setLayoutAnimation(layoutAC);
        flCircularLayout.setVisibility(View.VISIBLE);
        changing = false;
      }
      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
  }


  Handler handler = null;

  /**
   * makeBackButton --- 戻るボタンの生成
   * @return
   */
  private Button makeBackButton() {
    Button backButton = new Button(getApplicationContext());
    Drawable drawable = getResources().getDrawable(isBlackMode ? R.drawable.icon_back_bk : R.drawable.icon_back_wh);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {  // under API Level 16 (Android 4.1, 4.1.1)
      backButton.setBackgroundDrawable(drawable);
    } else {  // over API Level 16 (Android 4.1, 4.1.1)
      backButton.setBackground(drawable);
    }
    backButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isServiceRunning(getApplicationContext(), SpecialKeyAccessibilityService.class)) {
//          if (!closeLauncherWithBackKey) {
//            v.setOnClickListener(null);
//            closeLauncher();
//          }
//          handler = new Handler();
//          handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//              publishBackKeyEvent();
//            }
//          }, 500);

          publishBackKeyEvent();
          //          if (!closeLauncherWithBackkey) {
          //            handler = new Handler();
          //            handler.postDelayed(new Runnable() {
          //              @Override
          //              public void run() {
          //                startGetBackKeyActivity();
          //              }
          //            }, 1000);
          //          }
        } else {
          v.setOnClickListener(null);
          closeLauncher();
          Intent i = new Intent(getApplicationContext(), BackKeyAlertActivity.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(i);
        }
      }
    });
    return backButton;
  }

  /**
   * makeHomeButton --- ホームボタンの生成
   * @return
   */
  private Button makeHomeButton() {
    Button homeButton = new Button(getApplicationContext());
    Drawable drawable = getResources().getDrawable(isBlackMode ? R.drawable.icon_home_bk : R.drawable.icon_home_wh);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {  // under API Level 16 (Android 4.1, 4.1.1)
      homeButton.setBackgroundDrawable(drawable);
    } else {  // over API Level 16 (Android 4.1, 4.1.1)
      homeButton.setBackground(drawable);
    }
    homeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setOnClickListener(null);
        closeLauncher();
        publishHomeButtonEvent();
      }
    });
    return homeButton;
  }

  /**
   * makeRecentsButton --- 最近使ったアプリボタンの生成
   * @return
   */
  private Button makeRecentsButton() {
    Button recentsButton = new Button(getApplicationContext());
    Drawable drawable = getResources().getDrawable(isBlackMode ? R.drawable.icon_recents_bk : R.drawable.icon_recents_wh);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      recentsButton.setBackgroundDrawable(drawable);
    } else {
      recentsButton.setBackground(drawable);
    }
    recentsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setOnClickListener(null);
        Log.d(TAG, "RecentApps is clicked. (" + closeLauncherWithBackKey + ")");
        if (!closeLauncherWithBackKey) {
          closeLauncher();
        }
        publishOpenRecentAppsEvent();
      }
    });
    return recentsButton;
  }

  /**
   * makeNotificationsButton --- 通知ボタンの生成
   * @return
   */
  private Button makeNotificationsButton() {
    Button notificationsButton = new Button(getApplicationContext());
    Drawable drawable = getResources().getDrawable(isBlackMode ? R.drawable.icon_notifications_bk : R.drawable.icon_notifications_wh);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {  // under API Level 16 (Android 4.1, 4.1.1)
      notificationsButton.setBackgroundDrawable(drawable);
    } else {  // over API Level 16 (Android 4.1, 4.1.1)
      notificationsButton.setBackground(drawable);
    }
    notificationsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        publishOpenNotificationsEvent();
      }
    });
    return notificationsButton;
  }


  /**
   * publishBackKeyEvent --- 戻るボタン
   */
  private Toast toast;
  private void publishBackKeyEvent() {
//    TextView view = new TextView(getApplicationContext());
//    if (toast != null) {
//      toast.cancel();
//    }
//    toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
//    toast.setView(view);
//    toast.show();
    Intent i = new Intent(SpecialKeyAccessibilityService.BACK_KEY_ACTION);
    sendBroadcast(i);
  }

  /**
   * publishHomeButtonEvent --- ホームを開く
   */
  private void publishHomeButtonEvent() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    startActivity(intent);
  }

  /**
   * publishOpenRecentAppsEvent --- 「最近使ったアプリ」を開く
   *  - 参考: http://stackoverflow.com/questions/14267482/android-programmatically-open-recent-apps-dialog
   */
  private void publishOpenRecentAppsEvent() {
    Log.d(TAG, "Open RecentApps");
    try {
      Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");  // android.os.ServiceManager クラスを取得
      Method getService = serviceManagerClass.getMethod("getService", String.class);  // android.os.ServiceManager クラスの getService() メソッドを取得
      IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
      Class<?> statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());  // statusbar のクラスを取得
      Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[] { retbinder });
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
        try {
          Method preloadRecentApps = statusBarClass.getMethod("preloadRecentApps");  // 初期化 (アニメーションがおかしくなる)
          preloadRecentApps.setAccessible(true);
          preloadRecentApps.invoke(statusBarObject);
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        }
      }
      Method toggleRecentApps = statusBarClass.getMethod("toggleRecentApps");  // 発動
      toggleRecentApps.setAccessible(true);
      toggleRecentApps.invoke(statusBarObject);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * publishOpenNotificationsEvent --- 通知領域を開く
   *  - AndroidManifest.xml に要追加: <uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />
   */
  private void publishOpenNotificationsEvent() {
    try {
      Object sbservice = getSystemService("statusbar");
      Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
      Method showsb;
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {  // under API Level 17 (Android 4.2, 4.2.2)
        showsb = statusbarManager.getMethod("expand");
      } else {  // over API Level 17 (Android 4.2, 4.2.2)
        showsb = statusbarManager.getMethod("expandNotificationsPanel");
      }
      showsb.invoke(sbservice);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * サービスが起動中かどうか確認
   * @param serviceName サービス名
   * @return
   */
  private boolean isServiceRunning(Context context, Class<?> cls) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    for (RunningServiceInfo info : services) {
      if (cls.getName().equals(info.service.getClassName())) {
        return true;
      }
    }
    return false;
  }


  //  /**
  //   * showBackkeyAccessibilityEnableDialog --- Back キー有効化ダイアログ
  //   */
  //  private void showBackkeyAccessibilityEnableDialog() {
  //    Resources res = getResources();
  //    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
  //    alertDialogBuilder.setIcon(R.drawable.ic_alert);
  //    alertDialogBuilder.setTitle(res.getText(R.string.app_name));
  //    alertDialogBuilder.setMessage(res.getText(R.string.backkey_alert));
  //    alertDialogBuilder.setPositiveButton(res.getText(R.string.backkey_alert_accessivility_btn_text), new DialogInterface.OnClickListener() {
  //      @Override
  //      public void onClick(DialogInterface dialog, int which) {
  //        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
  //        startActivity(intent);
  //      }
  //    });
  //    alertDialogBuilder.setCancelable(true);
  //    AlertDialog alertDialog = alertDialogBuilder.create();
  //    alertDialog.setCanceledOnTouchOutside(false);
  //    alertDialog.show();
  //  }





  // 広告
  private AdView adView1 = null;
  private String adId1 = "ca-app-pub-1554537676397925/1950452499";

  /**
   * 広告のセットアップ
   * @param activity
   */
  public void setupAds(Context context) {
    /**
     * AdMob (広告)
     *************************/
    // adView を作成する
    adView1 = new AdView(context);
    adView1.setAdUnitId(adId1);
    adView1.setAdSize(AdSize.BANNER);

    // ルックアップ
    FrameLayout.LayoutParams flAdArea1LP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    flAdArea1LP.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    // 追加
    flAppsLauncherItems.addView(adView1, flAdArea1LP);

    // 広告のロード
    adView1.loadAd(new AdRequest.Builder().build());

    pauseAds();
  }

  /**
   * 広告の一時停止 (super.onPause の前)
   */
  public void pauseAds() {
    if (adView1 != null) {
      adView1.pause();
    }
  }

  /**
   * 広告の再開 (super.onResume の後)
   */
  public void resumeAds() {
    if (adView1 != null) {
      adView1.resume();
    }
  }

  /**
   * 広告の破棄 (super.onDestroy の前)
   */
  public void destroyAds() {
    if (adView1 != null) {
      flAppsLauncherItems.removeView(adView1);
      adView1.destroy();
      adView1 = null;
    }
  }

  /**
   * バックキー検知用 Activity を起動 (v1.0.2)
   */
  private void startGetBackKeyActivity() {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());
    if (parameterManager.getActionOfBackkeyStatus() == ParameterManager.BackKeyAction.CLOSE_LAUNCHER) {  // 戻るキーでランチャーを閉じる場合
      Intent intent = new Intent(getBaseContext(), GetBackKeyActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);  // Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY |
      startActivity(intent);
    }
  }

  /**
   * バックキー検知用 Activity を閉じる (v1.0.2)
   */
  private void broadcastActivityFinish() {
    ParameterManager parameterManager = new ParameterManager(getApplicationContext());
    if (parameterManager.getActionOfBackkeyStatus() == ParameterManager.BackKeyAction.CLOSE_LAUNCHER) {  // 戻るキーでランチャーを閉じる場合
      Intent i = new Intent(GetBackKeyActivity.FINISH_ACTION);
      sendBroadcast(i);
    }
  }

}
