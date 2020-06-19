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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 背景色ダイアログ
 * @author ku
 */
public class SettingParamsDialogColorBackground extends DialogFragment {
  private LinearLayout ll;
  private SeekBar sbR, sbG, sbB, sbA;
  private EditText etR, etG, etB, etA;

  private LayoutInflater factory;
  private ParameterManager parameterManager;
  private Context context;

  public SettingParamsDialogColorBackground(Context context, LayoutInflater layoutInflater) {
    this.factory = layoutInflater;
    parameterManager = new ParameterManager(context);
    this.context = context;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View inputView = factory.inflate(R.layout.dialog_color, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(context.getString(R.string.setting_launcher_bg_color));
    builder.setView(inputView);
    setCancelable(false);

    // 各種 View 関連の取得
    ll = (LinearLayout)inputView.findViewById(R.id.linearLayoutForDialogColor);
    sbR = (SeekBar)inputView.findViewById(R.id.paramR);
    etR = (EditText)inputView.findViewById(R.id.paramEtR);
    sbG = (SeekBar)inputView.findViewById(R.id.paramG);
    etG = (EditText)inputView.findViewById(R.id.paramEtG);
    sbB = (SeekBar)inputView.findViewById(R.id.paramB);
    etB = (EditText)inputView.findViewById(R.id.paramEtB);
    sbA = (SeekBar)inputView.findViewById(R.id.paramA);
    etA = (EditText)inputView.findViewById(R.id.paramEtA);

    // 各色の初期値の設定
    sbR.setProgress(parameterManager.getLauncherBgColorRed());
    sbG.setProgress(parameterManager.getLauncherBgColorGreen());
    sbB.setProgress(parameterManager.getLauncherBgColorBlue());
    sbA.setProgress(parameterManager.getLauncherBgColorAlpha());

    // 背景部分の初期色
    changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));

    // 赤色の設定
    sbR.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etR.setText(String.valueOf(sbR.getProgress()));
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            ll.requestFocus();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
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
    sbG.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etG.setText(String.valueOf(sbG.getProgress()));
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            ll.requestFocus();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
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
        if(etG.getText().toString().equals("")) {
          etG.setText("0");
        }
        sbG.setProgress(Integer.parseInt(etG.getText().toString()));
        etG.setSelection(etG.getText().toString().length());
      }
    });

    // 青色の設定
    sbB.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etB.setText(String.valueOf(sbB.getProgress()));
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            ll.requestFocus();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
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
    sbA.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          // ツマミをドラッグしたとき
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            changeColor(Color.argb(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            etA.setText(String.valueOf(sbA.getProgress()));
          }
          // ツマミに触れたとき
          public void onStartTrackingTouch(SeekBar seekBar) {
            ll.requestFocus();
          }
          // ツマミを離したとき
          public void onStopTrackingTouch(SeekBar seekBar) {
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
        parameterManager.putLauncherBgColor(sbA.getProgress(), sbR.getProgress(), sbG.getProgress(), sbB.getProgress());
      }
    });
    // キャンセルが押されたらダイアログを閉じる
    builder.setNegativeButton(getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
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
  
  /**
   * 背景色の変更
   * @param color
   */
  private void changeColor(int color) {
    ll.setBackgroundColor(color);
  }
}
