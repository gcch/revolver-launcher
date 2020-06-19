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

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;

/**
 * BackKeyAccessibilityService --- 特別なボタンを実装するためのクラス (for Android 4.1 or above)
 * @author Tag
 *
 */
public class SpecialKeyAccessibilityService extends AccessibilityService {

//  private final static String TAG = "SpecialKeyAccessibilityService";

  //  private ParameterManager parameterManager = null;
  //  private WindowManager wm = null;

  //  private View btnSeries, btnBack, btnHome, btnNotifications, btnRecents;
  //  private int btnRadius = 30;

  public final static String BACK_KEY_ACTION = "SpecialKeyAccessibilityService.BACK_KEY";

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();

    // レシーバの登録
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {  // API v16 以降
      IntentFilter filterTrackInfo = new IntentFilter();
      filterTrackInfo.addAction(BACK_KEY_ACTION);
      registerReceiver(mReceiver, filterTrackInfo);
    }
  }

  /**
   * イベント毎の処理
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    //    int eventType = event.getEventType();
    //    String packageName = (String)event.getPackageName();
    //    String className = (String)event.getClassName();
    //    Log.d(TAG, "Event: " + eventType + " by " + className + " in " + packageName);
    //
    //    if (className.equals("android.widget.Toast$TN")) {
    //      Log.d(TAG, "BackKey");
    //      performGlobalAction(GLOBAL_ACTION_BACK);  // バックキー
    //    }
  }

  @Override
  public void onInterrupt() {
  }

  @Override
  public void onDestroy() {

    // レシーバの登録解除
    unregisterReceiver(mReceiver);

    super.onDestroy();
  }

  /**
   * レシーバ
   */
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, final Intent intent) {
//      Log.d(TAG, "Action: " + BACK_KEY_ACTION);
      performGlobalAction(GLOBAL_ACTION_BACK);
    }
  };


  //  /**
  //   * onServiceConnected --- サービス起動後に実行されるメソッド
  //   */
  //  @Override
  //  protected void onServiceConnected() {
  //    super.onServiceConnected();
  //
  //    if (parameterManager == null) {
  //      parameterManager = new ParameterManager(this);
  //    }
  //    if (wm == null) {
  //      wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
  //    }
  //
  //    if (false) {
  //      LayoutInflater inflater = LayoutInflater.from(this);  // LayoutInflater
  //      WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
  //          WindowManager.LayoutParams.WRAP_CONTENT,
  //          WindowManager.LayoutParams.WRAP_CONTENT,
  //          WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
  //          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
  //          | WindowManager.LayoutParams.FLAG_FULLSCREEN          // 全画面
  //          | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR  //
  //          | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS    // ステータスバーに被せる
  //          | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN    //
  //          | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,    // タッチを伝播
  //          PixelFormat.TRANSLUCENT);
  //      layoutParams.x = 0;
  //      layoutParams.y = 0;
  //      btnSeries = inflater.inflate(R.layout.spkey_accessibility_service, null);
  //
  //      FrameLayout.LayoutParams btnLayoutParams = new FrameLayout.LayoutParams(btnRadius * 2, btnRadius * 2);
  //      btnLayoutParams.gravity = Gravity.NO_GRAVITY;
  //      btnLayoutParams.setMargins(0, 0, 0, 0);
  //
  //      // 戻るボタン
  //      btnBack = new Button(this);
  //      btnBack.setBackground(getResources().getDrawable(R.drawable.icon_back_bk));
  //      btnBack.setOnClickListener(new OnClickListener() {
  //        @Override
  //        public void onClick(View v) {
  //          performGlobalAction(GLOBAL_ACTION_BACK);
  //        }
  //      });
  //      ((ViewGroup) btnSeries).addView(btnBack, btnLayoutParams);
  //
  //      // ホームボタン
  //      btnHome = new Button(this);
  //      btnHome.setBackground(getResources().getDrawable(R.drawable.icon_home_bk));
  //      btnHome.setOnClickListener(new OnClickListener() {
  //        @Override
  //        public void onClick(View v) {
  //          performGlobalAction(GLOBAL_ACTION_HOME);
  //        }
  //      });
  //      ((ViewGroup) btnSeries).addView(btnHome, btnLayoutParams);
  //
  //      // 最近使用したアプリ一覧表示
  //      btnRecents = new Button(this);
  //      btnRecents.setBackground(getResources().getDrawable(R.drawable.icon_recents_bk));
  //      btnRecents.setOnClickListener(new OnClickListener() {
  //        @Override
  //        public void onClick(View v) {
  //          performGlobalAction(GLOBAL_ACTION_RECENTS);
  //        }
  //      });
  //      ((ViewGroup) btnSeries).addView(btnRecents, btnLayoutParams);
  //
  //      // 通知エリア表示
  //      btnNotifications = new Button(this);
  //      btnNotifications.setBackground(getResources().getDrawable(R.drawable.icon_notifications_bk));
  //      btnNotifications.setOnClickListener(new OnClickListener() {
  //        @Override
  //        public void onClick(View v) {
  //          performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
  //        }
  //      });
  //      ((ViewGroup) btnSeries).addView(btnNotifications, btnLayoutParams);
  //
  //      wm.addView(btnSeries, layoutParams);
  //    }
  //
  //  }



}
