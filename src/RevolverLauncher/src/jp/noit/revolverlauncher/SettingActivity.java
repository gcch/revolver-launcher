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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import jp.noit.revolverlauncher.util.billing.IabHelper;
import jp.noit.revolverlauncher.util.billing.IabResult;
import jp.noit.revolverlauncher.util.billing.Inventory;
import jp.noit.revolverlauncher.util.billing.Purchase;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils.TruncateAt;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.amazon.device.iap.PurchasingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * 設定画面 (リボルバーにアプリを登録)
 *
 * @author naka
 */
public class SettingActivity extends Activity {
  // クラス名 (Log 用)
  final String TAG = this.getClass().getName().toString();

  final boolean DragFromPage = true;

  final boolean DragFromRevolver = false;

  // RevolverDataManager
  private RevolverDataManager revolverDataManager = null;

  // ParameterManager
  ParameterManager parameterManager = null;

  // リボルバーデータを入れる変数
  private SerializableRevolverData revolverData;

  /* リボルバーを並べるレイアウトの親レイアウトリスト(含まれるレイアウトの個数=弾を並べるページの数) */
  private ArrayList<StaticCircularLayout> mRevolverParentLayoutList = new ArrayList<StaticCircularLayout>();

  /* リボルバー内の弾の情報リストのリスト */
  private ArrayList<ArrayList<String>> mBulletInfoListInRevolverList = new ArrayList<ArrayList<String>>();

  /* リボルバー内の弾のImageViewリストのリスト */
  private ArrayList<ArrayList<ImageView>> mBulletImageListInRevolverList = new ArrayList<ArrayList<ImageView>>();

  /* 一覧表示で弾を並べるレイアウトの親レイアウト */
  private LinearLayout mScrollParentLayout;

  /* 一覧表示で弾を並べるレイアウトリスト */
  // private ArrayList<LinearLayout> mScrollLayoutList = new
  // ArrayList<LinearLayout>();

  /* 弾を並べるレイアウトリストの個数(行数=縦に何個並べるか) */
  private int mScrollLayoutListNum;

  /* レイアウトリストに並べる弾の個数の最大値 (列数=横に何個並べるか) */
  private int mMaxBulletNumInScrollLayoutList = 0;

  /* 弾のレイアウトリスト(中のレイアウトの個数=弾数) */
  private ArrayList<LinearLayout> mBulletLayoutList = new ArrayList<LinearLayout>();

  /* 一覧表示ページ内の弾のイメージリスト(中のImageViewの個数=弾数) */
  private ArrayList<ImageView> mBulletImageListInPage = new ArrayList<ImageView>();

  /* 一覧表示ページに表示する弾の名前リスト(中のTextViewの個数=弾数) */
  private ArrayList<TextView> mBulletNameListInPage = new ArrayList<TextView>();

  /* 一覧表示ページに並べる弾の個数 */
  private int mBulletNumInPage = 0;

  /* 一覧表示ページに並べる弾画像の大きさ */
  private int mScrollBulletSize = 150;

  /* ドラッグしているImageViewのIDを入れる変数 */
  private int mNowBulletID = 0;

  /* 表示しているリボルバーページのページ番号 */
  private int mPageNumInRevolver = 0;

  /* インストールしているアプリの情報を取得するのに用いるマネージャー */
  private PackageManager packageManager = null;

  /* 取得したアプリ情報を保持するリスト */
  private List<_AppInfo> mAppDataList = new ArrayList<_AppInfo>();

  /* リボルバーに入れる弾画像の大きさ */
  private int mBulletImageSizeInRevolver;

  /* タッチした位置を保存 */
  /*
   * private float mTouchX = 0; private float mTouchY = 0;
   */

  /* ドラッグしているリボルバー画像のID */
  private int ClickedViewID = -1;

  /* ページ移動用のボタン */
  // private Button mFirstRevolverButton, mLastRevolverButton;
  private ImageView mPreviousRevolverButton, mForrwingRevolverButton;
  /* 連装数，弾数のレイアウト */
  private LinearLayout mRevolverNumLayout, mBulletNumLayout;
  /* ページ番号，連装数，弾数のテキストビュー */
  private TextView mRevolverPageNumText, mRevolverNumText, mBulletNumText;

  private FrameLayout frameLayoutBin;

  // ライセンス別変数上限
  private final int MAX_NUM_BULLET_FOR_FREE = 12;
  private final int MAX_NUM_BULLET_FOR_PREMIUM = 24;
  private final int MAX_NUM_REVOLVER_FOR_FREE = 4;
  private final int MAX_NUM_REVOLVER_FOR_PREMIUM = 12;

  // ページをめくるためアダプター
  private RevolverViewPager mRevolverViewPager;

  private FragmentTransaction fragmentTransaction;
  private LayoutInflater layoutInflater;

  // タブ周り
  private String TAG_TAB_REVOLVER = "tagTabRevolver";
  private String TAG_TAB_SETTINGS = "tagTabSettings";
  private String KEY_CURRENT_TAB_TAG = "currentTabTag";
  private TabHost tabHost;
  private TabWidget tabWidget;
  private View indicator;

  // 画面の横サイズおよび画面方向
  private int shortSide;
  private boolean isLandscape = true;

  private Activity activity;
  private SettingParams settingParams = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    activity = this;

    // ParameterManager の初期化
    parameterManager = new ParameterManager(getBaseContext());

    setContentView(R.layout.settings);

    // AppsLauncherStarterOverlayService のスイッチを ActionBar に表示
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
      if (savedInstanceState == null) {
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(new SettingPlaceholderFragment(activity), "action_bar");
        fragmentTransaction.commit();
      }
    }

    // タブホスト
    tabHost = (TabHost) findViewById(R.id.tabhost);
    tabHost.setup();

    tabWidget = (TabWidget) findViewById(android.R.id.tabs);

    /*
     * Material Design タブ (for Android 5.x) 参考:
     * http://y-anz-m.blogspot.jp/2014
     * /12/androidmaterial-design-tabhost-viewpager.html
     */
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      FrameLayout flTabMaster = (FrameLayout) findViewById(R.id.framelayout_tab_master);
      flTabMaster.setTranslationZ(0); // レイヤの高さ (これをしないと影がでない)
      flTabMaster.setBackgroundColor(getResources().getInteger(
          R.color.material_design_colorPrimary)); // タブ色
      flTabMaster.setElevation(4 * getResources().getDisplayMetrics().density); // 影の表示
      // (4dp)
      // (tabWidget
      // の色がないと何も出ない)
      tabWidget.setStripEnabled(false); // タブ間の区切り線を消す
      tabWidget.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE); // タブ間の区切り線を消す
      getActionBar().setElevation(0); // ActionBar 下の影を消す
    }

    LayoutInflater inflater = LayoutInflater.from(this);

    // タブ: REVOLVER
    TabSpec tabRevolver = tabHost.newTabSpec(TAG_TAB_REVOLVER);
    // テキストのスタイルを変更
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      tabRevolver.setIndicator(getString(R.string.tab_item_revolver));
    } else {
      TextView tvRevolver = (TextView) inflater.inflate(R.layout.material_design_tab_widget,
          tabWidget, false);
      tvRevolver.setText(getString(R.string.tab_item_revolver));
      tabRevolver.setIndicator(tvRevolver);
    }
    tabRevolver.setContent(R.id.contentlayout_tab_revolver);
    tabHost.addTab(tabRevolver);

    // タブ: SETTINGS
    TabSpec tabSettings = tabHost.newTabSpec(TAG_TAB_SETTINGS);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      tabSettings.setIndicator(getString(R.string.tab_item_settings));
    } else {
      TextView tvSettings = (TextView) inflater.inflate(R.layout.material_design_tab_widget,
          tabWidget, false);
      tvSettings.setText(getString(R.string.tab_item_settings));
      tabSettings.setIndicator(tvSettings);
    }
    tabSettings.setContent(R.id.contentlayout_tab_settings);
    tabHost.addTab(tabSettings);

    // Tab indicator
    indicator = findViewById(R.id.tabindicator);
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      // API v20 以前 - 不要なので非表示に
      indicator.setVisibility(View.GONE);
      indicator = null;
    } else {
      TypedValue val = new TypedValue();
      getTheme().resolveAttribute(android.R.attr.colorControlActivated, val, true);
      indicator.setBackgroundResource(val.resourceId);
    }

    tabHost.setOnTabChangedListener(new OnTabChangeListener() { // タブが切り替わったときのリスナ
      @Override
      public void onTabChanged(String tabId) {
        // タブタップ時のインジケータの変化
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
          int position = 0;
          if (tabId == TAG_TAB_REVOLVER) {
            position = 0;
          }
          if (tabId == TAG_TAB_SETTINGS) {
            position = 1;
          }
          updateTabIndicatorPosition(position);
        }

        if (!PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("kPremEd",
            false)) { // 通常版
          setupInterstitialAd(); // 広告準備
          showTransitAds(); // 広告表示
        }

      }
    });

    // 初めにフォーカスを与えるタブ
    if (savedInstanceState == null) {
      tabHost.setCurrentTabByTag(TAG_TAB_REVOLVER);
    } else {
      tabHost.setCurrentTabByTag(savedInstanceState.getString(KEY_CURRENT_TAB_TAG,
          TAG_TAB_REVOLVER));
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      updateTabIndicatorPosition(tabHost.getCurrentTab());
    }

    mScrollParentLayout = (LinearLayout) findViewById(R.id.linearLayout_pkgView);
    mRevolverNumLayout = (LinearLayout) findViewById(R.id.linearLayout_item_revolvers_revolver_revtab);
    mBulletNumLayout = (LinearLayout) findViewById(R.id.linearLayout_item_bullets_revolver_revtab);
    mRevolverPageNumText = (TextView) findViewById(R.id.revolver_page_num);
    mRevolverNumText = (TextView) findViewById(R.id.textView_value_revolvers_revolver_revtab);
    mBulletNumText = (TextView) findViewById(R.id.textView_value_bullets_revolver_revtab);
    frameLayoutBin = (FrameLayout) findViewById(R.id.framelayout_bin_top);
    // mFirstRevolverButton = (Button)
    // findViewById(R.id.first_revolver_button);
    mPreviousRevolverButton = (ImageView) findViewById(R.id.previous_revolver_button);
    mForrwingRevolverButton = (ImageView) findViewById(R.id.following_revolver_button);
    // mLastRevolverButton = (Button)
    // findViewById(R.id.last_revolver_button);

    // 強制的にイベントを伝播させる
    // ((FrameLayout)findViewById(R.id.framelayout_magazine_pager_top)).requestDisallowInterceptTouchEvent(true);
    // ((FrameLayout)findViewById(R.id.framelayout_magazine_pager)).requestDisallowInterceptTouchEvent(true);

    layoutInflater = (LayoutInflater) activity.getLayoutInflater();

    // 削除ビンのドロップリスナ
    frameLayoutBin.setOnDragListener(new View.OnDragListener() {

      @Override
      public boolean onDrag(View v, DragEvent event) {
        // Log.d(TAG, "onDrag (Bin)");
        boolean result = false;

        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED: { // ドラッグ開始時に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DRAG_STARTED");
          result = true;
        }
        break;

        case DragEvent.ACTION_DRAG_LOCATION: { // ドラッグ中に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DRAG_LOCATION");
          result = true;
        }
        break;

        case DragEvent.ACTION_DROP: { // ドロップ時に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DROP");
          ClipData data = event.getClipData(); // ドラッグで受け渡す情報が入っている
          String from = "" + data.getItemAt(0).getText();// ドロップ元がリボルバーから来ているか，リストビューから来ているか
          String id_str = "" + data.getItemAt(1).getText();
          int from_id = Integer.parseInt(id_str); // ドロップ元のid

          if (from.equals("Revolver")) { // リボルバーからドラッグされた場合
            if (!event.getResult()) {
              // ClickedViewIDが初期値ではなかったら
              if (ClickedViewID != -1) {
                // ドラッグ元の保存データを削除
                revolverData.setPackageName(mPageNumInRevolver, ClickedViewID,
                    null);
                revolverData.setBitmapByte(mPageNumInRevolver, ClickedViewID,
                    null);
                revolverDataManager.save(revolverData);

                // ドラッグ元の弾画像を初期化
                mBulletImageListInRevolverList
                .get(mPageNumInRevolver)
                .get(ClickedViewID)
                .setImageBitmap(
                    createInitIcon(mScrollBulletSize,
                        mScrollBulletSize / 3,
                        String.valueOf(ClickedViewID + 1)));
                mBulletImageListInRevolverList
                .get(mPageNumInRevolver)
                .get(ClickedViewID)
                .setLayoutParams(
                    new FrameLayout.LayoutParams(
                        mBulletImageSizeInRevolver,
                        mBulletImageSizeInRevolver));
              }
            }
          }
          result = true;
        }
        break;

        case DragEvent.ACTION_DRAG_ENDED: { // ドラッグ終了時に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DRAG_ENDED");
          result = true;
        }
        break;

        case DragEvent.ACTION_DRAG_ENTERED: { // ドラッグ開始直後に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DRAG_ENTERED");
          result = true;
        }
        break;

        case DragEvent.ACTION_DRAG_EXITED: { // ドラッグ終了直前に呼び出し
          // Log.i(TAG, "DragEvent.ACTION_DRAG_EXITED");
          result = false;
        }
        break;

        default: {
          // Log.i(TAG, "Unknown DragEvent...");
          result = false;
        }
        break;
        }
        return result;
      }

    });

    // ページ番号表示のタッチリスナ
    mRevolverPageNumText.setOnTouchListener(new View.OnTouchListener() {
      // ダイアログが起動しなくなる移動距離
      private float boundary = 10;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        DialogFragment dialogFragment = new RevolverPageDialogFragment(activity,
            layoutInflater);
        dialogFragment.setRetainInstance(true);
        mRevolverViewPager.onPaveTextTouchEvent(event, dialogFragment,
            getFragmentManager(), boundary);

        return true;
      }
    });

    // 連装数のダイアログ
    mRevolverNumLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new RevoloverNumDialogFragment(activity,
            layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(getFragmentManager(), null);
      }
    });

    // 弾数のダイアログ
    mBulletNumLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new BulletNumDialogFragment(activity,
            layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(getFragmentManager(), null);
      }
    });

    // 1つ前のページに移動するボタンのリスナー
    mPreviousRevolverButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mPageNumInRevolver != 0) {
          mPageNumInRevolver--;
          mRevolverViewPager.setCurrentItem(mPageNumInRevolver);
          mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));
        }
      }
    });

    // 1つ後のページに移動するボタンのリスナー
    mForrwingRevolverButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mPageNumInRevolver != (parameterManager.getRevolverNum() - 1)) {
          mPageNumInRevolver++;
          mRevolverViewPager.setCurrentItem(mPageNumInRevolver);
          mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));
        }
      }
    });

    // 端末がインストールしているアプリ情報を取得
    getPackageList();

    // 弾の数を更新する
    // setBulletNum(parameterManager.getBulletNum());

    // 画面サイズを測って，そこからアイコンの大きさを決定
    WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    // ディスプレイのインスタンス生成
    Display disp = wm.getDefaultDisplay();
    Point p = new Point();
    shortSide = 0;
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) { // API
      // 12
      // 以前
      int dispWidth = disp.getWidth();
      int dispHeight = disp.getHeight();
      isLandscape = dispWidth > dispHeight;
      shortSide = dispWidth < dispHeight ? dispWidth : dispHeight;
    } else { // API 13 以降
      p = new Point();
      disp.getSize(p);
      isLandscape = p.x > p.y;
      shortSide = p.x < p.y ? p.x : p.y;
    }

    mBulletNumInPage = mAppDataList.size(); // アプリの数

    // 弾を並べるレイアウトの数を設定
    mScrollLayoutListNum = isLandscape ? 3 : 2;

    // 横に何個並べるか計算
    if (mBulletNumInPage % mScrollLayoutListNum == 0) { // 割り切れる時
      mMaxBulletNumInScrollLayoutList = mBulletNumInPage / mScrollLayoutListNum;
    } else {
      mMaxBulletNumInScrollLayoutList = mBulletNumInPage / mScrollLayoutListNum + 1;
    }

    // // 画面のインチ数を計算
    // DisplayMetrics metrics = new DisplayMetrics();
    // wm.getDefaultDisplay().getMetrics(metrics);
    // float dispXinch = metrics.widthPixels / metrics.xdpi;
    // float dispYinch = metrics.heightPixels / metrics.ydpi;
    // float dispInch = (float)Math.sqrt(dispXinch * dispXinch + dispYinch *
    // dispYinch); // インチ

    mScrollBulletSize = shortSide / 8; // アイコンサイズ

    // RevolverDataManager の初期化
    revolverDataManager = new RevolverDataManager(this, parameterManager.getRevolverNum(),
        parameterManager.getBulletNum());
    // revolverData の初期化
    revolverData = (SerializableRevolverData) revolverDataManager.load();

    // ページ番号によってボタンを非表示に
    setButtonVisible();

    // 装填画面ビュー内のアイコンサイズの初期化
    mBulletImageSizeInRevolver = mScrollBulletSize;

    // 装填画面の描画
    invalidateRevolverPage();

    // パラメータ設定画面の設定 (SETTINGS タブ)
    settingParams = new SettingParams(this);


    // for Amazon Apps
    registerListenerForAmazonApps((SettingActivity) activity);
  }

  /**
   * Save
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) { // Bundleに保存する
    super.onSaveInstanceState(outState);
    outState.putString(KEY_CURRENT_TAB_TAG, tabHost.getCurrentTabTag()); // 値を保存
  }

  /**
   * 一時停止
   */
  @Override
  public void onPause() {
    settingParams.pauseAds();
    super.onPause();
  }

  /**
   * 再開
   */
  @Override
  public void onResume() {
    super.onResume();
    settingParams.resumeAds();
    invalidateOptionsMenu(); // ActionBar 関連

    // タブの位置の復元
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      updateTabIndicatorPosition(tabHost.getCurrentTab());
    }

    amazonIabOnResume();
  }

  /**
   * onStart
   */
  @Override
  protected void onStart() {
    super.onStart();
    amazonIabOnStart();
  }

  /**
   * 終了処理
   */
  @Override
  protected void onDestroy() {
    mRevolverParentLayoutList.clear();
    mBulletInfoListInRevolverList.clear();
    mBulletImageListInRevolverList.clear();
    mBulletLayoutList.clear();
    mBulletImageListInPage.clear();
    mBulletNameListInPage.clear();
    mAppDataList.clear();
    revolverData.clear();
    mScrollParentLayout.removeAllViews();

    settingParams.destroyAds();

    super.onDestroy();

    // ガベージコレクション
    System.gc();
  }

  /**
   * onWindowFocusChanged --- ウィンドウがフォーカス (描画完了) されたら呼ばれる
   */
  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);

    // タブインジケータの初期位置の指定
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      updateTabIndicatorPosition(tabHost.getCurrentTab());
    }
  }

  /**
   * onConfigurationChanged --- 画面回転検知 (layout-land と共存不可)
   */
  // @Override
  // public void onConfigurationChanged(Configuration newConfig) {
  // Log.d(TAG, "onConfigurationChanged");
  // super.onConfigurationChanged(newConfig);
  //
  // switch (newConfig.orientation) {
  // case Configuration.ORIENTATION_PORTRAIT: // 縦向き
  // isLandscape = true;
  // mScrollLayoutListNum = 2;
  // break;
  // case Configuration.ORIENTATION_LANDSCAPE: // 横向き
  // isLandscape = false;
  // mScrollLayoutListNum = 3;
  // break;
  // default:
  // break;
  // }
  //
  // mBulletLayoutList.clear();
  // mBulletImageListInPage.clear();
  // makeAppPkgListView();
  // }

  /**
   * updateTabIndicatorPosition --- タブインジケータの位置を変える
   *
   * @param position
   */
  private void updateTabIndicatorPosition(int position) {
    // Log.d(TAG, "updateTabIndicatorPosition");
    View tabView = tabWidget.getChildTabViewAt(position);
    int indicatorWidth = tabView.getWidth();
    int indicatorLeft = (int) (position * indicatorWidth);
    final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) indicator
        .getLayoutParams();
    layoutParams.width = indicatorWidth;
    layoutParams.setMargins(indicatorLeft, 0, 0, 0);
    indicator.setLayoutParams(layoutParams);
  }

  // 弾の数の設定を更新
  public void setBulletNum(int n) {
    parameterManager.putBulletNum(n); // ParameterManager の更新
    revolverDataManager.changeBulletNum(n); // RevolverDataManager の更新
  }

  /**
   * 再描画
   */
  public void invalidateRevolverPage() {
    // 配列の初期化
    mRevolverParentLayoutList.clear();
    mBulletInfoListInRevolverList.clear();
    mBulletImageListInRevolverList.clear();
    mBulletLayoutList.clear();
    mBulletImageListInPage.clear();
    mBulletNameListInPage.clear();
    mScrollParentLayout.removeAllViews();

    // リボルバーの連装数の変更を更新
    revolverDataManager.changeRevolverNum(parameterManager.getRevolverNum());

    // レイアウトの作成
    makeRevolverParentLayoutList();
    makeAppPkgListView();

    // ページをめくるためアダプターを設定 (リボルバーページ)
    mRevolverViewPager = (RevolverViewPager) findViewById(R.id.revolverviewpager);
    mRevolverViewPager.setAdapter(new RevolverPagerAdapter());

    // 現在表示しているページ番号を取得するためのリスナーを追加(リボルバーページ)
    mRevolverViewPager
    .setOnPageChangeListener(new RevolverViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        mPageNumInRevolver = position;
        mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));

        // ページ番号によってボタンを非表示に
        setButtonVisible();
      }
    });

    // フリー版用変数チェック
    if (!PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("kPremEd", false)) {
      if (parameterManager.getRevolverNum() > MAX_NUM_REVOLVER_FOR_FREE) {
        parameterManager.putRevolverNum(MAX_NUM_REVOLVER_FOR_FREE);
      }
      if (parameterManager.getBulletNum() > MAX_NUM_BULLET_FOR_FREE) {
        parameterManager.putBulletNum(MAX_NUM_BULLET_FOR_FREE);
      }
    }

    // 現在値の設定
    mRevolverNumText.setText(String.valueOf(parameterManager.getRevolverNum()));
    mBulletNumText.setText(String.valueOf(parameterManager.getBulletNum()));

    setIconsToRevolverPage();
  }

  /**
   * リボルバーのレイアウトを再描画
   */
  public void invalidateRevolverLayout() {
    for (int i = 0; i < mRevolverParentLayoutList.size() - 1; i++) {
      mRevolverParentLayoutList.get(i).removeAllViews();
    }

    // リボルバーのレイアウトを初期化
    mRevolverParentLayoutList.clear();
    mBulletInfoListInRevolverList.clear();
    mBulletImageListInRevolverList.clear();
    mBulletLayoutList.clear();

    // リボルバーの連装数の変更を更新
    revolverDataManager.changeRevolverNum(parameterManager.getRevolverNum());

    // レイアウトの作成
    makeRevolverParentLayoutList();

    // ページをめくるためアダプターを設定 (リボルバーページ)
    mRevolverViewPager = (RevolverViewPager) findViewById(R.id.revolverviewpager);
    mRevolverViewPager.setAdapter(new RevolverPagerAdapter());

    // 現在表示しているページ番号を取得するためのリスナーを追加(リボルバーページ)
    mRevolverViewPager
    .setOnPageChangeListener(new RevolverViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        mPageNumInRevolver = position;
        mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));

        // ページ番号によってボタンを非表示に
        setButtonVisible();
      }
    });

    // 連装数変更前のページ番号
    int tmpRevolverPage = Integer.parseInt(mRevolverPageNumText.getText().toString());

    // 連装数変更前のページが設定した連装数を超えていた場合，最後のページに移動
    if (tmpRevolverPage > parameterManager.getRevolverNum()) {
      mPageNumInRevolver = parameterManager.getRevolverNum() - 1;
      mRevolverViewPager.setCurrentItem(mPageNumInRevolver);
      mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));
      // ページ番号によってボタンを非表示に
      setButtonVisible();
    }
    // 超えていない場合，連装数変更前と同じページに移動
    else {
      mPageNumInRevolver = tmpRevolverPage - 1;
      mRevolverViewPager.setCurrentItem(mPageNumInRevolver);
      mRevolverPageNumText.setText(String.valueOf(tmpRevolverPage));
      // ページ番号によってボタンを非表示に
      setButtonVisible();
    }

    mRevolverNumText.setText(String.valueOf(parameterManager.getRevolverNum()));

    setIconsToRevolverPage();
  }

  /**
   * setIconsToRevolverPage --- 装填ビュー内のリボルバ上のアイコンを復元
   */
  public void setIconsToRevolverPage() {
    // 装填部のデータ復元
    revolverData = (SerializableRevolverData) revolverDataManager.load();
    for (int i = 0; i < parameterManager.getRevolverNum(); i++) {
      for (int j = 0; j < parameterManager.getBulletNum(); j++) {
        if (revolverData != null) {
          if (revolverData.getBitmapByte(i, j) != null) { // リボルバーデータを保存していた場合
            // ショートカットの画像を読み込み，リストに追加して表示させる
            Bitmap tmpBitmap = BitmapFactory.decodeByteArray(
                revolverData.getBitmapByte(i, j), 0,
                revolverData.getBitmapByte(i, j).length); // Bitmap
            // を取得
            mBulletImageListInRevolverList.get(i).get(j).setImageBitmap(tmpBitmap); // セット
            mBulletImageListInRevolverList
            .get(i)
            .get(j)
            .setLayoutParams(
                new FrameLayout.LayoutParams(mBulletImageSizeInRevolver,
                    mBulletImageSizeInRevolver)); // 画像の大きさを調整
          }
          else { // リボルバーデータを保存していない場合
            mBulletImageListInRevolverList
            .get(i)
            .get(j)
            .setImageBitmap(
                createInitIcon(mScrollBulletSize, mScrollBulletSize / 3,
                    String.valueOf(j + 1))); // 空っぽを示すアイコンをセット
            mBulletImageListInRevolverList
            .get(i)
            .get(j)
            .setLayoutParams(
                new FrameLayout.LayoutParams(mBulletImageSizeInRevolver,
                    mBulletImageSizeInRevolver)); // 画像の大きさを調整
          }
        }
      }
    }
  }

  /**
   * リボルバーを並べるレイアウトリストを生成
   */
  public void makeRevolverParentLayoutList() {

    for (int i = 0; i < parameterManager.getRevolverNum(); i++) {

      // ランチャー内アイコン配置用レイアウトの初期化
      StaticCircularLayout scl = new StaticCircularLayout(this);
      StaticCircularLayout.LayoutParams sclParams = new StaticCircularLayout.LayoutParams(
          StaticCircularLayout.LayoutParams.MATCH_PARENT,
          StaticCircularLayout.LayoutParams.MATCH_PARENT);
      scl.setLayoutParams(sclParams);
      scl.setChildSize(mBulletImageSizeInRevolver); // あらかじめ値をいれておくことで、描画が安定する
      mRevolverParentLayoutList.add(scl);

      // アプリ情報
      mBulletInfoListInRevolverList.add(new ArrayList<String>());
      mBulletImageListInRevolverList.add(new ArrayList<ImageView>());

      for (int j = 0; j < parameterManager.getBulletNum(); j++) {

        // アプリ名の取得
        mBulletInfoListInRevolverList.get(i).add(new String());
        mBulletInfoListInRevolverList.get(i).set(j, null);

        // アプリアイコンの取得
        mBulletImageListInRevolverList.get(i).add(new ImageView(this));
        mBulletImageListInRevolverList
        .get(i)
        .get(j)
        .setImageBitmap(
            createInitIcon(mScrollBulletSize, mScrollBulletSize / 3,
                String.valueOf(j + 1)));

        // リボルバーの弾のドラッグリスナーを設定
        mBulletImageListInRevolverList.get(i).get(j)
        .setOnDragListener(new View.OnDragListener() {
          @Override
          public boolean onDrag(View v, DragEvent event) {
            // Log.d(TAG, "onDrag (Bullet)");

            boolean result = false;

            switch (event.getAction()) {

            case DragEvent.ACTION_DRAG_STARTED: { // ドラッグ開始時に呼び出し
              // Log.i(TAG,
              // "DragEvent.ACTION_DRAG_STARTED");
              result = true;
            }
            break;

            case DragEvent.ACTION_DRAG_LOCATION: { // ドラッグ中に呼び出し
              // Log.i(TAG,
              // "DragEvent.ACTION_DRAG_LOCATION");
              result = true;
            }
            break;

            case DragEvent.ACTION_DROP: { // ドロップ時に呼び出し
              // Log.i(TAG, "DragEvent.ACTION_DROP");
              ClipData data = event.getClipData(); // ドラッグで受け渡す情報が入っている
              String from = "" + data.getItemAt(0).getText();// ドロップ元がリボルバーから来ているか，リストビューから来ているか
              String id_str = "" + data.getItemAt(1).getText();
              int from_id = Integer.parseInt(id_str); // ドロップ元のid

              if (from.equals("ListView")) { // 画面下のアプリリストからドラッグされた場合
                // ドロップされたViewのIDを探す
                int droppedViewID = 0; // ドロップされたViewのID
                for (int h = 0; h < parameterManager.getBulletNum(); h++) {
                  if (v == mBulletImageListInRevolverList.get(
                      mPageNumInRevolver).get(h)) {
                    droppedViewID = h;
                    break;
                  }
                }

                // ドロップされたアプリのパッケージ名を取得
                mBulletInfoListInRevolverList.get(mPageNumInRevolver)
                .set(droppedViewID,
                    mAppDataList.get(from_id)
                    .getPackagename());

                // ドロップされたアプリの画像をリボルバーの弾に表示
                mBulletImageListInRevolverList
                .get(mPageNumInRevolver)
                .get(droppedViewID)
                .setImageDrawable(
                    mAppDataList.get(from_id).getIcon());

                // アプリの画像の表示サイズを弾のサイズに合わせる
                mBulletImageListInRevolverList.get(mPageNumInRevolver)
                .get(droppedViewID)
                .setLayoutParams(createIconSizeFLParams());

                // Bitmap データの生成
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap tmpBitmap = ((BitmapDrawable) mAppDataList.get(
                    from_id).getIcon()).getBitmap();
                tmpBitmap.compress(CompressFormat.PNG, 100, bos);
                byte[] tmpBitmapByte = bos.toByteArray();

                // 取得データの保存
                revolverData.setPackageName(mPageNumInRevolver,
                    droppedViewID, mAppDataList.get(from_id)
                    .getPackagename()); // アプリ名
                revolverData.setBitmapByte(mPageNumInRevolver,
                    droppedViewID, tmpBitmapByte); // アイコン
                revolverDataManager.save(revolverData);
              }

              // リボルバーからドラッグされた場合
              else if (from.equals("Revolver")) {
                // Log.d(TAG, "from Revolver");
                // ドロップされたViewのIDを探す
                int droppedViewID = 0; // ドロップされたViewのID
                for (int h = 0; h < parameterManager.getBulletNum(); h++) {
                  if (v == mBulletImageListInRevolverList.get(
                      mPageNumInRevolver).get(h)) {
                    droppedViewID = h;
                    break;
                  }
                }

                // ドラッグ元とドラッグ先が違う場合(同じなら処理はしない)
                if (from_id != droppedViewID) {
                  if (revolverData != null) {
                    if (revolverData.getBitmapByte(
                        mPageNumInRevolver, from_id) != null) { // リボルバーデータを保存していた場合

                      String tmpPackageName2 = null;
                      Bitmap tmpBitmap2 = null;
                      byte[] tmpBitmapByte2 = null;

                      // ドロップ先にデータを保存していた場合
                      if (revolverData.getBitmapByte(
                          mPageNumInRevolver, droppedViewID) != null) {
                        // ドロップ先のアプリのパッケージ名を一時保存
                        tmpPackageName2 = new String(
                            revolverData.getPackageName(
                                mPageNumInRevolver,
                                droppedViewID));

                        // ドロップ先のアプリ画像からBitmapを生成
                        tmpBitmap2 = BitmapFactory.decodeByteArray(
                            revolverData.getBitmapByte(
                                mPageNumInRevolver,
                                droppedViewID), 0,
                                revolverData.getBitmapByte(
                                    mPageNumInRevolver,
                                    droppedViewID).length);

                        // Bitmap データの生成
                        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                        tmpBitmap2.compress(CompressFormat.PNG,
                            100, bos2);
                        tmpBitmapByte2 = bos2.toByteArray();
                      }

                      // ドロップ元のアプリのアイコン画像を保存
                      mBulletInfoListInRevolverList.get(
                          mPageNumInRevolver).set(
                              droppedViewID,
                              revolverData
                              .getPackageName(
                                  mPageNumInRevolver,
                                  from_id));

                      // ドロップ元のアプリのパッケージ名を保存
                      Bitmap tmpBitmap = BitmapFactory
                          .decodeByteArray(revolverData
                              .getBitmapByte(
                                  mPageNumInRevolver,
                                  from_id), 0,
                                  revolverData.getBitmapByte(
                                      mPageNumInRevolver,
                                      from_id).length);

                      // ドロップ元のアプリのパッケージ名を取得
                      mBulletInfoListInRevolverList.get(
                          mPageNumInRevolver).set(
                              droppedViewID,
                              revolverData
                              .getPackageName(
                                  mPageNumInRevolver,
                                  from_id));

                      // アプリの画像をドロップ先のリボルバーの弾に表示
                      mBulletImageListInRevolverList
                      .get(mPageNumInRevolver)
                      .get(droppedViewID)
                      .setImageBitmap(tmpBitmap); // Bitmap
                      // 画像の大きさを調整
                      mBulletImageListInRevolverList
                      .get(mPageNumInRevolver)
                      .get(mPageNumInRevolver)
                      .setLayoutParams(
                          createIconSizeFLParams());

                      // Bitmap データの生成
                      ByteArrayOutputStream bos = new ByteArrayOutputStream();
                      tmpBitmap.compress(CompressFormat.PNG, 100,
                          bos);
                      byte[] tmpBitmapByte = bos.toByteArray();

                      // データの保存
                      revolverData.setPackageName(
                          mPageNumInRevolver, droppedViewID,
                          revolverData
                          .getPackageName(
                              mPageNumInRevolver,
                              from_id)); // アプリ名
                      revolverData.setBitmapByte(
                          mPageNumInRevolver, droppedViewID,
                          tmpBitmapByte); // アイコン
                      revolverDataManager.save(revolverData);

                      // ドロップ先にデータを保存していた場合、入れ替えを行う
                      if (tmpPackageName2 != null) {
                        // ドロップ元の保存データをドロップ先のもの
                        revolverData.setPackageName(
                            mPageNumInRevolver, from_id,
                            tmpPackageName2);
                        revolverData.setBitmapByte(
                            mPageNumInRevolver, from_id,
                            tmpBitmapByte2);
                        revolverDataManager.save(revolverData);

                        // ドロップ元の弾画像をドロップ先のものに
                        mBulletImageListInRevolverList
                        .get(mPageNumInRevolver)
                        .get(from_id)
                        .setImageBitmap(tmpBitmap2);
                        mBulletImageListInRevolverList
                        .get(mPageNumInRevolver)
                        .get(from_id)
                        .setLayoutParams(
                            createIconSizeFLParams());
                      }
                      // ドロップ先にデータを保存していなかった場合、元は初期状態に戻す
                      else {
                        // ドロップ元の保存データを削除
                        revolverData.setPackageName(
                            mPageNumInRevolver, from_id,
                            null);
                        revolverData.setBitmapByte(
                            mPageNumInRevolver, from_id,
                            null);
                        revolverDataManager.save(revolverData);

                        // ドロップ元の弾画像を初期化
                        mBulletImageListInRevolverList
                        .get(mPageNumInRevolver)
                        .get(from_id)
                        .setImageBitmap(
                            createInitIcon(
                                mScrollBulletSize,
                                mScrollBulletSize / 3,
                                String.valueOf(from_id + 1)));
                        mBulletImageListInRevolverList
                        .get(mPageNumInRevolver)
                        .get(from_id)
                        .setLayoutParams(
                            createIconSizeFLParams());
                      }
                    }
                  }
                }
              }
              result = true;
            }
            break;

            case DragEvent.ACTION_DRAG_ENDED: { // ドラッグ終了時に呼び出し
              //                                        Log.i(TAG, "DragEvent.ACTION_DRAG_ENDED");
              frameLayoutBin.post(new Runnable() {
                public void run() {
                  frameLayoutBin.setVisibility(View.GONE);
                  mRevolverPageNumText.setVisibility(View.VISIBLE);
                }
              });
              result = true;
            }
            break;

            case DragEvent.ACTION_DRAG_ENTERED: { // ドラッグ開始直後に呼び出し
              // Log.i(TAG,
              // "DragEvent.ACTION_DRAG_ENTERED");
              result = true;
            }
            break;

            case DragEvent.ACTION_DRAG_EXITED: { // ドラッグ終了直前に呼び出し
              // Log.i(TAG,
              // "DragEvent.ACTION_DRAG_EXITED");
              result = true;
            }
            break;

            default: {
              // Log.i(TAG, "Unknown DragEvent...");
              result = true;
            }
            break;
            }
            return result;
          }

          /**
           * LayoutParams を生成
           *
           * @return
           */
          protected FrameLayout.LayoutParams createIconSizeFLParams() {
            return new FrameLayout.LayoutParams(mBulletImageSizeInRevolver,
                mBulletImageSizeInRevolver);
          }
        });

        /*
         * // リボルバーの弾をクリックした時のリスナーを設定
         * mBulletImageListInRevolverList.get(i).get(j)
         * .setOnClickListener(new OnClickListener() { public void
         * onClick(View v) { int ClickedViewID = 0; // View ID //
         * クリックされたリボルバーの弾のIDを探す for (int i = 0; i <
         * parameterManager.getBulletNum(); i++) { if (v ==
         * mBulletImageListInRevolverList.get(mPageNumInRevolver)
         * .get(i)) { ClickedViewID = i; break; } } // リボルバーデータを保存していた場合
         * (アプリを起動) if (revolverData.getPackageName(mPageNumInRevolver,
         * ClickedViewID) != null) { Intent intent = packageManager
         * .getLaunchIntentForPackage(revolverData.getPackageName(
         * mPageNumInRevolver, ClickedViewID)); startActivity(intent); }
         * } });
         */

        // リボルバーの弾をフリックした時のリスナーを設定
        mBulletImageListInRevolverList.get(i).get(j)
        .setOnTouchListener(new OnTouchListener() {

          // 最初にタッチされた座標
          private float startTouchX;
          private float startTouchY;

          // 現在タッチ中の座標
          private float nowTouchedX;
          private float nowTouchedY;

          // 最低限移動しないといけない距離 (アイコンサイズ÷4に設定)
          private float adjust = mScrollBulletSize / 4;

          @Override
          public boolean onTouch(View v, MotionEvent event) {

            // クリックされたリボルバーの弾のIDを探す
            for (int i = 0; i <= parameterManager.getBulletNum(); i++) {
              ClickedViewID = i;
              if (v == mBulletImageListInRevolverList.get(mPageNumInRevolver)
                  .get(i)) {
                break;
              }
            }

            if (ClickedViewID == parameterManager.getBulletNum()) {
              return true;
            }

            // リボルバーデータを保存していた場合，動作に応じて処理
            if (revolverData.getPackageName(mPageNumInRevolver, ClickedViewID) != null) {
              switch (event.getAction()) {
              // タッチ
              case MotionEvent.ACTION_DOWN:
                // Log.d(TAG,
                // "MotionEvent.ACTION_DOWN");
                // 位置の取得
                startTouchX = event.getX();
                startTouchY = event.getY();
                break;

                // スライド
              case MotionEvent.ACTION_MOVE:
                // Log.d(TAG,
                // "MotionEvent.ACTION_MOVE");
                nowTouchedX = event.getX();
                nowTouchedY = event.getY();

                // 動かした距離を求める
                double distance = Math.sqrt(Math.pow(startTouchX
                    - nowTouchedX, 2)
                    + Math.pow(startTouchY - nowTouchedY, 2));

                // 一定方向動かした場合，ドラッグ処理
                if (distance > adjust) {
                  // ビンの表示を有効化
                  frameLayoutBin.setVisibility(View.VISIBLE);
                  mRevolverPageNumText.setVisibility(View.GONE);

                  // ドラッグで受け渡す情報を入れる
                  ClipData data = ClipData.newPlainText("From",
                      "Revolver");
                  data.addItem(new ClipData.Item("" + ClickedViewID));

                  // タッチした画像のドラッグ開始
                  v.startDrag(data, new View.DragShadowBuilder(v),
                      (Object) v, 0);
                }
                break;

              case MotionEvent.ACTION_UP:
                // Log.d(TAG,
                // "MotionEvent.ACTION_UP");
                break;

              case MotionEvent.ACTION_CANCEL: // UP+DOWNの同時発生(キャンセル)の場合
                // Log.d(TAG,
                // "MotionEvent.ACTION_CANCEL");
                break;

              case MotionEvent.ACTION_OUTSIDE: // ターゲットとするUIの範囲外を押下
                // Log.d(TAG,
                // "MotionEvent.ACTION_OUTSIDE");
                break;

              }
            }
            return true;
          }
        });

        mRevolverParentLayoutList.get(i).addView(
            mBulletImageListInRevolverList.get(i).get(j)); // アイコン
        // (弾)
        // をレイアウトに追加
      }
    }
  }

  /**
   * BulletLayoutListにLinearLayoutを追加、設定
   */
  public void makeAppPkgListView() {

    /*
     * mBulletImageListInPage: アプリアイコン View のリスト mBulletNameListInPage: アプリ名
     * View のリスト
     */

    // マージン
    int marginNarrow = mScrollBulletSize / 10;
    int marginWide = mScrollBulletSize * 2 / 7;

    int appItemMarginL = isLandscape ? marginNarrow : marginWide;
    int appItemMarginT = isLandscape ? marginWide : marginNarrow;
    int appItemMarginR = isLandscape ? marginNarrow : marginWide;
    int appItemMarginB = isLandscape ? marginWide : marginNarrow;

    for (int i = 0; i < mBulletNumInPage; i++) {
      /* アプリアイコン View の生成 */
      ImageView appIconImageView = new ImageView(this);
      appIconImageView.setImageDrawable(mAppDataList.get(i).getIcon()); // アイコンのセット
      LinearLayout.LayoutParams appIconLayoutParams = new LinearLayout.LayoutParams(
          mScrollBulletSize, mScrollBulletSize);
      appIconLayoutParams.gravity = Gravity.CENTER;
      appIconImageView.setLayoutParams(appIconLayoutParams); // 大きさ
      appIconImageView.setOnTouchListener(new View.OnTouchListener() { // アプリ一覧のアイコン画像をタッチした時のリスナーを設定
        private float startTouchX, startTouchY; // 最初にタッチされた座標
        private float nowTouchedX, nowTouchedY; // 現在タッチ中の座標

        // タッチした画像をスライドさせたときの処理
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN: // タッチ
            startTouchX = event.getX();
            startTouchY = event.getY();
            break;
          case MotionEvent.ACTION_MOVE: // スライド
            ClickedViewID = -1; // 動かしているのはリボルバーの弾でないことを表す
            nowTouchedX = event.getX();
            nowTouchedY = event.getY();

            double angle = getRadian(startTouchX, startTouchY, nowTouchedX,
                nowTouchedY);
            // Log.d(TAG, "angle: " + angle);

            if (isLandscape ? angle < -(double) 13 / 24 * Math.PI
                || angle > (double) 13 / 24 * Math.PI : -(double) 23
                / 24 * Math.PI < angle
                && angle < -(double) 1 / 24 * Math.PI) {
              // 現在スライド中の弾のIDを取得
              for (int i = 0; i < mBulletNumInPage; i++) {
                if
                (v == mBulletImageListInPage.get(i)) {
                  mNowBulletID = i;
                }
              }
              // ドラッグで受け渡す情報を入れる
              ClipData data = ClipData.newPlainText("From", "ListView");
              data.addItem(new ClipData.Item("" + mNowBulletID));
              // タッチした画像のドラッグ開始
              v.startDrag(data, new View.DragShadowBuilder(v),
                  (Object) v, 0);
            }
            break;
          default:
            break;
          }
          return true;
        }

        /**
         * 2点の角度 (-PI < atan2() < PI)
         *
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @return
         */
        protected double getRadian(double x1, double y1, double x2, double y2) {
          return Math.atan2(y2 - y1, x2 - x1);
        }

        // protected double getDegree(double x1, double y1,
        // double x2, double y2) {
        // return getRadian(x1, y1, x2, y2) * 180 / Math.PI;
        // }
      });
      appIconImageView.setOnDragListener(new View.OnDragListener() { // アプリ一覧のアイコン画像をドラッグした時のリスナーを設定
        @Override
        public boolean onDrag(View v, DragEvent event) {
          // Log.d(TAG, "onDrag (Bullet list)");
          boolean result = false;
          switch (event.getAction()) {
          case DragEvent.ACTION_DRAG_STARTED: { // ドラッグ開始時に呼び出し
            result = true;
          }
          break;
          case DragEvent.ACTION_DRAG_ENDED: { // ドラッグ終了時に呼び出し
          }
          break;
          case DragEvent.ACTION_DRAG_LOCATION: { // ドラッグ中に呼び出し
            result = true;
          }
          break;
          case DragEvent.ACTION_DROP: { // ドロップ時に呼び出し
            result = true;
          }
          break;
          case DragEvent.ACTION_DRAG_ENTERED: { // ドラッグ開始直後に呼び出し
            result = true;
          }
          break;
          case DragEvent.ACTION_DRAG_EXITED: { // ドラッグ終了直前に呼び出し
            result = true;
          }
          break;
          default:
            result = true;
            break;
          }
          return result;
        }
      });
      mBulletImageListInPage.add(appIconImageView);

      /* アプリ名 View の生成 */
      TextView appNameTextView = new TextView(this);
      appNameTextView.setText(mAppDataList.get(i).getLabel());
      appNameTextView.setTextSize(12); // アプリ名テキストのサイズ
      appNameTextView.setPadding(0, mScrollBulletSize / 10, 0, 0); // アプリ名の
      // Padding
      appNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
          mScrollBulletSize / 5 * 7, LinearLayout.LayoutParams.WRAP_CONTENT)); // TextView
      // のサイズ
      appNameTextView.setGravity(Gravity.CENTER);
      appNameTextView.setSingleLine(); // 1行表示
      appNameTextView.setFocusableInTouchMode(false); // フォーカスを受け取るか否か

      // 文字の表示方法
      // appNameTextView.setEllipsize(TruncateAt.START); // 文字列先頭を省略
      // appNameTextView.setEllipsize(TruncateAt.MIDDLE); // 文字列中央を省略
      appNameTextView.setEllipsize(TruncateAt.END); // 文字列末尾を省略
      // appNameTextView.setEllipsize(TruncateAt.MARQUEE); // 文字をスクロールして表示
      // appNameTextView.setHorizontallyScrolling(true); // 水平方向のスクロール
      mBulletNameListInPage.add(appNameTextView);

      /* アプリアイコンおよびアプリ名の入った Layout の生成 */
      LinearLayout appItemLayout = new LinearLayout(this);
      appItemLayout.setOrientation(LinearLayout.VERTICAL);

      LinearLayout.LayoutParams appItemLayoutParams = new LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      appItemLayoutParams.setMargins(appItemMarginL, appItemMarginT, appItemMarginR,
          appItemMarginB); // アイコン周りのスペース

      appItemLayout.setLayoutParams(appItemLayoutParams); // LayoutParams
      // のセット
      appItemLayout.addView(appIconImageView); // アイコンの追加
      appItemLayout.addView(appNameTextView); // アプリ名の追加

      mBulletLayoutList.add(appItemLayout);
    }

    // 行と列数

    for (int i = 0; i < mScrollLayoutListNum; i++) {
      // LinearLayout を生成
      LinearLayout tmpLayout = new LinearLayout(this);
      tmpLayout.setOrientation(isLandscape ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL); // 水平方向
      // 生成した LinearLayout 用に LayoutParams を設定
      LinearLayout.LayoutParams params = new LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT,
          LinearLayout.LayoutParams.MATCH_PARENT, 1);
      tmpLayout.setLayoutParams(params);

      for (int j = 0; j < mMaxBulletNumInScrollLayoutList; j++) {
        if (i + mScrollLayoutListNum * j >= mBulletNumInPage)
          break;

        tmpLayout.addView(mBulletLayoutList.get(i + mScrollLayoutListNum * j));
      }
      mScrollParentLayout.addView(tmpLayout);

    }

  }

  /**
   * リボルバーを表示するページのアダプター
   */
  private class RevolverPagerAdapter extends PagerAdapter {
    // ページが切り替わったら，レイアウトを消去
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      ((RevolverViewPager) container).removeView((View) object);
    }

    // ページの番号(position)に対応したレイアウトの設定
    @Override
    public Object instantiateItem(View container, int position) {

      // makeLayoutで作成したリボルバーページのレイアウトを取得
      View layout = mRevolverParentLayoutList.get(position);

      // 取得したレイアウトに親レイアウトがあったら，removeViewで親レイアウトとの関係を消す(やらないとエラー発生)
      ViewGroup parent = (ViewGroup) layout.getParent();
      if (parent != null) {
        parent.removeView(layout);
      }

      ((RevolverViewPager) container).addView(layout);

      return layout;
    }

    // 表示するページの数を返す
    @Override
    public int getCount() {
      return parameterManager.getRevolverNum();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view.equals(object);
    }
  }

  /**
   * getPackageList --- mAppDataListにインストールされているアプリの情報を追加
   */
  protected void getPackageList() {
    //    Log.d(TAG, "getPackageList");

    // PackageManager の取得
    if (packageManager == null) {
      packageManager = getApplicationContext().getPackageManager();
    }

    mAppDataList.clear();

    // List<PackageInfo> pkgInfoList =
    // packageManager.getInstalledPackages(PackageManager.GET_META_DATA); //
    // あらゆるインストール済みアプリ情報の取得

    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER); // LAUNCHER カテゴリ
    List<ResolveInfo> pkgInfoList = packageManager.queryIntentActivities(intent, 0);

    for (ResolveInfo resolveInfo : pkgInfoList) {
      String label = resolveInfo.loadLabel(packageManager).toString();
      if (label == null) {
        label = "NoLabel";
      }

      String packageName = resolveInfo.activityInfo.packageName;

      // 高画質なアイコンを取得する
      Resources res;
      try {
        res = packageManager.getResourcesForApplication(packageName);

        Configuration config = res.getConfiguration(); // Configuration の取得
        Configuration originalConfig = new Configuration(config); // オリジナル
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
          //          Log.d(TAG, "Request high density icon");
          config.densityDpi = DisplayMetrics.DENSITY_XXHIGH; // 期待する解像度
          //          int iconSize = parameterManager.getIconSize();
          //          if (iconSize >= 320) {
          //            config.densityDpi = DisplayMetrics.DENSITY_XXXHIGH; // 期待する解像度
          //          } else if (iconSize >= 240) {
          //            config.densityDpi = DisplayMetrics.DENSITY_XXHIGH; // 期待する解像度
          //          } else if (iconSize >= 160) {
          //            config.densityDpi = DisplayMetrics.DENSITY_XHIGH; // 期待する解像度
          //          }

        }
        DisplayMetrics dm = res.getDisplayMetrics();
        res.updateConfiguration(config, dm);

        // アイコンの取得
        Drawable drawable;
        Drawable tmpDrawable = null;

        try {
          // res.getDrawableが失敗すると，リソース画像がデフォルトのドロイド君画像になってしまう．低画質のものを一時保存．
          tmpDrawable = packageManager.getApplicationIcon(packageName);
          drawable = res.getDrawable(0);
          Log.d(TAG, "get High-Quality Drawable");
        } catch (Exception e) {
          e.printStackTrace();
          // drawable = resolveInfo.loadIcon(packageManager);
          drawable = tmpDrawable;
        }

        // getApplicationIconも getDrawableも失敗していたらdrawableは初期値nullのまま
        if (drawable == null) {
          // アイコン画像が取得できなかったらデフォルトのドロイド君画像を設定
          drawable = packageManager.getDefaultActivityIcon();
          Log.d(TAG, "fault getDrawable");
        }

        mAppDataList.add(new _AppInfo(label, packageName, drawable));

        // 変更したリソースレベルを戻る
        res.updateConfiguration(originalConfig, dm);

      } catch (NameNotFoundException e) {
        e.printStackTrace();
      }

    }

    Collections.sort(mAppDataList, new Comparator<_AppInfo>() {
      @Override
      public int compare(_AppInfo lhs, _AppInfo rhs) {
        return lhs.getLabel().compareTo(rhs.getLabel());
      }
    });
  }

  /*    *//**
   * getPackageList --- mAppDataListにインストールされているアプリの情報を追加
   *//*
    protected void getPackageList() {
        // Log.d(TAG, "getPackageList");

        // PackageManager の取得
        if (packageManager == null) {
            packageManager = getApplicationContext().getPackageManager();
        }

        mAppDataList.clear();

        // List<PackageInfo> pkgInfoList =
        // packageManager.getInstalledPackages(PackageManager.GET_META_DATA); //
        // あらゆるインストール済みアプリ情報の取得

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER); // LAUNCHER カテゴリ
        List<ResolveInfo> pkgInfoList = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : pkgInfoList) {
            String label = resolveInfo.loadLabel(packageManager).toString();
            if (label == null) {
                label = "NoLabel";
            }

            String packageName = resolveInfo.activityInfo.packageName;

            // アイコン画像
            Drawable drawable = null;

            // 高画質なアイコンを取得する
            Resources res;
            Intent data = packageManager.getLaunchIntentForPackage(packageName); // アプリのインテントを取得
            Parcelable iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);

            // 高画質なアイコンが取得できる場合
            if (iconResource != null && iconResource instanceof ShortcutIconResource) {
                try {
                    ShortcutIconResource shortcutIconResource = (ShortcutIconResource) iconResource;

                    res = packageManager.getResourcesForApplication(packageName);

                    int id = res.getIdentifier(shortcutIconResource.resourceName, null, null);

                    Configuration config = res.getConfiguration(); // Configuration
                    // の取得
                    Configuration originalConfig = new Configuration(config); // オリジナル
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        // Log.d(TAG, "Request high density icon");
                        config.densityDpi = DisplayMetrics.DENSITY_XHIGH; // 期待する解像度
                    }
                    DisplayMetrics dm = res.getDisplayMetrics();
                    res.updateConfiguration(config, dm);

                    try {
                        drawable = res.getDrawable(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // drawable = resolveInfo.loadIcon(packageManager);
                        // 高画質な画像の取得に失敗したら，低画質のものでもよいのでを取得
                        drawable = packageManager.getApplicationIcon(packageName);
                    }

                    // 変更したリソースレベルを戻る
                    res.updateConfiguration(originalConfig, dm);

                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
            // 高画質なアイコン画像が取得できない場合
            else {
                try {
                    drawable = packageManager.getApplicationIcon(packageName); // 低画質な画像を取得
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // アイコン画像が取得できなかったらデフォルトのアイコンを設定
            if (drawable == null) {
                drawable = packageManager.getDefaultActivityIcon();
            }

            // アプリの情報をリストに追加
            mAppDataList.add(new _AppInfo(label, packageName, drawable));
        }

        Collections.sort(mAppDataList, new Comparator<_AppInfo>() {
            @Override
            public int compare(_AppInfo lhs, _AppInfo rhs) {
                return lhs.getLabel().compareTo(rhs.getLabel());
            }
        });
    }*/

  /**
   * 初期アイコンの作成 (by tag)
   *
   * @param iconSize アイコンの大きさ
   * @param fontSize フォントの大きさ
   * @param str アイコン内の文字列
   * @return
   */
  private Bitmap createInitIcon(int iconSize, int fontSize, String str) {
    int circleColor = Color.LTGRAY; // 円の色
    int textColor = Color.WHITE; // テキストの色
    Bitmap bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888); // Bitmap
    // の生成
    Canvas canvas = new Canvas(bitmap); // キャンバスの作成

    // 円
    float circleStrokeWidth = 2.0f; // 円周の線の太さ
    Paint circlePaint = new Paint(); // 円の描画設定
    circlePaint.setAntiAlias(true); // アンチエイリアス
    circlePaint.setColor(circleColor); // 円の色
    circlePaint.setStyle(Paint.Style.FILL); // 円内部の描画
    canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - circleStrokeWidth, circlePaint); // 円内部の描画
    // circlePaint.setStrokeWidth(circleStrokeWidth); // 円の太さ
    // circlePaint.setColor(circleColor); // 円の色
    // circlePaint.setStyle(Paint.Style.STROKE); // 円周の描画
    // canvas.drawCircle(iconSize/2, iconSize/2, iconSize/2 -
    // circleStrokeWidth, circlePaint); // 円周の描画

    // 数字
    String text = str; // テキストの取得
    Paint textPaint = new Paint(); // テキストの描画設定
    textPaint.setColor(textColor); // テキストの色
    textPaint.setTextSize(fontSize); // テキストサイズ
    textPaint.setAntiAlias(true); // アンチエイリアス
    FontMetrics fontMetrics = textPaint.getFontMetrics(); // フォントメトリクスの取得
    float textWidth = textPaint.measureText(text); // テキストの幅
    float baseX = iconSize / 2 - textWidth / 2; // テキストの開始位置 (x)
    float baseY = iconSize / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2; // テキストの開始位置
    // (y)
    canvas.drawText(text, baseX, baseY, textPaint); // テキストの描画

    return bitmap;
  }

  /**
   * RevolverPageDialogFragment --- リボルバーページダイアログ
   *
   * @author ku, tag, naka
   */
  class RevolverPageDialogFragment extends DialogFragment {

    private Activity activity;
    private LayoutInflater factory;

    public RevolverPageDialogFragment(Activity activity, LayoutInflater layoutInflater) {
      this.activity = activity;
      this.factory = layoutInflater;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(activity.getString(R.string.pager_page_number));
      builder.setView(inputView);

      // NumberPicker の初期値の設定
      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
      np.setMinValue(1); // 最小値
      np.setMaxValue(parameterManager.getRevolverNum()); // 最大値
      np.setValue(mPageNumInRevolver + 1);

      // 確定が押されたらデータの更新
      builder.setPositiveButton(activity.getString(R.string.pager_move),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          mPageNumInRevolver = np.getValue() - 1;
          mRevolverViewPager.setCurrentItem(mPageNumInRevolver);
          mRevolverPageNumText.setText(String.valueOf(mPageNumInRevolver + 1));
        }
      });
      // キャンセルが押されたらダイアログを閉じる
      builder.setNegativeButton(activity.getString(R.string.setting_abort),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dismiss();
        }
      });

      return builder.create();
    }

  }

  /**
   * RevoloverNumDialogFragment --- 連装数ダイアログ
   *
   * @author ku, tag, naka
   */
  class RevoloverNumDialogFragment extends DialogFragment {

    private Activity activity;
    private LayoutInflater factory;

    public RevoloverNumDialogFragment(Activity activity, LayoutInflater layoutInflater) {
      this.activity = activity;
      this.factory = layoutInflater;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(activity.getString(R.string.setting_revolvers));
      builder.setView(inputView);

      // NumberPicker の初期値の設定
      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
      np.setMinValue(1); // 最小値
      np.setMaxValue(PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(
          "kPremEd", false) ? MAX_NUM_REVOLVER_FOR_PREMIUM : MAX_NUM_REVOLVER_FOR_FREE); // 最大値
      np.setValue(parameterManager.getRevolverNum());

      // 確定が押されたらデータの更新
      builder.setPositiveButton(activity.getString(R.string.setting_apply),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          parameterManager.putRevolverNum(np.getValue());
          mRevolverNumText.setText(String.valueOf(parameterManager
              .getRevolverNum()));
          invalidateRevolverLayout();
        }
      });

      // キャンセルが押されたらダイアログを閉じる
      builder.setNegativeButton(activity.getString(R.string.setting_abort),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dismiss();
        }
      });

      return builder.create();
    }

    /**
     * onDestroyView --- 画面回転時対策用
     */
    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance()) {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
  }

  /**
   * BulletNumDialogFragment --- 弾数ダイアログ
   *
   * @author ku, tag, naka
   */
  class BulletNumDialogFragment extends DialogFragment {

    private Activity activity;
    private LayoutInflater factory;

    public BulletNumDialogFragment(Activity activity, LayoutInflater layoutInflater) {
      this.activity = activity;
      this.factory = layoutInflater;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(activity.getString(R.string.setting_bullets));
      builder.setView(inputView);

      // NumberPicker の初期値の設定
      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
      np.setMinValue(2); // 最小値
      np.setMaxValue(PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(
          "kPremEd", false) ? MAX_NUM_BULLET_FOR_PREMIUM : MAX_NUM_BULLET_FOR_FREE); // 最大値
      np.setValue(parameterManager.getBulletNum());

      // 確定が押されたらデータの更新
      builder.setPositiveButton(activity.getString(R.string.setting_apply),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          parameterManager.putBulletNum(np.getValue());
          mBulletNumText.setText(String.valueOf(parameterManager
              .getBulletNum()));
          invalidateRevolverLayout();
        }
      });
      // キャンセルが押されたらダイアログを閉じる
      builder.setNegativeButton(activity.getString(R.string.setting_abort),
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dismiss();
        }
      });

      return builder.create();
    }

    /**
     * onDestroyView --- 画面回転時対策用
     */
    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance()) {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
  }

  /**
   * 現在のぺージが最初なら，戻るボタンを消し，最後なら次へのボタンを消す
   */
  private void setButtonVisible() {

    // 現在のページが最初かつ最後 (連装数が1)なら，戻るボタン，次へのボタンともに非表示に
    if (mPageNumInRevolver == 0 && mPageNumInRevolver == parameterManager.getRevolverNum() - 1) {
      mPreviousRevolverButton.setVisibility(View.INVISIBLE);
      mForrwingRevolverButton.setVisibility(View.INVISIBLE);
    }

    // 1ページ目なら戻るボタンを消す
    else if (mPageNumInRevolver == 0) {
      mPreviousRevolverButton.setVisibility(View.INVISIBLE);
      mForrwingRevolverButton.setVisibility(View.VISIBLE);
    }

    // 最後のページなら次へのボタンを消す
    else if (mPageNumInRevolver == parameterManager.getRevolverNum() - 1) {
      mPreviousRevolverButton.setVisibility(View.VISIBLE);
      mForrwingRevolverButton.setVisibility(View.INVISIBLE);
    }

    // それ以外のページ
    else {
      mPreviousRevolverButton.setVisibility(View.VISIBLE);
      mForrwingRevolverButton.setVisibility(View.VISIBLE);
    }
  }

  /**
   * reloadActivity --- Activity の再起動
   *
   * @param activity
   */
  public void reloadActivity() {
    Intent intent = getIntent();

    overridePendingTransition(0, 0);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    finish();

    overridePendingTransition(0, 0);
    startActivity(intent);
  }

  /**
   * Activity 再起動ダイアログ
   */
  public void showReloadActivityDialog() {
    Resources res = getResources();
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setIcon(R.drawable.ic_launcher);
    alertDialogBuilder.setTitle(res.getText(R.string.app_name));
    alertDialogBuilder.setMessage(res.getText(R.string.reload_activity_alert));
    alertDialogBuilder.setPositiveButton(res.getText(R.string.reload_activity_alert_text),
        new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        reloadActivity();
      }
    });
    alertDialogBuilder.setCancelable(true);
    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.setCanceledOnTouchOutside(false);
    alertDialog.show();
  }


  /*****
   ********** アプリ内課金関連 - 購入ダイアログ関連
   ************************************************************/

  /**
   * キーの取り出し
   *
   * @return
   */
  public String getKey() {
    // EncryptByAES();
    SecretKeySpec key = generateKey(getPackageName()); // 鍵の生成
    // System.out.println("[getKey] pubKey: " +
    // getString(R.string.inapp_purchase_pubKey));
    String str = decode(getString(R.string.inapp_purchase_pubKey), key); // 復号化
    // System.out.println("[getKey] str: " + str);
    return str.substring(451, str.length() - 181);
  }

  // private void EncryptByAES() {
  // // Key
  // SecretKeySpec key = generateKey(getPackageName());
  //
  // String target = "";
  //
  // String encode = encode(target, key);
  // String decode = decode(encode, key);
  //
  // Log.d(TAG, "[KEY] Encode: " + encode);
  // Log.d(TAG, "[KEY] Decode: " + decode);
  // Log.d(TAG, "[KEY] Match: " + target.equals(decode));
  // }

  /**
   * Generating key
   *
   * @param sharedKey
   * @return
   */
  private static SecretKeySpec generateKey(String sharedKey) {
    byte[] bytes = new byte[128 / 8];
    byte[] keys = null;

    try {
      keys = sharedKey.getBytes("UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (int i = 0; i < sharedKey.length(); i++) {
      if (i >= bytes.length) {
        break;
      }
      bytes[i] = keys[i];
    }

    return new SecretKeySpec(bytes, "AES");
  }

  // /**
  // * Encode
  // * @return
  // */
  // private static String encode(String target, SecretKeySpec key) {
  // byte[] enc = null;
  // try {
  // Cipher cipher = Cipher.getInstance("AES");
  // cipher.init(Cipher.ENCRYPT_MODE, key);
  // enc = cipher.doFinal(target.getBytes("UTF-8"));
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // return Base64.encodeToString(enc, Base64.DEFAULT | Base64.NO_WRAP);
  // }

  /**
   * Decode
   */
  private static String decode(String target, SecretKeySpec key) {
    byte[] dec = null;
    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, key);
      dec = cipher.doFinal(Base64.decode(target.getBytes("UTF-8"), Base64.DEFAULT
          | Base64.NO_WRAP));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new String(dec);
  }

  private IabHelper iabHelper;
  private final String SKU_PREMIUM = "revolver_launcher_full_edition_key";

  /**
   * connectToGooglePlay --- Google Play への接続
   */
  private void connectToGooglePlay() {
    // Log.d(TAG, "[IAB] Request connecting...");
    iabHelper = new IabHelper(this, getKey()); // IabHelper の取得
    iabHelper.enableDebugLogging(false); // デバッグモードの有効化 (リリース時には false に)
    iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() { // 問い合わせ開始
      // *
      // AVD
      // だと
      // NullPointerException
      // が出る
      // (場合がある)
      @Override
      public void onIabSetupFinished(IabResult result) { // セットアップが完了したときに呼ばれる
        if (!result.isSuccess()) { // 失敗
          // Log.i(TAG, "[IAB] IAB の準備中に問題が発生しました: " +
          // result);
          return;
        }
        if (iabHelper == null) { // iabHelper が null だったら中止
          return;
        }
        // 問い合わせる
        // Log.i(TAG, "[IAB] 商品情報の取得リクエストします");
        // iabHelper.queryInventoryAsync(true, Arrays.asList(new
        // String[] {SKU_PREMIUM}), mGotInventoryListener);
        iabHelper.queryInventoryAsync(mGotInventoryListener);
      }
    });
  }

  // 問い合わせ結果の受取
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
      // Log.i(TAG, "[IAB] " + result);
      if (result.isFailure()) {
        // Log.i(TAG, "[IAB] アイテムの取得に失敗しました");
        return;
      }
      // Log.i(TAG, "[IAB] アイテムの取得に成功しました");

      // 商品詳細の取得
      // SkuDetails skuDetails = inv.getSkuDetails(SKU_PREMIUM);
      // Log.d(TAG, "[IAB] SkuDetails: " + skuDetails);
      // if (skuDetails != null) { // Google Play 側でアプリが (α or β含め)
      // 公開状態でないと取得できない
      // Log.d(TAG, "[IAB] Title: " + skuDetails.getTitle());
      // Log.d(TAG, "[IAB] Description: " + skuDetails.getDescription());
      // Log.d(TAG, "[IAB] Price: " + skuDetails.getPrice());
      // }

      // mIsPremium = inv.hasPurchase(SKU_PREMIUM);

      // String payload = ""; // ユーザごとに固有である必要がある (面倒いからそのうち。今は無効化)
      int requestCode = 10001; // リクエストコード

      // 機能解除キー購入ダイアログを表示
      iabHelper.launchPurchaseFlow(activity, SKU_PREMIUM, requestCode,
          mPurchaseFinishedListener);
      // iabHelper.launchPurchaseFlow(activity, SKU_PREMIUM, requestCode,
      // mPurchaseFinishedListener, payload);
    }
  };

  /**
   * onActivityResult --- 結果受け渡し (ないと OnIabPurchaseFinishedListener が呼ばれない)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // IabHelper の handleActivityResult に渡す
    if (resultCode != RESULT_CANCELED) {
      if (resultCode == RESULT_OK) { // 結果に問題がなければ
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
          super.onActivityResult(requestCode, resultCode, data);
        } else {
          // 何もしない
        }
      }
    }
    // if (resultCode == RESULT_CANCELED) {
    // Log.i(TAG, "[IAB] キャンセル");
    // }
  }

  // 購入完了後の処理
  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
      // Log.i(TAG, "[IAB] Result: " + result);
      // Log.i(TAG, "[IAB] Purchase: " + info);

      if (result.isFailure()) { // 失敗したら
        // Log.i(TAG, "[IAB] 購入に失敗しました");

        // switch (result.getResponse()) {
        // case IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED:
        // Log.i(TAG, "[IAB] ユーザーによってキャンセルされました");
        // break;
        // case IabHelper.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
        // Log.i(TAG, "[IAB] アイテムが利用できません");
        // break;
        // case IabHelper.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
        // Log.i(TAG, "[IAB] デベロッパー側で問題が発生しています");
        // break;
        // case IabHelper.BILLING_RESPONSE_RESULT_ERROR:
        // Log.i(TAG, "[IAB] 対応していない IAB バージョンのため、エラーが発生しました");
        // break;
        // case IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED: //
        // 購入済み
        // Log.i(TAG, "[IAB] 既に購入済みです");
        // break;
        // case IabHelper.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
        // Log.i(TAG, "[IAB] アイテムが不足しているため、消費に失敗しました");
        // break;
        // default:
        // break;
        // }
        return; // 終了
      }

      // Log.i(TAG, "[IAB] 購入が完了しました");

      if (info.getSku().equals(SKU_PREMIUM)) { // 購入したアイテムがプレミアムキーであったら
        putPurchasePremiumKeyResult(true);
      }

      // Activity の再起動
      showReloadActivityDialog();
    }
  };

  /**
   * 結果の保存
   */
  private void putPurchasePremiumKeyResult(boolean b) {
    Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    editor.putBoolean("kPremEd", b);
    editor.commit();
  }

  /**
   * disconnectFromGooglePlay --- 終了処理
   */
  public void disconnectFromGooglePlay() {
    if (iabHelper != null) {
      iabHelper.dispose();
    }
    iabHelper = null;
  }

  /**
   * トランジット時に広告表示を行う
   *
   * @return
   */
  public void showTransitAds() {
    // Log.d(TAG, "[Ads] showTransitAds");
    String key = "keyNumOfTransit";
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    int num = sharedPreferences.getInt(key, 0);
    if (++num >= 10) { // 登場頻度
      // 0に戻す
      Editor editor = sharedPreferences.edit();
      editor.putInt(key, 0);
      editor.commit();

      // Log.d(TAG, "[Ads] Load Interstitial Ad");
      displayInterstitial();
    } else {
      Editor editor = sharedPreferences.edit();
      editor.putInt(key, num);
      editor.commit();
    }
  }

  private InterstitialAd interstitialAd = null;
  private String interstitiaAdId = "ca-app-pub-1554537676397925/3383486491";

  /**
   * インタースティシャル広告の準備
   */
  private void setupInterstitialAd() {
    if (interstitialAd == null) {
      interstitialAd = new InterstitialAd(this);
      interstitialAd.setAdUnitId(interstitiaAdId);
      AdRequest adRequest = new AdRequest.Builder()
      .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Emulator
      .build();
      interstitialAd.loadAd(adRequest);
    }
  }

  /**
   * 広告の表示
   */
  public void displayInterstitial() {
    if (interstitialAd.isLoaded()) {
      interstitialAd.show();
    }
    interstitialAd = null;
  }






  /**
   * アプリ内課金 (Amazon)
   * - https://developer.amazon.com/public/apis/earn/in-app-purchasing/docs-v2/implementing-iap-2.0
   */

  /**
   * リスナの登録
   *    --- onCreate で呼ぶ
   */
  private void registerListenerForAmazonApps(SettingActivity activity) {
    if (getString(R.string.channel).matches(".*" + "Amazon" + ".*")) {
      PurchasingService.registerListener(this.getApplicationContext(), new AmznPurchasingListener(activity));
//      Log.i(TAG, "[IAB] onCreate: sandbox mode is:" + PurchasingService.IS_SANDBOX_MODE);
    }
  }

  /**
   *
   */
  private void amazonIabOnStart() {
    if (getString(R.string.channel).matches(".*" + "Amazon" + ".*")) {
//      final Set<String> productSkus = new HashSet<String>();
//      productSkus.add(AmznPurchasingListener.SKU_PREMIUM);
//      PurchasingService.getProductData(productSkus);  // アイテム情報の取得 (onProductDataResponse で結果受取)
    }
  }

  /**
   *
   */
  private void amazonIabOnResume() {
    if (getString(R.string.channel).matches(".*" + "Amazon" + ".*")) {
      // ユーザ情報の取得
//      PurchasingService.getUserData();
      //      Log.v(TAG, "[IAB] Validating SKUs with Amazon");

      /*
       * getPurchaseUpdates
       *  - 権利やサブスクリプションの同期
       *  - 権利の取り消し
       *  - 保留状態の回復
       *
       *  false: 最後に getPurchaseUpdates が呼ばれて以来の購入履歴が得られる (推奨)
       *  true:  消費者の購入履歴全てが得られる
       */
      PurchasingService.getPurchaseUpdates(AmznPurchasingListener.reset);  // onPurchaseUpdatesResponse で結果が返ってくる
    }
  }

  /**
   * 購入 (onResume)
   */
  private void purchaseOnAmazonApps() {
    PurchasingService.purchase(SKU_PREMIUM);  // onPurchaseResponse で結果受取
  }




  public void purchaseFullKey() {
    if (!activity.getString(R.string.channel).matches(".*" + "Amazon" + ".*")) {
//      Log.d(TAG, "[IAB] Google Play");
      connectToGooglePlay();
    } else {
//      Log.d(TAG, "[IAB] Amazon Apps");
      purchaseOnAmazonApps();
    }
  }
}
