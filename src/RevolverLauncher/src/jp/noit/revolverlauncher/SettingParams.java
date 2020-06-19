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

import java.util.List;

import jp.noit.revolverlauncher.ParameterManager.BackKeyAction;
import jp.noit.revolverlauncher.util.billing.IabHelper;
import jp.noit.revolverlauncher.util.billing.IabResult;
import jp.noit.revolverlauncher.util.billing.Inventory;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * パラメータ設定部分
 * @author ku, tag
 *
 */
public class SettingParams {

  private static final String TAG = "SettingParams";

  // リストビューのデータ管理
  private TextView tvRevolvers, tvRevolverRadius, tvBullets, tvIconSize, tvActionOfBack;
  private ToggleButton tbStarterBarLeftState, tbStarterBarRightState, tbAutorunService, tbAnimationState, tbBack, tbHome, tbRecentApps, tbNotifications, tbClock, tbResMeter;
  private View vAccessoriesColor;

  private FragmentManager fragmentManager;
  private LayoutInflater layoutInflater;

  private ParameterManager parameterManager;

  private SharedPreferences sharedPreferences;

  private Activity _activity;
  private Context context;

  public SettingParams(Activity activity) {
    //Context context = activity.getBaseContext();
    this.fragmentManager = activity.getFragmentManager();
    layoutInflater = (LayoutInflater)activity.getLayoutInflater();
    this._activity = activity;
    this.context = activity.getApplicationContext();

    // ParameterManager の初期化
    parameterManager = new ParameterManager(context);



    /*
     * ランチャー起動ボタンの設定 (ActionBar がない Android 4.0 未満向け)
     */
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {  // Android 4.2 未満向け
      ToggleButton tbLauncher = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableLauncher);
      tbLauncher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          if (isChecked) {  // On のとき
            startStarterBarService();  // StarterBar サービスの起動
          } else {  // Off のとき
            stopStarterBarService();  // StarterBar サービスの停止
          }
        }
      });
      if (parameterManager.getStarterBarServiceStatus()) {  // 保存状態を確認し、起動する必要がある (true) ならば
        tbLauncher.setChecked(true);
      }
    } else {  // Android 4.0 以上の場合には項目を消す
      LinearLayout llLauncher = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableLauncher);
      llLauncher.setVisibility(View.GONE);
      LinearLayout llLauncherBr = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableLauncher_borderline);
      llLauncherBr.setVisibility(View.GONE);
    }


    // 設定項目の取得
    LinearLayout llAccessoriesColor          = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_launcherAccessoryColor);
    LinearLayout llLauncherBgColor           = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_launcherBgColor);
    LinearLayout llRevolvers                 = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_revolvers);
    LinearLayout llRevolverRadius            = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_revolverRadius);
    LinearLayout llBullets                   = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_bullets);
    LinearLayout llIconSize                  = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_iconSize);
    //    LinearLayout llStarterBarLeftState       = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_starterBarLeftState);
    //    LinearLayout llStarterBarRightState      = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_starterBarRightState);
    LinearLayout llStarterBarLeftSizeAndPos  = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_starterBarLeftSizeAndPos);
    LinearLayout llStarterBarRightSizeAndPos = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_starterBarRightSizeAndPos);
    LinearLayout llStarterBarColor           = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_starterBarColor);
    //    LinearLayout llAutorunService            = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_autorunService);
    //    LinearLayout llAnimationState            = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_animatonState);
    //    LinearLayout llCloseLauncherWithBackkey  = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_closeLauncherWithBackkey);
    LinearLayout llAboutApp = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_aboutApp);
    LinearLayout llUpgrade = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_upgrade);
    LinearLayout llUpgradeBorder = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_upgrade_border);
    LinearLayout llCustomeClockAndDate = (LinearLayout)_activity.findViewById(R.id.linearLayout_custom_clock_and_date);

    LinearLayout llActionOfBack               = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_actionOfBackkey);
    
    /* アクセサリ色 */
    llAccessoriesColor.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        parameterManager.changeAccessoriesColorIsBlack();
        vAccessoriesColor.setBackgroundColor(parameterManager.getAccessoriesColorIsBlack() ? Color.BLACK : Color.WHITE);
      }
    });

    /* 背景色 */
    llLauncherBgColor.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new SettingParamsDialogColorBackground(context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    /* 連装数 */
    //    llRevolvers.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        DialogFragment dialogFragment = new DialogNumRevolover(_activity, context, layoutInflater);
    //        dialogFragment.show(fragmentManager, null);
    //      }
    //    });
    // 非表示に
    llRevolvers.setVisibility(View.GONE);
    LinearLayout llRevolversBorder = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_revolvers_borderline);
    llRevolversBorder.setVisibility(View.GONE);

    /* リボルバーの半径 */
    llRevolverRadius.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new DialogRadius(_activity, context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    /* 弾数 */
    //    llBullets.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        DialogFragment dialogFragment = new DialogNumBullet(_activity, context, layoutInflater);
    //        dialogFragment.show(fragmentManager, null);
    //      }
    //    });
    // 非表示に
    llBullets.setVisibility(View.GONE);
    LinearLayout llBulletsBorder = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_bullets_borderline);
    llBulletsBorder.setVisibility(View.GONE);

    /* アイコンサイズ */
    llIconSize.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new DialogBulletSize(_activity, context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    //    /* 左側バーの状態 */
    //    llStarterBarLeftState.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        
    //      }
    //    });
    //    
    //    /* 左側バーの状態 */
    //    llStarterBarRightState.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        
    //      }
    //    });

    /* スターターバー (左) の大きさと位置 */
    llStarterBarLeftSizeAndPos.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new SettingParamsDialogSizeStarterBarLeft(context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    /* スターターバー (右) の大きさと位置 */
    llStarterBarRightSizeAndPos.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new SettingParamsDialogSizeStarterBarRight(context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    /* スターターバーの色 */
    llStarterBarColor.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment dialogFragment = new SettingParamsDialogColorStarterBar(context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    //    /* 自動起動サービス */
    //    llAutorunService.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        parameterManager.changeBootServiceStatus();
    //      }
    //    });

    //    /* アニメーション状態 */
    //    llAnimationState.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        
    //      }
    //    });
    //    
    //    /* Back キーの挙動 */
    //    llCloseLauncherWithBackkey.setOnClickListener(new OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        
    //      }
    //    });

    /* アプリについて */
    llAboutApp.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        _activity.startActivity(new Intent(context, AboutApp.class));
      }
    });

    /* 完全版購入画面 */
    if ( parameterManager.getEdition() ) {  // 購入済みの場合
      llUpgrade.setVisibility(View.GONE);
      llUpgradeBorder.setVisibility(View.GONE);
    } else {
      llUpgrade.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          ((SettingActivity)_activity).purchaseFullKey();
        }
      });

    }

    /* 戻るキーの処理 */
    llActionOfBack.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        tvActionOfBack.setText(parameterManager.changeActionOfBackkeyStatus().getResId());
      }
    });


    /* アプリ内課金状態切替 (デバッグ用) */
    if (context.getString(R.string.channel).substring(5, 18).equals("IKSNiostn".substring(context.getString(R.string.channel).length() / 21) + "OOSSH")) {
      LinearLayout llChangeChannel = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_debug_change_channel);
      llChangeChannel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (parameterManager.getEdition()) {
            parameterManager.putEdition(false);
          } else {
            parameterManager.putEdition(true);
          }

          // Activity の再起動
          ((SettingActivity)_activity).showReloadActivityDialog();
        }
      });
    } else {  // リリース版
      LinearLayout llChangeChannelBorder = (LinearLayout)_activity.findViewById(R.id.linearLayout_border_debug_0);
      llChangeChannelBorder.setVisibility(View.GONE);
      LinearLayout llChangeChannel = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_debug_change_channel);
      llChangeChannel.setVisibility(View.GONE);

      // アプリ内課金の状態チェック (AVD だと、ぬるぽで落ちるのでコメントアウトすること。)
      checkLicense();
    }




    // データ表示部分の取得
    tvRevolvers      = (TextView)_activity.findViewById(R.id.textView_value_revolvers);
    tvRevolverRadius = (TextView)_activity.findViewById(R.id.textView_value_revolverRadius);
    tvBullets        = (TextView)_activity.findViewById(R.id.textView_value_bullets);
    tvIconSize       = (TextView)_activity.findViewById(R.id.textView_value_iconSize);
    tvActionOfBack   = (TextView)_activity.findViewById(R.id.textView_state_actionOfBackkey);
    vAccessoriesColor = (View)_activity.findViewById(R.id.view_value_launcherAccessoryColor);

    // 初期化
    tvRevolvers.setText(String.valueOf(parameterManager.getRevolverNum()));
    tvRevolverRadius.setText(String.valueOf(parameterManager.getRevolverRadius()));
    tvBullets.setText(String.valueOf(parameterManager.getBulletNum()));
    tvIconSize.setText(String.valueOf(parameterManager.getIconSize()));
    vAccessoriesColor.setBackgroundColor(parameterManager.getAccessoriesColorIsBlack() ? Color.BLACK : Color.WHITE);

    BackKeyAction strActionOfBack = parameterManager.getActionOfBackkeyStatus();
    tvActionOfBack.setText(strActionOfBack.getResId());
    
    // トグルボタンの取得
    tbAnimationState           = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_animatonState);
    tbAutorunService           = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_autorunService);

    tbStarterBarLeftState      = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_starterBarLeft);
    tbStarterBarRightState     = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_starterBarRight);
    //    tbCloseLauncherWithBackkeyOnDevice = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_closeLauncherWithBack);

    tbBack                     = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableBackkey);
    tbHome                     = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableHomekey);
    tbRecentApps               = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableRecentAppskey);
    tbNotifications            = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableNotificationskey);
    //    tbCloseLauncherWithBackkeyOnLauncher = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_closeLauncherWithBackkeyOnLauncher);

    tbClock                    = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableClock);
    tbResMeter                 = (ToggleButton)_activity.findViewById(R.id.toggleButton_state_enableResMeter);




    /* 左側バーの状態 */
    tbStarterBarLeftState.setChecked(parameterManager.getStatusStarterBarLeft());
    tbStarterBarLeftState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!tbStarterBarRightState.isChecked() && !isChecked) {
          showStarterBarDisabledDialog();
          tbStarterBarLeftState.setChecked(true);
        } else {
          parameterManager.putStatusStarterBarLeft(isChecked);
          bindAppsLauncherService();
        }
      }
    });

    /* 右側バーの状態 */
    tbStarterBarRightState.setChecked(parameterManager.getStatusStarterBarRight());
    tbStarterBarRightState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!tbStarterBarLeftState.isChecked() && !isChecked) {
          showStarterBarDisabledDialog();
          tbStarterBarRightState.setChecked(true);
        } else {
          parameterManager.putStatusStarterBarRight(isChecked);
          bindAppsLauncherService();
        }
      }
    });

    /* 自動起動サービス */
    tbAutorunService.setChecked(parameterManager.getBootServiceStatus());
    tbAutorunService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeBootServiceStatus();
      }
    });

    /* アニメーション */
    tbAnimationState.setChecked(parameterManager.getEnableAnimationStatus());
    tbAnimationState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeEnableAnimationStatus();
      }
    });

    /* 戻るボタンの挙動 */
    //    tbCloseLauncherWithBackkeyOnLauncher.setChecked(parameterManager.getCloseLauncherWithBackKeyOnLauncherStatus());
    //    tbCloseLauncherWithBackkeyOnLauncher.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    //      @Override
    //      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //        parameterManager.changeCloseLauncherWithBackKeyOnLauncherStatus();
    //      }
    //    });


    /* 本体のバックキーでランチャーを閉じる */
    //    tbCloseLauncherWithBackkeyOnDevice.setChecked(parameterManager.getCloseLauncherWithBackKeyOnDeviceStatus());
    //    tbCloseLauncherWithBackkeyOnDevice.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    //      @Override
    //      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //        if (isChecked && !parameterManager.getCloseLauncherWithBackKeyOnLauncherStatus()) {  // もし、有効化しようとしたときに、「ランチャー上の戻るキーでランチャーを閉じる」が有効だったとき
    //          showAlertDialog(R.string.dialog_backkey_on_device_alert_title, R.string.dialog_backkey_on_device_alert_message);  // 警告を出す
    //          tbCloseLauncherWithBackkeyOnDevice.setChecked(false);  // ToggleButton を戻す
    //        } else {
    //          parameterManager.putCloseLauncherWithBackKeyOnDeviceStatus(isChecked);
    //        }
    //      }
    //    });

    /* Back キー (Android 4.1 以降) */
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      if (parameterManager.getSpKeyBackStatus()) {
        parameterManager.changeSpKeyBackStatus();
      }
      LinearLayout llBack                      = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableBackkey);
      LinearLayout llBackBorder                = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableBackkey_borderline);
      llBack.setVisibility(View.GONE);
      llBackBorder.setVisibility(View.GONE);
    } else {
      tbBack.setChecked(parameterManager.getSpKeyBackStatus());
      tbBack.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          parameterManager.changeSpKeyBackStatus();
        }
      });
    }

    /* Home */
    tbHome.setChecked(parameterManager.getSpKeyHomeStatus());
    tbHome.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeSpKeyHomeStatus();
      }
    });

    /* Recent Apps */
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      if (parameterManager.getSpKeyRecentAppsStatus()) {
        parameterManager.changeSpKeyRecentAppsStatus();
      }
      LinearLayout llRecentApps                = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableRecentAppskey);
      LinearLayout llRecentAppsBorder          = (LinearLayout)_activity.findViewById(R.id.linearLayout_item_enableRecentApps_borderline);
      llRecentApps.setVisibility(View.GONE);
      llRecentAppsBorder.setVisibility(View.GONE);
    } else {
      tbRecentApps.setChecked(parameterManager.getSpKeyRecentAppsStatus());
      tbRecentApps.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          parameterManager.changeSpKeyRecentAppsStatus();
        }
      });
    }

    /* Notifications */
    tbNotifications.setChecked(parameterManager.getSpKeyNotificationsStatus());
    tbNotifications.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeSpKeyNotificationsStatus();
      }
    });

    /* 時計 */
    tbClock.setChecked(parameterManager.getClockStatus());
    tbClock.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeClockStatus();
      }
    });

    /* 時計と日付のカスタマイズ */
    llCustomeClockAndDate.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        CustomClockAndDateDialogFragment dialogFragment = new CustomClockAndDateDialogFragment(_activity, context, layoutInflater);
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(fragmentManager, null);
      }
    });

    /* リソースメータ */
    tbResMeter.setChecked(parameterManager.getResMeterStatus());
    tbResMeter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.changeResMeterStatus();
      }
    });



    // 広告の準備
    if ( !parameterManager.getEdition() ) {  // 通常版
      setupAds(activity);
    } else {
      // LinearLayout のルックアップ
      LinearLayout llAdArea1 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_1);
      LinearLayout llAdArea2 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_2);
      LinearLayout llAdArea3 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_3);
      llAdArea1.setVisibility(View.GONE);
      llAdArea2.setVisibility(View.GONE);
      llAdArea3.setVisibility(View.GONE);
    }

  }


  // 広告
  private AdView adView1 = null, adView2 = null, adView3 = null;
  private String adId1 = "ca-app-pub-1554537676397925/4860219691";
  private String adId2 = "ca-app-pub-1554537676397925/9290419298";
  private String adId3 = "ca-app-pub-1554537676397925/3243885698";

  /**
   * 広告のセットアップ
   * @param activity
   */
  public void setupAds(Activity activity) {
    /**
     * AdMob (広告)
     *************************/
    // adView を作成する
    adView1 = new AdView(activity);
    adView1.setAdUnitId(adId1);
    adView1.setAdSize(AdSize.BANNER);
    adView2 = new AdView(activity);
    adView2.setAdUnitId(adId2);
    adView2.setAdSize(AdSize.BANNER);
    adView3 = new AdView(activity);
    adView3.setAdUnitId(adId3);
    adView3.setAdSize(AdSize.BANNER);

    // LinearLayout のルックアップ
    LinearLayout llAdArea1 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_1);
    LinearLayout llAdArea2 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_2);
    LinearLayout llAdArea3 = (LinearLayout)activity.findViewById(R.id.linearLayout_adspace_3);

    // 追加
    llAdArea1.addView(adView1);
    llAdArea2.addView(adView2);
    llAdArea3.addView(adView3);

    // 広告のロード
    adView1.loadAd(new AdRequest.Builder().build());
    adView2.loadAd(new AdRequest.Builder().build());
    adView3.loadAd(new AdRequest.Builder().build());
  }

  /**
   * 広告の一時停止 (super.onPause の前)
   */
  public void pauseAds() {
    if (adView1 != null) {
      adView1.pause();
    }
    if (adView2 != null) {
      adView2.pause();
    }
    if (adView3 != null) {
      adView3.pause();
    }
  }

  /**
   * 広告の再開 (super.onResume の後)
   */
  public void resumeAds() {
    if (adView1 != null) {
      adView1.resume();
    }
    if (adView2 != null) {
      adView2.resume();
    }
    if (adView3 != null) {
      adView3.resume();
    }
  }

  /**
   * 広告の破棄 (super.onDestroy の前)
   */
  public void destroyAds() {
    if (adView1 != null) {
      adView1.destroy();
      adView1 = null;
    }
    if (adView2 != null) {
      adView2.destroy();
      adView2 = null;
    }
    if (adView3 != null) {
      adView3.destroy();
      adView3 = null;
    }
  }

  //  /**
  //   * DialogNumRevolver --- 連装数用ダイアログ
  //   * @author ku, tag
  //   *
  //   */
  //  class DialogNumRevolover extends DialogFragment {
  //
  //    private Activity activity;
  //    private LayoutInflater factory;
  //    private ParameterManager parameterManager;
  //
  //    public DialogNumRevolover(Activity activity, Context context, LayoutInflater layoutInflater) {
  //      this.activity = activity;
  //      this.factory = layoutInflater;
  //      parameterManager = new ParameterManager(context);
  //    }
  //
  //    @Override
  //    public Dialog onCreateDialog(Bundle savedInstanceState) {
  //      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
  //      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
  //      builder.setTitle(activity.getString(R.string.setting_revolvers));
  //      builder.setView(inputView);
  //
  //      // NumberPicker の初期値の設定
  //      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
  //      np.setMinValue(1);  // 最小値
  //      np.setMaxValue(12);  // 最大値
  //      np.setValue(parameterManager.getRevolverNum());
  //
  //      // 確定が押されたらデータの更新
  //      builder.setPositiveButton(activity.getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
  //        public void onClick(DialogInterface dialog, int id) {
  //          parameterManager.putRevolverNum(np.getValue());
  //          tvRevolvers.setText(String.valueOf(parameterManager.getRevolverNum()));
  //        }
  //      });
  //      // キャンセルが押されたらダイアログを閉じる
  //      builder.setNegativeButton(activity.getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
  //        public void onClick(DialogInterface dialog, int id) {
  //          dismiss();
  //        }
  //      });
  //
  //      return builder.create();
  //    }
  //  }

  /**
   * DialogRadius --- ランチャーの半径
   * @author ku, tag
   *
   */
  class DialogRadius extends DialogFragment {

    private Activity activity;
    private LayoutInflater factory;
    private ParameterManager parameterManager;

    public DialogRadius(Activity activity, Context context, LayoutInflater layoutInflater) {
      this.activity = activity;
      this.factory = layoutInflater;
      parameterManager = new ParameterManager(context);
    }

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(activity.getString(R.string.setting_revolver_rad));
      builder.setView(inputView);

      final int min = 100;
      final int max = 1000;
      final int step = 10;

      // NumberPicker の初期値の設定
      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
      np.setMinValue(0);  // 最小値
      np.setMaxValue((max - min) / step);  // 最大値
      String[] valueSet = new String[(max - min) / step + 1];
      for (int i = 0; i < (max - min) / step + 1; i++) {
        valueSet[i] = String.valueOf(min + step * i);
      }
      np.setDisplayedValues(valueSet);
      np.setValue((parameterManager.getRevolverRadius() - min) / step);

      // 確定が押されたらデータの更新
      builder.setPositiveButton(activity.getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          parameterManager.putRevolverRadius(min + np.getValue() * step);
          tvRevolverRadius.setText(String.valueOf(parameterManager.getRevolverRadius()));
        }
      });
      // キャンセルが押されたらダイアログを閉じる
      builder.setNegativeButton(activity.getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
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

  //  /**
  //   * DialogNumBullet --- 弾の数
  //   * @author ku, tag
  //   *
  //   */
  //  class DialogNumBullet extends DialogFragment {
  //
  //    private Activity activity;
  //    private LayoutInflater factory;
  //    private ParameterManager parameterManager;
  //
  //    public DialogNumBullet(Activity activity, Context context, LayoutInflater layoutInflater) {
  //      this.activity = activity;
  //      this.factory = layoutInflater;
  //      parameterManager = new ParameterManager(context);
  //    }
  //
  //    @Override
  //    public Dialog onCreateDialog(Bundle savedInstanceState) {
  //      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
  //      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
  //      builder.setTitle(activity.getString(R.string.setting_bullets));
  //      builder.setView(inputView);
  //
  //      // NumberPicker の初期値の設定
  //      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
  //      np.setMinValue(2);  // 最小値
  //      np.setMaxValue(24);  // 最大値
  //      np.setValue(parameterManager.getBulletNum());
  //
  //      // 確定が押されたらデータの更新
  //      builder.setPositiveButton(activity.getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
  //        public void onClick(DialogInterface dialog, int id) {
  //          parameterManager.putBulletNum(np.getValue());
  //          tvBullets.setText(String.valueOf(parameterManager.getBulletNum()));
  //        }
  //      });
  //      // キャンセルが押されたらダイアログを閉じる
  //      builder.setNegativeButton(activity.getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
  //        public void onClick(DialogInterface dialog, int id) {
  //          dismiss();
  //        }
  //      });
  //
  //      return builder.create();
  //    }
  //  }



  /**
   * DialogBulletSize --- 弾のサイズ
   * @author ku, tag
   *
   */
  class DialogBulletSize extends DialogFragment {

    private Activity activity;
    private LayoutInflater factory;
    private ParameterManager parameterManager;

    public DialogBulletSize(Activity activity, Context context, LayoutInflater layoutInflater) {
      this.activity = activity;
      this.factory = layoutInflater;
      parameterManager = new ParameterManager(context);
    }

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

      final View inputView = factory.inflate(R.layout.dialog_numberpicker, null);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(activity.getString(R.string.setting_bullets_size));
      builder.setView(inputView);

      final int min = 10;
      final int max = 500;
      final int step = 10;

      // NumberPicker の初期値の設定
      final NumberPicker np = (NumberPicker) inputView.findViewById(R.id.numberPicker);
      np.setMinValue(0);  // 最小値
      np.setMaxValue((max - min) / step);  // 最大値
      String[] valueSet = new String[(max - min) / step + 1];
      for (int i = 0; i < (max - min) / step + 1; i++) {
        valueSet[i] = String.valueOf(min + step * i);
      }
      np.setDisplayedValues(valueSet);
      np.setValue((parameterManager.getIconSize() - min) / step);

      // 確定が押されたらデータの更新
      builder.setPositiveButton(activity.getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          parameterManager.putIconSize(min + np.getValue() * step);
          tvIconSize.setText(String.valueOf(parameterManager.getIconSize()));
        }
      });
      // キャンセルが押されたらダイアログを閉じる
      builder.setNegativeButton(activity.getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
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
   * バーの描画関連
   *************************/
  private AppsLauncherService appsLauncherService;

  ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
      //      Log.d(TAG, "onServiceConnected()");
      appsLauncherService = ((AppsLauncherService.AppsLauncherBinder)binder).getService();
      vibivilityOfBars();  // 非表示を行う
      unbindAppsLauncherService();  // アンバインド
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
      //      Log.d(TAG, "onServiceDisconnected()");
      appsLauncherService = null;
    }
  };

  private void bindAppsLauncherService() {  // これだけ呼べばバーの表示切り替え可能。
    //    Log.d(TAG, "bindAppsLauncherService()");
    if (parameterManager.getStarterBarServiceStatus()) {
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
  }
  private void vibivilityOfBars() {
    //    Log.d(TAG, "vibivilityOfBars - appsLauncherService: " + appsLauncherService);
    if (appsLauncherService != null) {
      appsLauncherService.vibivilityOfBars();
    }
  }
  private void unbindAppsLauncherService() {
    //    Log.d(TAG, "unbindAppsLauncherService()");
    context.unbindService(connection);
  }



  /**
   * showStarterBarsDisabledDialog --- 両スターターバーが非表示となっていることを通知するダイアログ
   */
  private void showStarterBarDisabledDialog() {
    Resources res = context.getResources();
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(_activity);
    alertDialogBuilder.setIcon(R.drawable.ic_alert);
    alertDialogBuilder.setTitle(res.getText(R.string.staterbars_disabled_alert_dialog_title));
    alertDialogBuilder.setMessage(res.getText(R.string.staterbars_disabled_alert_dialog_message));
    alertDialogBuilder.setPositiveButton(res.getText(R.string.setting_ok), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });
    alertDialogBuilder.setCancelable(true);
    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.setCanceledOnTouchOutside(false);
    alertDialog.show();
  }



  /**
   * showAlertDialog --- 警告ダイアログ
   */
  private void showAlertDialog(int titleId, int messageId) {
    Resources res = context.getResources();
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(_activity);
    alertDialogBuilder.setIcon(R.drawable.ic_alert);
    alertDialogBuilder.setTitle(res.getText(titleId));
    alertDialogBuilder.setMessage(res.getText(messageId));
    alertDialogBuilder.setPositiveButton(res.getText(R.string.setting_ok), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });
    alertDialogBuilder.setCancelable(true);
    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.setCanceledOnTouchOutside(false);
    alertDialog.show();
  }

  /**
   * サービスが起動中かどうか確認
   * @param serviceName サービス名
   * @return
   */
  private boolean isServiceRunning(Context context, Class<?> cls) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    for (RunningServiceInfo info : services) {
      if (cls.getName().equals(info.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * StarterBar のサービスを起動
   */
  private void startStarterBarService() {
    //    Log.d(TAG, "startStarterBarService: " + isServiceRunning(context, AppsLauncherService.class));
    if (!isServiceRunning(context, AppsLauncherService.class)) {  // サービスが既に起動していないかを確認
      parameterManager.putStarterBarServiceStatus(true);
      // StarterBar サービスの起動
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.startService(intent);
    }
  }
  /**
   * StarterBar のサービスを停止
   */
  private void stopStarterBarService() {
    //    Log.d(TAG, "stopStarterBarService: " + isServiceRunning(context, AppsLauncherService.class));
    if (isServiceRunning(context, AppsLauncherService.class)) {  // サービスが起動しているか確認
      parameterManager.putStarterBarServiceStatus(false);
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.stopService(intent);

    }
  }





  /*****
   **********
   * アプリ内課金関連
   *  - ライセンスチェック (Google Play)
   *      再インストール時や払い戻し後のキー復元
   *******************************************************
   ************************************************************/

  private IabHelper iabHelper;
  private final String SKU_PREMIUM = "revolver_launcher_full_edition_key";

  /**
   * checkLicense --- ライセンスのチェック (別の端末に入れたり、再インストールした場合に完全版キーを復元する)
   * @return
   */
  public void checkLicense() {

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    // フル版
    if (context.getString(R.string.channel).substring(5, 18).equals("IKSNiostn".substring(context.getString(R.string.channel).length() / 21) + "OOSSH")) {
      Editor editor = sharedPreferences.edit();
      editor.putBoolean("successIabRequest", true);
      //      editor.putBoolean("kPremEd", true);
      editor.commit();
      return;
    }

    // 通常版
    if (!_activity.getString(R.string.channel).matches(".*" + "Amazon" + ".*")) {
      iabHelper = new IabHelper(context, ((SettingActivity)_activity).getKey());  // IabHelper の取得
      iabHelper.enableDebugLogging(false);  // デバッグモードの有効化 (リリース時には false に)
      iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {  // 問い合わせ開始  (AVD だと NullPointerException が出る)
        @Override
        public void onIabSetupFinished(IabResult result) {  // セットアップが完了したときに呼ばれる
          if (!result.isSuccess()) {  // 失敗
            //          Log.i(TAG, "[IAB] IAB の準備中に問題が発生しました: " + result);
            return;
          }
          if (iabHelper == null) {  // iabHelper が null だったら中止
            return;
          }
          // 問い合わせる
          //        Log.i(TAG, "[IAB] 商品情報の取得リクエストします");
          //iabHelper.queryInventoryAsync(true, Arrays.asList(new String[] {SKU_PREMIUM}), mGotInventoryListener);
          iabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
              //            Log.i(TAG, "[IAB] " + result);
              if (result.isFailure()) {
                //              Log.i(TAG, "[IAB] アイテムの取得に失敗しました");
                return;
              }

              //            Log.i(TAG, "[IAB] アイテムの取得に成功しました");

              // 通信成功したことを記録 (通信失敗した場合には、次回起動時にリクエストを行う)
              Editor editor = sharedPreferences.edit();
              editor.putBoolean("successIabRequest", true);
              editor.commit();

              //            Log.i(TAG, "[IAB] 完全版の購入状況: " + inv.getPurchase(SKU_PREMIUM));

              // 購入済みか確認
              if (inv.getPurchase(SKU_PREMIUM) != null) {  // 購入済み
                if (!parameterManager.getEdition()) {  // データとして保存されていない場合
                  parameterManager.putEdition(true);
                  ((SettingActivity)_activity).showReloadActivityDialog();  // 再描画
                }
              } else {  // 未購入
                parameterManager.putEdition(false);
              }

            }
          });
        }
      });
    }
  }

}
