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
 * CurvedMemMeter --- メモリメータ (CurvedBarMeterView を拡張)
 * @author Tag
 *
 */
public class CurvedMemMeter extends CurvedBarMeterView {

  // Resource 状況保持
  private Handler handler;  // 時刻の更新用

  private boolean monitorStopped = false;

  private int updateCycle = 10000;  // 更新頻度 (単位はミリ秒で、1000 で 1 秒おきに更新)

  /**
   * コンストラクタ ---
   * @param context
   * @param attrs
   */
  public CurvedMemMeter(Context context) {
    super(context);
  }
  public CurvedMemMeter(Context context, AttributeSet attrs) {
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
   * メモリ使用率の取得 --- 物理メモリ量とアクティブなメモリ量から使用率を計算します。
   *   - /proc/meminfo で取得した情報を使っています。
   *   - 色々と試してみましたが、Android の [設定] - [アプリ] - [実行中] で見られるものに一番近かった Active の値を利用しています。
   *     また、Inactive の値が [キャッシュしたプロセス] の容量に近い気がします。
   * 参考
   *   http://blog.shonanshachu.com/2012/11/android_27.html
   *   http://d.hatena.ne.jp/enakai00/20110906/1315315488
   *   http://goungoun.dip.jp/app/fswiki/wiki.cgi/devnotebook?page=Linux%A1%A2%C9%E9%B2%D9%A4%DE%A4%EF%A4%EA%A4%CE%CF%C3
   *   http://www.atmarkit.co.jp/ait/articles/0903/25/news131.html
   * @return
   */
  private float getMemoryUsage() {
    float memTotal = 0f;  // システム全体の物理メモリ容量
    float active = 0f;    // 最近アクセスした物理メモリ容量

    try {
      // 読み込み用変数
      RandomAccessFile file;
      String load;

      // CPU 使用率情報の取得
      file = new RandomAccessFile("/proc/meminfo", "r");
      while ((load = file.readLine()) != null) {
        if (load.contains("MemTotal:")) {
          memTotal = Float.parseFloat(load.replaceAll("\\D+", ""));
        } else if (load.contains("Active:")) {
          active = Float.parseFloat(load.replaceAll("\\D+", ""));
        }
      }
      file.close();
    } catch (IOException e) {}
    float ratio = active / memTotal * 100;         // メモリ使用容量
    //Log.d(TAG, "[RAM (MB)] Total: " + memTotal/1024 + " Used: " + active/1024 + " (" + ratio + "%)");
    return ratio;
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
      setLevel(getMemoryUsage());
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
