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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * StarterBar 左サイズダイアログ
 * @author ku
 */
public class SettingParamsDialogSizeStarterBarLeft extends DialogFragment {
  private static final String TAG = "SettingParamsDialogSizeStarterBarLeft";
  
  private EditText barWidth, barHeight, barPosition;
  private SeekBar barWidthSb, barHeightSb, barPositionSb;

  private LayoutInflater factory;
  private ParameterManager parameterManager;
  private Context context;

  public SettingParamsDialogSizeStarterBarLeft(Context context, LayoutInflater layoutInflater) {
    this.factory = layoutInflater;
    parameterManager = new ParameterManager(context);
    this.context = context;
    bindAppsLauncherService();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    View inputView = factory.inflate(R.layout.dialog_size, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(context.getString(R.string.setting_starterbar_left_size_pos));
    builder.setView(inputView);
    setCancelable(false);
    
    // 各種 View 関連の取得
    barWidth = (EditText)inputView.findViewById(R.id.bar_width_et);
    barWidthSb = (SeekBar)inputView.findViewById(R.id.bar_width_sb);
    barHeight = (EditText)inputView.findViewById(R.id.bar_height_et);
    barHeightSb = (SeekBar)inputView.findViewById(R.id.bar_height_sb);
    barPosition = (EditText)inputView.findViewById(R.id.bar_position_et);
    barPositionSb = (SeekBar)inputView.findViewById(R.id.bar_position_sb);

    // 幅の設定
    barWidthSb.setProgress(parameterManager.getStarterBarWidthLeft());
    barWidthSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      // ツマミをドラッグしたとき
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        barWidth.setText(String.valueOf(barWidthSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミに触れたとき
      public void onStartTrackingTouch(SeekBar seekBar) {
        barWidth.setText(String.valueOf(barWidthSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミを離したとき
      public void onStopTrackingTouch(SeekBar seekBar) {
        barWidth.setText(String.valueOf(barWidthSb.getProgress()));
        redrawLeftBar();
      }
    });
    barWidth.setText(String.valueOf(parameterManager.getStarterBarWidthLeft()));
    barWidth.addTextChangedListener(new TextWatcher() {
      // 操作前の状態
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
      }
      // 操作中の状態
      public void onTextChanged(CharSequence s, int start, int count, int after) {
      }
      // 操作後の状態
      public void afterTextChanged(Editable s) {
        if (barWidth.getText().toString().equals("")) {
          barWidth.setText("0");
        }
        barWidthSb.setProgress(Integer.parseInt(barWidth.getText().toString()));
        barWidth.setSelection(barWidth.getText().toString().length());
        redrawLeftBar();
      }
    });

    // 初期値の設定
    barHeightSb.setProgress(parameterManager.getStarterBarHeightLeft());
    barPositionSb.setProgress(parameterManager.getStarterBarPositionLeft());

    // 高さの設定
    barHeightSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      // ツマミをドラッグしたとき
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        barHeight.setText(String.valueOf(barHeightSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミに触れたとき
      public void onStartTrackingTouch(SeekBar seekBar) {
        barHeight.setText(String.valueOf(barHeightSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミを離したとき
      public void onStopTrackingTouch(SeekBar seekBar) {
        barHeight.setText(String.valueOf(barHeightSb.getProgress()));
        redrawLeftBar();
      }
    });
    barHeight.setText(String.valueOf(barHeightSb.getProgress()));
    // エディットテキストの変化を受け取るリスナ
    barHeight.addTextChangedListener(new TextWatcher() {
      // 操作前の状態
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
      }
      // 操作中の状態
      public void onTextChanged(CharSequence s, int start, int count, int after) {
      }
      // 操作後の状態
      public void afterTextChanged(Editable s) {
        if (barHeight.getText().toString().equals("")) {
          barHeight.setText("0");
        }
        barHeightSb.setProgress(Integer.parseInt(barHeight.getText().toString()));
        barHeight.setSelection(barHeight.getText().toString().length());
        redrawLeftBar();
      }
    });

    // 位置の設定
    barPositionSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      // ツマミをドラッグしたとき
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        barPosition.setText(String.valueOf(barPositionSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミに触れたとき
      public void onStartTrackingTouch(SeekBar seekBar) {
        barPosition.setText(String.valueOf(barPositionSb.getProgress()));
        redrawLeftBar();
      }
      // ツマミを離したとき
      public void onStopTrackingTouch(SeekBar seekBar) {
        barPosition.setText(String.valueOf(barPositionSb.getProgress()));
        redrawLeftBar();
      }
    });
    barPosition.setText(String.valueOf(barPositionSb.getProgress()));
    // エディットテキストの変化を受け取るリスナ
    barPosition.addTextChangedListener(new TextWatcher() {
      // 操作前の状態
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
      }
      // 操作中の状態
      public void onTextChanged(CharSequence s, int start, int count, int after) {
      }
      // 操作後の状態
      public void afterTextChanged(Editable s) {
        if (barPosition.getText().toString().equals("")) {
          barPosition.setText("0");
        }
        barPositionSb.setProgress(Integer.parseInt(barPosition.getText().toString()));
        barPosition.setSelection(barPosition.getText().toString().length());
        redrawLeftBar();
      }
    });

    // 確定が押されたらデータの更新
    builder.setPositiveButton(getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        
        // バーの幅を保存
        parameterManager.putStarterBarWidthLeft(Integer.parseInt(barWidth.getText().toString()));
        
        // バーの高さを保存
        parameterManager.putStarterBarHeightLeft(Integer.parseInt(barHeight.getText().toString()));
        
        // バーの位置を保存
        parameterManager.putStarterBarPositionLeft(Integer.parseInt(barPosition.getText().toString()));

        // バーの再起動
        redrawBars();
        unbindAppsLauncherService();
        
      }
    });
    // キャンセルが押されたらダイアログを閉じる
    builder.setNegativeButton(getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dismiss();
        redrawBars();
        unbindAppsLauncherService();
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
  
  /**
   * バーの描画関連
   *************************/
  private AppsLauncherService appsLauncherService;
  
  ServiceConnection connection = new ServiceConnection() {  
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
      Log.d(TAG, "onServiceConnected()");
      appsLauncherService = ((AppsLauncherService.AppsLauncherBinder)binder).getService();
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.d(TAG, "onServiceDisconnected()");
      appsLauncherService = null;
    }
  };
  
  private void bindAppsLauncherService() {
    Log.d(TAG, "bindAppsLauncherService()");
    if (parameterManager.getStarterBarServiceStatus()) {
      Intent intent = new Intent(context, AppsLauncherService.class);
      context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
  }
  private void redrawBars() {
    if (appsLauncherService != null) {
      appsLauncherService.redrawBars();
    }
  }
  private void redrawLeftBar() {
    if (appsLauncherService != null) {
      appsLauncherService.redrawLeftBar(barWidthSb.getProgress(), barHeightSb.getProgress(), parameterManager.getStarterBarColor(), barPositionSb.getProgress());
    }
  }
  private void unbindAppsLauncherService() {
    Log.d(TAG, "unbindAppsLauncherService()");
    if (appsLauncherService != null) {
      context.unbindService(connection);
    }
  }
}