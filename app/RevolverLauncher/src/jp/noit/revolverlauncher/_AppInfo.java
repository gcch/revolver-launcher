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

import android.graphics.drawable.Drawable;

/**
 * アプリ情報
 * @author naka
 *
 */
public class _AppInfo {
//  private String srcDir;  // ディレクトリ
  private String label;   // ラベル
  private String name;    // パッケージ名
  private Drawable icon;  // アイコン

  /**
   * コンストラクタ
   * @param srcDir
   * @param label
   * @param packagename
   * @param icon
   */
  public _AppInfo(String label, String packagename, Drawable icon) {
//.srcDir = srcDir;
    this.label = label;
    this.name = packagename;
    this.icon = icon;
  }

//  /**
//   * ディレクトリの取得
//   * @return
//   */
//  public String getSourceDir() {
//    return this.srcDir;
//  }

  /**
   * ラベルの取得
   * @return
   */
  public String getLabel() {
    return this.label;
  }

  public String getPackagename() {
    return this.name;
  }

  public Drawable getIcon() {
    return this.icon;
  }
}
