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

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * SettingPlaceholderFragment --- ActionBar に ToggleButton を追加
 * @author Tag
 * 参考: http://qiita.com/musicfair/items/c64626905a684711e760
 */
@SuppressLint("ValidFragment")
public class SettingPlaceholderFragment extends Fragment {

  private static final String TAG = "SettingPlaceholderFragment";

  // StarterBar 関連
  private static ToggleButton tb;  // ToggleButton
  private static Context context;
  private static ParameterManager parameterManager;

  public SettingPlaceholderFragment() {
  }

  public SettingPlaceholderFragment(Activity activity) {
    context = activity.getApplicationContext();
    parameterManager = new ParameterManager(context);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);  // メニューが存在していることを設定 (重要)
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  /**
   * onCreateOptionsMenu --- メニューボタンが押されたときに実行
   */
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.actionbar_menu_item, menu);  // メニューのアイテムを取得

    // ToggleButtonのセットアップ
    tb = (ToggleButton)menu.findItem(R.id.toggle_button).getActionView();
    int tbWidth = getResources().getDimensionPixelSize(R.dimen.toggle_button_width);
    int tbHeight = getResources().getDimensionPixelSize(R.dimen.toggle_button_height);
//    Log.d(TAG, "size: " + tbWidth + ":" + tbHeight);
    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(tbWidth, tbHeight);
//    layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
    layoutParams.width = tbWidth;
    layoutParams.height = tbHeight;
//    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    tb.setLayoutParams(layoutParams);
    tb.setWidth(tbWidth);
    tb.setHeight(tbHeight);
//    tb.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
    tb.setBackgroundResource(R.drawable.toggle_background);  // バックグラウンドの設定
    tb.setButtonDrawable(R.drawable.toggle_button);          // ボタンの設定
    tb.setChecked(isServiceRunning(context, AppsLauncherService.class));                       // ボタンの初期状態
    tb.setText("");
    tb.setTextOn("");   // On 時のテキスト
    tb.setTextOff("");  // Off 時のテキスト
    tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Log.d(TAG, "onCheckedChanged");
        if (isChecked) {  // On のとき
          startStarterBarService();  // StarterBar サービスの起動
        } else {  // Off のとき
          stopStarterBarService();  // StarterBar サービスの停止
        }
      }
    });

    if (parameterManager.getStarterBarServiceStatus()) {  // 保存状態を確認し、起動する必要がある (true) ならば
      tb.setChecked(true);
    }

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public void onResume() {
    super.onResume();
    this.getActivity().invalidateOptionsMenu();
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

  /**
   * StarterBar のサービスを起動
   */
  private void startStarterBarService() {
//    Log.d(TAG, "startStarterBarService: " + isServiceRunning(context, AppsLauncherService.class));
    if (!isServiceRunning(context, AppsLauncherService.class)) {  // サービスが既に起動していないかを確認
      parameterManager.putStarterBarServiceStatus(true);
      // StarterBar サービスの起動
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.startService(intent);
    }
  }
  /**
   * StarterBar のサービスを停止
   */
  private void stopStarterBarService() {
//    Log.d(TAG, "stopStarterBarService: " + isServiceRunning(context, AppsLauncherService.class));
    if (isServiceRunning(context, AppsLauncherService.class)) {  // サービスが起動しているか確認
      parameterManager.putStarterBarServiceStatus(false);
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.stopService(intent);
    }
  }
}