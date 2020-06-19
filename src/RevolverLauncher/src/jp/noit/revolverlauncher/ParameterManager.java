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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * ParameterManager --- 設定画面のパラメータ値を一元管理します。
 * @author Tag
 *
 */
public class ParameterManager {

  protected static final String TAG = "Revolver Launcher";

  // SharedPreferences
  private SharedPreferences sharedPreferences = null;

  // 画面関連
  private WindowManager wm;
  private Display disp;
  private Point p;

  // コンテキスト
  private Context context;

  /**
   * コンストラクタ
   * @param context
   */
  public ParameterManager(Context context) {
    this.context = context;
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);       // WindowManager の取得

    // 初期値の設定
    initParams();

    wm = null;
  }

  /**
   * initParams --- 初期値を設定
   */
  public void initParams() {
    disp = wm.getDefaultDisplay();
    int shortSideLength = 0;
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {  // API 12 以前
      int dispWidth = disp.getWidth();
      int dispHeight = disp.getHeight();
      shortSideLength = dispWidth < dispHeight ? dispWidth : dispHeight;
    } else {  // API 13 以降
      p = new Point();
      disp.getSize(p);
      shortSideLength = p.x < p.y ? p.x : p.y;
    }

    // 初期値
    iconSize = ((int)shortSideLength / 5 / 10) * 10 ;       // アイコンサイズの初期値
    revolverRadius = ((int)shortSideLength / 3 / 10) * 10;  // リボルバーの半径
    starterBarWidthLeft = ((int)shortSideLength / 50);      // 左バー幅の初期値
    starterBarWidthRight = ((int)shortSideLength / 50);     // 右バー幅の初期値

    if (!getEnableClockAndDateCustomize()) {
      resetClockAndDateTextSizeAndBaseline();
    }
    
    
    // 初回起動かチェック
    isFirstTime();


  }


  /**
   * isFirstTime --- 初回起動か否か (今は、用途なし……)
   * @return
   */
  private boolean isFirstTime() {

    if (sharedPreferences.getBoolean("is1st", true)){
      Log.i("Revolver Launcher", "Thank you for choosing and using this app!");

      Editor editor = sharedPreferences.edit();
      editor.putBoolean("is1st", false);
      editor.commit();
      return true;
    } else {
      return false;
    }
  }


  
  /**
   * アプリのエディション
   **************************************************/
  private final boolean premiumEdition = false;
  private final String keyPremiumEdition = "kPremEd";
  /**
   * changeBootServiceStatus --- アプリのエディションの切り替え
   * @param n
   */
  public void putEdition(boolean b) {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyPremiumEdition, b);
    editor.commit();
  }
  /**
   * getBootServiceStatus --- アプリのエディションの取得
   * @return
   */
  public boolean getEdition() {
    return sharedPreferences.getBoolean(keyPremiumEdition, premiumEdition);
  }
  
  

  /**
   * 弾の数
   **************************************************/
  private final int bulletNum = 6;
  private final String keyBulletNum = "bNum";
  /**
   * putBulletNum --- 弾数の保存
   * @param n
   */
  public void putBulletNum(int n) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyBulletNum, n);
    editor.commit();

    RevolverDataManager revolverDataManager = new RevolverDataManager(context);  // RevolverDataManager の初期化
    revolverDataManager.changeBulletNum(n);  // RevolverDataManager のデータ数を変更
  }
  /**
   * getBulletNum --- 弾数の取得
   * @return
   */
  public int getBulletNum() {
    return sharedPreferences.getInt(keyBulletNum, bulletNum);
  }

  /**
   * アイコンサイズ
   **************************************************/
  private int iconSize = 100;
  private final String keyIconSize = "iSize";
  /**
   * putBulletNum --- アイコンサイズの保存
   * @param n
   */
  public void putIconSize(int n) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyIconSize, n);
    editor.commit();
  }
  /**
   * getIconSize --- アイコンサイズの取得
   * @return
   */
  public int getIconSize() {
    return sharedPreferences.getInt(keyIconSize, iconSize);
  }

  /**
   * 連装数
   **************************************************/
  private final int revolverNum = 2;
  private final String keyRevolverNum = "rNum";
  /**
   * putRevolverNum --- 連装数の保存
   * @param n
   */
  public void putRevolverNum(int n) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyRevolverNum, n);
    editor.commit();
  }
  /**
   * getRevolverNum --- 連装数の取得
   * @return
   */
  public int getRevolverNum() {
    return sharedPreferences.getInt(keyRevolverNum, revolverNum);
  }


  /**
   * リボルバーの半径
   **************************************************/
  private int revolverRadius = 200;
  private final String keyRevolverRadius = "rRadius";
  /**
   * putRevolverRadius --- リボルバーの半径の保存
   * @param n
   */
  public void putRevolverRadius(int n) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyRevolverRadius, n);
    editor.commit();
  }
  /**
   * getRevolverRadius --- リボルバーの半径の取得
   * @return
   */
  public int getRevolverRadius() {
    return sharedPreferences.getInt(keyRevolverRadius, revolverRadius);
  }


  /**
   * StarterBar サービスの状態
   **************************************************/
  private final boolean runStarterBarService = true;
  private final String keyRunStarterBarService = "runStBarSvc";
  /**
   * changeStarterBarServiceStatus --- StarterBar の状態
   * @param n
   */
  public void putStarterBarServiceStatus(boolean b) {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyRunStarterBarService, b);
    editor.commit();
  }
  /**
   * getStarterBarServiceStatus --- StarterBar の状態の取得
   * @return
   */
  public boolean getStarterBarServiceStatus() {
    return sharedPreferences.getBoolean(keyRunStarterBarService, runStarterBarService);
  }


  /**
   * ブートサービス状態
   **************************************************/
  private final boolean bootService = true;
  private final String keyBootService = "bootSvc";
  /**
   * changeBootServiceStatus --- ブートサービス状態の切り替え
   * @param n
   */
  public void changeBootServiceStatus() {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyBootService, !getBootServiceStatus());
    editor.commit();
  }
  /**
   * getBootServiceStatus --- ブートサービス状態の取得
   * @return
   */
  public boolean getBootServiceStatus() {
    return sharedPreferences.getBoolean(keyBootService, bootService);
  }

  /**
   * アニメーションの有効化状態
   **************************************************/
  private final boolean enableAnimation = false;
  private final String keyEnableAnimation = "enableAnm";
  /**
   * changeEnableAnimationStatus --- アニメーションの有効化状態の切り替え
   * @param n
   */
  public void changeEnableAnimationStatus() {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableAnimation, !getEnableAnimationStatus());
    editor.commit();
  }
  /**
   * getEnableAnimationStatus --- アニメーションの有効化状態の取得
   * @return
   */
  public boolean getEnableAnimationStatus() {
    return sharedPreferences.getBoolean(keyEnableAnimation, enableAnimation);
  }




  //  /**
  //   * 設定画面への遷移
  //   **************************************************/
  //  private final boolean disableTransitSettingMenu = false;
  //  private final String keyDisableTransitSettingMenu = "transitMenu";
  //  /**
  //   * changeTransitSettingMenu --- 切り替え
  //   * @param n
  //   */
  //  public void changeTransitSettingMenu() {
  //    Editor editor = sharedPreferences.edit();
  //    editor.putBoolean(keyDisableTransitSettingMenu, !getTransitSettingMenuStatus());
  //    editor.commit();
  //  }
  //  /**
  //   * getTransitSettingMenu --- 取得
  //   * @return
  //   */
  //  public boolean getTransitSettingMenuStatus() {
  //    return sharedPreferences.getBoolean(keyDisableTransitSettingMenu, disableTransitSettingMenu);
  //  }


  /**
   * スターターバー関連
   **************************************************/

  // 初期値: スターターバーの色
  private final int starterBarColorAlpha = 150;
  private final int starterBarColorRed   = 51;
  private final int starterBarColorGreen = 181;
  private final int starterBarColorBlue  = 229;
  // キー: スターターバーの色
  private final String keyStarterBarColorAlpha = "sbA";
  private final String keyStarterBarColorRed   = "sbR";
  private final String keyStarterBarColorGreen = "sbG";
  private final String keyStarterBarColorBlue  = "sbB";
  /**
   * putStarterBarColor --- StarterBar の色を保存
   * @param a
   * @param r
   * @param g
   * @param b
   */
  public void putStarterBarColor(int a, int r, int g, int b) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarColorAlpha, a);
    editor.putInt(keyStarterBarColorRed, r);
    editor.putInt(keyStarterBarColorGreen, g);
    editor.putInt(keyStarterBarColorBlue, b);
    editor.commit();
  }
  /**
   * getStarterBarColor --- StarterBar の Color (ARGB) 値を取得
   * @return Color
   */
  public int getStarterBarColor() {
    return Color.argb(getStarterBarColorAlpha(), getStarterBarColorRed(), getStarterBarColorGreen(), getStarterBarColorBlue());
  }
  /**
   * getStarterBarColorAlpha --- StarterBar のアルファ値を取得
   * @return
   */
  public int getStarterBarColorAlpha() {
    return sharedPreferences.getInt(keyStarterBarColorAlpha, starterBarColorAlpha);
  }
  /**
   * getStarterBarColorRed --- StarterBar の赤値を取得
   * @return
   */
  public int getStarterBarColorRed() {
    return sharedPreferences.getInt(keyStarterBarColorRed, starterBarColorRed);
  }
  /**
   * getStarterBarColorGreen --- StarterBar の緑値を取得
   * @return
   */
  public int getStarterBarColorGreen() {
    return sharedPreferences.getInt(keyStarterBarColorGreen, starterBarColorGreen);
  }
  /**
   * getStarterBarColorGreen --- StarterBar の緑値を取得
   * @return
   */
  public int getStarterBarColorBlue() {
    return sharedPreferences.getInt(keyStarterBarColorBlue, starterBarColorBlue);
  }

  /**
   *  スターターバーの状態
   *************************/
  private final boolean enableStarterBarLeft = true;
  private final String keyEnableStarterBarLeft = "starterBarL";
  private final boolean enableStarterBarRight = true;
  private final String keyEnableStarterBarRight = "starterBarR";
  /**
   * putStatusStarterBarLeft --- 切り替え
   * @param n
   */
  public void putStatusStarterBarLeft(boolean b) {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableStarterBarLeft, b);
    editor.commit();
  }
  /**
   * getStatusStarterBarLeft --- 取得
   * @return
   */
  public boolean getStatusStarterBarLeft() {
    return sharedPreferences.getBoolean(keyEnableStarterBarLeft, enableStarterBarLeft);
  }
  /**
   * putStatusStarterBarRight --- 切り替え
   * @param n
   */
  public void putStatusStarterBarRight(boolean b) {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableStarterBarRight, b);
    editor.commit();
  }
  /**
   * getStatusStarterBarRight --- 取得
   * @return
   */
  public boolean getStatusStarterBarRight() {
    return sharedPreferences.getBoolean(keyEnableStarterBarRight, enableStarterBarRight);
  }

  /**
   * スターターバーの幅
   *************************/
  // 初期値: スターターバーの幅
  private int starterBarWidthLeft     = 15;
  private int starterBarWidthRight    = 15;
  // キー: スターターバーの幅
  private final String keyStarterBarWidthLeft     = "sbWL";
  private final String keyStarterBarWidthRight    = "sbWR";
  /**
   * putStarterBarWidthLeft --- 左側スターターバーの幅の設定
   * @param val
   */
  public void putStarterBarWidthLeft(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarWidthLeft, val);
    editor.commit();
  }
  /**
   * getStarterBarWidthLeft --- 左側スターターバーの幅の取得
   * @return
   */
  public int getStarterBarWidthLeft() {
    return sharedPreferences.getInt(keyStarterBarWidthLeft, starterBarWidthLeft);
  }
  /**
   * putStarterBarWidthRight --- 右側スターターバーの幅の設定
   * @param val
   */
  public void putStarterBarWidthRight(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarWidthRight, val);
    editor.commit();
  }
  /**
   * getStarterBarWidthRight --- 右側スターターバーの幅の取得
   * @return
   */
  public int getStarterBarWidthRight() {
    return sharedPreferences.getInt(keyStarterBarWidthRight, starterBarWidthRight);
  }

  /**
   * スターターバーの高さ
   *************************/
  // 初期値: スターターバーの高さ (画面の高さ比)
  private final int starterBarHeightLeft    = 40;
  private final int starterBarHeightRight   = 40;
  // キー: スターターバーの高さ
  private final String keyStarterBarHeightLeft    = "sbHL";
  private final String keyStarterBarHeightRight   = "sbHR";
  /**
   * putStarterBarHeightLeft --- 左側スターターバーの高さの設定
   * @param val
   */
  public void putStarterBarHeightLeft(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarHeightLeft, val);
    editor.commit();
  }
  /**
   * getStarterBarHeightLeft --- 左側スターターバーの高さの取得
   * @return
   */
  public int getStarterBarHeightLeft() {
    return sharedPreferences.getInt(keyStarterBarHeightLeft, starterBarHeightLeft);
  }
  /**
   * putStarterBarHeightRight --- 右側スターターバーの高さの設定
   * @param val
   */
  public void putStarterBarHeightRight(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarHeightRight, val);
    editor.commit();
  }
  /**
   * getStarterBarHeightRight --- 右側スターターバーの高さの取得
   * @return
   */
  public int getStarterBarHeightRight() {
    return sharedPreferences.getInt(keyStarterBarHeightRight, starterBarHeightRight);
  }

  /**
   * スターターバーの位置
   *************************/
  // 初期値: スターターバーの位置 (画面上端からの位置、パーセント表示)
  private final int starterBarPositionLeft  = 40;
  private final int starterBarPositionRight = 40;
  // キー: スターターバーの位置 (画面上端からの位置、パーセント表示)
  private final String keyStarterBarPositionLeft  = "sbPL";
  private final String keyStarterBarPositionRight = "sbPR";
  /**
   * putStarterBarPositionLeft --- 左側スターターバーの位置の設定
   * @param val
   */
  public void putStarterBarPositionLeft(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarPositionLeft, val);
    editor.commit();
  }
  /**
   * getStarterBarPositionLeft --- 左側スターターバーの位置の取得
   * @return
   */
  public int getStarterBarPositionLeft() {
    return sharedPreferences.getInt(keyStarterBarPositionLeft, starterBarPositionLeft);
  }
  /**
   * putStarterBarPositionRight --- 右側スターターバーの位置の設定
   * @param val
   */
  public void putStarterBarPositionRight(int val) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyStarterBarPositionRight, val);
    editor.commit();
  }
  /**
   * getStarterBarPositionRight --- 右側スターターバーの位置の取得
   * @return
   */
  public int getStarterBarPositionRight() {
    return sharedPreferences.getInt(keyStarterBarPositionRight, starterBarPositionRight);
  }


  /**
   * ランチャー関連
   **************************************************/

  // 初期値: ランチャーの背景色
  private final int launcherBgColorAlpha = 200;
  private final int launcherBgColorRed   = 255;
  private final int launcherBgColorGreen = 255;
  private final int launcherBgColorBlue  = 255;
  // キー: ランチャーの背景色
  private final String keyLauncherBgColorAlpha = "lBgA";
  private final String keyLauncherBgColorRed   = "lBgR";
  private final String keyLauncherBgColorGreen = "lBgG";
  private final String keyLauncherBgColorBlue  = "lBgB";
  /**
   * putLauncherBgColor --- Launcher 背景色を保存
   * @param a
   * @param r
   * @param g
   * @param b
   */
  public void putLauncherBgColor(int a, int r, int g, int b) {
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyLauncherBgColorAlpha, a);
    editor.putInt(keyLauncherBgColorRed, r);
    editor.putInt(keyLauncherBgColorGreen, g);
    editor.putInt(keyLauncherBgColorBlue, b);
    editor.commit();
  }
  /**
   * getLauncherBgBarColor --- LauncherBg の Color (ARGB) 値を取得
   * @return Color
   */
  public int getLauncherBgBarColor() {
    return Color.argb(getLauncherBgColorAlpha(), getLauncherBgColorRed(), getLauncherBgColorGreen(), getLauncherBgColorBlue());
  }
  /**
   * getLauncherBgColorAlpha --- LauncherBg のアルファ値を取得
   * @return
   */
  public int getLauncherBgColorAlpha() {
    return sharedPreferences.getInt(keyLauncherBgColorAlpha, launcherBgColorAlpha);
  }
  /**
   * getLauncherBgColorRed --- LauncherBg の赤値を取得
   * @return
   */
  public int getLauncherBgColorRed() {
    return sharedPreferences.getInt(keyLauncherBgColorRed, launcherBgColorRed);
  }
  /**
   * getLauncherBgColorGreen --- LauncherBg の緑値を取得
   * @return
   */
  public int getLauncherBgColorGreen() {
    return sharedPreferences.getInt(keyLauncherBgColorGreen, launcherBgColorGreen);
  }
  /**
   * getLauncherBgColorGreen --- LauncherBg の緑値を取得
   * @return
   */
  public int getLauncherBgColorBlue() {
    return sharedPreferences.getInt(keyLauncherBgColorBlue, launcherBgColorBlue);
  }


  /**
   * 特殊キー関連
   **************************************************/
  private final boolean enableBack = (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) ? false : true;
  private final boolean enableHome = true;
  private final boolean enableRecentApps = true;
  private final boolean enableNotifications = true;
  private final String keyEnableBack = "spKeyBack";
  private final String keyEnableHome = "spKeyHome";
  private final String keyEnableRecentApps = "spKeyRecents";
  private final String keyEnableNotifications = "spKeyNotifications";
  /**
   * changeSpKeyBackStatus --- Back キーの有効状態の変更
   * @param n
   */
  public void changeSpKeyBackStatus() {
    boolean b = true;
    if (getSpKeyBackStatus()) {
      b = false;
    }
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableBack, b);
    editor.commit();
  }
  /**
   * getSpKeyBackStatus --- Back キーの有効状態の取得
   * @return
   */
  public boolean getSpKeyBackStatus() {
    return sharedPreferences.getBoolean(keyEnableBack, enableBack);
  }
  /**
   * changeSpKeyHomeStatus --- Home キーの有効状態の変更
   * @param n
   */
  public void changeSpKeyHomeStatus() {
    boolean b = true;
    if (getSpKeyHomeStatus()) {
      b = false;
    }
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableHome, b);
    editor.commit();
  }
  /**
   * getSpKeyHomeStatus --- Home キーの有効状態の取得
   * @return
   */
  public boolean getSpKeyHomeStatus() {
    return sharedPreferences.getBoolean(keyEnableHome, enableHome);
  }
  /**
   * changeSpKeyRecentsStatus --- RecentApps キーの有効状態の変更
   * @param n
   */
  public void changeSpKeyRecentAppsStatus() {
    boolean b = true;
    if (getSpKeyRecentAppsStatus()) {
      b = false;
    }
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableRecentApps, b);
    editor.commit();
  }
  /**
   * getSpKeyRecentsStatus --- RecentApps キーの有効状態の取得
   * @return
   */
  public boolean getSpKeyRecentAppsStatus() {
    return sharedPreferences.getBoolean(keyEnableRecentApps, enableRecentApps);
  }
  /**
   * changeSpKeyNotificationsStatus --- Notifications キーの有効状態の変更
   * @param n
   */
  public void changeSpKeyNotificationsStatus() {
    boolean b = true;
    if (getSpKeyNotificationsStatus()) {
      b = false;
    }
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableNotifications, b);
    editor.commit();
  }
  /**
   * getSpKeyNotificationsStatus --- Notifications キーの有効状態の取得
   * @return
   */
  public boolean getSpKeyNotificationsStatus() {
    return sharedPreferences.getBoolean(keyEnableNotifications, enableNotifications);
  }

  /**
   * バックキー時にランチャーを閉じるか
   **************************************************/
//  private final boolean closeLauncherWithBackKeyOnLauncher = false;
//  private final String keyCloseLauncherWithBackKeyOnLauncher = "clsLchWBk";
//  /**
//   * changeCloseLauncherWithBackkeyStatus --- 切り替え
//   * @param n
//   */
//  public void changeCloseLauncherWithBackKeyOnLauncherStatus() {
//    Editor editor = sharedPreferences.edit();
//    editor.putBoolean(keyCloseLauncherWithBackKeyOnLauncher, !getCloseLauncherWithBackKeyOnLauncherStatus());
//    editor.commit();
//  }
//  /**
//   * getCloseLauncherWithBackKeyOnLauncherStatus --- 取得
//   * @return
//   */
//  public boolean getCloseLauncherWithBackKeyOnLauncherStatus() {
//    return sharedPreferences.getBoolean(keyCloseLauncherWithBackKeyOnLauncher, closeLauncherWithBackKeyOnLauncher);
//  }


  /**
   * アクセサリの色
   **************************************************/
  private final boolean accessoriesColorIsBlack = true;
  private final String keyAccessoriesColorIsBlack = "acsBk";
  /**
   * changeAccessoriesColorIsBlack --- 切り替え
   * @param n
   */
  public void changeAccessoriesColorIsBlack() {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyAccessoriesColorIsBlack, !getAccessoriesColorIsBlack());
    editor.commit();
  }
  /**
   * getAccessoriesColorIsBlack --- 取得
   * @return
   */
  public boolean getAccessoriesColorIsBlack() {
    return sharedPreferences.getBoolean(keyAccessoriesColorIsBlack, accessoriesColorIsBlack);
  }


  /**
   * 時計の有効化
   **************************************************/
  private final boolean enableClock = true;
  private final String keyEnableClock = "enbClk";
  /**
   * changeClockStatus --- 切り替え
   * @param n
   */
  public void changeClockStatus() {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableClock, !getClockStatus());
    editor.commit();
  }
  /**
   * getClockStatus --- 取得
   * @return
   */
  public boolean getClockStatus() {
    return sharedPreferences.getBoolean(keyEnableClock, enableClock);
  }


  /**
   * リソースメータの有効化
   **************************************************/
  private final boolean enableResMeter = true;
  private final String keyEnableResMeter = "enbResM";
  /**
   * changeResMeterStatus --- 切り替え
   * @param n
   */
  public void changeResMeterStatus() {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableResMeter, !getResMeterStatus());
    editor.commit();
  }
  /**
   * getResMeterStatus --- 取得
   * @return
   */
  public boolean getResMeterStatus() {
    return sharedPreferences.getBoolean(keyEnableResMeter, enableResMeter);
  }

  /**
   * 端末の戻るキーを押したときにランチャーを閉じる
   */
//  private final boolean enableCloseLauncherWithBackKeyOnDevice = true;
//  private final String keyCloseLauncherWithBackKeyOnDevice = "clsLnchrWbkInDvc";
//  /**
//   * putCloseLauncherWithBackKeyOnDeviceStatus --- 切り替え
//   * @param n
//   */
//  public void putCloseLauncherWithBackKeyOnDeviceStatus(boolean b) {
//    Editor editor = sharedPreferences.edit();
//    editor.putBoolean(keyCloseLauncherWithBackKeyOnDevice, b);
//    editor.commit();
//  }
//  /**
//   * getCloseLauncherWithBackKeyOnDeviceStatus --- 取得
//   * @return
//   */
//  public boolean getCloseLauncherWithBackKeyOnDeviceStatus() {
//    return sharedPreferences.getBoolean(keyCloseLauncherWithBackKeyOnDevice, enableCloseLauncherWithBackKeyOnDevice);
//  }
  
  
  /**
   * バックキー時にランチャーを閉じるか
   **************************************************/
  enum BackKeyAction {
    CLOSE_LAUNCHER(0, R.string.setting_action_of_backkey_close_launcher),
    BACK(1, R.string.setting_action_of_backkey_back);
    
    private int idx;
    private int strResId;
    BackKeyAction(int idx, int strResId) {
      this.idx = idx;
      this.strResId = strResId;
    }
    public int getIndex() {
      return idx;
    }
    public int getResId() {
      return strResId;
    }
  }
  private final BackKeyAction actionOfBackkey = BackKeyAction.CLOSE_LAUNCHER;
  private final String keyActionOfBackkey = "keyActOfbk";
  /**
   * changeActionOfBackkeyStatus --- 切り替え
   * @param n
   */
  public BackKeyAction changeActionOfBackkeyStatus() {
    BackKeyAction action = getActionOfBackkeyStatus();
    if (action == BackKeyAction.CLOSE_LAUNCHER) {
      action = BackKeyAction.BACK;
    } else if (action == BackKeyAction.BACK) {
      action = BackKeyAction.CLOSE_LAUNCHER;
    }
    Editor editor = sharedPreferences.edit();
    editor.putInt(keyActionOfBackkey, action.getIndex());
    editor.commit();
    return action;
  }
  /**
   * getActionOfBackkeyStatus --- 取得
   * @return
   */
  public BackKeyAction getActionOfBackkeyStatus() {
    int actionIdx = sharedPreferences.getInt(keyActionOfBackkey, actionOfBackkey.getIndex());
    return BackKeyAction.values()[actionIdx];
  }
  
  
  /**
   * 時計のカスタマイズ有効化
   */
  private final boolean enableEnableClockAndDateCustomize = false;
  private final String keyEnableClockAndDateCustomize = "enblCandDcustom";
  /**
   * putEnableClockAndDateCustomize --- 切り替え
   * @param n
   */
  public void putEnableClockAndDateCustomize(boolean b) {
    Editor editor = sharedPreferences.edit();
    editor.putBoolean(keyEnableClockAndDateCustomize, b);
    editor.commit();
  }
  /**
   * getEnableClockAndDateCustomize --- 取得
   * @return
   */
  public boolean getEnableClockAndDateCustomize() {
    return sharedPreferences.getBoolean(keyEnableClockAndDateCustomize, enableEnableClockAndDateCustomize);
  }
  
  /**
   * 時計ベースライン
   **************************************************/
  private final float valClockBaseline = 2;
  private final String keyClockBaseline = "keyClockBaseline";
  /**
   * putClockBaseline --- 保存
   * @param n
   */
  public void putClockBaseline(float n) {
    Editor editor = sharedPreferences.edit();
    editor.putFloat(keyClockBaseline, n);
    editor.commit();
  }
  /**
   * getClockBaseline --- 取得
   * @return
   */
  public float getClockBaseline() {
    return sharedPreferences.getFloat(keyClockBaseline, valClockBaseline);
  }
  
  /**
   * 時計フォントサイズ
   **************************************************/
  private final float valClockFontSize = 2;
  private final String keyClockFontSize = "keyClockFontSize";
  /**
   * putClockFontSize --- 保存
   * @param n
   */
  public void putClockFontSize(float n) {
    Editor editor = sharedPreferences.edit();
    editor.putFloat(keyClockFontSize, n);
    editor.commit();
  }
  /**
   * getClockFontSize --- 取得
   * @return
   */
  public float getClockFontSize() {
    return sharedPreferences.getFloat(keyClockFontSize, valClockFontSize);
  }
  
  /**
   * 日付ベースライン
   **************************************************/
  private final float valDateBaseline = 2;
  private final String keyDateBaseline = "keyDateBaseline";
  /**
   * putDateBaseline --- 保存
   * @param n
   */
  public void putDateBaseline(float n) {
    Editor editor = sharedPreferences.edit();
    editor.putFloat(keyDateBaseline, n);
    editor.commit();
  }
  /**
   * getDateBaseline --- 取得
   * @return
   */
  public float getDateBaseline() {
    return sharedPreferences.getFloat(keyDateBaseline, valDateBaseline);
  }
  
  /**
   * 日付フォントサイズ
   **************************************************/
  private final float valDateFontSize = 2;
  private final String keyDateFontSize = "keyDateFontSize";
  /**
   * putDateFontSize --- 保存
   * @param n
   */
  public void putDateFontSize(float n) {
    Editor editor = sharedPreferences.edit();
    editor.putFloat(keyDateFontSize, n);
    editor.commit();
  }
  /**
   * getDateFontSize --- 取得
   * @return
   */
  public float getDateFontSize() {
    return sharedPreferences.getFloat(keyDateFontSize, valDateFontSize);
  }

  /**
   * テキスト周りの初期化
   */
  public void resetClockAndDateTextSizeAndBaseline() {
    int launcherRadius = getRevolverRadius();
    int aroundLauncherRadius = launcherRadius + getIconSize() / 2 + launcherRadius / 10;  // テキストのベースラインの半径
    int sizeunit = aroundLauncherRadius / 10;
    
    float clockFontSize = 0.9f * sizeunit;
    float clockBaseline = 2.5f * sizeunit;
    float dateFontSize = 2.7f * sizeunit;
    float dateBaseline = 0.0f * sizeunit;
    
    putClockBaseline(clockBaseline);
    putClockFontSize(clockFontSize);
    putDateBaseline(dateBaseline);
    putDateFontSize(dateFontSize);
  }
}
