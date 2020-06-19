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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * CustomClockAndDateDialogFragment --- 時計と日付のカスタマイズ
 * @author tag
 *
 */
public class CustomClockAndDateDialogFragment extends DialogFragment {

  private Activity activity;
  private LayoutInflater inflater;
  private ParameterManager parameterManager;

  public CustomClockAndDateDialogFragment(Activity activity, Context context, LayoutInflater layoutInflater) {
    this.activity = activity;
    this.inflater = layoutInflater;
    parameterManager = new ParameterManager(context);
  }

  private boolean enableClockAndDate;
  private float clockFontSize, clockBaseline, dateFontSize, dateBaseline;

  private SeekBar sbClockFontSize, sbClockBaseline, sbDateFontSize, sbDateBaseline;
  private EditText etClockFontSize, etClockBaseline, etDateFontSize, etDateBaseline;

  @SuppressLint("NewApi")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    final View v = inflater.inflate(R.layout.dialog_clock_and_date, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(activity.getString(R.string.setting_custom_clock_and_date));
    builder.setView(v);

    enableClockAndDate = parameterManager.getEnableClockAndDateCustomize();
    if (!enableClockAndDate) {
      parameterManager.resetClockAndDateTextSizeAndBaseline();
    }
    clockFontSize = parameterManager.getClockFontSize();
    clockBaseline = parameterManager.getClockBaseline();
    dateFontSize = parameterManager.getDateFontSize();
    dateBaseline = parameterManager.getDateBaseline();

    sbClockFontSize = (SeekBar)v.findViewById(R.id.seekBar_dialog_clock_font_size);
    sbClockBaseline = (SeekBar)v.findViewById(R.id.seekBar_dialog_clock_baseline);
    sbDateFontSize = (SeekBar)v.findViewById(R.id.seekBar_dialog_date_font_size);
    sbDateBaseline = (SeekBar)v.findViewById(R.id.seekBar_dialog_date_baseline);

    etClockFontSize = (EditText)v.findViewById(R.id.editText_dialog_clock_font_size);
    etClockBaseline = (EditText)v.findViewById(R.id.editText_dialog_clock_baseline);
    etDateFontSize = (EditText)v.findViewById(R.id.editText_dialog_date_font_size);
    etDateBaseline = (EditText)v.findViewById(R.id.editText_dialog_date_baseline);

    sbClockFontSize.setEnabled(enableClockAndDate);
    sbClockBaseline.setEnabled(enableClockAndDate);
    sbDateFontSize.setEnabled(enableClockAndDate);
    sbDateBaseline.setEnabled(enableClockAndDate);
    etClockFontSize.setEnabled(enableClockAndDate);
    etClockBaseline.setEnabled(enableClockAndDate);
    etDateFontSize.setEnabled(enableClockAndDate);
    etDateBaseline.setEnabled(enableClockAndDate);

    // チェックボックス
    CheckBox cb = (CheckBox)v.findViewById(R.id.checkBox_clock_and_date);
    cb.setChecked(enableClockAndDate);
    cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        parameterManager.putEnableClockAndDateCustomize(isChecked);
        sbClockFontSize.setEnabled(isChecked);
        sbClockBaseline.setEnabled(isChecked);
        sbDateFontSize.setEnabled(isChecked);
        sbDateBaseline.setEnabled(isChecked);
        etClockFontSize.setEnabled(isChecked);
        etClockBaseline.setEnabled(isChecked);
        etDateFontSize.setEnabled(isChecked);
        etDateBaseline.setEnabled(isChecked);
        if (!isChecked) {
          parameterManager.resetClockAndDateTextSizeAndBaseline();
          sbClockFontSize.setProgress((int)parameterManager.getClockFontSize());
          etClockFontSize.setText(String.valueOf(sbClockFontSize.getProgress()));
          sbClockBaseline.setProgress((int)parameterManager.getClockBaseline());
          etClockBaseline.setText(String.valueOf(sbClockBaseline.getProgress()));
          sbDateFontSize.setProgress((int)parameterManager.getDateFontSize());
          etDateFontSize.setText(String.valueOf(sbDateFontSize.getProgress()));
          sbDateBaseline.setProgress((int)parameterManager.getDateBaseline());
          etDateBaseline.setText(String.valueOf(sbDateBaseline.getProgress()));
        }
      }
    });

    int maxFontSize = parameterManager.getRevolverRadius();
    int maxBaseline = parameterManager.getRevolverRadius();

    // 時計のフォントサイズ
    sbClockFontSize.setMax(maxFontSize);
    sbClockFontSize.setProgress((int)clockFontSize);
    sbClockFontSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  // ツマミをドラッグしたとき
        parameterManager.putClockFontSize(progress);
        etClockFontSize.setText(String.valueOf(progress));
      }
      public void onStartTrackingTouch(SeekBar seekBar) {  // ツマミに触れたとき
        int progress = seekBar.getProgress();
        parameterManager.putClockFontSize(progress);
        etClockFontSize.setText(String.valueOf(progress));
      }
      public void onStopTrackingTouch(SeekBar seekBar) {  // ツマミを離したとき
        int progress = seekBar.getProgress();
        parameterManager.putClockFontSize(progress);
        etClockFontSize.setText(String.valueOf(progress));
      }
    });
    etClockFontSize.setText(String.valueOf(sbClockFontSize.getProgress()));
    etClockFontSize.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {  // 操作前の状態
      }
      public void onTextChanged(CharSequence s, int start, int count, int after) {  // 操作中の状態
      }
      public void afterTextChanged(Editable s) {  // 操作後の状態
        float val = 0.0f;
        if (s.toString().equals("")) {
          parameterManager.putClockFontSize(val);
          etClockFontSize.setText(String.valueOf(val));
        } else {
          val = Float.valueOf(s.toString());
          parameterManager.putClockFontSize(val);
          sbClockFontSize.setProgress((int)val);
          etClockFontSize.setSelection(s.toString().length());  // カーソル位置
        }
      }
    });

    // 時計のベースライン
    sbClockBaseline.setMax(maxBaseline);
    sbClockBaseline.setProgress((int)clockBaseline);
    sbClockBaseline.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  // ツマミをドラッグしたとき
        parameterManager.putClockBaseline(progress);
        etClockBaseline.setText(String.valueOf(progress));
      }
      public void onStartTrackingTouch(SeekBar seekBar) {  // ツマミに触れたとき
        int progress = seekBar.getProgress();
        parameterManager.putClockBaseline(progress);
        etClockBaseline.setText(String.valueOf(progress));
      }
      public void onStopTrackingTouch(SeekBar seekBar) {  // ツマミを離したとき
        int progress = seekBar.getProgress();
        parameterManager.putClockBaseline(progress);
        etClockBaseline.setText(String.valueOf(progress));
      }
    });
    etClockBaseline.setText(String.valueOf(sbClockBaseline.getProgress()));
    etClockBaseline.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {  // 操作前の状態
      }
      public void onTextChanged(CharSequence s, int start, int count, int after) {  // 操作中の状態
      }
      public void afterTextChanged(Editable s) {  // 操作後の状態
        float val = 0.0f;
        if (s.toString().equals("")) {
          parameterManager.putClockBaseline(val);
          etClockBaseline.setText(String.valueOf(val));
        } else {
          val = Float.valueOf(s.toString());
          parameterManager.putClockBaseline(val);
          sbClockBaseline.setProgress((int)val);
          etClockBaseline.setSelection(s.toString().length());  // カーソル位置
        }
      }
    });


    // 日付のフォントサイズ
    sbDateFontSize.setMax(maxFontSize);
    sbDateFontSize.setProgress((int)clockFontSize);
    sbDateFontSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  // ツマミをドラッグしたとき
        parameterManager.putDateFontSize(progress);
        etDateFontSize.setText(String.valueOf(progress));
      }
      public void onStartTrackingTouch(SeekBar seekBar) {  // ツマミに触れたとき
        int progress = seekBar.getProgress();
        parameterManager.putDateFontSize(progress);
        etDateFontSize.setText(String.valueOf(progress));
      }
      public void onStopTrackingTouch(SeekBar seekBar) {  // ツマミを離したとき
        int progress = seekBar.getProgress();
        parameterManager.putDateFontSize(progress);
        etDateFontSize.setText(String.valueOf(progress));
      }
    });
    etDateFontSize.setText(String.valueOf(sbDateFontSize.getProgress()));
    etDateFontSize.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {  // 操作前の状態
      }
      public void onTextChanged(CharSequence s, int start, int count, int after) {  // 操作中の状態
      }
      public void afterTextChanged(Editable s) {  // 操作後の状態
        float val = 0.0f;
        if (s.toString().equals("")) {
          parameterManager.putDateFontSize(val);
          etDateFontSize.setText(String.valueOf(val));
        } else {
          val = Float.valueOf(s.toString());
          parameterManager.putDateFontSize(val);
          sbDateFontSize.setProgress((int)val);
          etDateFontSize.setSelection(s.toString().length());  // カーソル位置
        }
      }
    });

    // 日付のベースライン
    sbDateBaseline.setMax(maxBaseline);
    sbDateBaseline.setProgress((int)clockBaseline);
    sbDateBaseline.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  // ツマミをドラッグしたとき
        parameterManager.putDateBaseline(progress);
        etDateBaseline.setText(String.valueOf(progress));
      }
      public void onStartTrackingTouch(SeekBar seekBar) {  // ツマミに触れたとき
        int progress = seekBar.getProgress();
        parameterManager.putDateBaseline(progress);
        etDateBaseline.setText(String.valueOf(progress));
      }
      public void onStopTrackingTouch(SeekBar seekBar) {  // ツマミを離したとき
        int progress = seekBar.getProgress();
        parameterManager.putDateBaseline(progress);
        etDateBaseline.setText(String.valueOf(progress));
      }
    });
    etDateBaseline.setText(String.valueOf(sbDateBaseline.getProgress()));
    etDateBaseline.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {  // 操作前の状態
      }
      public void onTextChanged(CharSequence s, int start, int count, int after) {  // 操作中の状態
      }
      public void afterTextChanged(Editable s) {  // 操作後の状態
        float val = 0.0f;
        if (s.toString().equals("")) {
          parameterManager.putDateBaseline(val);
          etDateBaseline.setText(String.valueOf(val));
        } else {
          val = Float.valueOf(s.toString());
          parameterManager.putDateBaseline(val);
          sbDateBaseline.setProgress((int)val);
          etDateBaseline.setSelection(s.toString().length());  // カーソル位置
        }
      }
    });


    // 確定
    builder.setPositiveButton(activity.getString(R.string.setting_apply), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dismiss();
      }
    });
    // キャンセル
    builder.setNegativeButton(activity.getString(R.string.setting_abort), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        parameterManager.putEnableClockAndDateCustomize(enableClockAndDate);
        parameterManager.putClockFontSize(clockFontSize);
        parameterManager.putClockBaseline(clockBaseline);
        parameterManager.putDateFontSize(dateFontSize);
        parameterManager.putDateBaseline(dateBaseline);
      }
    });

    return builder.create();
  }
  
  /**
   * キャンセル時 (ダイアログ外タップ) の処理
   */
  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    parameterManager.putEnableClockAndDateCustomize(enableClockAndDate);
    parameterManager.putClockFontSize(clockFontSize);
    parameterManager.putClockBaseline(clockBaseline);
    parameterManager.putDateFontSize(dateFontSize);
    parameterManager.putDateBaseline(dateBaseline);
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
