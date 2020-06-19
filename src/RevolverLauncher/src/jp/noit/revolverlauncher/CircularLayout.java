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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * CircularLayout --- 円形に配置するレイアウト
 * @author Tag
 */
public class CircularLayout extends ViewGroup {

  final String TAG = this.getClass().getName().toString();

  final int dispPadding = 20;

  // ViewGroup 関連
  private int layoutViewGroupSize;         // ViewGroup 全体のサイズ
  private int layoutViewGroupRadius;       // 配置線の半径
  private int layoutViewGroupCenterX;      // ViewGroup の中心
  private int layoutViewGroupCenterY;      // ViewGroup の中心
  private float layoutInitialAngle = 90f;  // 初期角度 (アナログ時計の12時の位置)
  //private int layoutNumChildView = 12;     // 子 View の数

  // 円関係
  private boolean drawCircle = false;
  private int circleStrokeWidth = 2;
  private int circleDrawColor = Color.WHITE;
  
  // 子 View 関連
  private int layoutChildViewSize;         // 子 View サイズ
  private int layoutChildViewMargin = 5;   // 子 View のマージン
  private int layoutChildViewRadius;       // 子 View エリアの半径 (マージン込み)

  private boolean layoutCounterclockwise = true;  // 反時計回りに配置するか、時計回りに配置するか

  private int dispWidth;
  private int dispHeight;
  private float dispInch;  // 画面のインチ数
  
  private double insideRadius;   // 内部 (insideRadius = layoutViewGroupRadius - layoutChildViewRadius)
  private double outsideRadius;  // 外部 (outsideRadius = layoutViewGroupRadius + layoutChildViewRadius)

  // 子 View 管理
  private double[][] childPosition;         // x, y, angle

  // GestureDetector --- タッチ処理
  private GestureDetector gestureDetector;

  // WindowManager
  WindowManager wm;

  /*****
   * コンストラクタ
   ***************************************************************************/

  /**
   * CircularLayout --- コンストラクタ
   * @param context
   */

  public CircularLayout(Context context) {
    super(context);
    
    // WindowManager の取得
    wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

    // 画面サイズの取得
    Display dp = wm.getDefaultDisplay();
    Point dispSize = new Point();
    dp.getSize(dispSize);
    dispWidth = dispSize.x;
    dispHeight = dispSize.y;


    // 画面の短辺の長さを取得
    final int dispShorterLength = (dispSize.x < dispSize.y) ? dispSize.x : dispSize.y;

    // 子 View サイズを決める (画面サイズの短辺の 1/5)
    layoutChildViewSize = dispShorterLength / 5;

    // マージン込みの子 View の半径を決定
    layoutChildViewRadius = layoutChildViewSize / 2 + layoutChildViewMargin;

    // ViewGroup のサイズを決定
    layoutViewGroupSize = dispShorterLength - 2 * dispPadding;

    // 配置円の半径を決める
    layoutViewGroupRadius = layoutViewGroupSize / 2 - layoutChildViewRadius;

    // ViewGroup のサイズの半分を中心座標に
    layoutViewGroupCenterX = layoutViewGroupSize / 2;
    layoutViewGroupCenterY = layoutViewGroupCenterX;

    // アプリアイコン表示部分の内側と外側の半径
    insideRadius = layoutViewGroupRadius - layoutChildViewRadius;
    outsideRadius = layoutViewGroupRadius + layoutChildViewRadius;

    // ハンドラーの類を初期化
    flingAnimationHandler = new Handler();
    gestureDetector = new GestureDetector(context, new MyGestureListener());
    
    // 画面サイズの取得
    DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(metrics);
    float widthPx = metrics.widthPixels;    // 高さのピクセル数
    float heightPx = metrics.heightPixels;    // 幅のピクセル数
    float dispXdpi = metrics.xdpi;    // 高さの DPI 数
    float dispYdpi = metrics.ydpi;    // 幅の DPI 数
    float dispXinch = widthPx / dispXdpi;
    float dispYinch = heightPx / dispYdpi;
//    float density = metrics.density;  // 画面密度
    dispInch = (float)Math.sqrt(dispXinch * dispXinch + dispYinch * dispYinch);  // インチ
//    Log.d(TAG, "dispInch: " + dispInch);
  }
  /**
   * CircularLayout --- コンストラクタ (for XML)
   * @param context
   * @param attrs
   */
  @SuppressLint("NewApi")
  public CircularLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    // WindowManager の取得
    wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

    // 画面サイズの取得
    Display dp = wm.getDefaultDisplay();
    Point dispSize = new Point();
    dp.getSize(dispSize);
    dispWidth = dispSize.x;
    dispHeight = dispSize.y;


    // 画面の短辺の長さを取得
    final int dispShorterLength = (dispSize.x < dispSize.y) ? dispSize.x : dispSize.y;

    // 子 View サイズを決める (画面サイズの短辺の 1/5)
    layoutChildViewSize = dispShorterLength / 5;

    // マージン込みの子 View の半径を決定
    layoutChildViewRadius = layoutChildViewSize / 2 + layoutChildViewMargin;


    // ViewGroup のサイズを決定
    layoutViewGroupSize = dispShorterLength - 2 * dispPadding;

    // 配置円の半径を決める
    layoutViewGroupRadius = layoutViewGroupSize / 2 - layoutChildViewRadius;

    // ViewGroup のサイズの半分を中心座標に
    layoutViewGroupCenterX = layoutViewGroupSize / 2;
    layoutViewGroupCenterY = layoutViewGroupCenterX;

    // アプリアイコン表示部分の内側と外側の半径
    insideRadius = layoutViewGroupRadius - layoutChildViewRadius;
    outsideRadius = layoutViewGroupRadius + layoutChildViewRadius;

    // ハンドラーの類を初期化
    flingAnimationHandler = new Handler();
    gestureDetector = new GestureDetector(context, new MyGestureListener());
    
    // 画面サイズの取得
    DisplayMetrics metrics = new DisplayMetrics();
    wm.getDefaultDisplay().getMetrics(metrics);
    float widthPx = metrics.widthPixels;    // 高さのピクセル数
    float heightPx = metrics.heightPixels;    // 幅のピクセル数
    float dispXdpi = metrics.xdpi;    // 高さの DPI 数
    float dispYdpi = metrics.ydpi;    // 幅の DPI 数
    float dispXinch = widthPx / dispXdpi;
    float dispYinch = heightPx / dispYdpi;
//    float density = metrics.density;  // 画面密度
    dispInch = (float)Math.sqrt(dispXinch * dispXinch + dispYinch * dispYinch);  // インチ
//    Log.d(TAG, "dispInch: " + dispInch);
  }

  /**
   * CircularLayout --- コンストラクタ
   * @param context
   * @param attrs
   * @param defStyle
   */
  public CircularLayout(Context context, AttributeSet attrs, int defStyle) {  //
    super(context, attrs, defStyle);
  }

  public static class LayoutParams extends ViewGroup.LayoutParams {
    public LayoutParams(int width, int height) {
      super(width, height);
    }
    public LayoutParams(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }



  /*****
   * ViewGroup のためのメソッド
   *   - onMeasure, onLayout
   ***************************************************************************/

  /**
   * onMeasure --- ビューのサイズ測定
   */
  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    Log.d(TAG, "onMeasure() is running.");

    // 小ビューのサイズ測定
    final int numChild = getChildCount();  // 子ビューの数の取得
    for (int i = 0; i < numChild; i++) {
      final View v = getChildAt(i);  // i 番目の小ビューの取得
      if (v.getVisibility() != GONE) {  // ビューが存在しているなら (VISIBLE or INVISIBLE)
        measureChild(v, widthMeasureSpec, heightMeasureSpec);  // 子ノードに自身のサイズを測定させる (これをしないと子ビューが配置されない)
      }
    }

    // ビューグループのサイズを決める
    setMeasuredDimension(dispWidth, dispHeight);
  }

  /**
   * onLayout --- 子ビューの配置を決める
   */
  @SuppressLint("NewApi")
  @Override
  public void onLayout(boolean c, int l, int t, int r, int b) {
//    Log.d(TAG, "onLayout() is running.");

    // 子ビューの数の取得
    final int numChild = getChildCount();
//    Log.d(TAG, "numChild: " + numChild);

    // 子 View の位置情報を保存するための配列を初期化
    childPosition = new double[numChild][3];

    for (int i = 0; i < numChild; i++) {
      final View v = getChildAt(i);  // 子ビューの取得

      float angle = 360.0f / numChild * i + 180.0f / numChild;  // 回転量 (等間隔表示のために小ビューの数に合わせて角度を計算)

      // 位置の保持
      childPosition[i][0] = layoutViewGroupRadius * Math.cos(Math.toRadians(layoutInitialAngle + angle));
      childPosition[i][1] = layoutViewGroupRadius * Math.sin(Math.toRadians(layoutInitialAngle + angle));
      childPosition[i][2] = angle;

      // 配置位置
      final int x = (int)(layoutCounterclockwise ? (childPosition[i][0] + layoutViewGroupCenterX) : -(childPosition[i][0] + layoutViewGroupCenterX));
      final int y = (int)((-1) * childPosition[i][1]) + layoutViewGroupCenterY;

      // 子ビューサイズの半分を取得
      final int halfChildWidth = v.getMeasuredWidth() / 2;
      final int halfChildHeight = v.getMeasuredHeight() / 2;

      // 子ビューの配置位置
      final int left   = x - halfChildWidth;
      final int top    = y - halfChildHeight;
      final int right  = x + halfChildWidth;
      final int bottom = y + halfChildHeight;

      // アイコンのリサイズ
      /*v.setScaleX(layoutChildViewSize / v.getScaleX());
      v.setScaleY(layoutChildViewSize / v.getScaleY());*/

      // 子ビューの配置
      v.layout(left, top, right, bottom);

      // 回転
      if (layoutCounterclockwise) {
        v.setRotation(90 - (layoutInitialAngle + angle));
      } else {
        v.setRotation((layoutInitialAngle + angle) - 90);
      }
    }

    // 再描画
    invalidate();
  }



  /*****
   * 描画関連
   ***************************************************************************/

  /**
   * dispatchDraw --- 描画処理 (View での onDraw に相当)
   */
  @Override
  protected void dispatchDraw(Canvas canvas) {
//    Log.d(TAG, "dispatchDraw");
    // 円の描画
    if (drawCircle) {
      Paint p = new Paint();
      //circleStrokeWidth = layoutChildViewRadius * 2;
      p.setStrokeWidth(circleStrokeWidth);
      p.setColor(circleDrawColor);
      p.setStyle(Paint.Style.STROKE);  // 塗りつぶしなし
      p.setAntiAlias(true);
//      canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius - layoutChildViewRadius, p);
      canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius, p);
//      canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius + layoutChildViewRadius, p);
    }
    super.dispatchDraw(canvas);
  }


  /*****
   * 回転動作関係メソッド
   ***************************************************************************/

  /**
   * 2ベクトルのなす角
   * @param c0 中心の x 座標
   * @param c1 中心の y 座標
   * @param x0 点 1 の x 座標
   * @param x1 点 1 の y 座標
   * @param y0 点 2 の x 座標
   * @param y1 点 2 の y 座標
   * @return 回転角
   */
  private float calcAngle(double c0, double c1, double x0, double x1, double y0, double y1) {
    if (x0 == y0 && x1 == y1) {  // 移動がなければ何もしないで終了
      return 0f;
    }
    // 2ベクトル
    final double vecX0 = x0 - c0, vecX1 = x1 - c1;
    final double vecY0 = y0 - c0, vecY1 = y1 - c1;
    //Log.d(TAG, "Vec: (" + vecX0 + ", " + vecX1 + "), (" + vecY0 + ", " + vecY1 + ")");

    double norm = Math.sqrt(vecX0 * vecX0 + vecX1 * vecX1) * Math.sqrt(vecY0 * vecY0 + vecY1 * vecY1);
    if (norm == 0) {  // 零割防止
      return 0f;
    }

    // なす角
    final double angle = Math.toDegrees( Math.acos( (vecX0 * vecY0 + vecX1 * vecY1) / norm ) );

    // 外積
    final double crossProduct = vecX0 * vecY1 - vecX1 * vecY0;

    return (crossProduct < 0) ? (float)angle : (-1) * (float)angle;
  }

  private float calcAngle(double x0, double x1, double y0, double y1) {
    return calcAngle(layoutViewGroupCenterX, layoutViewGroupCenterY, x0, x1, y0, y1);
  }

  /**
   * 子ビューを回転する
   * @param angle 回転角
   */
  @SuppressLint("NewApi")
  public void rotateChild(float angle) {

    // 0 度ならば何もする必要がないので終了
    if (angle == 0f) {
      return;
    }

    if (!layoutCounterclockwise) {
      angle *= -1;
    }

    final int numChild = getChildCount();

    for (int i = 0; i < numChild; i++) {
      View v = getChildAt(i);

      // 現在のビュー位置
      final double currentX = childPosition[i][0];
      final double currentY = childPosition[i][1];

      // ビュー位置の保持
      childPosition[i][0] = currentX * Math.cos(Math.toRadians((double)angle)) - currentY * Math.sin(Math.toRadians((double)angle));
      childPosition[i][1] = currentX * Math.sin(Math.toRadians((double)angle)) + currentY * Math.cos(Math.toRadians((double)angle));
      childPosition[i][2] = (childPosition[i][2] + angle) % 360;

      int viewHalfWidth = (v.getRight() - v.getLeft()) / 2;
      int viewHalfHeight = (v.getBottom() - v.getTop()) / 2;

      int dstX = (int) ((layoutCounterclockwise ? 1 : -1) * childPosition[i][0] + layoutViewGroupCenterX);
      int dstY = (int)(-childPosition[i][1]) + layoutViewGroupCenterY;
      //Log.d(TAG, "Layout: " + (dstX - viewHalfWidth) + ", " + (dstY - viewHalfHeight) + ", " + (dstX + viewHalfWidth) + ", " + (dstY + viewHalfHeight));
      v.layout(dstX - viewHalfWidth, dstY - viewHalfHeight, dstX + viewHalfWidth, dstY + viewHalfHeight);
      // 回転
      if (layoutCounterclockwise) {
        v.setRotation(90 - (layoutInitialAngle + (int)childPosition[i][2]));
      } else {
        v.setRotation((layoutInitialAngle + (int)childPosition[i][2]) - 90);
      }
    }
  }

  /**
   * フリックアニメーション
   * @author Tag
   *
   */
  Handler flingAnimationHandler;
  boolean allowFlingAnimation = false;  // フリックアニメーションを作動させるかどうか
  float currentVelocity = 0.0f;
  private class FlingRunnable implements Runnable {
    private float velocity;  // 速度
    FlingRunnable(float velocity) {  // コンストラクタ
      this.velocity = velocity;
    }
    @Override
    public void run() {
      if (Math.abs(velocity) < 10) {
        allowFlingAnimation = false;
      }
      if (allowFlingAnimation) {
        rotateChild(velocity / 360);        // 小ビューの回転
        velocity /= 1.1f;               // 速度を減らす
        currentVelocity = velocity;
        flingAnimationHandler.post(this);  // 再度呼び出す
      }
    }
  }



  /*****
   * パラメータ取得／変更メソッド
   ***************************************************************************/


  /**
   * setRadius --- 小ビュー配置位置の半径の設定
   * @param radius
   */
  public void setRadius(int radius) {
    layoutViewGroupRadius = radius;
    // アプリアイコン表示部分の内側と外側の半径
    insideRadius = layoutViewGroupRadius - layoutChildViewRadius;
    outsideRadius = layoutViewGroupRadius + layoutChildViewRadius;
    invalidate();
  }

  /**
   * 初期角度の更新
   * @param angle
   */
  public void setInitialAngle(float angle) {
    layoutInitialAngle = angle % 360;
    invalidate();
  }

  /**
   * 中心を設定
   * @param x
   * @param y
   */
  public void setCenter(int x, int y) {
    this.layoutViewGroupCenterX = x;
    this.layoutViewGroupCenterY = y;
    invalidate();
  }

  /**
   * 子ビューの大きさを設定
   * @param size
   */
  public void setChildSize(int size) {
    this.layoutChildViewSize = size;
    invalidate();
  }

  /**
   * 小ビューの数を設定
   * @param num
   */
//  public void setChildNum(int num) {
//    this.layoutNumChildView = num;
//    invalidate();
//  }

  /**
   * 反時計回り (true) に小ビューを配置するか、時計回り (false) にするか
   * @param b
   */
  public void setLayoutCounterclockwise(boolean b) {
    this.layoutCounterclockwise = b;
  }

  /**
   * 描画線の太さ
   * @param width
   */
  public void setCircleDrawStrokeWidth(int width) {
    this.circleStrokeWidth = width;
  }
  
  /**
   * 円の描画色
   * @param color
   */
  public void setCircleDrawColor(int color) {
    this.circleDrawColor = color;
  }


  /*****
   * タッチイベント
   *   - dispatchTouchEvent, onInterceptTouchEvent, onTouchEvent
   ***************************************************************************/

  float startX, startY;
  float bufX, bufY;
  float currentX, currentY;
  float endX, endY;
  boolean isDragging = false;  // スクロール状態かどうか
  boolean isTap = false;
  boolean outOfArea = false;
  final float tapMaxDst = 20.0f;

  /**
   * dispatchTouchEvent --- タッチイベントを受け取る
   */
  /*
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    super.dispatchTouchEvent(ev);
    return true;
  }
  */

  
  boolean onInterceptTouchEventReturnValue = false;  // 伝搬
  
  /**
   * onInterceptTouchEvent --- タッチイベントが子ビューに伝搬するのを阻止
   * return true  -> イベントを消費 (onTouchEvent の呼び出し)
   *        false -> 小ビューに伝播 (何もしない)
   */
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
//    Log.d(TAG, "onInterceptTouchEvent()");
    
    if (isTap) {
//      Log.d(TAG, "isTap");
      isTap = false;
      onInterceptTouchEventReturnValue = false;
    }

    onTouchEvent(ev);  // 先に onTouchEvent() を呼ぶ

    if (isDragging) {
//      Log.d(TAG, "isDragging");
      onInterceptTouchEventReturnValue = true;
    }
    
    if (outOfArea) {
//      Log.d(TAG, "outOfArea");
      outOfArea = false;
      onInterceptTouchEventReturnValue = false;
    }
    
//    Log.d(TAG, "onInterceptTouchEventReturnValue: " + onInterceptTouchEventReturnValue);
    return onInterceptTouchEventReturnValue;  // Intercept

  }

  /**
   * onTouchEvent --- ViewGroup のタッチイベント
   * return true  -> イベント終了
   *        false -> 子ビューに伝播
   */
  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    //Log.d(TAG, "onTouchEvent()");

    if (!isDragging) {
      // アプリアイコンの並んでいる部分以外であれば、イベントは処理を行わない
      double dist = Math.pow(ev.getX() - layoutViewGroupCenterX, 2) + Math.pow(ev.getY() - layoutViewGroupCenterY, 2);
      if ( (dist <= Math.pow(insideRadius, 2)) || dist >= Math.pow(outsideRadius, 2) ) {
        outOfArea = true;
        //Log.d(TAG, "Out of Area is clicked");
        return false;
      }
    }

    gestureDetector.onTouchEvent(ev);  // MyGestureListener の呼び出し

    // アクション状態の取得
    final int action = ev.getActionMasked();
    switch (action) {
    case MotionEvent.ACTION_DOWN:  // 押す
//      Log.d(TAG, "ACTION_DOWN");
      // タッチ位置情報の取得
      allowFlingAnimation = false;  // フリックアニメーションを停止
      startX = ev.getX();
      startY = ev.getY();
      bufX = startX;
      bufY = startY;
      endX = startX;
      endY = startY;
      break;

    case MotionEvent.ACTION_UP:  // 離した
      // Log.d(TAG, "ACTION_UP");
      endX = ev.getX();
      endY = ev.getY();
      if (outOfArea) {
        outOfArea = false;
        return false;
      }
      isDragging = false;
      break;

    case MotionEvent.ACTION_MOVE:  // 押したままスライド
      //Log.d(TAG, "ACTION_MOVE");
      if (outOfArea) {
        return false;
      }
      currentX = ev.getX();  currentY = ev.getY();             // 移動前にタッチ位置を取得
      rotateChild(calcAngle(bufX, bufY, currentX, currentY));  // 移動
      bufX = currentX;  bufY = currentY;                       // タッチ位置の保存
//      Log.d(TAG, "isDragging?: " + Math.sqrt( Math.pow(startX - bufX, 2) + Math.pow(startY - bufY, 2) ));
      if ( Math.sqrt( Math.pow(startX - bufX, 2) + Math.pow(startY - bufY, 2) ) > tapMaxDst) {
        isDragging = true;
      }
      break;

    case MotionEvent.ACTION_CANCEL:  // キャンセル
      //Log.d(TAG, "ACTION_CANCEL");
      break;
    }

    return true;
  }


  /**
   * OnGestureListener の実装
   *   onInterceptTouchEvent, onTouchEvent あたりと連動して動きます。
   * @author Tag
   *
   */
  class MyGestureListener implements OnGestureListener {
    /**
     * OnGestureListener 用メソッド
     ***************************************************************************/
    @Override
    public boolean onDown(MotionEvent e) {  // 押した
      //Log.d(TAG, "onDown()");
      return true;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {  // 弾いた
      //Log.d(TAG, "onFling(): " + "v = (" + velocityX + ", " + velocityY + ")");
      float v = (float) Math.sqrt((double)velocityX * velocityX + velocityY * velocityY);  // 速度 (距離)
      float angle = calcAngle(e1.getX(), e1.getY(), e2.getX(), e2.getY());
//      if (angle < 0) {  // 回転方向の決定
//        v *= -1;
//      }
//      Log.d(TAG, "Velocity | angle: " + v + " | " + angle);
      // フリックアニメーションの実行
      if (v > 1000) {
        allowFlingAnimation = true;
        flingAnimationHandler.post(new FlingRunnable( (angle > 0 ? 1 : -1) * (0.5f * v) * (float)dispInch ));  // 回転速度の設定 (0.25f * angle) * 
      }
      return true;
    }
    @Override
    public void onLongPress(MotionEvent e) {  // 長押し
      //Log.d(TAG, "onLongPress()");
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {  // スクロール
      //Log.d(TAG, "onScroll()");
      return true;
    }
    @Override
    public void onShowPress(MotionEvent e) {  // 押した (画面から手は離していない)
      //Log.d(TAG, "onShowPress()");
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {  // シングルタップ (ダブルタップ時も呼び出される)
//    Log.d(TAG, "onSingleTapUp(): " + currentVelocity);
      if (Math.abs(currentVelocity) > 200) {
//        Log.d(TAG, "Stopper");
        currentVelocity = 0.0f;
        onInterceptTouchEventReturnValue = true;
      }
      isTap = true;
      return true;
    }
  }
}
