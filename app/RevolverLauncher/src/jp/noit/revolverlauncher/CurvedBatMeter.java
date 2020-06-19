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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.AttributeSet;

/**
 * CurvedBatMeter --- バッテリメータ (CurvedBarMeterView を拡張)
 * @author Tag
 *
 */
public class CurvedBatMeter extends CurvedBarMeterView {

  // バッテリー残量関係
  private int batteryScale = 0;
  private int batteryLevel = 0;

  /**
   * コンストラクタ ---
   * @param context
   * @param attrs
   */
  public CurvedBatMeter(Context context) {
    super(context);
    initialize(context);
  }
  public CurvedBatMeter(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  /**
   * 初期化
   * @param context
   */
  private void initialize(Context context) {
    // とりあえず、残量を取得
    Intent bat = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    batteryLevel = bat.getIntExtra("level", 50);
    batteryScale = bat.getIntExtra("scale", 100);
    setLevel((float)batteryLevel / batteryScale * 100);

    // バッテリの変化を検知するサービスの起動
    Intent intent = new Intent(context, BatteryChangedReceiverService.class);
    context.startService(intent);
  }

  /**
   * onAttachedToWindow --- アクティビティに追加された後に実行される
   */
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  /**
   * onDetachedFromWindow --- アクティビティ終了時の処理
   *   - http://boco.hp3200.com/beginner/widget02-3.html
   */
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }


  /**
   * ブロードキャストレシーバ
   */
  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {  // バッテリに変化があったとき
        batteryLevel = intent.getIntExtra("level", 50);
        batteryScale = intent.getIntExtra("scale", 100);
        setLevel((float)batteryLevel / batteryScale * 100);
      }
    }
  };

  /**
   * BatteryChangedReceiverService --- バッテリー変化を検知するためのサービス
   * @author Tag
   *
   */
  private class BatteryChangedReceiverService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
      // インテントフィルタの設定
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
      registerReceiver(receiver, intentFilter);  // レシーバの登録
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      unregisterReceiver(receiver);  // レシーバの登録解除
    }
  }

}
