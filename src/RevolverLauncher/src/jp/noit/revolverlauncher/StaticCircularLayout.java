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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * CircularLayout --- 円形に配置するレイアウト
 * @author Tag
 */
public class StaticCircularLayout extends ViewGroup {

//  final String TAG = this.getClass().getName().toString();

  final int dispPadding = 20;

  // ViewGroup 関連
//  private int layoutViewGroupSize;         // ViewGroup 全体のサイズ
  private int layoutViewGroupRadius;       // 配置線の半径
  private int layoutViewGroupCenterX;      // ViewGroup の中心
  private int layoutViewGroupCenterY;      // ViewGroup の中心
  private float layoutInitialAngle = 90f;  // 初期角度 (アナログ時計の12時の位置)
  //private int layoutNumChildView = 12;     // 子 View の数
  
  // 円描画
  private int strokeWidth = 2;
  private int strokeColor = Color.LTGRAY;

  // 子 View 関連
  private int layoutChildViewSize;         // 子 View サイズ
//  private int layoutChildViewMargin = 5;   // 子 View のマージン
//  private int layoutChildViewRadius = 0;       // 子 View エリアの半径 (マージン込み)

  private boolean layoutCounterclockwise = false;  // 反時計回りに配置するか、時計回りに配置するか

//  private int dispWidth;
//  private int dispHeight;

  // 子 View 管理
  private double[][] childPosition;         // x, y, angle

  // WindowManager
//  WindowManager wm;

  /*****
   * コンストラクタ
   ***************************************************************************/

  /**
   * CircularLayout --- コンストラクタ
   * @param context
   */

  public StaticCircularLayout(Context context) {
    super(context);
  }
  /**
   * CircularLayout --- コンストラクタ (for XML)
   * @param context
   * @param attrs
   */
  @SuppressLint("NewApi")
  public StaticCircularLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * CircularLayout --- コンストラクタ
   * @param context
   * @param attrs
   * @param defStyle
   */
  public StaticCircularLayout(Context context, AttributeSet attrs, int defStyle) {  //
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
//    Log.d(TAG, "onMeasure()");

    // 小ビューのサイズ測定
    final int numChild = getChildCount();  // 子ビューの数の取得
    for (int i = 0; i < numChild; i++) {
      final View v = getChildAt(i);  // i 番目の小ビューの取得
      if (v.getVisibility() != GONE) {  // ビューが存在しているなら (VISIBLE or INVISIBLE)
        measureChild(v, widthMeasureSpec, heightMeasureSpec);  // 子ノードに自身のサイズを測定させる (これをしないと子ビューが配置されない)
      }
    }
    
    // 子のサイズを取得
    int childWidth = getChildAt(0).getWidth();
    int childHeight = getChildAt(0).getHeight();
//    Log.d(TAG, "childSize: " + childWidth + ":" + childHeight);
    double childDiagonalLength = (childWidth > 0 && childHeight > 0) ? Math.sqrt(childWidth * childWidth + childHeight * childHeight) : Math.sqrt(2) * layoutChildViewSize;  // 対角線の長さ
    
    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
//    Log.d(TAG, "measuredSize: " + measuredWidth + ":" + measuredHeight);
    
    // 中心座標・半径の設定
    layoutViewGroupCenterX = measuredWidth / 2;
    layoutViewGroupCenterY = measuredHeight / 2;
    layoutViewGroupRadius = (measuredWidth < measuredHeight ? measuredWidth / 2 / 5 * 3 : measuredHeight) / 2 - (int)childDiagonalLength / 2;
    
    // ビューグループのサイズを決める
    setMeasuredDimension(measuredWidth, measuredHeight);
  }

  /**
   * onLayout --- 子ビューの配置を決める
   */
  @SuppressLint("NewApi")
  @Override
  public void onLayout(boolean c, int l, int t, int r, int b) {
//    Log.d(TAG, "onLayout()");

    // 子ビューの数の取得
    final int numChild = getChildCount();
//    Log.d(TAG, "numChild: " + numChild);

    // 子 View の位置情報を保存するための配列を初期化
    childPosition = new double[numChild][3];

    for (int i = 0; i < numChild; i++) {
      final View v = getChildAt(i);  // 子ビューの取得

      float angle = 360.0f / numChild * i;  // 回転量 (等間隔表示のために小ビューの数に合わせて角度を計算)

      // 位置の保持
      childPosition[i][0] = layoutViewGroupRadius * Math.cos(Math.toRadians(layoutInitialAngle + angle));
      childPosition[i][1] = layoutViewGroupRadius * Math.sin(Math.toRadians(layoutInitialAngle + angle));
      childPosition[i][2] = angle;

      // 配置位置
      final int x = (int)(layoutCounterclockwise ? (childPosition[i][0] + layoutViewGroupCenterX) : (-childPosition[i][0] + layoutViewGroupCenterX));
      final int y = (int)((-1) * childPosition[i][1]) + layoutViewGroupCenterY;

      // 子ビューサイズの半分を取得
      final int halfChildWidth = v.getMeasuredWidth() / 2;
      final int halfChildHeight = v.getMeasuredHeight() / 2;

      // 子ビューの配置位置
      final int left   = x - halfChildWidth;
      final int top    = y - halfChildHeight;
      final int right  = x + halfChildWidth;
      final int bottom = y + halfChildHeight;

      // 子ビューの配置
      v.layout(left, top, right, bottom);

//      // 回転
//      if (layoutCounterclockwise) {
//        v.setRotation(90 - (layoutInitialAngle + angle));
//      } else {
//        v.setRotation((layoutInitialAngle + angle) - 90);
//      }
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
    Paint p = new Paint();
    p.setStrokeWidth(strokeWidth);
    p.setColor(strokeColor);
    p.setStyle(Paint.Style.STROKE);  // 塗りつぶしなし
    p.setAntiAlias(true);
//    canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius - layoutChildViewRadius, p);
    canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius, p);
//    canvas.drawCircle(layoutViewGroupCenterX, layoutViewGroupCenterY, layoutViewGroupRadius + layoutChildViewRadius, p);
    super.dispatchDraw(canvas);
  }


  /*****
   * パラメータ取得／変更メソッド
   ***************************************************************************/


//  /**
//   * setRadius --- 小ビュー配置位置の半径の設定
//   * @param radius
//   */
//  public void setRadius(int radius) {
//    layoutViewGroupRadius = radius;
//    invalidate();
//  }

  /**
   * 初期角度の更新
   * @param angle
   */
  public void setInitialAngle(float angle) {
    layoutInitialAngle = angle % 360;
    invalidate();
  }

//  /**
//   * 中心を設定
//   * @param x
//   * @param y
//   */
//  public void setCenter(int x, int y) {
//    this.layoutViewGroupCenterX = x;
//    this.layoutViewGroupCenterY = y;
//    invalidate();
//  }

  /**
   * 子ビューの大きさを設定
   * @param size
   */
  public void setChildSize(int size) {
    this.layoutChildViewSize = size;
    invalidate();
  }

  /**
   * 反時計回り (true) に小ビューを配置するか、時計回り (false) にするか
   * @param b
   */
  public void setLayoutCounterclockwise(boolean b) {
    this.layoutCounterclockwise = b;
  }

}
