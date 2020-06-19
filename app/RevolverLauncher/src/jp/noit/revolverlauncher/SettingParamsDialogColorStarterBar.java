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
import android.graphics.Color;
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
 * StarterBar 色ダイアログ
 * @author ku
 */
public class SettingParamsDialogColorStarterBar extends DialogFragment {
  private static final String TAG = "SettingParamsDialogColorStarterBar";

  private SeekBar sbR, sbG, sbB,sbA;
  private EditText etR, etG, etB, etA;
  
  private LayoutInflater factory;
  private ParameterManager parameterManager;
  private Context context;
  
  public SettingParamsDialogColorStarterBar(Context context, LayoutInflater layoutInflater) {
    this.factory = layoutInflater;
    parameterManager = new ParameterManager(context);
    this.context = context;
    bindAppsLauncherService();
  }
  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    
    View inputView = factory.inflate(R.layout.dialog_color, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(context.getString(R.string.setting_starterbar_color));
    builder.setView(inputView);
    setCancelable(false);

    // 各種 View 関連の取得
    sbR = (SeekBar)inputView.findViewById(R.id.paramR);
    etR = (EditText)inputView.findViewById(R.id.paramEtR);
    sbG = (SeekBar)inputView.findViewById(R.id.paramG);
    etG = (EditText)inputView.findViewById(R.id.paramEtG);
    sbB = (SeekBar)inputView.findViewById(R.id.paramB);
    etB = (EditText)inputView.findViewById(R.id.paramEtB);
    sbA = (SeekBar)inputView.findViewById(R.id.paramA);
    etA = (EditText)inputView.findViewById(R.id.paramEtA);

    
    // 背景部分の初期色
    //changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));

    // 赤色の設定
    sbR.setProgress(parameterManager.getStarterBarColorRed());
    sbR.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etR.setText(String.valueOf(sbR.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            etR.setText(String.valueOf(sbR.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
            etR.setText(String.valueOf(sbR.getProgress()));
            redrawLeftRightBar();
          }
        });
    etR.setText(String.valueOf(sbR.getProgress()));
    // エディットテキストの変化を受け取るリスナ
    etR.addTextChangedListener(new TextWatcher() {
        // 操作前の状態
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
        }
        // 操作中の状態
        public void onTextChanged(CharSequence s, int start, int count, int after) {
        }
        // 操作後の状態
        public void afterTextChanged(Editable s) {
          if(etR.getText().toString().equals("")) {
            etR.setText("0");
          }
          sbR.setProgress(Integer.parseInt(etR.getText().toString()));
          etR.setSelection(etR.getText().toString().length());
        }
      });

    // 緑色の設定
    sbG.setProgress(parameterManager.getStarterBarColorGreen());
    sbG.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etG.setText(String.valueOf(sbG.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            etG.setText(String.valueOf(sbG.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
            etG.setText(String.valueOf(sbG.getProgress()));
            redrawLeftRightBar();
          }
        });
    etG.setText(String.valueOf(sbG.getProgress()));
    // エディットテキストの変化を受け取るリスナ
    etG.addTextChangedListener(new TextWatcher() {
        // 操作前の状態
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
        }
        // 操作中の状態
        public void onTextChanged(CharSequence s, int start, int count, int after) {
        }
        // 操作後の状態
        public void afterTextChanged(Editable s) {
          if(etG.getText().toString().equals(""))
            etG.setText("0");
          sbG.setProgress(Integer.parseInt(etG.getText().toString()));
          etG.setSelection(etG.getText().toString().length());
        }
      });

    // 青色の設定
    sbB.setProgress(parameterManager.getStarterBarColorBlue());
    sbB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etB.setText(String.valueOf(sbB.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            etB.setText(String.valueOf(sbB.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
            etB.setText(String.valueOf(sbB.getProgress()));
            redrawLeftRightBar();
          }
        });
    etB.setText(String.valueOf(sbB.getProgress()));
    // エディットテキストの変化を受け取るリスナ
    etB.addTextChangedListener(new TextWatcher() {
        // 操作前の状態
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
        }
        // 操作中の状態
        public void onTextChanged(CharSequence s, int start, int count, int after) {
        }
        // 操作後の状態
        public void afterTextChanged(Editable s) {
          if(etB.getText().toString().equals("")) {
            etB.setText("0");
          }
          sbB.setProgress(Integer.parseInt(etB.getText().toString()));
          etB.setSelection(etB.getText().toString().length());
        }
      });

    // 透過度の設定
    sbA.setProgress(parameterManager.getStarterBarColorAlpha());
    sbA.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            etA.setText(String.valueOf(sbA.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            etA.setText(String.valueOf(sbA.getProgress()));
            redrawLeftRightBar();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
            etA.setText(String.valueOf(sbA.getProgress()));
            redrawLeftRightBar();
          }
        });
    etA.setText(String.valueOf(sbA.getProgress()));  // エディットテキストの初期値の設定
    // エディットテキストの変化を受け取るリスナ
    etA.addTextChangedListener(new TextWatcher() {
      // 操作前の状態
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
      }
      // 操作中の状態
      public void onTextChanged(CharSequence s, int start, int count, int after) {
      }
      // 操作後の状態
      public void afterTextChanged(Editable s) {
        if(etA.getText().toString().equals("")) {
          etA.setText("0");
        }
        sbA.setProgress(Integer.parseInt(etA.getText().toString()));
        etA.setSelection(etA.getText().toString().length());
      }
    });

    // 確定が押されたらデータの更新
    builder.setPositiveButton(getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        parameterManager.putStarterBarColor(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress());
        if (parameterManager.getStarterBarServiceStatus()) {
          redrawBars();
          unbindAppsLauncherService();
        }
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
  private void redrawLeftRightBar() {
    if (appsLauncherService != null) {
      int widthLeft = parameterManager.getStarterBarWidthLeft();
      int widthRight = parameterManager.getStarterBarWidthRight();
      int heightLeft = parameterManager.getStarterBarHeightLeft();
      int heightRight = parameterManager.getStarterBarHeightRight();
      int posLeft = parameterManager.getStarterBarPositionLeft();
      int posRight = parameterManager.getStarterBarPositionRight();
      int color = Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress());
      appsLauncherService.redrawLeftBar(widthLeft, heightLeft, color, posLeft);
      appsLauncherService.redrawRightBar(widthRight, heightRight, color, posRight);
    }
  }
  private void unbindAppsLauncherService() {
    Log.d(TAG, "unbindAppsLauncherService()");
    if (appsLauncherService != null) {
      context.unbindService(connection);
    }
  }
}
