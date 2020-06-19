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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

/**
 * BackKeyAlertActivity --- Back キーのためのサービスが起動していないことを警告するアクティビティ
 * @author Tag
 *
 */
public class BackKeyAlertActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /**
     * Back キー有効化ダイアログ
     */
    Resources res = getResources();
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setIcon(R.drawable.ic_alert);
    alertDialogBuilder.setTitle(res.getText(R.string.app_name));
    alertDialogBuilder.setMessage(res.getText(R.string.backkey_alert));
    alertDialogBuilder.setPositiveButton(res.getText(R.string.backkey_alert_accessivility_btn_text), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        finish();
      }
    });
    alertDialogBuilder.setCancelable(false);
    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.setCanceledOnTouchOutside(false);
    alertDialog.show();

  }
}
