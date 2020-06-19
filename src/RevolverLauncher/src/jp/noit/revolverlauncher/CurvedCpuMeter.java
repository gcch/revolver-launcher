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


import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;

/**
 * CurvedCpuMeter --- CPUメータ (CurvedBarMeterView を拡張)
 * @author Tag
 *
 */
public class CurvedCpuMeter extends CurvedBarMeterView {

  // Resource 状況保持
  private Handler handler;  // 時刻の更新用

  private boolean monitorStopped = false;

  private int updateCycle = 1000;  // 更新頻度 (単位はミリ秒で、1000 で 1 秒おきに更新)

  /**
   * コンストラクタ ---
   * @param context
   * @param attrs
   */
  public CurvedCpuMeter(Context context) {
    super(context);
  }
  public CurvedCpuMeter(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * onAttachedToWindow --- アクティビティに追加された後に実行される
   */
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    handler = new Handler();
    Runnable r = new UpdateResourceUsageRunnable();  // リソース使用率の取得
    r.run();
  }

  /**
   * onDetachedFromWindow --- アクティビティ終了時の処理
   */
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    monitorStopped = true;
  }

  /**
   * CPU 使用率の取得
   *     http://stackoverflow.com/questions/3118234/how-to-get-memory-usage-and-cpu-usage-in-android
   */
  public float getCpuUsage() {
    try {
      // 読み込み用変数
      RandomAccessFile file;
      String load;
      String[] toks;

      // CPU 使用率情報の取得
      file = new RandomAccessFile("/proc/stat", "r");
      load = file.readLine();
      toks = load.split(" ");  // スペース区切り
      long idle1 = 0;
      long cpu1 = 0;
      for (int i = 2; i < toks.length; i++) {
        /**
         * /proc/stat で取得した情報
         *               0     1  2    3    4      5    6      7   8       9     10    11
         *   toks[12] = ["cpu"|""|user|nice|system|idle|iowait|irq|softirq|steal|guest|guest_nice]
         */
        switch (i) {
        case 5:
          idle1 = Long.parseLong(toks[i]);
          break;
        case 2:
        case 3:
        case 4:
        case 6:
        case 7:
        case 8:
          cpu1 += Long.parseLong(toks[i]);
          break;
        default:
          break;
        }
      }

      // ちょっと待つ
      try {
        Thread.sleep(10);
      } catch (Exception e) {}

      // CPU 使用率情報の取得
      file.seek(0);
      load = file.readLine();
      file.close();
      toks = load.split(" ");

      long idle2 = 0;
      long cpu2 = 0;
      for (int i = 2; i < toks.length; i++) {
        switch (i) {
        case 5:
          idle2 = Long.parseLong(toks[i]);
          break;
        case 2:
        case 3:
        case 4:
        case 6:
        case 7:
        case 8:
          cpu2 += Long.parseLong(toks[i]);
          break;
        default:
          break;
        }
      }

      // 2回の取得情報から、使用率を計算
      float ratio = (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)) * 100;
      //Log.d(TAG, "CPU Usage: " + ratio);
      return ratio;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0f;
  }

  /**
   * UpdateResourceUsageRunnable --- リソース使用率を更新する
   * @author Tag
   *
   */
  private class UpdateResourceUsageRunnable implements Runnable {
    @Override
    public synchronized void run() {
      if (monitorStopped) {
        return;
      }
      setLevel(getCpuUsage());
      invalidate();  // 再描画
      final long now = SystemClock.uptimeMillis();
      final long uptimeMillis = now + (updateCycle - now % updateCycle);
      handler.postAtTime(new UpdateResourceUsageRunnable(), uptimeMillis);
    }
  }

  /**
   * 更新頻度の変更
   * @param millisec 更新周期 (ミリ秒)
   */
  public void setCycle(int millisec) {
    this.updateCycle = millisec;
  }
  
  /**
   * stopMonitor --- 止める
   */
  public void stopMonitor() {
    monitorStopped = true;
  }
}
