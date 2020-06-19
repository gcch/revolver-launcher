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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 「アプリについて」の画面
 * @author Tag
 */
@SuppressLint("NewApi")
public class AboutApp extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about_app);
    
    // LinearLayout
    LinearLayout ll = (LinearLayout)findViewById(R.id.linearLayout_aboutApp);
    ll.setOnClickListener(new OnClickListener() {  // リスナの設定
      @Override
      public void onClick(View v) {
        finish();  // Activity 終了
      }
    });
    
    // アプリ名
    TextView appName = (TextView)findViewById(R.id.textView_appName);
    appName.setText(getString(R.string.app_name) + (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("kPremEd", false) ? "" : " Free") );  // テキストの取得 (完全版であれば、文字を連結)
    
    // バージョン
    TextView appVer = (TextView)findViewById(R.id.textView_appVer);
    appVer.setText(getResources().getText(R.string.about_app_version) + " " + getVersionName() + (getString(R.string.channel).matches(".*" + "Amazon" + ".*") ? " for Kindle" : "") );          // テキストの取得
    
  }
  
  private String getVersionName() {
    PackageInfo packageInfo = null;
    try {
      packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return packageInfo.versionName;
  }

}
