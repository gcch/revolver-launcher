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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * カーブしたバー状のメーターを提供します。(大元)
 *   - /res/values/attrs.xml も必要。
 * @author Tag
 *
 */
public class CurvedBarMeterView extends View {

  final String TAG = this.getClass().getName().toString();

  // 描画関係
  private int circleCenterX = 480;  // 中心 (x)
  private int circleCenterY = 480;  // 中心 (y)
  private float circleRadius = 300;   // 半径

  // onDraw() 関連
  private boolean drawCircle = false;  // 円の表示 (テスト用)

  // メータ関係
  private float barWidth = 5.0f;                       // バーの太さ
  private int barColor = 0x000000ff;                   // バーの色
  private int barBackColor = barColor & Color.argb(50, 0, 0, 0);  // バーの背景色

  // メータの表示範囲
  private float startAngle = 180.0f;  // 開始角度
  private float sweepAngle = 90.0f;   // レベルバーの角度 (長さ)

  private float barLevel = 50.0f;

  // 文字
  private String text = "CurvedBarMeterView";
  private int textColor = 0x00ffffff;
  private int textSize = 5;
  private float textOffset = 0;
  private float textStartPointAngle = 1.5f;
  private int vPathOffset = 0;  // パスとのオフセット (円の中心方向がプラス)
  private int hPathOffset = 0;  // 文字の開始位置 (円の右が0度。ただ、360度以降は切れるので、弄るの非推奨。)
  
  /**
   * コンストラクタ ---
   * @param context
   * @param attrs
   */
  public CurvedBarMeterView(Context context) {
    super(context);


  }
  public CurvedBarMeterView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // 変数の取得
    TypedArray ar = context.obtainStyledAttributes(attrs, R.styleable.CurvedBarMeterView);
    circleCenterX = ar.getInteger(R.styleable.CurvedBarMeterView_circle_center_x, 480);
    circleCenterY = ar.getInteger(R.styleable.CurvedBarMeterView_circle_center_y, 480);
    circleRadius = ar.getFloat(R.styleable.CurvedBarMeterView_circle_radius, 300);
    startAngle = ar.getFloat(R.styleable.CurvedBarMeterView_bar_start_angle, 180.0f);
    sweepAngle = ar.getFloat(R.styleable.CurvedBarMeterView_bar_sweep_angle, 90.0f);
    barWidth = ar.getFloat(R.styleable.CurvedBarMeterView_bar_width, 5.0f);
    barColor = ar.getColor(R.styleable.CurvedBarMeterView_bar_color, Color.BLACK);
    barBackColor = ar.getColor(R.styleable.CurvedBarMeterView_bar_back_color, barColor & Color.argb(50, 0, 0, 0));
    text = ar.getString(R.styleable.CurvedBarMeterView_label_text);
    textOffset = ar.getFloat(R.styleable.CurvedBarMeterView_label_text_offset, textOffset);
    textStartPointAngle = ar.getFloat(R.styleable.CurvedBarMeterView_label_text_startangle, textStartPointAngle);
    textColor = ar.getColor(R.styleable.CurvedBarMeterView_label_text_color, textColor);
    textSize = ar.getInteger(R.styleable.CurvedBarMeterView_label_text_size, textSize);
    ar.recycle();
  }


  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension((int)circleRadius * 2, (int)circleRadius * 2);

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);  // 最後に呼ぶらしい
  }

  @Override
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
  }

  /**
   * onDraw --- 描画処理
   */
  @Override
  public void onDraw(Canvas canvas) {
    
    Paint paint = new Paint();
    RectF oval = new RectF(circleCenterX - circleRadius, circleCenterY - circleRadius, circleCenterX + circleRadius, circleCenterY + circleRadius);
    
    // 描画設定
    paint.setFlags(Paint.ANTI_ALIAS_FLAG);  // アンチエイリアス
    paint.setStyle(Paint.Style.STROKE);     // 線で描画

    if (drawCircle) {
      paint.setStrokeWidth(1);              // 線の太さ
      paint.setColor(Color.BLACK);
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

    // バーの背景部分の描画
    paint.setStrokeWidth(barWidth);
    paint.setColor(barBackColor);
    canvas.drawArc(oval, startAngle, sweepAngle, false, paint);

    // バーのレベルメータ部分の描画
    paint.setStrokeWidth(barWidth);
    paint.setColor(barColor);
    canvas.drawArc(oval, startAngle, sweepAngle * barLevel / 100, false, paint);

    canvas.save();
    
    // ラベル
    canvas.rotate(startAngle + textStartPointAngle, circleCenterX, circleCenterY);  // Canvas の回転
    paint.setStrokeWidth(1);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setColor(textColor);              // 色の指定
    paint.setTextSize(textSize);       // フォントサイズの指定
    Path path = new Path();
    path.addCircle(circleCenterX, circleCenterY, circleRadius, Path.Direction.CW);
    vPathOffset = textSize * 1 / 3;
    canvas.drawTextOnPath(text, path, hPathOffset, vPathOffset - textOffset, paint);  // パスに沿って描画
    canvas.restore();
    
    super.onDraw(canvas);
  }

  /**
   * onAttachedToWindow --- アクティビティに追加された後に実行される
   */
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }


  /**
   * バーの幅
   * @param width
   */
  public void setBarWidth(float width) {
    this.barWidth = width;
    invalidate();
  }

  /**
   * バーの色
   * @param color
   */
  public void setBarColor(int color) {
    this.barColor = color;
    invalidate();
  }

  /**
   * バーの背景色
   * @param color
   */
  public void setBarBackColor(int color) {
    this.barBackColor = color;
    invalidate();
  }

  /**
   * 表示レベル
   * @param lv
   */
  public void setLevel(float lv) {
    this.barLevel = lv;
    invalidate();
  }

  /**
   * バー表示場所
   * @param start
   * @param sweep
   */
  public void setBarAngle(float start, float sweep) {
    this.startAngle = start;
    this.sweepAngle = sweep;
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
   * 弧の半径
   * @param radius
   */
  public void setRadius(float radius) {
    this.circleRadius = radius;
    invalidate();
  }

  /**
   * ラベルの設定
   */
  public void setLabel(String str, int size, int color) {
    this.text = str;
    this.textSize = size;
    this.textColor = color;
    invalidate();
  }
}
