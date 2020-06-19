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
import android.util.AttributeSet;
import android.view.View;

/**
 * CurvedIndexView --- インデックス表示用レイアウト
 * @author Tag
 */
public class CurvedIndexView extends View {

  // TAG
//  private final String TAG = this.getClass().getName().toString();
  
  // onDraw() 関連
  private boolean drawCircle = false;  // 円の表示 (テスト用)
  private boolean drawClockwise = true;  // 時計回りでの描画
  
  // 円関係
  private int circleCenterX = 480;  // 中心 (x)
  private int circleCenterY = 480;  // 中心 (y)
  private float circleRadius = 300;   // 半径
  
  // 表示範囲
  private float startAngle = 180.0f;  // 開始角度
  private float sweepAngle = 90.0f;   // レベルバーの角度 (長さ)
  
  // ドット関係
  private float dotRadius = 5.0f;
  private int activeDotColor = 0x000000ff;
  private int noActiveDotColor = activeDotColor & Color.argb(50, 0, 0, 0);
  private int maxIndex = 12;
  private int activeIndex = 0;
  
  
  /**
   * コンストラクタ
   * @param context
   */
  public CurvedIndexView(Context context) {
    super(context);
  }
  /**
   * コンストラクタ
   * @param context
   * @param attrs
   */
  public CurvedIndexView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // 変数の取得
    TypedArray ar = context.obtainStyledAttributes(attrs, R.styleable.CurvedIndexView);
    drawClockwise = ar.getBoolean(R.styleable.CurvedIndexView_drawClockwise, drawClockwise);
    circleCenterX = ar.getInteger(R.styleable.CurvedIndexView_circleCenterX, circleCenterX);
    circleCenterY = ar.getInteger(R.styleable.CurvedIndexView_circleCenterY, circleCenterY);
    circleRadius = ar.getFloat(R.styleable.CurvedIndexView_circleRadius, circleRadius);
    startAngle = ar.getFloat(R.styleable.CurvedIndexView_startAngle, startAngle);
    sweepAngle = ar.getFloat(R.styleable.CurvedIndexView_sweepAngle, sweepAngle);
    dotRadius = ar.getFloat(R.styleable.CurvedIndexView_dotRadius, dotRadius);
    activeDotColor = ar.getColor(R.styleable.CurvedIndexView_activeDotColor, activeDotColor);
    noActiveDotColor = ar.getColor(R.styleable.CurvedIndexView_noActiveDotColor, noActiveDotColor);
    maxIndex = ar.getInteger(R.styleable.CurvedIndexView_maxIndex, maxIndex);
    activeIndex = ar.getInteger(R.styleable.CurvedIndexView_activeIndex, activeIndex);
    ar.recycle();
  }

  /**
   * サイズ計測
   */
  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension((int)circleRadius * 2, (int)circleRadius * 2);

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);  // 最後に呼ぶらしい
  }

  /**
   * レイアウト
   */
  @Override
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
  }

  /**
   * onDraw --- 描画処理
   */
  @Override
  public void onDraw(Canvas canvas) {
//    Log.d(TAG, "onDraw");
    // テスト用の円を描画
    if (drawCircle) {
      Paint paint = new Paint();
      paint.setStrokeWidth(1);             // 線の太さ
      paint.setStyle(Paint.Style.STROKE);  // 線で描画
      paint.setColor(Color.BLACK);         // 色
      paint.setAntiAlias(true);            // アンチエイリアス
      
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
    
    // アクティブなインデックス用 Paint
    Paint activePaint = new Paint();
    activePaint.setStyle(Paint.Style.FILL);  // 内部塗りつぶし
    activePaint.setColor(activeDotColor);    // 色
    activePaint.setAntiAlias(true);          // アンチエイリアス
    
    // 非アクティブなインデックス用 Paint
    Paint noActivePaint = new Paint();
    noActivePaint.setStyle(Paint.Style.FILL);  // 内部塗りつぶし
    noActivePaint.setColor(noActiveDotColor);  // 色
    noActivePaint.setAntiAlias(true);          // アンチエイリアス
    
    double angle = startAngle;  // 角度
    double step = sweepAngle / (maxIndex - 1);  // 間隔
    for (int i = 0; i < maxIndex; i++) {
      float x, y;
      if (drawClockwise) {
        x = (float)(circleRadius * Math.cos(Math.toRadians(angle - 180)) + circleCenterX);
        y = (float)(circleRadius * Math.sin(Math.toRadians(angle - 180)) + circleCenterY);
      } else {
        x = (float)(circleRadius * Math.cos(Math.toRadians(-angle)) + circleCenterX);
        y = (float)(circleRadius * Math.sin(Math.toRadians(-angle)) + circleCenterY);
      }
      if (i == activeIndex) {
        canvas.drawCircle(x, y, dotRadius, activePaint);
      } else {
        canvas.drawCircle(x, y, dotRadius, noActivePaint);
      }
      angle += step;
    }

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
   * 描画方向の設定
   * @param clockwise
   */
  public void setDrawDirection(boolean clockwise) {
    this.drawClockwise = clockwise;
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
   * 半径
   * @param radius
   */
  public void setRadius(float radius) {
    this.circleRadius = radius;
    invalidate();
  }
  
  /**
   * ドット表示場所
   * @param start
   * @param sweep
   */
  public void setViewAngle(float start, float sweep) {
    this.startAngle = start;
    this.sweepAngle = sweep;
    invalidate();
  }

  
  
  /**
   * ドットの半径
   * @param width
   */
  public void setDotRadius(float radius) {
    this.dotRadius = radius;
    invalidate();
  }

  /**
   * アクティブな Index の色
   * @param color
   */
  public void setActiveDotColor(int color) {
    this.activeDotColor = color;
    invalidate();
  }

  /**
   * アクティブでない Index の色
   * @param color
   */
  public void setNoActiveDotColor(int color) {
    this.noActiveDotColor = color;
    invalidate();
  }
  
  /**
   * 表示レベル
   * @param index
   */
  public void setMaxIndex(int index) {
    this.maxIndex = index;
    invalidate();
  }
  
  /**
   * 表示レベル
   * @param index
   */
  public void setActiveIndex(int index) {
    this.activeIndex = index;
    invalidate();
  }



}
