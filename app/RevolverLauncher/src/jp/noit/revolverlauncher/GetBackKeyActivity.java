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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * バックキー検出用 Activity (v1.0.2)
 * @author Tag
 *
 */
public class GetBackKeyActivity extends FragmentActivity {

  protected static final String TAG = "GetBackKeyActivity";

  public static final String FINISH_ACTION = "jp.noit.revolverlauncher.GetBackKeyActivity.FINISH_ACTION";


  ParameterManager parameterManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("GetBackKeyActivity", "onCreate()");

    parameterManager = new ParameterManager(this);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(FINISH_ACTION);  // レシーバの登録
    intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);  // ホームボタン
    registerReceiver(mReceiver, intentFilter);

    // バインド
    Intent intent = new Intent(this, AppsLauncherService.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

  }

  @Override
  public void onDestroy() {

    // アンバインド
    unbindService(connection);

    // レシーバの解除
    unregisterReceiver(mReceiver);

    super.onDestroy();
  }

  /**
   * 戻るキー
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    appsLauncherService.closeLauncher();  // ランチャーを閉じる
    finish();
  }


  /**
   * bind 関連
   *************************/
  private AppsLauncherService appsLauncherService;

  ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
//      Log.d(TAG, "onServiceConnected()");
      appsLauncherService = ((AppsLauncherService.AppsLauncherBinder)binder).getService();
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
//      Log.d(TAG, "onServiceDisconnected()");
      appsLauncherService = null;
    }
  };



  /**
   * レシーバ
   */
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(FINISH_ACTION)) {  // 終了命令
        finish();
      } else if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  // ホームキーや最近使ったアプリボタンが押された時など
        Log.d(TAG, "ACTION_CLOSE_SYSTEM_DIALOGS");
        appsLauncherService.closeLauncher();  // ランチャーを閉じる

        // 勝手に閉じる動作
//        finish();
      }
    }
  };


}
