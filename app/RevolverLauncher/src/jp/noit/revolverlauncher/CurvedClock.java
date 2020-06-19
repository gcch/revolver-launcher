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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * CurveClock --- 時計
 *   - /res/values/attrs.xml もセットで。
 * @author Tag
 *
 */
public class CurvedClock extends View {

  final String TAG = "CurvedClock";

  // カレンダー関係
  private Handler ticker;  // 時刻の更新用
  private boolean tickerStopped = false;

  // フォーマット関係
  private String dateFormat = "EEEE, MMM dd, yyyy. hh:mm a";
  private String currentDate = dateFormat;

  // 描画関係
  private int circleCenterX = 480;
  private int circleCenterY = 480;
  private float circleRadius = 300;

  private int vPathOffset = 0;  // パスとのオフセット (円の中心方向がプラス)
  private int hPathOffset = 0;  // 文字の開始位置 (円の右が0度。ただ、360度以降は切れるので、弄るの非推奨。)

  // 文字色
  private int textColor = 0x00000000;

  // フォントサイズ
  private float textSize = 50;

  // フォントタイプ (defalt: SANS_SERIF)
  private Typeface typeface = Typeface.SANS_SERIF;
  
  // 始点角度
  private float textStartPointAngle = 0;

  // 描画オフセット
  private float textOffset = 12;

  // onDraw() 関連
  private boolean drawCircle = false;

  /**
   * コンストラクタ
   * @param context
   */
  public CurvedClock(Context context) {
    super(context);
  }
  public CurvedClock(Context context, AttributeSet attrs) {
    super(context, attrs);
    // Get attributes
    TypedArray ar = context.obtainStyledAttributes(attrs, R.styleable.CurvedClock);
    circleCenterX = ar.getInteger(R.styleable.CurvedClock_center_x, 480);
    circleCenterY = ar.getInteger(R.styleable.CurvedClock_center_y, 480);
    circleRadius = ar.getInteger(R.styleable.CurvedClock_circle_radius, 300);
    textOffset = ar.getInteger(R.styleable.CurvedClock_text_offset, 12);
    textStartPointAngle = ar.getFloat(R.styleable.CurvedClock_text_startangle, 180);
    textColor = ar.getColor(R.styleable.CurvedClock_text_color, Color.BLACK);
    textSize = ar.getInteger(R.styleable.CurvedClock_text_size, 25);
    dateFormat = ar.getString(R.styleable.CurvedClock_date_format);
    drawCircle = ar.getBoolean(R.styleable.CurvedClock_draw_circle, false);
    ar.recycle();
  }

  /**
   * onAttachedToWindow --- アクティビティに追加された後に実行される
   */
  @Override
  protected void onAttachedToWindow() {
    tickerStopped = false;
    ticker = new Handler();  // 初期化
    super.onAttachedToWindow();
    Runnable r = new UpdateCalendarRunnable();  // 秒を再描画するため
    r.run();
  }

  /**
   * onDetachedFromWindow --- 除去時に呼ばれる
   */
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    tickerStopped = true;
  }
  
  /**
   * onDraw --- 描画処理
   */
  @Override
  public void onDraw(Canvas canvas) {
    Paint paint = new Paint();
    // 描画設定
    paint.setFlags(Paint.ANTI_ALIAS_FLAG);  // アンチエイリアス
    paint.setStyle(Paint.Style.STROKE);
    // 描画パス
    Path path = new Path();
    path.addCircle(circleCenterX, circleCenterY, circleRadius, Path.Direction.CW);
    if (drawCircle) {
      canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, paint);
      for (int i = 0; i < 360; i++) {
        if ((i % 10) == 0) {
          paint.setColor(Color.BLACK);
        } else {
          paint.setColor(Color.GRAY);
        }
        canvas.drawLine((float)circleCenterX, (float)circleCenterY, (float)(circleRadius * Math.cos(Math.toRadians(i)) + circleCenterX), (float)(circleRadius * Math.sin(Math.toRadians(i)) + circleCenterY), paint);
      }
    }
    // Canvas の状態保存
    canvas.save();

    // 時刻の描画
    canvas.rotate(textStartPointAngle, circleCenterX, circleCenterY);                    // Canvas の回転
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setColor(textColor);              // 色の指定
    paint.setTextSize(textSize);                                                         // フォントサイズの指定
    paint.setTypeface(typeface);
    canvas.drawTextOnPath(currentDate, path, hPathOffset, vPathOffset - textOffset, paint);  // パスに沿って描画
    canvas.restore();                                                                    // 角度の復元

    super.onDraw(canvas);
  }

  /**
   * UpdateCalendarRunnable --- 時刻を更新する
   * @author Tag
   *
   */
  private class UpdateCalendarRunnable implements Runnable {
    @Override
    public void run() {
      if (tickerStopped) {
        return;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());  // 現在時刻の取得
      Date date = calendar.getTime();
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
      currentDate = sdf.format(date);  // 日付
      invalidate();  // 再描画
      final long now = SystemClock.uptimeMillis();
      final long uptimeMillis = now + (1000 - now % 1000);
      ticker.postAtTime(new UpdateCalendarRunnable(), uptimeMillis);
    }
  }
  
  /**
   * stopTicker --- 止める
   */
  public void stopTicker() {
    tickerStopped = true;
  }

  /**
   * テキスト色
   * @param color
   */
  public void setTextColor(int color) {
    this.textColor = color;
    invalidate();
  }

  /**
   * 表示開始角度
   * @param start
   * @param sweep
   */
  public void setTextStartPointAngle(float start) {
    this.textStartPointAngle = start;
    invalidate();
  }

  /**
   * 中心の設定
   * @param x
   * @param y
   */
  public void setCenter(int x, int y) {
    this.circleCenterX = x;
    this.circleCenterY = y;
    invalidate();
  }

  /**
   * 円の半径
   * @param radius
   */
  public void setRadius(float radius) {
    this.circleRadius = radius;
    invalidate();
  }

  /**
   * 時計の表示フォーマットの設定
   * @param format
   */
  public void setDateFormat(String format) {
    this.dateFormat = format;
    invalidate();
  }

  /**
   * 文字サイズの設定
   * @param size
   */
  public void setTextSize(float size) {
    this.textSize = size;
    invalidate();
  }

  /**
   * テキストの垂直方向オフセットの設定 (半径で弄っても OK)
   */
  public void setTextOffset(float f) {
    this.textOffset = f;
    invalidate();
  }
}
